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
package edu.harvard.hms.dbmi.bd2k.irct.model.query;

import java.util.List;

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

import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.DataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.predicate.PredicateImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.util.converter.PredicateImplementationConverter;

/**
 * The predicate type class provides a way for the IRCT application to keep
 * track of which predicates can be used. 
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@Entity
public class PredicateType {
	@Id
	private long id;
	
	private String name;
	private String description;
	private boolean requiresValue;
	private boolean requiresAdditionalValue;

	@ElementCollection(targetClass = DataType.class)
	@CollectionTable(name = "PredicateType_SupportedDataType", joinColumns = @JoinColumn(name = "id"))
	@Column(name = "supportedDataType", nullable = false)
	@Enumerated(EnumType.STRING)
	private List<DataType> supportedDataTypes;
	
	@Convert(converter = PredicateImplementationConverter.class)
	private PredicateImplementationInterface implementingInterface;
	
	/**
	 * Returns if the predicate supports a given Data Type
	 * 
	 * @param dataType
	 *            The Data Type
	 * @return If the data type is supported
	 */
	public boolean supportsDataType(DataType dataType) {
		if (getSupportedDataTypes().isEmpty()
				|| getSupportedDataTypes().contains(dataType)) {
			return true;
		}
		return false;
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
		
		JsonObjectBuilder predicateTypeJSON = Json.createObjectBuilder();
		
		predicateTypeJSON.add("name", getName());
		predicateTypeJSON.add("description", getDescription());
		predicateTypeJSON.add("requiresValue", isRequiresValue());
		predicateTypeJSON.add("requiresAdditionalValue", isRequiresAdditionalValue());
		if(this.getImplementingInterface() != null) {
			predicateTypeJSON.add("implementation", this.getImplementingInterface().toJson(depth));
		}
		
		JsonArrayBuilder dataTypes = Json.createArrayBuilder();
		for(DataType dt : supportedDataTypes) {
			dataTypes.add(dt.toString());
		}
		predicateTypeJSON.add("dataTypes", dataTypes);
		
		return predicateTypeJSON.build();
	}

	/**
	 * Returns the name of the predicate
	 * 
	 * @return The predicate name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the predicate
	 * 
	 * @param name
	 *            The predicate name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the description of the predicate
	 * 
	 * @return The predicate description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description of the predicate
	 * 
	 * @param description
	 *            The predicate description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Returns if the predicate requires a value
	 * 
	 * @return If the predicate requires a value
	 */
	public boolean isRequiresValue() {
		return requiresValue;
	}

	/**
	 * Sets if the predicate requires a value
	 * 
	 * @param requiresValue
	 *            If the predicate requires a value
	 */
	public void setRequiresValue(boolean requiresValue) {
		this.requiresValue = requiresValue;
	}

	/**
	 * Returns if the predicate requires an additional value
	 * 
	 * @return If the predicate requires an additional value
	 */
	public boolean isRequiresAdditionalValue() {
		return requiresAdditionalValue;
	}

	/**
	 * Returns if the predicate requires an additional value
	 * 
	 * @param requiresAdditionalValue
	 *            If the predicate requires an additional value
	 * 
	 */
	public void setRequiresAdditionalValue(boolean requiresAdditionalValue) {
		this.requiresAdditionalValue = requiresAdditionalValue;
	}
	
	/**
	 * Returns a list of supported data types
	 * 
	 * @return Supported data types
	 */
	public List<DataType> getSupportedDataTypes() {
		return supportedDataTypes;
	}

	/**
	 * Sets the list of supported data types
	 * 
	 * @param supportedDataTypes Supported data types
	 */
	public void setSupportedDataTypes(List<DataType> supportedDataTypes) {
		this.supportedDataTypes = supportedDataTypes;
	}

	/**
	 * Returns a list of supported predicate types
	 * 
	 * @return Supported predicate types
	 */
	public PredicateImplementationInterface getImplementingInterface() {
		return implementingInterface;
	}

	/**
	 * Sets the list of supported predicates types 
	 * @param implementingInterface Supported predicate types
	 */
	public void setImplementingInterface(PredicateImplementationInterface implementingInterface) {
		this.implementingInterface = implementingInterface;
	}
}
