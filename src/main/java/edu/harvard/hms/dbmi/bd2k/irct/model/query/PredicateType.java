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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.DataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Field;
import edu.harvard.hms.dbmi.bd2k.irct.util.converter.DataTypeConverter;

/**
 * The predicate type class provides a way for the IRCT application to keep
 * track of which predicates can be used. 
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@Entity
public class PredicateType implements Serializable {
	private static final long serialVersionUID = -8767223525164395205L;

	@Id
	@GeneratedValue
	private long id;
	
	private String name;
	private String displayName;
	private String description;
	private boolean defaultPredicate;
	
	@OneToMany
	private List<Field> fields;

	@ElementCollection
	@Convert(converter = DataTypeConverter.class)
	private List<DataType> dataTypes;
	
	@ElementCollection
	private List<String> paths;
	

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
		
		JsonObjectBuilder predicateTypeJSON = Json.createObjectBuilder();
		
		predicateTypeJSON.add("predicateName", getName());
		predicateTypeJSON.add("displayName", getDisplayName());
		predicateTypeJSON.add("description", getDescription());
		predicateTypeJSON.add("default", isDefaultPredicate());
		
		JsonArrayBuilder valuesType = Json.createArrayBuilder();
		if(this.fields != null) {
			for(Field value : this.getFields()) {
				valuesType.add(value.toJson());
			}
		}
		predicateTypeJSON.add("fields", valuesType.build());
		
		JsonArrayBuilder dataTypes = Json.createArrayBuilder();
		if(this.dataTypes != null) {
			for(DataType dt : this.dataTypes) {
				dataTypes.add(dt.toString());
			}
		}
		predicateTypeJSON.add("dataTypes", dataTypes.build());
		
		
		JsonArrayBuilder pathArray = Json.createArrayBuilder();
		if(this.paths != null) {
			for(String path : paths) {
				pathArray.add(path);
			}
		}
		predicateTypeJSON.add("paths", pathArray.build());
		
		
		return predicateTypeJSON.build();
	}

	/**
	 * Returns the predicates id
	 * 
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * Sets the predicate id
	 *  
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Returns the name of the predicate
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the predicate
	 * 
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the display name of the predicate
	 * 
	 * @return the displayName
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Sets the display name of the predicate
	 * 
	 * @param displayName the displayName to set
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * Returns a description of the predicate
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description of the predicate
	 * 
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Returns if this should be the default predicate
	 * 
	 * @return the defaultPredicate
	 */
	public boolean isDefaultPredicate() {
		return defaultPredicate;
	}

	/**
	 * Sets if this should be the default predicate
	 * 
	 * @param defaultPredicate the defaultPredicate to set
	 */
	public void setDefaultPredicate(boolean defaultPredicate) {
		this.defaultPredicate = defaultPredicate;
	}

	/**
	 * Returns a list of fields associated with the predicate
	 * 
	 * @return the fields
	 */
	public List<Field> getFields() {
		return fields;
	}

	/**
	 * Sets the list of fields associated with the predicate
	 * 
	 * @param fields the fields to set
	 */
	public void setFields(List<Field> fields) {
		this.fields = fields;
	}

	/**
	 * Returns a list of data types that this predicate can be run on. An empty list means all data types.
	 * 
	 * @return the dataTypes
	 */
	public List<DataType> getDataTypes() {
		return dataTypes;
	}

	/**
	 * Sets the list of data type that this predicate can be run on. An empty list means all data types.
	 * 
	 * @param dataTypes the dataTypes to set
	 */
	public void setDataTypes(List<DataType> dataTypes) {
		this.dataTypes = dataTypes;
	}

	/**
	 * Returns a list of paths that restrict what fields this predicate can be run on. An empty list means all paths.
	 * @return the paths
	 */
	public List<String> getPaths() {
		return paths;
	}

	/**
	 * Sets a list of paths that restrict what fields this predicate can be run on. An empty list means all paths.
	 * 
	 * @param paths the paths to set
	 */
	public void setPaths(List<String> paths) {
		this.paths = paths;
	}
}
