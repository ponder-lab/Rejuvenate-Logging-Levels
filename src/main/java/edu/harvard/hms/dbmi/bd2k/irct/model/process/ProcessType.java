/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.process;

import java.io.Serializable;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.persistence.Entity;
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
	
	@OneToMany
	private List<ProcessTypeParameter> parameter;
	
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
		
		JsonArrayBuilder processType = Json.createArrayBuilder();
		if(depth == 0) {
			for(ProcessTypeParameter param : parameter) {
				processType.add(param.getName());
			}

		} else {
			for(ProcessTypeParameter param : parameter) {
				processType.add(param.toJson(depth));
			}
		}
		
		processTypeJSON.add("parameters", processType);
		
		return processTypeJSON.build();
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
	 * Returns the name of the process
	 * 
	 * @return Name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the process
	 * 
	 * @param name Name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns a description of the process
	 * 
	 * @return Description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description of the process
	 * 
	 * @param description Description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Returns a list parameters
	 * 
	 * @return List of parameters
	 */
	public List<ProcessTypeParameter> getParameter() {
		return parameter;
	}

	/**
	 * Sets a list of the parameters
	 * 
	 * @param parameter List of the parameters
	 */
	public void setParameter(List<ProcessTypeParameter> parameter) {
		this.parameter = parameter;
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
	
	

}
