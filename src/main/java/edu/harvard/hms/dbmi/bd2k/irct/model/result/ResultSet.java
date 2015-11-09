/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.result;

import java.util.Date;

import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.ResultSetException;

/**
 * Provides an interface that all result sets are to implement. It is based on
 * the Java ResultSet classes but provides more customizable features.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public interface ResultSet {
	// Administrative
	/**
	 * Releases the connection to the datasource
	 * 
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	void close() throws ResultSetException;

	/**
	 * Returns the status of the connection to the datasource
	 * 
	 * @return true if closed
	 */
	boolean isClosed();

	// Cursor Position

	/**
	 * Moves the cursor to the given row number in the ResultSet
	 * 
	 * @param row
	 *            The row to move to
	 * @return true if the cursor moved successfully, false otherwise
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	boolean absolute(long row) throws ResultSetException;

	/**
	 * Moves the cursor to the end of the ResultSet, after the last row.
	 * 
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	void afterLast() throws ResultSetException;

	/**
	 * Moves the cursor to the beginning of the ResultSet object, before the
	 * first row.
	 * 
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	void beforeFirst() throws ResultSetException;

	/**
	 * Moves the cursor to the first row of the ResultSet object.
	 * 
	 * @return true if moved to the first row, false otherwise
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	boolean first() throws ResultSetException;

	/**
	 * Moves the cursor to the next row of the ResultSet object
	 * 
	 * @return true if moved to the next row, false otherwise
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	boolean next() throws ResultSetException;

	/**
	 * Moves the cursor a given number of rows forward
	 * 
	 * @param rows
	 *            The number of rows to move forward
	 * @return true if the cursor moved to the new row
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	boolean relative(long rows) throws ResultSetException;

	/**
	 * Moves the cursor to the previous row in the ResultSet object
	 * 
	 * @return true if the cursor moved to the previous row, false otherwise
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	boolean previous() throws ResultSetException;

	/**
	 * Moves the cursor to the last row in the ResultSet object
	 * 
	 * @return true if the cursor moved to the new row
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	boolean last() throws ResultSetException;

	/**
	 * Retrieves if the cursor is on the first row in the ResultSet object
	 * 
	 * @return true if the cursor is on the first row; false otherwise
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	boolean isFirst() throws ResultSetException;

	/**
	 * Retrieves if the cursor is on the last row in the ResultSet object
	 * 
	 * @return true if the cursor is on the last row; false otherwise
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	boolean isLast() throws ResultSetException;

	/**
	 * Returns the current position of the cursor in the ResultSet object
	 * 
	 * @return The position of the cursor in the ResultSet object
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	long getRow() throws ResultSetException;

	/**
	 * Returns the size of the ResultSetObject
	 * 
	 * @return The size of the ResultSet object
	 * 
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	long getSize() throws ResultSetException;

	// Column Information
	/**
	 * Returns the position of the given column in the ResultSet object
	 * 
	 * @param columnLabel
	 *            The name of the column
	 * @return The position of the column
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	int findColumn(String columnLabel) throws ResultSetException;

	/**
	 * Gets the column associated with the columnIndex
	 * 
	 * @param columnIndex
	 *            The column starting at position 1
	 * @return the column object
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	Column getColumn(int columnIndex) throws ResultSetException;

	/**
	 * Returns an array of columns associated with the result set
	 * 
	 * @return Columns
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	Column[] getColumns() throws ResultSetException;

	// Data Retrieval and editing
	// BOOLEAN
	/**
	 * Retrieves the boolean value of the given column position at the current
	 * row
	 * 
	 * @param columnIndex
	 *            The column starting at position 1
	 * @return the column value
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	boolean getBoolean(int columnIndex) throws ResultSetException;

	/**
	 * Retrieves the boolean value of the given column at the current row
	 * 
	 * @param columnLabel
	 *            the name of the column to retrieve
	 * @return the column value
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	boolean getBoolean(String columnLabel) throws ResultSetException;

	/**
	 * Updates the boolean value of the given column position at the current row
	 * 
	 * @param columnIndex
	 *            The column starting at position 1
	 * @param value
	 *            The new column value
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	void updateBoolean(int columnIndex, boolean value)
			throws ResultSetException;

	/**
	 * Updates the boolean value of the given column at the current row
	 * 
	 * @param columnLabel
	 *            the name of the column to set
	 * @param value
	 *            The new column value
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	void updateBoolean(String columnLabel, boolean value)
			throws ResultSetException;

	// BYTE
	/**
	 * Retrieves the byte value of the given column position at the current row
	 * 
	 * @param columnIndex
	 *            The column starting at position 1
	 * @return the column value
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	byte getByte(int columnIndex) throws ResultSetException;

	/**
	 * Retrieves the byte value of the given column at the current row
	 * 
	 * @param columnLabel
	 *            the name of the column to retrieve
	 * @return the column value
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	byte getByte(String columnLabel) throws ResultSetException;

	/**
	 * Updates the byte value of the given column position at the current row
	 * 
	 * @param columnIndex
	 *            The column starting at position 1
	 * @param value
	 *            The new column value
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	void updateByte(int columnIndex, byte value) throws ResultSetException;

	/**
	 * Updates the byte value of the given column at the current row
	 * 
	 * @param columnLabel
	 *            the name of the column to set
	 * @param value
	 *            The new column value
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	void updateByte(String columnLabel, byte value) throws ResultSetException;

	// DATE
	/**
	 * Retrieves the Date value of the given column position at the current row
	 * 
	 * @param columnIndex
	 *            The column starting at position 1
	 * @return the column value
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	Date getDate(int columnIndex) throws ResultSetException;

	/**
	 * Retrieves the Date value of the given column at the current row
	 * 
	 * @param columnLabel
	 *            the name of the column to retrieve
	 * @return the column value
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	Date getDate(String columnLabel) throws ResultSetException;

	/**
	 * Updates the Date value of the given column position at the current row
	 * 
	 * @param columnIndex
	 *            The column starting at position 1
	 * @param value
	 *            The new column value
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	void updateDate(int columnIndex, Date value) throws ResultSetException;

	/**
	 * Updates the Date value of the given column at the current row
	 * 
	 * @param columnLabel
	 *            the name of the column to set
	 * @param value
	 *            The new column value
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	void updateDate(String columnLabel, Date value) throws ResultSetException;

	// DOUBLE
	/**
	 * Retrieves the double value of the given column position at the current
	 * row
	 * 
	 * @param columnIndex
	 *            The column starting at position 1
	 * @return the column value
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	double getDouble(int columnIndex) throws ResultSetException;

	/**
	 * Retrieves the double value of the given column at the current row
	 * 
	 * @param columnLabel
	 *            the name of the column to retrieve
	 * @return the column value
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	double getDouble(String columnLabel) throws ResultSetException;

	/**
	 * Updates the double value of the given column position at the current row
	 * 
	 * @param columnIndex
	 *            The column starting at position 1
	 * @param value
	 *            The new column value
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	void updateDouble(int columnIndex, double value) throws ResultSetException;

	/**
	 * Updates the double value of the given column at the current row
	 * 
	 * @param columnLabel
	 *            the name of the column to set
	 * @param value
	 *            The new column value
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	void updateDouble(String columnLabel, double value)
			throws ResultSetException;

	// FLOAT
	/**
	 * Retrieves the float value of the given column position at the current row
	 * 
	 * @param columnIndex
	 *            The column starting at position 1
	 * @return the column value
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	float getFloat(int columnIndex) throws ResultSetException;

	/**
	 * Retrieves the float value of the given column at the current row
	 * 
	 * @param columnLabel
	 *            the name of the column to retrieve
	 * @return the column value
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	float getFloat(String columnLabel) throws ResultSetException;

	/**
	 * Updates the float value of the given column position at the current row
	 * 
	 * @param columnIndex
	 *            The column starting at position 1
	 * @param value
	 *            The new column value
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	void updateFloat(int columnIndex, float value) throws ResultSetException;

	/**
	 * Updates the float value of the given column at the current row
	 * 
	 * @param columnLabel
	 *            the name of the column to set
	 * @param value
	 *            The new column value
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	void updateFloat(String columnLabel, float value) throws ResultSetException;

	// INT
	/**
	 * Retrieves the int value of the given column position at the current row
	 * 
	 * @param columnIndex
	 *            The column starting at position 1
	 * @return the column value
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	int getInt(int columnIndex) throws ResultSetException;

	/**
	 * Retrieves the int value of the given column at the current row
	 * 
	 * @param columnLabel
	 *            the name of the column to retrieve
	 * @return the column value
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	int getInt(String columnLabel) throws ResultSetException;

	/**
	 * Updates the int value of the given column position at the current row
	 * 
	 * @param columnIndex
	 *            The column starting at position 1
	 * @param value
	 *            The new column value
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	void updateInt(int columnIndex, int value) throws ResultSetException;

	/**
	 * Updates the int value of the given column at the current row
	 * 
	 * @param columnLabel
	 *            the name of the column to set
	 * @param value
	 *            The new column value
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	void updateInt(String columnLabel, int value) throws ResultSetException;

	// LONG
	/**
	 * Retrieves the long value of the given column position at the current row
	 * 
	 * @param columnIndex
	 *            The column starting at position 1
	 * @return the column value
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	long getLong(int columnIndex) throws ResultSetException;

	/**
	 * Retrieves the long value of the given column at the current row
	 * 
	 * @param columnLabel
	 *            the name of the column to retrieve
	 * @return the column value
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	long getLong(String columnLabel) throws ResultSetException;

	/**
	 * Updates the long value of the given column position at the current row
	 * 
	 * @param columnIndex
	 *            The column starting at position 1
	 * @param value
	 *            The new column value
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	void updateLong(int columnIndex, long value) throws ResultSetException;

	/**
	 * Updates the long value of the given column at the current row
	 * 
	 * @param columnLabel
	 *            the name of the column to set
	 * @param value
	 *            The new column value
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	void updateLong(String columnLabel, long value) throws ResultSetException;

	// STRING
	/**
	 * Retrieves the String value of the given column position at the current
	 * row
	 * 
	 * @param columnIndex
	 *            The column starting at position 1
	 * @return the column value
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	String getString(int columnIndex) throws ResultSetException;

	/**
	 * Retrieves the String value of the given column at the current row
	 * 
	 * @param columnLabel
	 *            the name of the column to retrieve
	 * @return the column value
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	String getString(String columnLabel) throws ResultSetException;

	/**
	 * Updates the String value of the given column position at the current row
	 * 
	 * @param columnIndex
	 *            The column starting at position 1
	 * @param value
	 *            The new column value
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	void updateString(int columnIndex, String value) throws ResultSetException;

	/**
	 * Updates the String value of the given column at the current row
	 * 
	 * @param columnLabel
	 *            the name of the column to set
	 * @param value
	 *            The new column value
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	void updateString(String columnLabel, String value)
			throws ResultSetException;

	/**
	 * Returns the column size
	 * 
	 * @return Column size
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	int getColumnSize() throws ResultSetException;

}
