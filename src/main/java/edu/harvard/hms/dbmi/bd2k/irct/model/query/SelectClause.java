/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.query;

import java.io.Serializable;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Entity;

/**
 * The select clause provides a list of parameters and/or operations that should
 * be returned by the query.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@javax.persistence.Entity
public class SelectClause extends ClauseAbstract implements Serializable {
	private static final long serialVersionUID = 3728919497144122930L;
	@OneToOne(cascade = CascadeType.ALL)
	private Entity parameter;

	private String alias;

	@ManyToOne
	private SelectOperationType operationType;
	
	@ElementCollection(fetch = FetchType.EAGER)
	@MapKeyColumn(name="name")
	@Column(name="value")
	@CollectionTable(name="select_values", joinColumns=@JoinColumn(name="select_id"))
	private Map<String, String> stringValues;

	@Transient
	private Map<String, Object> objectValues;
	
	/**
	 * Creates an empty select clause
	 */
	public SelectClause() {
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
		// TODO: FILL IN
		return jsonBuilder.build();
	}

	// -------------------------------------------------------------------------
	// SETTERS AND GETTERS
	// -------------------------------------------------------------------------

	/**
	 * Returns the parameters for the select clause
	 * 
	 * @return Select parameters
	 */
	public Entity getParameter() {
		return parameter;
	}

	/**
	 * Sets the parameters for the select clause
	 * 
	 * @param parameter
	 *            Select parameters
	 */
	public void setParameters(Entity parameter) {
		this.parameter = parameter;
	}

	/**
	 * Returns the alias to set for the select clause
	 * 
	 * @return the alias
	 */
	public String getAlias() {
		return alias;
	}

	/**
	 * Gets the alias for the select clause
	 * @param alias
	 *            the alias to set
	 */
	public void setAlias(String alias) {
		this.alias = alias;
	}

	/**
	 * Returns the operation type of the select if added
	 * 
	 * @return the operationType
	 */
	public SelectOperationType getOperationType() {
		return operationType;
	}

	/**
	 * Returns the operation type of the select if added
	 * 
	 * @param operationType
	 *            the operationType to set
	 */
	public void setOperationType(SelectOperationType operationType) {
		this.operationType = operationType;
	}

	/**
	 * Returns a string representation of the values
	 * 
	 * @return the stringValues
	 */
	public Map<String, String> getStringValues() {
		return stringValues;
	}

	/**
	 * Sets a string representation of the values
	 * 
	 * @param stringValues the stringValues to set
	 */
	public void setStringValues(Map<String, String> stringValues) {
		this.stringValues = stringValues;
	}

	/**
	 * @return the objectValues
	 */
	public Map<String, Object> getObjectValues() {
		return objectValues;
	}

	/**
	 * @param objectValues the objectValues to set
	 */
	public void setObjectValues(Map<String, Object> objectValues) {
		this.objectValues = objectValues;
	}
}
