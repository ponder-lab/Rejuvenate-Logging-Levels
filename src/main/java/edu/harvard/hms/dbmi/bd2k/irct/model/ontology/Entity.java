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
 * The Entity class represents a path in a resource. Paths can be linked to other
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

	

	public Entity() {
		this("");
	}

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
		jsonBuilder.add("count", countObject.build());
		
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
	 * @return the pui
	 */
	public String getPui() {
		return pui;
	}

	/**
	 * @param pui the pui to set
	 */
	public void setPui(String pui) {
		this.pui = pui;
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
	 * @return the ontology
	 */
	public String getOntology() {
		return ontology;
	}

	/**
	 * @param ontology the ontology to set
	 */
	public void setOntology(String ontology) {
		this.ontology = ontology;
	}

	/**
	 * @return the ontologyId
	 */
	public String getOntologyId() {
		return ontologyId;
	}

	/**
	 * @param ontologyId the ontologyId to set
	 */
	public void setOntologyId(String ontologyId) {
		this.ontologyId = ontologyId;
	}

	/**
	 * @return the dataType
	 */
	public DataType getDataType() {
		return dataType;
	}

	/**
	 * @param dataType the dataType to set
	 */
	public void setDataType(DataType dataType) {
		this.dataType = dataType;
	}

	/**
	 * @return the relationships
	 */
	public List<OntologyRelationship> getRelationships() {
		return relationships;
	}

	/**
	 * @param relationships the relationships to set
	 */
	public void setRelationships(List<OntologyRelationship> relationships) {
		this.relationships = relationships;
	}

	/**
	 * @return the counts
	 */
	public Map<String, Integer> getCounts() {
		return counts;
	}

	/**
	 * @param counts the counts to set
	 */
	public void setCounts(Map<String, Integer> counts) {
		this.counts = counts;
	}

	/**
	 * @return the attributes
	 */
	public Map<String, String> getAttributes() {
		return attributes;
	}

	/**
	 * @param attributes the attributes to set
	 */
	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}

}
