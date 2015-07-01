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
 * The Path class represents a path in a resource. Paths can be linked to other paths through ontology relationships.
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
	
	public Path() {
		relationships = new HashMap<OntologyRelationship, List<Path>>();
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
		if(this.dataType != null) {
			jsonBuilder.add("dataType", this.dataType.toString());
		}
		JsonObjectBuilder relationshipsObject = Json.createObjectBuilder();
		if(depth > 1) {
		for(OntologyRelationship relationship : relationships.keySet()) {
			JsonArrayBuilder relationshipArray = Json.createArrayBuilder();
			
			for(Path relPath : relationships.get(relationship)) {
				relationshipArray.add(relPath.toJson(depth));
			}
			
			relationshipsObject.add(relationship.toString(), relationshipArray.build());
		}
		}
		jsonBuilder.add("relationships", relationshipsObject.build());
		
		return jsonBuilder.build();
		
	}
	
	// -------------------------------------------------------------------------
	// SETTERS AND GETTERS
	// -------------------------------------------------------------------------

	
	public String getPui() {
		return pui;
	}

	public void setPui(String pui) {
		this.pui = pui;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDefinition() {
		return definition;
	}

	public void setDefinition(String definition) {
		this.definition = definition;
	}

	public String getConcept() {
		return concept;
	}

	public void setConcept(String concept) {
		this.concept = concept;
	}

	public String getConceptId() {
		return conceptId;
	}

	public void setConceptId(String conceptId) {
		this.conceptId = conceptId;
	}

	public DataType getDataType() {
		return dataType;
	}

	public void setDataType(DataType dataType) {
		this.dataType = dataType;
	}

	public Map<OntologyRelationship, List<Path>> getRelationships() {
		return relationships;
	}

	public void setRelationships(Map<OntologyRelationship, List<Path>> relationships) {
		this.relationships = relationships;
	}
	
	
	
}
