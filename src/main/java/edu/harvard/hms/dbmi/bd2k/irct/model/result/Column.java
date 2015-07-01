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

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.DataType;

/**
 * Describes a column of a Result Set
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class Column {
	private String name;
	private DataType dataType;

	/**
	 * Returns the name of the column
	 * 
	 * @return Column name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the column
	 * 
	 * @param name
	 *            Column name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the Data Type of the column
	 * 
	 * @return Column Data Type
	 */
	public DataType getDataType() {
		return dataType;
	}

	/**
	 * Sets the Data Type of the column
	 * 
	 * @param dataType
	 *            Column Data Type
	 */
	public void setDataType(DataType dataType) {
		this.dataType = dataType;
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
		jsonBuilder.add("name", this.name);
		jsonBuilder.add("dataType", this.dataType.toString());
		return jsonBuilder.build();
	}

}
