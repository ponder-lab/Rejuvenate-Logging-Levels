/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.result;

/**
 * Provides a row representation of a resultSet
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class Row {
	Object[] data;
	
	/**
	 * Creates a new row of a given column size
	 * 
	 * @param columnSize Number of columns
	 */
	public Row(int columnSize) {
		this.data = new Object[columnSize];
	}

	/**
	 * Returns the object associated with a given column in the row
	 * 
	 * @param columnIndex Column Index
	 * @return Value
	 */
	public Object getColumn(int columnIndex) {
		return data[columnIndex];
	}
	
	/**
	 * Sets the object associated with a given column in the row
	 * 
	 * @param columnIndex Column Index
	 * @param value Value
	 */
	public void setColumn(int columnIndex, Object value) {
		data[columnIndex] = value;
	}
}
