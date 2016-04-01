/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.result;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.ArrayUtils;

import edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.ResultSetException;

/**
 * The memory ResultSet stores a result set in JVM memory. It is a fast
 * implementation that provides a way to handle small data sets.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class MemoryResultSet extends ResultSetImpl {
	private Row[] data;
	private long size;

	/**
	 * Adds a new row to the memory result set
	 * 
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	public void appendRow() throws ResultSetException {
		this.data = ArrayUtils.add(data, new Row(this.getColumnSize()));
		setSize(this.getSize() + 1);
		next();
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

	/**
	 * Returns a cell from the given column at the current position
	 * 
	 * @param columnIndex Column Index
	 * @return Value
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	private Object getCell(int columnIndex) throws ResultSetException {
		if (columnIndex >= getColumnSize()) {
			throw new ResultSetException("Column not found");
		}
		return data[(int) getRow()].getColumn(columnIndex);
	}

	/**
	 * Sets the value of a cell at the given column at the current position
	 * @param columnIndex Column Index
	 * @param value Value
	 * @throws ResultSetException If a ResultSetException occurs
	 */
	private void setCell(int columnIndex, Object value)
			throws ResultSetException {
		if (columnIndex >= getColumnSize()) {
			throw new ResultSetException("Column not found");
		}
		data[(int) getRow()].setColumn(columnIndex, value);
	}

	
	public long getSize() throws ResultSetException {
		if (isClosed()) {
			throw new ResultSetException("ResultSet is closed");
		}
		return size;
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
	@Override
	public Object getObject(int columnIndex) throws ResultSetException {
		return (Object) getCell(columnIndex);
	}

	@Override
	public void updateObject(int columnIndex, Object obj)
			throws ResultSetException {
		setCell(columnIndex, obj);
	}

}
