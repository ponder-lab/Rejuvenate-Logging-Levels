package edu.harvard.hms.dbmi.bd2k.irct.model.query;

import java.io.Serializable;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;

import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.DataType;

/**
 * A predicate type value that is associated with a predicate
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@Entity
public class PredicateTypeValue implements Serializable {

	private static final long serialVersionUID = -2150406406694041615L;

	@Id
	private long id;

	private String name;

	private boolean required;

	@ElementCollection(targetClass = PredicateTypeValueDataType.class)
	@CollectionTable(name = "PTV_SDT", joinColumns = @JoinColumn(name = "id"))
	@Column(name = "supportedDataType", nullable = false)
	@Enumerated(EnumType.STRING)
	private List<PredicateTypeValueDataType> supportedDataTypes;

	@ElementCollection
	private List<String> permittedValues;

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

		JsonObjectBuilder predicateTypeValueJSON = Json.createObjectBuilder();

		predicateTypeValueJSON.add("name", getName());

		JsonArrayBuilder dataTypes = Json.createArrayBuilder();
		for (PredicateTypeValueDataType dt : supportedDataTypes) {
			dataTypes.add(dt.toString());
		}

		predicateTypeValueJSON.add("dataTypes", dataTypes);

		JsonArrayBuilder permittedValues = Json.createArrayBuilder();
		for (String permittedValue : this.permittedValues) {
			permittedValues.add(permittedValue);
		}
		predicateTypeValueJSON.add("permittedValues", permittedValues);

		return predicateTypeValueJSON.build();
	}

	/**
	 * Returns the id
	 * 
	 * @return Id
	 */
	public long getId() {
		return id;
	}

	/**
	 * Sets the id
	 * 
	 * @param id
	 *            Id
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Returns the name of the predicate type value
	 * 
	 * @return Name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the predicate type
	 * 
	 * @param name
	 *            Name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns if this value is required
	 * 
	 * @return Required
	 */
	public boolean isRequired() {
		return required;
	}

	/**
	 * Sets if this value is required
	 * 
	 * @param required
	 *            Required
	 */
	public void setRequired(boolean required) {
		this.required = required;
	}

	/**
	 * Returns a list of supported predicate types
	 * 
	 * @return Supported predicate types
	 */
	public List<PredicateTypeValueDataType> getSupportedDataTypes() {
		return supportedDataTypes;
	}

	/**
	 * Sets the supported predicate types
	 * 
	 * @param supportedDataTypes Supported predicate types
	 */
	public void setSupportedDataTypes(
			List<PredicateTypeValueDataType> supportedDataTypes) {
		this.supportedDataTypes = supportedDataTypes;
	}

	/**
	 * Returns a list of permitted values if they are limited
	 * 
	 * @return Permitted values
	 */
	public List<String> getPermittedValues() {
		return permittedValues;
	}

	/**
	 * Sets a list of permitted values if they are limited
	 * 
	 * @param permittedValues Permitted values
	 */
	public void setPermittedValues(List<String> permittedValues) {
		this.permittedValues = permittedValues;
	}

}
