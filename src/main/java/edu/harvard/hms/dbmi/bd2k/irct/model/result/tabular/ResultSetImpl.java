/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.ResultSetException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.RowSetExeception;

/**
 * This class provides a base implementation of ResultSets that adds basic
 * functionality that can then be extended by individual implementations.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class ResultSetImpl implements ResultSet {
	private Column[] columns;
	private String[] columnNames;
	private long size;
	private long rowPosition = -1;
	protected boolean closed = false;
	protected boolean current = false;

	@Override
	public void appendColumn(Column column) throws ResultSetException {
		if ((getSize() == 0)
				&& (!ArrayUtils.contains(columnNames, column.getName()))) {
			this.columns = ArrayUtils.add(columns, column);
			this.columnNames = ArrayUtils.add(columnNames, column.getName());
			this.current = false;
		}
	}

	@Override
	public void load(String resultSetLocation) throws ResultSetException,
			PersistableException {

	}

	@Override
	public List<File> getFileList() {
		return null;
	}

	@Override
	public void close() throws ResultSetException {
		this.closed = true;
	}

	@Override
	public boolean isClosed() {
		return closed;
	}

	@Override
	public boolean absolute(long row) throws ResultSetException {
		if (isClosed()) {
			throw new ResultSetException("ResultSet is closed");
		}
		if ((row > getSize() - 1) || (row < 0)) {
			throw new RowSetExeception("Row is not in ResultSet");
		}
		this.setRowPosition(row);

		return true;
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

	}

	@Override
	public boolean first() throws ResultSetException {
		if (isClosed()) {
			throw new ResultSetException("ResultSet is closed");
		}
		this.setRowPosition(0);
		return true;
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
	public boolean last() throws ResultSetException {
		this.setRowPosition(this.size - 1);
		return false;
	}

	@Override
	public boolean isFirst() throws ResultSetException {
		if (isClosed()) {
			throw new ResultSetException("ResultSet is closed");
		}
		return getRowPosition() == 0;
	}

	@Override
	public boolean isLast() throws ResultSetException {
		if (isClosed()) {
			throw new ResultSetException("ResultSet is closed");
		}
		return (getRowPosition() == size - 1);
	}

	@Override
	public long getRow() throws ResultSetException {
		if (isClosed()) {
			throw new ResultSetException("ResultSet is closed");
		}
		return getRowPosition();
	}

	@Override
	public long getSize() throws ResultSetException {
		if (isClosed()) {
			throw new ResultSetException("ResultSet is closed");
		}
		return size;
	}

	@Override
	public int getColumnSize() throws ResultSetException {
		if (isClosed()) {
			throw new ResultSetException("ResultSet is closed");
		}
		return columns.length;
	}

	// COLUMN INFORMATION
	@Override
	public int findColumn(String columnLabel) throws ResultSetException {
		if (isClosed()) {
			throw new ResultSetException("ResultSet is closed");
		}

		int position = ArrayUtils.indexOf(this.columnNames, columnLabel);
		if (position == ArrayUtils.INDEX_NOT_FOUND) {
			throw new ResultSetException("Column not found");
		}
		return position;
	}

	@Override
	public Column getColumn(int columnIndex) throws ResultSetException {
		if (isClosed()) {
			throw new ResultSetException("ResultSet is closed");
		}
		if (columnIndex >= columns.length) {
			throw new ResultSetException("Column not found");
		}

		return columns[columnIndex];
	}

	@Override
	public Column[] getColumns() throws ResultSetException {
		if (isClosed()) {
			throw new ResultSetException("ResultSet is closed");
		}
		return columns;
	}

	// Data Retrieval and editing
	// BOOLEAN
	@Override
	public boolean getBoolean(int columnIndex) throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	@Override
	public boolean getBoolean(String columnLabel) throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	@Override
	public void updateBoolean(int columnIndex, boolean value)
			throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	@Override
	public void updateBoolean(String columnLabel, boolean value)
			throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	// BYTE
	@Override
	public byte getByte(int columnIndex) throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	@Override
	public byte getByte(String columnLabel) throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	@Override
	public void updateByte(int columnIndex, byte value)
			throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	@Override
	public void updateByte(String columnLabel, byte value)
			throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	// DATE
	@Override
	public Date getDate(int columnIndex) throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	@Override
	public Date getDate(String columnLabel) throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	@Override
	public void updateDate(int columnIndex, Date value)
			throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	@Override
	public void updateDate(String columnLabel, Date value)
			throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	// DOUBLE
	@Override
	public double getDouble(int columnIndex) throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	@Override
	public double getDouble(String columnLabel) throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	@Override
	public void updateDouble(int columnIndex, double value)
			throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	@Override
	public void updateDouble(String columnLabel, double value)
			throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	// FLOAT
	@Override
	public float getFloat(int columnIndex) throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	@Override
	public float getFloat(String columnLabel) throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	@Override
	public void updateFloat(int columnIndex, float value)
			throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	@Override
	public void updateFloat(String columnLabel, float value)
			throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	// INT
	@Override
	public int getInt(int columnIndex) throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	@Override
	public int getInt(String columnLabel) throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	@Override
	public void updateInt(int columnIndex, int value) throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	@Override
	public void updateInt(String columnLabel, int value)
			throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	// LONG
	@Override
	public long getLong(int columnIndex) throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	@Override
	public long getLong(String columnLabel) throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	@Override
	public void updateLong(int columnIndex, long value)
			throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	@Override
	public void updateLong(String columnLabel, long value)
			throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	// STRING
	@Override
	public String getString(int columnIndex) throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	@Override
	public String getString(String columnLabel) throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	@Override
	public void updateString(int columnIndex, String value)
			throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	@Override
	public void updateString(String columnLabel, String value)
			throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	// OBJECT

	@Override
	public Object getObject(int columnIndex) throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	@Override
	public void updateObject(int columnIndex, Object obj)
			throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	/**
	 * Gets the current row position
	 * 
	 * @return current row
	 */
	public long getRowPosition() {
		return rowPosition;
	}

	/**
	 * Sets the current row position
	 * 
	 * @param rowPosition
	 *            Row
	 */
	public void setRowPosition(long rowPosition) {
		this.rowPosition = rowPosition;
	}

	@Override
	public void appendRow() throws ResultSetException, PersistableException {
		throw new ResultSetException("Not Implemented in this class");

	}
}
