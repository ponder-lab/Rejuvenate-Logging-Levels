/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.process;

import java.io.Serializable;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

/**
 * An IRCT Process Parameter is used by an IRCT Process to describe a set of
 * values that can be passed into it.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@Entity
public class ProcessTypeParameter implements Serializable {

	private static final long serialVersionUID = 4141141792005064323L;

	@Id
	private long id;
	private String name;
	
	@Enumerated(EnumType.STRING)
	private ProcessTypeParameterType type;
	private String value;
	
	
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
		
		JsonObjectBuilder processTypeParameterJSON = Json.createObjectBuilder();
		
		processTypeParameterJSON.add("name", this.name);
		processTypeParameterJSON.add("type", this.type.toString());
		
		return processTypeParameterJSON.build();
	}
	
	/**
	 * Returns the id
	 * 
	 * @return ID
	 */
	public long getId() {
		return id;
	}

	/**
	 * Sets the id
	 * 
	 * @param id ID
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Returns the name of the parameter
	 * 
	 * @return Name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the parameter
	 * 
	 * @param name Name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the IRCT Parameter Type
	 * 
	 * @return Parameter Type
	 */
	public ProcessTypeParameterType getType() {
		return type;
	}

	/**
	 * Sets the IRCT Parameter Type
	 * 
	 * @param type Parameter Type
	 */
	public void setType(ProcessTypeParameterType type) {
		this.type = type;
	}

	/**
	 * Returns the value of the parameter
	 * 
	 * @return Value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Sets the value of the parameter
	 * 
	 * @param value Value
	 */
	public void setValue(String value) {
		this.value = value;
	}

}
