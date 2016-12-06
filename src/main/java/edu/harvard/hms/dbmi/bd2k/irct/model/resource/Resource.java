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
import javax.persistence.Transient;

import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.DataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyRelationship;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyType;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.JoinType;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.PredicateType;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.SelectOperationType;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.SortOperationType;
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
	private List<SelectOperationType> supportedSelectOperations;
	
	@OneToMany
	private List<SortOperationType> supportedSortOperations;

	@OneToMany
	private List<JoinType> supportedJoins;

	@OneToMany
	private List<ProcessType> supportedProcesses;

	@OneToMany
	private List<VisualizationType> supportedVisualizations;

	@ElementCollection
	@MapKeyColumn(name = "name")
	@Column(name = "value")
	@CollectionTable(name = "resource_parameters", joinColumns = @JoinColumn(name = "id"))
	private Map<String, String> parameters;

	@Transient
	private boolean setup = false;
	
	/**
	 * Sets up the Resource and the implementing interface
	 * @throws ResourceInterfaceException Throws a resource interface
	 */
	public void setup() throws ResourceInterfaceException {
		implementingInterface.setup(this.parameters);
		this.setSetup(true);
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
				
				// PREDICATES (Query Interface)
				JsonArrayBuilder selectArray = Json.createArrayBuilder();
				if (this.supportedPredicates != null) {
					for (SelectOperationType pt : this.supportedSelectOperations) {
						selectArray.add(pt.toJson());
					}
				}
				jsonBuilder.add("selectOperations", selectArray.build());

				// JOINS (Query Interface)
				JsonArrayBuilder joinArray = Json.createArrayBuilder();
				if (this.supportedJoins != null) {
					for (JoinType jt : this.supportedJoins) {
						joinArray.add(jt.toJson());
					}
				}
				jsonBuilder.add("joins", joinArray.build());
				
				// SORTS (Query Interface)
				JsonArrayBuilder sortArray = Json.createArrayBuilder();
				if (this.supportedSortOperations != null) {
					for (SortOperationType st : this.supportedSortOperations) {
						sortArray.add(st.toJson());
					}
				}
				jsonBuilder.add("sorts", sortArray.build());
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
	
	/**
	 * Returns a relationship from its name. It will return null if it does not exist.
	 * 
	 * @param relationshipString Relationship name
	 * @return Ontology Relationship
	 */
	public OntologyRelationship getRelationshipByName(String relationshipString) {
		for(OntologyRelationship relationship : this.relationships) {
			if(relationship.toString().equalsIgnoreCase(relationshipString)) {
				return relationship;
			}
		}
		return null;
	}
	
	/**
	 * Returns a predicate type from its name. It will return null if it does not exist.
	 * 
	 * @param predicateName Predicate name
	 * @return Predicate Type
	 */
	public PredicateType getSupportedPredicateByName(String predicateName) {
		for(PredicateType predicateType : this.supportedPredicates) {
			if(predicateType.getName().equals(predicateName)) {
				return predicateType;
			}
		}
		return null;
	}
	
	/**
	 * Returns a select operation from its name. It will return null if it does not exist
	 * 
	 * @param operationName Operation name
	 * @return Select Operation Type
	 */
	public SelectOperationType getSupportedSelectOperationByName(
			String operationName) {
		for(SelectOperationType operationType : this.supportedSelectOperations) {
			if(operationType.getName().equals(operationName)) {
				return operationType;
			}
		}
		return null;
	}
	
	/**
	 * Returns a sort operation from its name. It will return null if it does not exist.
	 * 
	 * @param operationName Sort name
	 * @return Sort Operation Type
	 */
	public SortOperationType getSupportedSortOperationByName(
			String operationName) {
		for(SortOperationType operationType : this.supportedSortOperations) {
			if(operationType.getName().equals(operationName)) {
				return operationType;
			}
		}
		return null;
	}
	
	/**
	 * Returns a process type from its name. It will return null if it does not exist.
	 * 
	 * @param processName Process Name
	 * @return Process Type
	 */
	public ProcessType getSupportedProcessesByName(String processName) {
		for(ProcessType processType : this.supportedProcesses) {
			if(processType.getName().equalsIgnoreCase(processName)) {
				return processType;
			}
		}
		return null;
	}
	
	/**
	 * Returns a Logical Operator from its name. It will return null if it does not exist.
	 * 
	 * @param logicalOperatorName Logical Operator Name
	 * @return Logical Operator
	 */
	public LogicalOperator getLogicalOperatorByName(String logicalOperatorName) {
		for(LogicalOperator logicalOperator : this.logicalOperators) {
			if(logicalOperator.toString().equals(logicalOperatorName)) {
				return logicalOperator;
			}
		}
		return null;
	}
	
	/**
	 * Returns a data type from its name. It will return null if it does not exist.
	 * 
	 * @param dataTypeName Data Type Name
	 * @return Data Type
	 */
	public DataType getDataTypeByName(String dataTypeName) {
		for(DataType dataType : this.dataTypes) {
			if(dataType.toString().equals(dataTypeName)) {
				return dataType;
			}
		}
		return null;
	}
	
	
	public JoinType getSupportedJoinByName(String joinTypeName) {
		for(JoinType joinType : this.supportedJoins) {
			if(joinType.getName().equals(joinTypeName)) {
				return joinType;
			}
		}
		return null;
	}

	// -------------------------------------------------------------------------
	// SETTERS AND GETTERS
	// -------------------------------------------------------------------------

	/**
	 * Returns the id of the resource
	 * 
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * Sets the id of the resource
	 * 
	 * @param id
	 *            the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Returns the name of the resource
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the resource
	 * 
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the ontology type of the resource
	 * 
	 * @return the ontologyType
	 */
	public OntologyType getOntologyType() {
		return ontologyType;
	}

	/**
	 * Sets the ontology type of the resource
	 * 
	 * @param ontologyType
	 *            the ontologyType to set
	 */
	public void setOntologyType(OntologyType ontologyType) {
		this.ontologyType = ontologyType;
	}

	/**
	 * Returns the implementing interface of the resource
	 * 
	 * @return the implementingInterface
	 */
	public ResourceImplementationInterface getImplementingInterface() {
		return implementingInterface;
	}

	/**
	 * Sets the implementing interface of the resource
	 * 
	 * @param implementingInterface
	 *            the implementingInterface to set
	 */
	public void setImplementingInterface(
			ResourceImplementationInterface implementingInterface) {
		this.implementingInterface = implementingInterface;
	}

	/**
	 * Returns a list of data types that are supported by the resource
	 *  
	 * @return the dataTypes
	 */
	public List<DataType> getDataTypes() {
		return dataTypes;
	}

	/**
	 * Sets the list of data type that are supported by the resource
	 * 
	 * @param dataTypes
	 *            the dataTypes to set
	 */
	public void setDataTypes(List<DataType> dataTypes) {
		this.dataTypes = dataTypes;
	}

	/**
	 * Returns a list of relationships that are supported by the resource
	 * 
	 * @return the relationships
	 */
	public List<OntologyRelationship> getRelationships() {
		return relationships;
	}

	/**
	 * Sets the list of relationships that are supported by the resource
	 * 
	 * @param relationships
	 *            the relationships to set
	 */
	public void setRelationships(List<OntologyRelationship> relationships) {
		this.relationships = relationships;
	}

	/**
	 * Returns a list of logical operators that are supported by the resource
	 * 
	 * @return the logicalOperators
	 */
	public List<LogicalOperator> getLogicalOperators() {
		return logicalOperators;
	}

	/**
	 * Sets a list of logical operators that are supported by the resource
	 * 
	 * @param logicalOperators
	 *            the logicalOperators to set
	 */
	public void setLogicalOperators(List<LogicalOperator> logicalOperators) {
		this.logicalOperators = logicalOperators;
	}

	/**
	 * Returns a list of supported predicates that are supported by the resource
	 * 
	 * @return the supportedPredicates
	 */
	public List<PredicateType> getSupportedPredicates() {
		return supportedPredicates;
	}

	/**
	 * Sets the list of supported predicates that are supported by the resource
	 * 
	 * @param supportedPredicates
	 *            the supportedPredicates to set
	 */
	public void setSupportedPredicates(List<PredicateType> supportedPredicates) {
		this.supportedPredicates = supportedPredicates;
	}

	/**
	 * Returns a list of select operation that are supported by the resource
	 *  
	 * @return the supportedSelectOperations
	 */
	public List<SelectOperationType> getSupportedSelectOperations() {
		return supportedSelectOperations;
	}

	/**
	 * Sets the list of select operations that are supported by the resources
	 * 
	 * @param supportedSelectOperations the supportedSelectOperations to set
	 */
	public void setSupportedSelectOperations(
			List<SelectOperationType> supportedSelectOperations) {
		this.supportedSelectOperations = supportedSelectOperations;
	}

	/**
	 * Returns a list of sort operation that are supported by the resource
	 * 
	 * @return the supportedSortOperations
	 */
	public List<SortOperationType> getSupportedSortOperations() {
		return supportedSortOperations;
	}

	/**
	 * Sets the list of sort operations that are supporterd by the resource
	 * 
	 * @param supportedSortOperations the supportedSortOperations to set
	 */
	public void setSupportedSortOperations(List<SortOperationType> supportedSortOperations) {
		this.supportedSortOperations = supportedSortOperations;
	}

	/**
	 * Returns a list joins that are supported by the resource
	 * 
	 * @return the supportedJoins
	 */
	public List<JoinType> getSupportedJoins() {
		return supportedJoins;
	}

	/**
	 * Sets the list joins that are supported by the resource
	 * 
	 * @param supportedJoins
	 *            the supportedJoins to set
	 */
	public void setSupportedJoins(List<JoinType> supportedJoins) {
		this.supportedJoins = supportedJoins;
	}

	/**
	 * Returns a list of process that are supported by the resource
	 * 
	 * @return the supportedProcesses
	 */
	public List<ProcessType> getSupportedProcesses() {
		return supportedProcesses;
	}

	/**
	 * Sets the list of processes that are supported by the resource
	 * @param supportedProcesses
	 *            the supportedProcesses to set
	 */
	public void setSupportedProcesses(List<ProcessType> supportedProcesses) {
		this.supportedProcesses = supportedProcesses;
	}

	/**
	 * Returns a list of visualizations that are supported by the resource
	 * 
	 * @return the supportedVisualizations
	 */
	public List<VisualizationType> getSupportedVisualizations() {
		return supportedVisualizations;
	}

	/**
	 * Sets a list of visualizations that are supported by the resource
	 * 
	 * @param supportedVisualizations
	 *            the supportedVisualizations to set
	 */
	public void setSupportedVisualizations(
			List<VisualizationType> supportedVisualizations) {
		this.supportedVisualizations = supportedVisualizations;
	}

	/**
	 * Returns a map of parameters for the resource
	 * 
	 * @return the parameters
	 */
	public Map<String, String> getParameters() {
		return parameters;
	}

	/**
	 * Sets a map of resources for the resource
	 * 
	 * @param parameters
	 *            the parameters to set
	 */
	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	/**
	 * @return the setup
	 */
	public boolean isSetup() {
		return setup;
	}

	/**
	 * @param setup the setup to set
	 */
	public void setSetup(boolean setup) {
		this.setup = setup;
	}

	
}
