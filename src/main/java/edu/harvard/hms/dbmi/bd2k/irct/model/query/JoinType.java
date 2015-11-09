/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.query;

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
import javax.persistence.MapKeyColumn;

import edu.harvard.hms.dbmi.bd2k.irct.action.join.JoinAction;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.PrimitiveDataType;
import edu.harvard.hms.dbmi.bd2k.irct.util.JsonUtilities;
import edu.harvard.hms.dbmi.bd2k.irct.util.converter.JoinActionConverter;

/**
 * The join type class provides a way for the IRCT application to keep track of
 * which joins can be used.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@Entity
public class JoinType {

	@Id
	private long id;

	private String name;
	
	@ElementCollection
    @MapKeyColumn(name="name")
    @Column(name="value")
    @CollectionTable(name="jointype_parameters", joinColumns=@JoinColumn(name="id"))
	private Map<String, String> parameters;
	private String description;
	private boolean requireFields;
	private boolean requireRelationships;

	@ElementCollection(targetClass = PrimitiveDataType.class)
	@CollectionTable(name = "JoinType_SupportedDataType", joinColumns = @JoinColumn(name = "id"))
	@Column(name = "supportedDataType", nullable = false)
	@Enumerated(EnumType.STRING)
	private List<PrimitiveDataType> supportedDataTypes;

	@Convert(converter = JoinActionConverter.class)
	private JoinAction joinImplementation;
	
	/**
	 * Returns if the join type supports a given data type
	 * 
	 * @param dataType
	 *            The data type
	 * @return If the data type is supported
	 */
	public boolean supportsDataType(PrimitiveDataType dataType) {
		if (supportedDataTypes.isEmpty()
				|| supportedDataTypes.contains(dataType)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Sets up the JoinType with the parameters that have been passed to it
	 */
	public void setup() {
		joinImplementation.setup(parameters);
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
		jsonBuilder.add("description", this.description);
		jsonBuilder.add("parameters", JsonUtilities.mapToJson(this.parameters));
		jsonBuilder.add("requireFields", this.requireFields);
		jsonBuilder.add("reqireRelationships", this.requireRelationships);

		JsonArrayBuilder supportedDataTypesArray = Json.createArrayBuilder();
		for(PrimitiveDataType dataType : this.supportedDataTypes) {
			supportedDataTypesArray.add(dataType.toString());
		}
		jsonBuilder.add("supportedDataTypes", supportedDataTypesArray.build());
		
		//joinImplementation
		if(depth == 0) {
			jsonBuilder.add("implementation", this.joinImplementation.getType());
		} else {
			jsonBuilder.add("implementation", this.joinImplementation.toJson(depth));
		}
		
		return jsonBuilder.build();
	}

	// -------------------------------------------------------------------------
	// SETTERS AND GETTERS
	// -------------------------------------------------------------------------

	/**
	 * Returns the id of the join type
	 * 
	 * @return Join type id
	 */
	public long getId() {
		return id;
	}

	/**
	 * Sets the id of the join type
	 * 
	 * @param id
	 *            Join type id
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Returns the name of the join type
	 * 
	 * @return The join type
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the join type
	 * 
	 * @param name
	 *            The join type name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the parameters for the join type
	 * 
	 * @return Join type parameters
	 */
	public Map<String, String> getParameters() {
		return parameters;
	}

	/**
	 * Sets the parameters for the join type
	 * 
	 * @param parameters
	 *            Join type parameters
	 */
	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	/**
	 * Returns the description of the join type
	 * 
	 * @return The join type description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description of the join type
	 * 
	 * @param description
	 *            The join type description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Returns if the join type requires a value
	 * 
	 * @return If the join type requires a value
	 */
	public boolean isRequireFields() {
		return requireFields;
	}

	/**
	 * Sets if the join type requires a value
	 * 
	 * @param requireFields
	 *            If the join type requires a value
	 */
	public void setRequireFields(boolean requireFields) {
		this.requireFields = requireFields;
	}

	/**
	 * Returns if the join type requires a relationship
	 * 
	 * @return If the join type requires a relationship
	 */
	public boolean isRequireRelationships() {
		return requireRelationships;
	}

	/**
	 * Sets if the join type requires a relationship
	 * 
	 * @param requireRelationships
	 *            If the join type requires a relationship
	 */
	public void setRequireRelationships(boolean requireRelationships) {
		this.requireRelationships = requireRelationships;
	}

	/**
	 * Returns a list of supported data types
	 * 
	 * @return Supported data types
	 */
	public List<PrimitiveDataType> getSupportedDataTypes() {
		return supportedDataTypes;
	}

	/**
	 * Sets the list of supported data types
	 * 
	 * @param supportedDataTypes
	 *            Supported data types
	 */
	public void setSupportedDataTypes(List<PrimitiveDataType> supportedDataTypes) {
		this.supportedDataTypes = supportedDataTypes;
	}

	/**
	 * Returns the implementing class of the join type
	 * 
	 * @return The implementing class
	 */
	public JoinAction getJoinImplementation() {
		return joinImplementation;
	}

	/**
	 * Sets the implementing class of the join type
	 * 
	 * @param joinImplementation
	 *            The implementing class
	 */
	public void setJoinImplementation(JoinAction joinImplementation) {
		this.joinImplementation = joinImplementation;
	}

}
