/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.MapKeyColumn;

import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyType;
import edu.harvard.hms.dbmi.bd2k.irct.model.process.ProcessType;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.LogicalOperator;
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
	@GeneratedValue
	private long id;

	private String name;

	@ElementCollection
	@MapKeyColumn(name = "name")
	@Column(name = "value")
	@CollectionTable(name = "resource_parameters", joinColumns = @JoinColumn(name = "id"))
	private Map<String, String> parameters;

	@Enumerated(EnumType.STRING)
	private OntologyType ontologyType;
	
	@ElementCollection(targetClass = LogicalOperator.class)
	@CollectionTable(name = "Resource_Join", joinColumns = @JoinColumn(name = "id"))
	@Column(name = "supportedJoins", nullable = false)
	@Enumerated(EnumType.STRING)
	private List<LogicalOperator> supportedJoins;
	
	@ManyToMany
	private List<PredicateType> supportedPredicates;
	
	@ManyToMany
	private List<ProcessType> availableProcesses;

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
		jsonBuilder.add("ontologyType", this.ontologyType.toString());

		JsonArrayBuilder joinArray = Json.createArrayBuilder();
		JsonArrayBuilder predicateArray = Json.createArrayBuilder();
		JsonArrayBuilder processArray = Json.createArrayBuilder();
		
		for (LogicalOperator join : this.supportedJoins) {
			joinArray.add(join.toString());
		}
		
		if (depth == 0) {
			for (PredicateType predicate : this.supportedPredicates) {
				predicateArray.add(predicate.getName());
			}
			
			for(ProcessType process : this.availableProcesses) {
				processArray.add(process.getName());
			}
			
			if(this.implementingInterface != null) {
				jsonBuilder.add("implementation",
					this.implementingInterface.getType());
			}

		} else {
			for (PredicateType predicate : this.supportedPredicates) {
				predicateArray.add(predicate.toJson(depth));
			}
			
			for(ProcessType process : this.availableProcesses) {
				processArray.add(process.toJson(depth));
			}
			
			if(this.implementingInterface != null) {
				jsonBuilder.add("implementation",
					this.implementingInterface.toJson(depth));
			}
		}

		jsonBuilder.add("supportedJoins", joinArray.build());
		jsonBuilder.add("supportedPredicates", predicateArray.build());
		jsonBuilder.add("availableProcesses", processArray.build());

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
	public List<LogicalOperator> getSupportedJoins() {
		return supportedJoins;
	}

	/**
	 * Sets the list of supported joins for the resource
	 * 
	 * @param supportedJoins
	 *            Supported joins
	 */
	public void setSupportedJoins(List<LogicalOperator> supportedJoins) {
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

	/**
	 * @return the availableProcesses
	 */
	public List<ProcessType> getAvailableProcesses() {
		return availableProcesses;
	}

	/**
	 * @param availableProcesses the availableProcesses to set
	 */
	public void setAvailableProcesses(List<ProcessType> availableProcesses) {
		this.availableProcesses = availableProcesses;
	}

}
