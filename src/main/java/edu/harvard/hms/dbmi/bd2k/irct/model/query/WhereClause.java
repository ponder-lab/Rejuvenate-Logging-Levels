/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.query;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToOne;

import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Entity;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.LogicalOperator;

/**
 * The where clause contains information used in a query to filter upon the
 * data. A where clause can be done on a single query, a subquery or path.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@javax.persistence.Entity
public class WhereClause extends ClauseAbstract implements Serializable {
	private static final long serialVersionUID = 5846062257054747524L;
	
	@OneToOne(cascade=CascadeType.ALL)
	private SubQuery subQuery;
	
	@Enumerated(EnumType.STRING)
	private LogicalOperator logicalOperator;
	
	@OneToOne(cascade=CascadeType.ALL)
	private Entity field;
	
	@ManyToOne
	private PredicateType predicateType;
	
	@ElementCollection(fetch = FetchType.EAGER)
	@MapKeyColumn(name="name")
	@Column(name="value")
	@CollectionTable(name="where_values", joinColumns=@JoinColumn(name="where_id"))
	private Map<String, String> stringValues;
	
	/**
	 * Creates an empty where clause
	 * 
	 */
	public WhereClause() {
		this.stringValues = new HashMap<String, String>();
	}

	/**
	 * Returns the subquery associated with the where clause
	 * 
	 * @return SubQuery
	 */
	public SubQuery getSubQuery() {
		return subQuery;
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
		jsonBuilder.add("id", super.getId());
		jsonBuilder.add("logicalOperator", this.logicalOperator.toString());

		if (subQuery != null) {
			if (depth > 1) {
				jsonBuilder.add("subQuery", this.subQuery.toJson(depth));
			} else {
				jsonBuilder.add("subQuery", this.subQuery.toJson());
			}
		} else if (field != null) {
			if (depth > 1) {
				jsonBuilder.add("field", this.field.toJson(depth));
			} else {
				jsonBuilder.add("field", this.field.getName());
			}
		}

		jsonBuilder.add("predicateType", predicateType.toJson(depth));

		if (stringValues != null) {
			JsonArrayBuilder jsonValues = Json.createArrayBuilder();
			for(String valueKey : this.stringValues.keySet()) {
				JsonObjectBuilder valueInstance = Json.createObjectBuilder();
				valueInstance.add(valueKey, this.stringValues.get(valueKey));
			}
			jsonBuilder.add("value", jsonValues);
		}

		return jsonBuilder.build();
	}

	// -------------------------------------------------------------------------
	// SETTERS AND GETTERS
	// -------------------------------------------------------------------------

	/**
	 * Sets the subQuery to be associated with the where clause
	 * 
	 * @param subQuery Sub Query
	 */
	public void setSubQuery(SubQuery subQuery) {
		this.subQuery = subQuery;
	}

	/**
	 * Returns the logical operator used to combine this where clause with other
	 * where clauses
	 * 
	 * @return Logical operator
	 */
	public LogicalOperator getLogicalOperator() {
		return logicalOperator;
	}

	/**
	 * Sets the logical operator to be used to combine this where clause with
	 * other where clauses
	 * 
	 * @param logicalOperator
	 *            Logical operator
	 */
	public void setLogicalOperator(LogicalOperator logicalOperator) {
		this.logicalOperator = logicalOperator;
	}

	/**
	 * Returns the path that is associated with the where clause
	 * 
	 * @return Field
	 */
	public Entity getField() {
		return field;
	}

	/**
	 * Sets the path that is associated with the where clause
	 * 
	 * @param field
	 *            Field
	 */
	public void setField(Entity field) {
		this.field = field;
	}

	/**
	 * Returns the predicate type that is associated with this where clause
	 * 
	 * @return Predicate Type
	 */
	public PredicateType getPredicateType() {
		return predicateType;
	}

	/**
	 * Sets the predicate type that is associated with this where clause.
	 * 
	 * @param predicateType Predicate Type
	 */
	public void setPredicateType(PredicateType predicateType) {
		this.predicateType = predicateType;
	}

	/**
	 * Returns the values that the predicate type operates against if it is
	 * needed.
	 * 
	 * @return Value
	 */
	public Map<String, String> getStringValues() {
		return stringValues;
	}

	/**
	 * Sets the value that the predicate type operates against if it is needed.
	 * 
	 * @param stringValues
	 *            A map of values
	 */
	public void setStringValues(Map<String, String> stringValues) {
		this.stringValues = stringValues;
	}

}
