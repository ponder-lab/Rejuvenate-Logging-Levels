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
	
	@OneToMany(fetch=FetchType.EAGER)
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
	 * @return the defaultPredicate
	 */
	public boolean isDefaultPredicate() {
		return defaultPredicate;
	}

	/**
	 * @param defaultPredicate the defaultPredicate to set
	 */
	public void setDefaultPredicate(boolean defaultPredicate) {
		this.defaultPredicate = defaultPredicate;
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
	 * @return the dataTypes
	 */
	public List<DataType> getDataTypes() {
		return dataTypes;
	}

	/**
	 * @param dataTypes the dataTypes to set
	 */
	public void setDataTypes(List<DataType> dataTypes) {
		this.dataTypes = dataTypes;
	}

	/**
	 * @return the paths
	 */
	public List<String> getPaths() {
		return paths;
	}

	/**
	 * @param paths the paths to set
	 */
	public void setPaths(List<String> paths) {
		this.paths = paths;
	}
}
