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
