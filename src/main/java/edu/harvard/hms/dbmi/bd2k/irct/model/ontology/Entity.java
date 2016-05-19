/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.ontology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.persistence.Convert;
import javax.persistence.Id;
import javax.persistence.Transient;

import edu.harvard.hms.dbmi.bd2k.irct.util.converter.DataTypeConverter;

/**
 * The Entity class represents a path, and object in a resource. Entities can be linked to other
 * paths through ontology relationships.
 * 
 * @author Jeremy R. Easton-Marks
 */
@javax.persistence.Entity
public class Entity {
	
	
	@Id
	private String pui;
	private String name;
	private String displayName;
	private String description;
	private String ontology;
	private String ontologyId;

	@Convert(converter = DataTypeConverter.class)
	private DataType dataType;
	@Transient
	private List<OntologyRelationship> relationships;
	@Transient
	private Map<String, Integer> counts;
	@Transient
	private Map<String, String> attributes;

	
	/**
	 * Creates an empty Entity with no PUI
	 */
	public Entity() {
		this("");
	}

	/**
	 * Creates an empty Entity with a pui
	 * 
	 * @param pui Path Unique Identifier
	 */
	public Entity(String pui) {
		this.pui = pui;
		this.name = "";
		this.displayName = "";
		this.description = "";
		this.ontology = "";
		this.ontologyId = "";
		this.dataType = null;
		this.relationships = new ArrayList<OntologyRelationship>();
		this.counts = new HashMap<String, Integer>();
		this.attributes = new HashMap<String, String>();
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
		JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
		jsonBuilder.add("pui", this.pui);
		jsonBuilder.add("name", this.name);
		if(this.displayName != null) {
			jsonBuilder.add("displayName", this.displayName);
		}
		if(this.description != null) {
			jsonBuilder.add("description", this.description);
		}
		if(this.ontology != null) {
			jsonBuilder.add("ontology", this.ontology);
		}
		if(this.ontologyId != null) {
			jsonBuilder.add("ontologyId", this.ontologyId);
		}
		if(dataType != null) {
			jsonBuilder.add("dataType", this.dataType.toJson());
		}
		
		//relationships
		JsonArrayBuilder relationships = Json.createArrayBuilder();
		for(OntologyRelationship or : this.relationships) {
			relationships.add(or.toString());
		}
		jsonBuilder.add("relationships", relationships.build());
		
		//counts
		JsonObjectBuilder countObject = Json.createObjectBuilder();
		for(String countname : this.counts.keySet()) {
			countObject.add(countname, this.counts.get(countname));
		}
		jsonBuilder.add("counts", countObject.build());
		
		//attributes
		JsonObjectBuilder attributesObject = Json.createObjectBuilder();
		for(String attributeName : this.attributes.keySet()) {
			if(this.attributes.get(attributeName) == null) {
				attributesObject.addNull(attributeName);
			} else {
				attributesObject.add(attributeName, this.attributes.get(attributeName));
			}
		}
		jsonBuilder.add("attributes", attributesObject.build());
		
		return jsonBuilder.build();

	}
	

	// -------------------------------------------------------------------------
	// SETTERS AND GETTERS
	// -------------------------------------------------------------------------

	/**
	 * Returns the Path Unique Identifier
	 * 
	 * @return the pui
	 */
	public String getPui() {
		return pui;
	}

	/**
	 * Sets the Path Unique Identifier
	 * 
	 * @param pui the pui to set
	 */
	public void setPui(String pui) {
		this.pui = pui;
	}

	/**
	 * Returns the entity name
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the entity name
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the display name
	 * 
	 * @return the displayName
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Sets the display name
	 * 
	 * @param displayName the displayName to set
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * Returns the entity description
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the entity description
	 * 
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Returns the entity ontology
	 * 
	 * @return the ontology
	 */
	public String getOntology() {
		return ontology;
	}

	/**
	 * Sets the entity ontology
	 * 
	 * @param ontology the ontology to set
	 */
	public void setOntology(String ontology) {
		this.ontology = ontology;
	}

	/**
	 * Returns the entity ontology term id
	 * 
	 * @return the ontologyId
	 */
	public String getOntologyId() {
		return ontologyId;
	}

	/**
	 * Sets the entity ontology term id
	 * @param ontologyId the ontologyId to set
	 */
	public void setOntologyId(String ontologyId) {
		this.ontologyId = ontologyId;
	}

	/**
	 * Return the data type of the entity
	 * 
	 * @return the dataType
	 */
	public DataType getDataType() {
		return dataType;
	}

	/**
	 * Sets the data type of the entity
	 * 
	 * @param dataType the dataType to set
	 */
	public void setDataType(DataType dataType) {
		this.dataType = dataType;
	}

	/**
	 * Returns a list of relationships this entity has
	 * 
	 * @return the relationships 
	 */
	public List<OntologyRelationship> getRelationships() {
		return relationships;
	}

	/**
	 * Sets the list of relationships this entity has
	 * 
	 * @param relationships the relationships to set
	 */
	public void setRelationships(List<OntologyRelationship> relationships) {
		this.relationships = relationships;
	}

	/**
	 * Returns a map of the counts associated with this entity
	 * 
	 * @return the counts
	 */
	public Map<String, Integer> getCounts() {
		return counts;
	}

	/**
	 * Sets a map of the counts associated with this entity
	 * 
	 * @param counts the counts to set
	 */
	public void setCounts(Map<String, Integer> counts) {
		this.counts = counts;
	}

	/**
	 * Returns a map of additional attributes of the entity
	 * 
	 * @return the attributes
	 */
	public Map<String, String> getAttributes() {
		return attributes;
	}

	/**
	 * Sets a map of additional attributes of the entity
	 * 
	 * @param attributes the attributes to set
	 */
	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}

}
