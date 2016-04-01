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
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
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
	
	@OneToMany(fetch = FetchType.EAGER)
	private List<Field> fields;
	
	@OneToMany(fetch = FetchType.EAGER)
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
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the displayName
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * @param displayName the displayName to set
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the fields
	 */
	public List<Field> getFields() {
		return fields;
	}

	/**
	 * @param fields the fields to set
	 */
	public void setFields(List<Field> fields) {
		this.fields = fields;
	}

	/**
	 * @return the returns
	 */
	public List<Field> getReturns() {
		return returns;
	}

	/**
	 * @param returns the returns to set
	 */
	public void setReturns(List<Field> returns) {
		this.returns = returns;
	}
	
	
	
}
