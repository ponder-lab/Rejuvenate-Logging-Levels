/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular;

import java.util.Date;

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
	private int[] matchColumnIndex;
	private Column[] columns;
	private String[] columnNames;
	private long size;
	private long rowPosition = -1;
	private boolean closed = false;

	/**
	 * Adds a new column to a Result Set. The Result set must be empty for this
	 * to be performed.
	 * 
	 * @param column
	 *            New Column
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	public void appendColumn(Column column) throws ResultSetException {
		if (getSize() == 0) {
			this.columns = ArrayUtils.add(columns, column);
			this.columnNames = ArrayUtils.add(columnNames, column.getName());
		}
	}

	public int[] getMatchColumnIndexes() throws ResultSetException {
		return matchColumnIndex;
	}

	public String[] getMatchColumnNames() throws ResultSetException {
		String[] columnNames = new String[matchColumnIndex.length];

		int i = 0;
		for (int columnIndex : matchColumnIndex) {
			columnNames[i] = columns[columnIndex].getName();
			i++;
		}

		return columnNames;
	}

	public void setMatchColumn(int columnIndex) throws ResultSetException {
		this.matchColumnIndex = new int[] { columnIndex };
	}

	public void setMatchColumn(int[] columnIndexes) throws ResultSetException {
		this.matchColumnIndex = columnIndexes;
	}

	public void setMatchColumn(String columnLabel) throws ResultSetException {
		setMatchColumn(findColumn(columnLabel));
	}

	public void setMatchColumns(String[] columnLabels)
			throws ResultSetException {
		this.matchColumnIndex = new int[columnLabels.length];

		int i = 0;
		for (String columnLabel : columnLabels) {
			this.matchColumnIndex[i] = findColumn(columnLabel);
			i++;
		}
	}

	public void unsetMatchColumn(int columnIndex) throws ResultSetException {
		this.matchColumnIndex = ArrayUtils.removeElement(matchColumnIndex,
				columnIndex);
	}

	public void unsetMatchColumn(int[] columnIndexes) throws ResultSetException {
		this.matchColumnIndex = ArrayUtils.removeElements(matchColumnIndex,
				columnIndexes);

	}

	public void unsetMatchColumn(String columnLabel) throws ResultSetException {
		unsetMatchColumn(findColumn(columnLabel));
	}

	public void unsetMatchColumn(String[] columnLabels)
			throws ResultSetException {
		int[] removeColumnIndex = new int[columnLabels.length];

		int i = 0;
		for (String columnLabel : columnLabels) {
			removeColumnIndex[i] = findColumn(columnLabel);
			i++;
		}

		unsetMatchColumn(removeColumnIndex);

	}
	
	@Override
	public void load(String resultSetLocation) throws ResultSetException,
			PersistableException {
		throw new ResultSetException("Not Implemented in this class");
		
	}

	public void close() throws ResultSetException {
		this.closed = true;
	}

	public boolean isClosed() {
		return closed;
	}

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

	public void afterLast() throws ResultSetException {
		if (isClosed()) {
			throw new ResultSetException("ResultSet is closed");
		}
		this.setRowPosition(size);

	}

	public void beforeFirst() throws ResultSetException {
		if (isClosed()) {
			throw new ResultSetException("ResultSet is closed");
		}
		this.setRowPosition(-1);

	}

	public boolean first() throws ResultSetException {
		if (isClosed()) {
			throw new ResultSetException("ResultSet is closed");
		}
		this.setRowPosition(0);
		return true;
	}

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

	public boolean previous() throws ResultSetException {
		if (isClosed()) {
			throw new ResultSetException("ResultSet is closed");
		}
		relative(-1);
		return false;
	}

	public boolean last() throws ResultSetException {
		this.setRowPosition(this.size - 1);
		return false;
	}

	public boolean isFirst() throws ResultSetException {
		if (isClosed()) {
			throw new ResultSetException("ResultSet is closed");
		}
		return getRowPosition() == 0;
	}

	public boolean isLast() throws ResultSetException {
		if (isClosed()) {
			throw new ResultSetException("ResultSet is closed");
		}
		return (getRowPosition() == size - 1);
	}

	public long getRow() throws ResultSetException {
		if (isClosed()) {
			throw new ResultSetException("ResultSet is closed");
		}
		return getRowPosition();
	}

	public long getSize() throws ResultSetException {
		if (isClosed()) {
			throw new ResultSetException("ResultSet is closed");
		}
		return size;
	}

	public int getColumnSize() throws ResultSetException {
		if (isClosed()) {
			throw new ResultSetException("ResultSet is closed");
		}
		return columns.length;
	}

	// COLUMN INFORMATION
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

	public Column getColumn(int columnIndex) throws ResultSetException {
		if (isClosed()) {
			throw new ResultSetException("ResultSet is closed");
		}
		if (columnIndex >= columns.length) {
			throw new ResultSetException("Column not found");
		}

		return columns[columnIndex];
	}
	
	public Column[] getColumns() throws ResultSetException {
		if (isClosed()) {
			throw new ResultSetException("ResultSet is closed");
		}
		return columns;
	}
	
	public void setColumns(Column[] columns) throws ResultSetException {
		if (isClosed()) {
			throw new ResultSetException("ResultSet is closed");
		}
		this.columns = columns;
	}

	// Data Retrieval and editing
	// BOOLEAN
	public boolean getBoolean(int columnIndex) throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	public boolean getBoolean(String columnLabel) throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	public void updateBoolean(int columnIndex, boolean value)
			throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	public void updateBoolean(String columnLabel, boolean value)
			throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	// BYTE
	public byte getByte(int columnIndex) throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	public byte getByte(String columnLabel) throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	public void updateByte(int columnIndex, byte value)
			throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	public void updateByte(String columnLabel, byte value)
			throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	// DATE
	public Date getDate(int columnIndex) throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	public Date getDate(String columnLabel) throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	public void updateDate(int columnIndex, Date value)
			throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	public void updateDate(String columnLabel, Date value)
			throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	// DOUBLE
	public double getDouble(int columnIndex) throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	public double getDouble(String columnLabel) throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	public void updateDouble(int columnIndex, double value)
			throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	public void updateDouble(String columnLabel, double value)
			throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	// FLOAT
	public float getFloat(int columnIndex) throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	public float getFloat(String columnLabel) throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	public void updateFloat(int columnIndex, float value)
			throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	public void updateFloat(String columnLabel, float value)
			throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	// INT
	public int getInt(int columnIndex) throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	public int getInt(String columnLabel) throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	public void updateInt(int columnIndex, int value) throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	public void updateInt(String columnLabel, int value)
			throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	// LONG
	public long getLong(int columnIndex) throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	public long getLong(String columnLabel) throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	public void updateLong(int columnIndex, long value)
			throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	public void updateLong(String columnLabel, long value)
			throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	// STRING
	public String getString(int columnIndex) throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	public String getString(String columnLabel) throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	public void updateString(int columnIndex, String value)
			throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	public void updateString(String columnLabel, String value)
			throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	// OBJECT
	public Object getObject(int columnIndex) throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	public void updateObject(int columnIndex, Object obj)
			throws ResultSetException {
		throw new ResultSetException("Not Implemented in this class");
	}

	public long getRowPosition() {
		return rowPosition;
	}

	public void setRowPosition(long rowPosition) {
		this.rowPosition = rowPosition;
	}
}
