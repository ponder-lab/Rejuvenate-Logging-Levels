/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Persistable;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.ResultSetException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.RowSetExeception;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular.Row;

/**
 * An implementation of a Result Set that is persistable to the file system
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class FileResultSet extends ResultSetImpl implements Persistable {
	private long size;
	private int maxReadSize = 4096;
	private char DELIMITER = '\t';
	private char QUOTE = '"';

	private String fileName;
	private Path infoFile;
	private Path dataFile;
	private FileChannel dataReadFC;

	private ByteBuffer read;
	// private long rowPosition = -1;
	private Row currentRow;

	private boolean persisted = false;

	private Map<Long, Row> pendingData;
	private int MAXPENDING = 100000;

	public FileResultSet() {
		this.pendingData = new HashMap<Long, Row>();
	}

	@Override
	public boolean isAvailable(String location) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void load(String fileName) throws ResultSetException,
			PersistableException {

		// Setup the initial variables
		this.fileName = fileName;
		this.pendingData = new HashMap<Long, Row>();

		// Initialize the buffer to read
		read = ByteBuffer.allocate(maxReadSize);
		// Set up the paths
		infoFile = Paths.get(fileName + ".info");
		dataFile = Paths.get(fileName + ".data");
		// Check to see if the file exists
		try {
			if (Files.isReadable(infoFile) && Files.isReadable(dataFile)) {
				this.persisted = true;
				// If it exists then refresh the data and set the states
				dataReadFC = FileChannel
						.open(dataFile, StandardOpenOption.READ);
				refresh();
			} else {
				// If both files do not exist then create the file
				this.persisted = false;
				this.current = true;
				Files.createFile(dataFile);
				dataReadFC = FileChannel
						.open(dataFile, StandardOpenOption.READ);
				
			}
		} catch (IOException e) {
			if (dataReadFC != null) {
				try {
					dataReadFC.close();
				} catch (IOException closeException) {
					throw new PersistableException(
							"Unable to initiate the result set", closeException);
				}
			}
			throw new PersistableException("Unable to initiate the result set",
					e);
		}

	}

	/**
	 * Adds a new row to the file result set
	 * 
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 * @throws PersistableException
	 *             If a PersistableException occurs
	 */
	@Override
	public void appendRow() throws ResultSetException, PersistableException {
		if (isClosed()) {
			throw new ResultSetException("ResultSet is closed");
		}
		if (getRow() == MAXPENDING) {
			throw new PersistableException("Maximum Pending Size Reached");
		}

		Row newRow = new Row(this.getColumnSize());
		this.currentRow = newRow;

		pendingData.put(getSize(), newRow);

		this.setRowPosition(this.getSize());
		this.setSize(this.getSize() + 1);

	}

	/**
	 * Sets the number of rows in the result set
	 * 
	 * @param size
	 *            Number of rows
	 */
	private void setSize(long size) {
		this.size = size;
	}

	@Override
	public long getSize() throws ResultSetException {
		if (isClosed()) {
			throw new ResultSetException("ResultSet is closed");
		}
		return size;
	}

	@Override
	public boolean absolute(long newRow) throws ResultSetException {
		if (isClosed()) {
			throw new ResultSetException("ResultSet is closed");
		}
		if ((newRow != 0) && ((newRow > getSize() - 1) || (newRow < 0))) {
			throw new RowSetExeception("Row is not in ResultSet");
		}
		// Is the row already loaded into pending?
		if (this.pendingData.containsKey(newRow)) {
			this.setRowPosition(newRow);
			currentRow = this.pendingData.get(newRow);
			return true;
		}
		// If the row is not in memory
		// Set the file cursor position
		try {
			if (newRow == 0) {
				dataReadFC.position(0);
			} else {
				if (newRow == getRowPosition()) {
					return true;
				} else if (newRow > getRowPosition()) {
					moveForward(newRow - getRowPosition());
				} else {
					moveBackward(getRowPosition() - newRow);
				}
				this.setRowPosition(newRow);
			}
			this.setRowPosition(newRow);

			// Read the line and load it as the currentRow
			this.currentRow = loadCurrentLine();

			return true;
		} catch (IOException | PersistableException e) {
			throw new ResultSetException("Unable to read the result set", e);
		}
	}
	@Override
	public void afterLast() throws ResultSetException {
		if (isClosed()) {
			throw new ResultSetException("ResultSet is closed");
		}
		this.setRowPosition(size);

	}
	@Override
	public void beforeFirst() throws ResultSetException {
		if (isClosed()) {
			throw new ResultSetException("ResultSet is closed");
		}
		this.setRowPosition(-1);
		this.currentRow = null;

	}
	@Override
	public boolean first() throws ResultSetException {
		if (isClosed()) {
			throw new ResultSetException("ResultSet is closed");
		}
		return absolute(0);
	}
	@Override
	public boolean last() throws ResultSetException {
		if (isClosed()) {
			throw new ResultSetException("ResultSet is closed");
		}
		return absolute(this.size - 1);
	}
	@Override
	public boolean next() throws ResultSetException {
		if (isClosed()) {
			throw new ResultSetException("ResultSet is closed");
		}

		try {
			return relative(1);
		} catch (ResultSetException re) {
			return false;
		}
	}
	@Override
	public boolean relative(long rows) throws ResultSetException {
		if (isClosed()) {
			throw new ResultSetException("ResultSet is closed");
		}
		try {
			return absolute(getRow() + rows);
		} catch (ResultSetException re) {
			return false;
		}
	}
	@Override
	public boolean previous() throws ResultSetException {
		if (isClosed()) {
			throw new ResultSetException("ResultSet is closed");
		}
		relative(-1);
		return false;
	}
	@Override
	public long getRow() throws ResultSetException {
		if (isClosed()) {
			throw new ResultSetException("ResultSet is closed");
		}
		return getRowPosition();
	}

	private void moveForward(long rowMovement) throws IOException,
			PersistableException {
		long originalPosition = dataReadFC.position();
		long currentCount = 0;

		while (currentCount < rowMovement) {
			read.clear();
			int nRead = dataReadFC.read(read);

			if (nRead == -1) {
				read.clear();
				throw new PersistableException("Unable to find row");
			}
			byte[] byteArray = read.array();
			for (int position = 0; position < nRead; position++) {
				char charRead = (char) byteArray[position];

				if ((charRead == '\r') || (charRead == '\n')) {
					currentCount++;
					if (currentCount == rowMovement) {
						dataReadFC.position(originalPosition + position + 1);
						break;
					}
				}
			}
			originalPosition += this.maxReadSize;
		}
	}

	private void moveBackward(long rowMovement) throws IOException,
			PersistableException {
		long originalPosition = dataReadFC.position();
		long currentCount = 0;
		long offset = 0;

		while (currentCount < rowMovement) {
			long newPosition = originalPosition - offset - maxReadSize;
			if (newPosition <= 0) {
				newPosition = 0;
			}

			dataReadFC.position(newPosition);

			read.clear();
			int nRead = dataReadFC.read(read);

			byte[] byteArray = read.array();
			int startPosition = nRead;
			if (nRead >= originalPosition) {
				startPosition = (int) originalPosition - 2;
			}
			for (int position = startPosition; position >= 0; position--) {
				char charRead = (char) byteArray[position];

				if ((charRead == '\r') || (charRead == '\n')) {
					currentCount++;
					if (currentCount == rowMovement) {
						dataReadFC.position(originalPosition - offset
								- startPosition + position - 1);
						break;
					}
				}
			}
			offset += maxReadSize;

			if (nRead == -1) {
				read.clear();
				throw new PersistableException("Unable to find row");
			}

			if ((newPosition == 0) && (currentCount < rowMovement)) {
				read.clear();
				throw new PersistableException("Unable to find row");
			}
		}

	}

	private Row loadCurrentLine() throws IOException, ResultSetException {
		Row row = new Row(this.getColumnSize());
		read.clear();
		long originalPosition = dataReadFC.position();
		boolean outsideQuote = true;
		int currentColumn = 0;
		ByteBuffer line = ByteBuffer.allocate(maxReadSize);

		do {
			int nRead = dataReadFC.read(read);

			if (nRead == -1) {
				break;
			}

			for (byte readByte : read.array()) {
				char charRead = (char) readByte;

				if ((charRead == '\r') || (charRead == '\n')) {
					row.setColumn(
							currentColumn,
							getColumn(currentColumn).getDataType()
									.fromBytes(
											Arrays.copyOf(line.array(),
													line.position())));
					line.clear();
					dataReadFC.position(originalPosition);
					break;
				} else if (charRead == QUOTE) {
					// Inverts the state of being inside or outside a quote
					outsideQuote = !outsideQuote;
				} else if ((charRead == DELIMITER) && (outsideQuote)) {
					// If a delimiter is found and the current position is
					// outside a quote

					row.setColumn(
							currentColumn,
							getColumn(currentColumn).getDataType()
									.fromBytes(
											Arrays.copyOf(line.array(),
													line.position())));
					currentColumn++;
					line.clear();
				} else {
					line.put(readByte);
				}

			}

		} while (dataReadFC.position() != originalPosition);
		return row;
	}

	/**
	 * Sets the value of a cell at the given column at the current position
	 * 
	 * @param columnIndex
	 *            Column Index
	 * @param value
	 *            Value
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	private void setCell(int columnIndex, Object value)
			throws ResultSetException {
		if (columnIndex >= getColumnSize()) {
			throw new ResultSetException("Column not found");
		}
		this.currentRow.setColumn(columnIndex, value);
		this.pendingData.put(this.getRowPosition(), this.currentRow);
		this.current = false;
	}

	/**
	 * Returns a cell from the given column at the current position
	 * 
	 * @param columnIndex
	 *            Column Index
	 * @return Value
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	private Object getCell(int columnIndex) throws ResultSetException {
		if (columnIndex >= getColumnSize()) {
			throw new ResultSetException("Column not found");
		}
		return this.currentRow.getColumn(columnIndex);
	}

	@Override
	public void persist(String fileName) throws PersistableException {
		if (this.persisted) {
			throw new PersistableException(this.fileName
					+ " has been persisted");
		}

		this.fileName = fileName;

		// Initialize the buffer to read
		read = ByteBuffer.allocate(maxReadSize);
		// Set up the paths
		infoFile = Paths.get(fileName + ".info");
		dataFile = Paths.get(fileName + ".data");
		// Check to see if the file exists
		try {
			Files.createFile(dataFile);
			dataReadFC = FileChannel.open(dataFile, StandardOpenOption.READ);
		} catch (IOException e) {
			if (dataReadFC != null) {
				try {
					dataReadFC.close();
				} catch (IOException closeException) {
					throw new PersistableException(
							"Unable to initiate the result set", closeException);
				}
			}
			throw new PersistableException("Unable to initiate the result set",
					e);
		}

		persist();
	}

	public void persist() throws PersistableException {
		// Throw an exception if the file has not been initially persisted
		if (this.persisted) {
			throw new PersistableException(this.fileName
					+ " has been persisted");
		}

		try (SeekableByteChannel dataOutStream = Files.newByteChannel(dataFile,
				StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
			// Write the information to the info File
			Files.write(infoFile, toJson().toString().getBytes());

			// Write pending data to file
			Long[] keys = new Long[0];
			keys = this.pendingData.keySet().toArray(keys);
			Arrays.sort(keys);

			for (Long key : keys) {
				writeRowToFile(dataOutStream, this.pendingData.get(key));
				ByteBuffer bb = ByteBuffer
						.wrap(new byte[] { (byte) ((byte) '\n' & 0x00FF) });
				dataOutStream.write(bb);
			}

			// Reset the FileChannel and position
			dataReadFC.close();
			dataReadFC = FileChannel.open(dataFile, StandardOpenOption.READ);

			// Reset the variables and clear out the pending results
			dataOutStream.close();
			this.current = true;
			this.persisted = true;
			this.pendingData.clear();
		} catch (IOException | ResultSetException e) {
			throw new PersistableException("Unable to persist the result set",
					e);
		}
	}

	public void merge() throws PersistableException {
		// Throw an exception if the file has not been initially persisted
		if (!this.persisted) {
			throw new PersistableException(this.fileName
					+ " has not been persisted");
		}
		// Returns if the data is current
		if (this.current) {
			return;
		}

		// Create temporary file
		Path tempDataFile = Paths.get(fileName + ".temp");

		// Write all changes to temporary file
		try (SeekableByteChannel dataOutStream = Files.newByteChannel(
				tempDataFile, StandardOpenOption.CREATE,
				StandardOpenOption.APPEND)) {
			// Write the information to the info File
			Files.write(infoFile, toJson().toString().getBytes());

			for (long rowIndex = 0; rowIndex < this.getSize(); rowIndex++) {
				if (this.pendingData.containsKey(rowIndex)) {
					writeRowToFile(dataOutStream,
							this.pendingData.get(rowIndex));
				} else {
					this.absolute(rowIndex);
					writeRowToFile(dataOutStream, this.currentRow);
				}
				ByteBuffer bb = ByteBuffer
						.wrap(new byte[] { (byte) ((byte) '\n' & 0x00FF) });
				dataOutStream.write(bb);
			}
			dataOutStream.close();

			// Replace the current file with the temporary file
			Files.copy(tempDataFile, dataFile,
					StandardCopyOption.REPLACE_EXISTING);
			Files.delete(tempDataFile);

			// Reset the FileChannel and position
			dataReadFC.close();
			dataReadFC = FileChannel.open(dataFile, StandardOpenOption.READ);

			// Reset the variables and clear out the pending results
			this.current = true;
			this.persisted = true;
			this.pendingData.clear();
		} catch (IOException | ResultSetException e) {
			throw new PersistableException("Unable to persist the result set",
					e);
		}

	}

	public void refresh() throws PersistableException {
		// Throw an exception if the file has not been initially persisted
		if (!this.persisted) {
			throw new PersistableException(this.fileName
					+ " has not been persisted");
		}
		// Returns if the data is current
		if (this.current) {
			return;
		}

		FileChannel dataFC = null;
		try {
			// Load JSON Data from info file and create a JSON Object
			JsonObject jsonReader = Json.createReader(
					new StringReader(new String(Files.readAllBytes(infoFile))))
					.readObject();

			// Setup the columns
			JsonArray jsonColArray = jsonReader.getJsonArray("columns");

			for (int i = 0; i < jsonColArray.size(); i++) {
				JsonObject job = (JsonObject) jsonColArray.get(i);
				Column newColumn = new Column();
				newColumn.setDataType(PrimitiveDataType.valueOf(job
						.getString("dataType")));
				newColumn.setName(job.getString("name"));
				this.appendColumn(newColumn);
			}

			// Set the size
			this.size = jsonReader.getInt("size");

			// Clear out any pending data
			this.pendingData = new HashMap<Long, Row>();
			this.current = true;

		} catch (IOException | ResultSetException e) {
			throw new PersistableException("Unable to merge the result set", e);
		} finally {
			try {
				if (dataFC != null) {
					dataFC.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private void writeRowToFile(SeekableByteChannel dataOutStream, Row row)
			throws ResultSetException, IOException {
		// Loop through columns and write the serialized data to file with
		// delimiter between.
		for (int columnIndex = 0; columnIndex < this.getColumnSize(); columnIndex++) {
			
			byte[] outBytes = this.getColumn(columnIndex).getDataType()
					.toBytes(row.getColumn(columnIndex));

			ByteBuffer bb = ByteBuffer.wrap(outBytes);
			dataOutStream.write(bb);
			if (columnIndex != this.getColumnSize() - 1) {
				bb = ByteBuffer
						.wrap(new byte[] { (byte) ((byte) this.DELIMITER & 0x00FF) });
				dataOutStream.write(bb);
			}

		}
	}
	
	@Override
	public List<File> getFileList() {
		List<File> files = new ArrayList<File>();
		files.add(infoFile.toFile());
		files.add(dataFile.toFile());
		return files;
	}

	public boolean isCurrent() {
		return this.current;
	}

	public boolean isPersisted() {
		return this.persisted;
	}

	// Data Retrieval and editing
	// BOOLEAN
	@Override
	public boolean getBoolean(int columnIndex) throws ResultSetException {
		return (Boolean) getCell(columnIndex);
	}

	@Override
	public boolean getBoolean(String columnLabel) throws ResultSetException {
		return getBoolean(findColumn(columnLabel));
	}

	@Override
	public void updateBoolean(int columnIndex, boolean value)
			throws ResultSetException {
		setCell(columnIndex, value);
	}

	@Override
	public void updateBoolean(String columnLabel, boolean value)
			throws ResultSetException {
		updateBoolean(findColumn(columnLabel), value);
	}

	// BYTE
	@Override
	public byte getByte(int columnIndex) throws ResultSetException {
		return (Byte) getCell(columnIndex);
	}

	@Override
	public byte getByte(String columnLabel) throws ResultSetException {
		return getByte(findColumn(columnLabel));
	}

	@Override
	public void updateByte(int columnIndex, byte value)
			throws ResultSetException {
		setCell(columnIndex, value);
	}

	@Override
	public void updateByte(String columnLabel, byte value)
			throws ResultSetException {
		updateByte(findColumn(columnLabel), value);
	}

	// DATE
	@Override
	public Date getDate(int columnIndex) throws ResultSetException {
		String dateString = getString(columnIndex);

		PrimitiveDataType dt = getColumn(columnIndex).getDataType();

		String pattern = null;
		if (dt == PrimitiveDataType.DATE) {
			pattern = "YYYY-MM-dd";
		} else if (dt == PrimitiveDataType.DATETIME) {
			pattern = "YYYY-MM-dd HH:mm:ss";
		} else if (dt == PrimitiveDataType.TIME) {
			pattern = "HH:mm:ss";
		}
		DateFormat formatter = new SimpleDateFormat(pattern);

		try {
			return formatter.parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public Date getDate(String columnLabel) throws ResultSetException {
		return getDate(findColumn(columnLabel));
	}

	@Override
	public void updateDate(int columnIndex, Date value)
			throws ResultSetException {
		PrimitiveDataType dt = getColumn(columnIndex).getDataType();

		String pattern = null;
		if (dt == PrimitiveDataType.DATE) {
			pattern = "yyyy-MM-dd";
		} else if (dt == PrimitiveDataType.DATETIME) {
			pattern = "yyyy-MM-dd HH:mm:ss";
		} else if (dt == PrimitiveDataType.TIME) {
			pattern = "HH:mm:ss";
		}
		DateFormat formatter = new SimpleDateFormat(pattern);

		setCell(columnIndex, formatter.format(value));
	}

	@Override
	public void updateDate(String columnLabel, Date value)
			throws ResultSetException {
		updateDate(findColumn(columnLabel), value);
	}

	// DOUBLE
	@Override
	public double getDouble(int columnIndex) throws ResultSetException {
		return (Double) getCell(columnIndex);
	}

	@Override
	public double getDouble(String columnLabel) throws ResultSetException {
		return getDouble(findColumn(columnLabel));
	}

	@Override
	public void updateDouble(int columnIndex, double value)
			throws ResultSetException {
		setCell(columnIndex, value);
	}

	@Override
	public void updateDouble(String columnLabel, double value)
			throws ResultSetException {
		updateDouble(findColumn(columnLabel), value);
	}

	// FLOAT
	@Override
	public float getFloat(int columnIndex) throws ResultSetException {
		return (Float) getCell(columnIndex);
	}

	@Override
	public float getFloat(String columnLabel) throws ResultSetException {
		return getFloat(findColumn(columnLabel));
	}

	@Override
	public void updateFloat(int columnIndex, float value)
			throws ResultSetException {
		setCell(columnIndex, value);
	}

	@Override
	public void updateFloat(String columnLabel, float value)
			throws ResultSetException {
		updateFloat(findColumn(columnLabel), value);
	}

	// INT
	@Override
	public int getInt(int columnIndex) throws ResultSetException {
		return (Integer) getCell(columnIndex);
	}

	@Override
	public int getInt(String columnLabel) throws ResultSetException {
		return getInt(findColumn(columnLabel));
	}

	@Override
	public void updateInt(int columnIndex, int value) throws ResultSetException {
		setCell(columnIndex, value);
	}

	@Override
	public void updateInt(String columnLabel, int value)
			throws ResultSetException {
		updateInt(findColumn(columnLabel), value);
	}

	// LONG
	@Override
	public long getLong(int columnIndex) throws ResultSetException {
		return (Long) getCell(columnIndex);
	}

	@Override
	public long getLong(String columnLabel) throws ResultSetException {
		return getLong(findColumn(columnLabel));
	}

	@Override
	public void updateLong(int columnIndex, long value)
			throws ResultSetException {
		setCell(columnIndex, value);
	}

	@Override
	public void updateLong(String columnLabel, long value)
			throws ResultSetException {
		updateLong(findColumn(columnLabel), value);
	}

	// STRING
	@Override
	public String getString(int columnIndex) throws ResultSetException {
		return (String) getCell(columnIndex);
	}

	@Override
	public String getString(String columnLabel) throws ResultSetException {
		return getString(findColumn(columnLabel));
	}

	@Override
	public void updateString(int columnIndex, String value)
			throws ResultSetException {
		setCell(columnIndex, value);
	}

	@Override
	public void updateString(String columnLabel, String value)
			throws ResultSetException {
		updateString(findColumn(columnLabel), value);
	}

	// OBJECT
	public Object getObject(int columnIndex) throws ResultSetException {
		return (Object) getCell(columnIndex);
	}

	public void updateObject(int columnIndex, Object obj)
			throws ResultSetException {
		setCell(columnIndex, obj);
	}

	/**
	 * Returns a JSONObject representation of the object. This returns only the
	 * attributes associated with this object and not their representation.
	 * 
	 * This is equivalent of toJson(1);
	 * 
	 * @return JSON Representation
	 */
	public JsonObject toJson() {
		return toJson(1);
	}

	/**
	 * Returns a JSONObject representation of the object. This returns only the
	 * attributes associated with this object and not their representation.
	 * 
	 * 
	 * @param depth
	 *            Depth to travel
	 * @return JSON Representation
	 */
	public JsonObject toJson(int depth) {
		depth--;
		JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
		jsonBuilder.add("size", this.size);
		JsonArrayBuilder jsonColArray = Json.createArrayBuilder();
		try {
			if (getColumns() != null) {
				for (Column column : getColumns()) {
					jsonColArray.add(column.toJson());
				}
			}
		} catch (ResultSetException e) {
			e.printStackTrace();
		}

		jsonBuilder.add("columns", jsonColArray);
		return jsonBuilder.build();

	}
}
