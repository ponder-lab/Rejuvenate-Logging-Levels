/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.resource;

import java.io.Serializable;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;

/**
 * 
 * Defines the process type
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@Entity
public class ProcessType implements Serializable {
	private static final long serialVersionUID = 30045608286165958L;

	@Id
	private long id;
	
	private String name;
	private String displayName;
	private String description;
	
	@JoinTable(name = "ProcessType_Fields")
//	@OneToMany(fetch = FetchType.EAGER)
	@OneToMany
	private List<Field> fields;
	
	@JoinTable(name = "ProcessType_Returns")
//	@OneToMany(fetch = FetchType.EAGER)
	@OneToMany
	private List<Field> returns;	
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
		
		JsonObjectBuilder processTypeJSON = Json.createObjectBuilder();
		
		processTypeJSON.add("name", name);
		processTypeJSON.add("displayName", displayName);
		processTypeJSON.add("description", description);
		
		JsonArrayBuilder fieldType = Json.createArrayBuilder();
		if (this.fields != null) {
			for (Field value : this.fields) {
				fieldType.add(value.toJson());
			}
		}
		
		processTypeJSON.add("fields", fieldType);
		
		JsonArrayBuilder returnsType = Json.createArrayBuilder();
		if (this.returns != null) {
			for (Field value : this.returns) {
				returnsType.add(value.toJson());
			}
		}
		
		processTypeJSON.add("returns", returnsType);
		
		return processTypeJSON.build();
	}

	/**
	 * Returns the id of the process type
	 * 
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * Sets the id of the process type
	 * 
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Returns the name of the process type
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the process type
	 * 
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the display name of the process type
	 * 
	 * @return the displayName
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Sets the display name of the process type
	 * 
	 * @param displayName the displayName to set
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * Returns a description of the process type
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets a description of the process type
	 * 
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Returns a list of fields for the process type
	 * 
	 * @return the fields
	 */
	public List<Field> getFields() {
		return fields;
	}

	/**
	 * Sets a list of fields for the process type
	 * 
	 * @param fields the fields to set
	 */
	public void setFields(List<Field> fields) {
		this.fields = fields;
	}

	/**
	 * Returns a list of fields that can be returned by the process type
	 * 
	 * @return the returns
	 */
	public List<Field> getReturns() {
		return returns;
	}

	/**
	 * Sets a list of fields that can be returned by the process type
	 * @param returns the returns to set
	 */
	public void setReturns(List<Field> returns) {
		this.returns = returns;
	}
	
	
	
}
