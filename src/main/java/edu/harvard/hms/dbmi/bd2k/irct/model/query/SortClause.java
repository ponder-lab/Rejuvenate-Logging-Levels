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
 * The sort clause provides a method to sort data that is to
 * be returned by the query.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@javax.persistence.Entity
public class SortClause extends ClauseAbstract implements Serializable {
	private static final long serialVersionUID = 1746287992174155091L;

	@OneToOne(cascade = CascadeType.ALL)
	private Entity parameter;
	
	@ManyToOne
	private SortOperationType operationType;

	@ElementCollection(fetch = FetchType.EAGER)
	@MapKeyColumn(name="name")
	@Column(name="value")
	@CollectionTable(name="sort_values", joinColumns=@JoinColumn(name="sort_id"))
	private Map<String, String> stringValues;
	
	@Transient
	private Map<String, Object> objectValues;

	/**
	 * Creates an empty sort clause
	 */
	public SortClause() {
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
	 * Returns the sort operation
	 * 
	 * @return the operationType
	 */
	public SortOperationType getOperationType() {
		return operationType;
	}

	/**
	 * Sets the sort operation
	 * 
	 * @param operationType the operationType to set
	 */
	public void setOperationType(SortOperationType operationType) {
		this.operationType = operationType;
	}

	/**
	 * Returns the values that the sort type operates against if it is
	 * needed.
	 * 
	 * @return Value
	 */
	public Map<String, String> getStringValues() {
		return stringValues;
	}

	/**
	 * Sets the value that the sort type operates against if it is needed.
	 * 
	 * @param stringValues
	 *            A map of values
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
