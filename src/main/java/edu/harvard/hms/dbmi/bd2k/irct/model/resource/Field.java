/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.resource;

import java.io.Serializable;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.DataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyRelationship;
import edu.harvard.hms.dbmi.bd2k.irct.util.converter.DataTypeConverter;
import edu.harvard.hms.dbmi.bd2k.irct.util.converter.OntologyRelationshipConverter;

/**
 * A representation of a field that restricts what can be put in to it.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@Entity
public class Field implements Serializable {

	private static final long serialVersionUID = -2150406406694041615L;

	@Id
	@GeneratedValue
	private long id;

	private String name;
	private String path;
	private String description;

	@ElementCollection
	@Convert(converter = DataTypeConverter.class)
	private List<DataType> dataTypes;

	@ElementCollection
	private List<String> permittedValues;

	@Convert(converter = OntologyRelationshipConverter.class)
	private OntologyRelationship relationship;

	private boolean required;

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

		JsonObjectBuilder fieldJSON = Json.createObjectBuilder();

		fieldJSON.add("name", getName());
		fieldJSON.add("path", getPath());
		fieldJSON.add("description", getDescription());
		fieldJSON.add("required", isRequired());

		JsonArrayBuilder dataTypesArray = Json.createArrayBuilder();
		if (getDataTypes() != null) {
			for (DataType dt : getDataTypes()) {
				dataTypesArray.add(dt.toJson());
			}
		}
		fieldJSON.add("dataTypes", dataTypesArray.build());

		JsonArrayBuilder permittedValuesArray = Json.createArrayBuilder();
		if (getPermittedValues() != null) {
			for (String pv : getPermittedValues()) {
				permittedValuesArray.add(pv);
			}
		}
		fieldJSON.add("permittedValues", permittedValuesArray.build());

		if (this.relationship != null) {
			fieldJSON.add("relationship", this.relationship.getName());
		}

		return fieldJSON.build();
	}

	/**
	 * Returns the field id
	 * 
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * Sets the field id
	 * 
	 * @param id
	 *            the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Returns the name of the field
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the field
	 * 
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the path of the field
	 * 
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Sets the path of the field
	 * 
	 * @param path
	 *            the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * Returns a description of the field
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets a description of the field
	 * 
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Returns a list of data types that are supported by the field. An empty list means all data types.
	 * 
	 * @return the dataTypes
	 */
	public List<DataType> getDataTypes() {
		return dataTypes;
	}

	/**
	 * Sets a list of data types that are supported by the field. An empty list means all data types.
	 * 
	 * @param dataTypes
	 *            the dataTypes to set
	 */
	public void setDataTypes(List<DataType> dataTypes) {
		this.dataTypes = dataTypes;
	}

	/**
	 * Returns a list of permitted values for the field. An empty list mean any value can be set.
	 * 
	 * @return the permittedValues
	 */
	public List<String> getPermittedValues() {
		return permittedValues;
	}

	/**
	 * Sets a list of permitted values for the field. An empty list mean any value can be set.
	 * 
	 * @param permittedValues
	 *            the permittedValues to set
	 */
	public void setPermittedValues(List<String> permittedValues) {
		this.permittedValues = permittedValues;
	}

	/**
	 * Returns a relationship that this field must have with the predicates entity. This is optional.
	 * 
	 * @return the relationship
	 */
	public OntologyRelationship getRelationships() {
		return relationship;
	}

	/**
	 * Sets a relationship that this field must have with the predicates entity. This is optional.
	 * 
	 * @param relationship
	 *            the relationship to set
	 */
	public void setRelationship(OntologyRelationship relationship) {
		this.relationship = relationship;
	}

	/**
	 * Returns if the field required by the operation
	 * 
	 * @return the required
	 */
	public boolean isRequired() {
		return required;
	}

	/**
	 * Sets if the field is required by the operation
	 * @param required
	 *            the required to set
	 */
	public void setRequired(boolean required) {
		this.required = required;
	}

}
