/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType;

/**
 * Describes a column of a Result Set
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class Column {
	private String name;
	private PrimitiveDataType dataType;

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
	public PrimitiveDataType getDataType() {
		return dataType;
	}

	/**
	 * Sets the Data Type of the column
	 * 
	 * @param dataType
	 *            Column Data Type
	 */
	public void setDataType(PrimitiveDataType dataType) {
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

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((dataType == null) ? 0 : dataType.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Column other = (Column) obj;
		if (dataType != other.dataType)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
