/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.query;

import java.io.Serializable;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.DataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Field;
import edu.harvard.hms.dbmi.bd2k.irct.util.converter.DataTypeConverter;

@Entity
public class JoinType implements Serializable {
	private static final long serialVersionUID = 7650772906469344618L;

	@Id
	@GeneratedValue
	private long id;
	
	private String name;
	private String displayName;
	private String description;
	
	@OneToMany(fetch=FetchType.EAGER)
	private List<Field> fields;

	@ElementCollection
	@Convert(converter = DataTypeConverter.class)
	private List<DataType> dataTypes;
	
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
		jsonBuilder.add("displayName", this.displayName);
		jsonBuilder.add("description", this.description);

		JsonArrayBuilder valuesType = Json.createArrayBuilder();
		if (this.fields != null) {
			for (Field value : this.fields) {
				valuesType.add(value.toJson());
			}
		}
		jsonBuilder.add("fields", valuesType.build());

		return jsonBuilder.build();
	}

	/**
	 * Returns the id of the join type
	 * 
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * Sets the id of the join type
	 * 
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Returns the name of the join type
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the join type
	 * 
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the display name of the join type
	 * @return the displayName
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Sets the display name of the join type
	 * 
	 * @param displayName the displayName to set
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * Returns the description of the join type
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets a description of the join type
	 * 
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Returns a list of fields for the join type
	 * 
	 * @return the fields
	 */
	public List<Field> getFields() {
		return fields;
	}

	/**
	 * Sets a list of fields for the join type
	 * 
	 * @param fields the fields to set
	 */
	public void setFields(List<Field> fields) {
		this.fields = fields;
	}

	/**
	 * Returns a list of join types that this join can be performed on. An empty list means all data types.
	 * 
	 * @return the dataTypes
	 */
	public List<DataType> getDataTypes() {
		return dataTypes;
	}

	/**
	 * Sets a list of join types that this join can be performed on. An empty list means all data types.
	 * @param dataTypes the dataTypes to set
	 */
	public void setDataTypes(List<DataType> dataTypes) {
		this.dataTypes = dataTypes;
	}
	
	
}
