/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.resource;

import java.io.Serializable;
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
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;

import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.model.join.IRCTJoin;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.DataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyRelationship;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyType;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.PredicateType;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.PathResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.ResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.visualization.VisualizationType;
import edu.harvard.hms.dbmi.bd2k.irct.util.converter.DataTypeConverter;
import edu.harvard.hms.dbmi.bd2k.irct.util.converter.OntologyRelationshipConverter;
import edu.harvard.hms.dbmi.bd2k.irct.util.converter.ResourceImplementationConverter;

/**
 * The resource class provides a way for the IRCT application to keep track of
 * which resources are available.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@Entity
public class Resource implements Serializable {
	private static final long serialVersionUID = 8099637983212553759L;
	
	@Id
	@GeneratedValue
	private long id;
	private String name;

	@Enumerated(EnumType.STRING)
	private OntologyType ontologyType;

	@Convert(converter = ResourceImplementationConverter.class)
	private ResourceImplementationInterface implementingInterface;

	@ElementCollection
	@Convert(converter = DataTypeConverter.class)
	private List<DataType> dataTypes;

	@ElementCollection
	@Convert(converter = OntologyRelationshipConverter.class)
	private List<OntologyRelationship> relationships;

	@ElementCollection(targetClass = LogicalOperator.class)
	@CollectionTable(name = "Resource_LogicalOperator", joinColumns = @JoinColumn(name = "id"))
	@Column(name = "logicalOperator", nullable = false)
	@Enumerated(EnumType.STRING)
	private List<LogicalOperator> logicalOperators;

	@OneToMany
	private List<PredicateType> supportedPredicates;

	@OneToMany
	private List<IRCTJoin> supportedJoins;

	@OneToMany
	private List<ProcessType> supportedProcesses;

	@OneToMany
	private List<VisualizationType> supportedVisualizations;

	// KEEP
	@ElementCollection
	@MapKeyColumn(name = "name")
	@Column(name = "value")
	@CollectionTable(name = "resource_parameters", joinColumns = @JoinColumn(name = "id"))
	private Map<String, String> parameters;

	/**
	 * Sets up the Resource and the implementing interface
	 * @throws ResourceInterfaceException 
	 */
	public void setup() throws ResourceInterfaceException {
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

		if (this.implementingInterface != null) {
			jsonBuilder.add("implementation", implementingInterface.getType());

			if (this.implementingInterface instanceof PathResourceImplementationInterface) {
				// RELATIONSHIPS (PATH Interface)
				JsonArrayBuilder relationshipArray = Json.createArrayBuilder();
				if (this.getRelationships() != null) {
					for (OntologyRelationship rel : this.relationships) {
						relationshipArray.add(rel.toString());
					}
				}
				jsonBuilder.add("relationships", relationshipArray.build());

			}
			if (this.implementingInterface instanceof PathResourceImplementationInterface) {
				// LOGICALOPERATORS (Query Interface)
				JsonArrayBuilder logicalArray = Json.createArrayBuilder();
				if (this.logicalOperators != null) {
					for (LogicalOperator lo : this.logicalOperators) {
						logicalArray.add(lo.toString());
					}
				}
				jsonBuilder.add("logicaloperators", logicalArray.build());

				// PREDICATES (Query Interface)
				JsonArrayBuilder predicateArray = Json.createArrayBuilder();
				if (this.supportedPredicates != null) {
					for (PredicateType pt : this.supportedPredicates) {
						predicateArray.add(pt.toJson());
					}
				}
				jsonBuilder.add("predicates", predicateArray.build());

				// JOINS (Query Interface)
				JsonArrayBuilder joinArray = Json.createArrayBuilder();
				if (this.supportedJoins != null) {
					for (IRCTJoin jt : this.supportedJoins) {
						joinArray.add(jt.toJson());
					}
				}
				jsonBuilder.add("joins", joinArray.build());
			}
			if (this.implementingInterface instanceof PathResourceImplementationInterface) {
				// PROCESSES (Process Interface)
				JsonArrayBuilder processArray = Json.createArrayBuilder();
				if (this.supportedProcesses != null) {
					for (ProcessType pt : this.supportedProcesses) {
						processArray.add(pt.toJson());
					}
				}
				jsonBuilder.add("processes", processArray.build());
			}
			if (this.implementingInterface instanceof PathResourceImplementationInterface) {
				// VISUALIZATIONS (Visualization Interface)
				JsonArrayBuilder visualizationArray = Json.createArrayBuilder();
				if (this.supportedVisualizations != null) {
					for (VisualizationType vt : this.supportedVisualizations) {
						visualizationArray.add(vt.toJson());
					}
				}
				jsonBuilder.add("visualization", visualizationArray.build());

			}
		}

		// DATATYPES
		JsonArrayBuilder dataTypeArray = Json.createArrayBuilder();
		if (this.getDataTypes() != null) {
			for (DataType dataType : this.dataTypes) {
				dataTypeArray.add(dataType.toJson());
			}
		}
		jsonBuilder.add("dataTypes", dataTypeArray.build());

		return jsonBuilder.build();
	}
	
	public OntologyRelationship getRelationshipByName(String relationshipString) {
		for(OntologyRelationship relationship : this.relationships) {
			if(relationship.toString().equalsIgnoreCase(relationshipString)) {
				return relationship;
			}
		}
		return null;
	}
	
	public PredicateType getSupportedPredicateByName(String predicateName) {
		for(PredicateType predicateType : this.supportedPredicates) {
			if(predicateType.getName().equals(predicateName)) {
				return predicateType;
			}
		}
		return null;
	}
	
	public LogicalOperator getLogicalOperatorByName(String logicalOperatorName) {
		for(LogicalOperator logicalOperator : this.logicalOperators) {
			if(logicalOperator.toString().equals(logicalOperatorName)) {
				return logicalOperator;
			}
		}
		return null;
	}
	
	public DataType getDataTypeByName(String dataTypeName) {
		for(DataType dataType : this.dataTypes) {
			if(dataType.toString().equals(dataTypeName)) {
				return dataType;
			}
		}
		return null;
	}

	// -------------------------------------------------------------------------
	// SETTERS AND GETTERS
	// -------------------------------------------------------------------------

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
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
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the ontologyType
	 */
	public OntologyType getOntologyType() {
		return ontologyType;
	}

	/**
	 * @param ontologyType
	 *            the ontologyType to set
	 */
	public void setOntologyType(OntologyType ontologyType) {
		this.ontologyType = ontologyType;
	}

	/**
	 * @return the implementingInterface
	 */
	public ResourceImplementationInterface getImplementingInterface() {
		return implementingInterface;
	}

	/**
	 * @param implementingInterface
	 *            the implementingInterface to set
	 */
	public void setImplementingInterface(
			ResourceImplementationInterface implementingInterface) {
		this.implementingInterface = implementingInterface;
	}

	/**
	 * @return the dataTypes
	 */
	public List<DataType> getDataTypes() {
		return dataTypes;
	}

	/**
	 * @param dataTypes
	 *            the dataTypes to set
	 */
	public void setDataTypes(List<DataType> dataTypes) {
		this.dataTypes = dataTypes;
	}

	/**
	 * @return the relationships
	 */
	public List<OntologyRelationship> getRelationships() {
		return relationships;
	}

	/**
	 * @param relationships
	 *            the relationships to set
	 */
	public void setRelationships(List<OntologyRelationship> relationships) {
		this.relationships = relationships;
	}

	/**
	 * @return the logicalOperators
	 */
	public List<LogicalOperator> getLogicalOperators() {
		return logicalOperators;
	}

	/**
	 * @param logicalOperators
	 *            the logicalOperators to set
	 */
	public void setLogicalOperators(List<LogicalOperator> logicalOperators) {
		this.logicalOperators = logicalOperators;
	}

	/**
	 * @return the supportedPredicates
	 */
	public List<PredicateType> getSupportedPredicates() {
		return supportedPredicates;
	}

	/**
	 * @param supportedPredicates
	 *            the supportedPredicates to set
	 */
	public void setSupportedPredicates(List<PredicateType> supportedPredicates) {
		this.supportedPredicates = supportedPredicates;
	}

	/**
	 * @return the supportedJoins
	 */
	public List<IRCTJoin> getSupportedJoins() {
		return supportedJoins;
	}

	/**
	 * @param supportedJoins
	 *            the supportedJoins to set
	 */
	public void setSupportedJoins(List<IRCTJoin> supportedJoins) {
		this.supportedJoins = supportedJoins;
	}

	/**
	 * @return the supportedProcesses
	 */
	public List<ProcessType> getSupportedProcesses() {
		return supportedProcesses;
	}

	/**
	 * @param supportedProcesses
	 *            the supportedProcesses to set
	 */
	public void setSupportedProcesses(List<ProcessType> supportedProcesses) {
		this.supportedProcesses = supportedProcesses;
	}

	/**
	 * @return the supportedVisualizations
	 */
	public List<VisualizationType> getSupportedVisualizations() {
		return supportedVisualizations;
	}

	/**
	 * @param supportedVisualizations
	 *            the supportedVisualizations to set
	 */
	public void setSupportedVisualizations(
			List<VisualizationType> supportedVisualizations) {
		this.supportedVisualizations = supportedVisualizations;
	}

	/**
	 * @return the parameters
	 */
	public Map<String, String> getParameters() {
		return parameters;
	}

	/**
	 * @param parameters
	 *            the parameters to set
	 */
	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}
}
