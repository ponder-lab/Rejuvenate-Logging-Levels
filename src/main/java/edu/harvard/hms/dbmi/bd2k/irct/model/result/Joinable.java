/*
 *  This file is part of Inter-Resource Communication Tool (IRCT).
 *
 *  IRCT is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  IRCT is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with IRCT.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.harvard.hms.dbmi.bd2k.irct.model.result;

import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.ResultSetException;

/**
 * The Joinable interface provides a means for different ResultSets to be
 * combined. Different types of Joins can implement this interface which
 * provides the basis for setting the columns that will be joined upon.
 * 
 * @author Jeremy R. Easton-Marks
 * @version 1.0
 *
 */
public interface Joinable {

	/**
	 * Returns an object representation of the value at the given column index
	 * 
	 * @param columnIndex
	 *            The column starting at position 0
	 * @return The column value
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	Object getObject(int columnIndex) throws ResultSetException;

	/**
	 * Updates the column with an object representation of the value
	 * 
	 * @param columnIndex
	 *            The column starting at position 0
	 * @param obj
	 *            The new column value
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	void updateObject(int columnIndex, Object obj) throws ResultSetException;

	/**
	 * Gets the indexes of the columns that are to be matched
	 * 
	 * @return an int array of column indexes that are set as matched columns
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	int[] getMatchColumnIndexes() throws ResultSetException;

	/**
	 * Gets the names of the columns that are to be matched
	 * 
	 * @return a string array of column indexes that are set as matched columns
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	String[] getMatchColumnNames() throws ResultSetException;

	/**
	 * Sets the designated column index to be joined on
	 * 
	 * @param columnIndex
	 *            the column index to join on
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	void setMatchColumn(int columnIndex) throws ResultSetException;

	/**
	 * Sets the designated columns by index to be joined on
	 * 
	 * @param columnIndexes
	 *            the column indexes to join on
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	void setMatchColumn(int[] columnIndexes) throws ResultSetException;

	/**
	 * Sets the designated column label to be joined on
	 * 
	 * @param columnLabel
	 *            the column index to join on
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	void setMatchColumn(String columnLabel) throws ResultSetException;

	/**
	 * Sets the designated columns by label to be joined on
	 * 
	 * @param columnLabels
	 *            the column labels to join on
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	void setMatchColumns(String[] columnLabels) throws ResultSetException;

	/**
	 * Unsets the given column as a joining column
	 * 
	 * @param columnIndex
	 *            the index of the column to no longer match on
	 * 
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	void unsetMatchColumn(int columnIndex) throws ResultSetException;

	/**
	 * Unsets the given columns as a joining column
	 * 
	 * @param columnIndexes
	 *            the indexes of the column to no longer match on
	 * 
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	void unsetMatchColumn(int[] columnIndexes) throws ResultSetException;

	/**
	 * Unsets the given column as a joining column
	 * 
	 * @param columnLabel
	 *            the label of the column to no longer match on
	 * 
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	void unsetMatchColumn(String columnLabel) throws ResultSetException;

	/**
	 * Unsets the given columns as a joining column
	 * 
	 * @param columnLabels
	 *            the index of the column to no longer match on
	 * 
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	void unsetMatchColumn(String[] columnLabels) throws ResultSetException;
}
