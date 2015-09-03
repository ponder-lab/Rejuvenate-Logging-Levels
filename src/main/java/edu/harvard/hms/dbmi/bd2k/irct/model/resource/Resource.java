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
package edu.harvard.hms.dbmi.bd2k.irct.model.resource;

import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.MapKeyColumn;

import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyType;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.JoinType;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.PredicateType;
import edu.harvard.hms.dbmi.bd2k.irct.util.converter.ResourceImplementationConverter;

/**
 * The resource class provides a way for the IRCT application to keep track of
 * which resources are available.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@Entity
public class Resource {
	@Id
	private long id;

	private String name;

	@ElementCollection
	@MapKeyColumn(name = "name")
	@Column(name = "value")
	@CollectionTable(name = "resource_parameters", joinColumns = @JoinColumn(name = "id"))
	private Map<String, String> parameters;

	@Enumerated(EnumType.STRING)
	private OntologyType ontologyType;
	
	@ManyToMany
	private List<JoinType> supportedJoins;
	
	@ManyToMany
	private List<PredicateType> supportedPredicates;

	@Convert(converter = ResourceImplementationConverter.class)
	private ResourceImplementationInterface implementingInterface;
	
	/**
	 * Sets up the Resource and the implementing interface
	 */
	public void setup() {
		implementingInterface.setup(this.parameters);
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
		jsonBuilder.add("id", this.id);
		jsonBuilder.add("name", this.name);
//		jsonBuilder.add("parameters", JsonUtilities.mapToJson(this.parameters));
		jsonBuilder.add("ontologyType", this.ontologyType.toString());

		JsonArrayBuilder joinArray = Json.createArrayBuilder();
		JsonArrayBuilder predicateArray = Json.createArrayBuilder();

		if (depth == 0) {

			for (JoinType join : this.supportedJoins) {
				joinArray.add(join.getName());
			}

			for (PredicateType predicate : this.supportedPredicates) {
				predicateArray.add(predicate.getName());
			}
			if(this.implementingInterface != null) {
				jsonBuilder.add("implementation",
					this.implementingInterface.getType());
			}

		} else {
			for (JoinType join : this.supportedJoins) {
				joinArray.add(join.toJson(depth));
			}
			for (PredicateType predicate : this.supportedPredicates) {
				predicateArray.add(predicate.toJson(depth));
			}
			if(this.implementingInterface != null) {
				jsonBuilder.add("implementation",
					this.implementingInterface.toJson(depth));
			}
		}

		jsonBuilder.add("supportedJoins", joinArray.build());
		jsonBuilder.add("supportedPredicates", predicateArray.build());

		return jsonBuilder.build();
	}

	// -------------------------------------------------------------------------
	// SETTERS AND GETTERS
	// -------------------------------------------------------------------------
	/**
	 * Returns the id of the resource
	 * 
	 * @return Id of the resource
	 */
	public long getId() {
		return id;
	}

	/**
	 * Sets the id of the resource
	 * 
	 * @param id
	 *            Resource id
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Returns the name of the resource
	 * 
	 * @return Resource name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the resource
	 * 
	 * @param name
	 *            The resource name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the parameters of the resource
	 * 
	 * @return Resource parameters
	 */
	public Map<String, String> getParameters() {
		return parameters;
	}

	/**
	 * Sets the parameters of the resource
	 * 
	 * @param parameters
	 *            Resource parameters
	 */
	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	/**
	 * Returns the ontology type
	 * 
	 * @return Ontology type
	 */
	public OntologyType getOntologyType() {
		return ontologyType;
	}

	/**
	 * Sets the ontology type
	 * 
	 * @param ontologyType
	 *            Ontology type
	 */
	public void setOntologyType(OntologyType ontologyType) {
		this.ontologyType = ontologyType;
	}

	/**
	 * Returns a list of supported joins by the resource
	 * 
	 * @return Supported joins
	 */
	public List<JoinType> getSupportedJoins() {
		return supportedJoins;
	}

	/**
	 * Sets the list of supported joins for the resource
	 * 
	 * @param supportedJoins
	 *            Supported joins
	 */
	public void setSupportedJoins(List<JoinType> supportedJoins) {
		this.supportedJoins = supportedJoins;
	}

	/**
	 * Returns a list of predicates supported by the resource
	 * 
	 * @return Supported predicates
	 */
	public List<PredicateType> getSupportedPredicates() {
		return supportedPredicates;
	}

	/**
	 * Sets the list of supported predicates for the resource
	 * 
	 * @param supportedPredicates
	 *            Supported predicates
	 */
	public void setSupportedPredicates(List<PredicateType> supportedPredicates) {
		this.supportedPredicates = supportedPredicates;
	}

	/**
	 * Returns the implementing interface for this resource
	 * 
	 * @return The implementing interface
	 */
	public ResourceImplementationInterface getImplementingInterface() {
		return implementingInterface;
	}

	/**
	 * Sets the implementing interface for this resource
	 * 
	 * @param implementingInterface
	 *            The implementing resource
	 */
	public void setImplementingInterface(
			ResourceImplementationInterface implementingInterface) {
		this.implementingInterface = implementingInterface;
	}

}
