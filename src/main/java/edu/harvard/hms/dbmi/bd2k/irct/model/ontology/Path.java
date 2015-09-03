/*
 *  This file is part of Inter-Resource Communication Tool (IRCT).
 *
 *  IRCT is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  IRCT is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with IRCT.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.harvard.hms.dbmi.bd2k.irct.model.ontology;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * The Path class represents a path in a resource. Paths can be linked to other
 * paths through ontology relationships.
 * 
 * @author Jeremy R. Easton-Marks
 */
public class Path {
	private String pui;

	private String name;
	private String definition;

	private String concept;
	private String conceptId;

	private DataType dataType;

	private Map<OntologyRelationship, List<Path>> relationships;

	private Map<String, String> attributes;

	private Map<String, Integer> counts;

	public Path() {
		relationships = new HashMap<OntologyRelationship, List<Path>>();
		setAttributes(new HashMap<String, String>());
		setCounts(new HashMap<String, Integer>());
		this.definition = "";
		this.concept = "";
		this.conceptId = "";
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
		jsonBuilder.add("pui", this.pui);
		jsonBuilder.add("name", this.name);
		jsonBuilder.add("definition", this.definition);
		jsonBuilder.add("concept", this.concept);
		jsonBuilder.add("conceptId", this.conceptId);
		if (this.dataType != null) {
			jsonBuilder.add("dataType", this.dataType.toString());
		}
		JsonObjectBuilder relationshipsObject = Json.createObjectBuilder();

		if (depth > 1) {
			for (OntologyRelationship relationship : relationships.keySet()) {
				JsonArrayBuilder relationshipArray = Json.createArrayBuilder();

				for (Path relPath : relationships.get(relationship)) {
					relationshipArray.add(relPath.toJson(depth));
				}

				relationshipsObject.add(relationship.toString(),
						relationshipArray.build());
			}

		}
		jsonBuilder.add("relationships", relationshipsObject.build());

		JsonObjectBuilder countsObject = Json.createObjectBuilder();
		for (String key : counts.keySet()) {
			Integer value = counts.get(key);
			if (value != null) {
				countsObject.add(key, value);
			}
		}

		jsonBuilder.add("counts", countsObject.build());

		JsonObjectBuilder attributesObject = Json.createObjectBuilder();
		for (String key : attributes.keySet()) {
			String value = attributes.get(key);
			if (value != null) {
				attributesObject.add(key, value);
			}
		}

		jsonBuilder.add("attributes", attributesObject.build());

		return jsonBuilder.build();

	}

	// -------------------------------------------------------------------------
	// SETTERS AND GETTERS
	// -------------------------------------------------------------------------

	/**
	 * Returns the Path Unique Identifier (PUI)
	 * 
	 * @return PUI
	 */
	public String getPui() {
		return pui;
	}

	/**
	 * Sets the Path Unique Identifier (PUI)
	 * 
	 * @param pui
	 *            PUI
	 */
	public void setPui(String pui) {
		this.pui = pui;
	}

	/**
	 * Returns the name of the path
	 * 
	 * @return Path Name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the path name
	 * 
	 * @param name
	 *            Path name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the path definition
	 * 
	 * @return Path definition
	 */
	public String getDefinition() {
		return definition;
	}

	/**
	 * Sets the path definition
	 * 
	 * @param definition
	 *            Path definition
	 */
	public void setDefinition(String definition) {
		this.definition = definition;
	}

	/**
	 * Returns the path concept
	 * 
	 * @return Concept
	 */
	public String getConcept() {
		return concept;
	}

	/**
	 * Sets the path concept
	 * 
	 * @param concept Concept
	 */
	public void setConcept(String concept) {
		this.concept = concept;
	}

	/**
	 * Returns the path concept id
	 * 
	 * @return Concept Id
	 */
	public String getConceptId() {
		return conceptId;
	}

	/**
	 * Sets the paths concept id
	 * 
	 * @param conceptId
	 *            Concept id
	 */
	public void setConceptId(String conceptId) {
		this.conceptId = conceptId;
	}

	/**
	 * Returns the data type of the path
	 * 
	 * @return Data Type
	 */
	public DataType getDataType() {
		return dataType;
	}

	/**
	 * Sets the data type of the path
	 * 
	 * @param dataType
	 *            Data Type
	 */
	public void setDataType(DataType dataType) {
		this.dataType = dataType;
	}

	/**
	 * Returns a map of the relationships where the key is relationship type,
	 * and the value is a list of paths
	 * 
	 * 
	 * @return Relationships
	 */
	public Map<OntologyRelationship, List<Path>> getRelationships() {
		return relationships;
	}

	/**
	 * Sets the relationship map where the key is the relationship type, and the
	 * value is a list of paths.
	 * 
	 * @param relationships
	 *            Relationships
	 */
	public void setRelationships(
			Map<OntologyRelationship, List<Path>> relationships) {
		this.relationships = relationships;
	}

	/**
	 * Returns a map of attributes
	 * 
	 * @return Attributes map
	 */
	public Map<String, String> getAttributes() {
		return attributes;
	}

	/**
	 * Sets a map of the attributes
	 * 
	 * @param attributes
	 *            Attributes map
	 */
	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}

	/**
	 * Returns a map of all the counts, where the key is the count name, and the
	 * value is the count as an integer value
	 * 
	 * @return Counts map
	 */
	public Map<String, Integer> getCounts() {
		return counts;
	}

	/**
	 * Sets a map of all the counts of the path
	 * 
	 * @param counts Counts map
	 */
	public void setCounts(Map<String, Integer> counts) {
		this.counts = counts;
	}

}
