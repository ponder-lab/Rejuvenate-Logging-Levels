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

import java.util.Map;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Path;

/**
 * The where clause contains information used in a query to filter upon the
 * data. A where clause can be done on a single a subquery or path.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class WhereClause extends ClauseAbstract {
	private SubQuery subQuery;
	private LogicalOperator logicalOperator;
	private Path field;
	private PredicateType predicateType;
	private Map<String, String> values;

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

		if (values != null) {
			JsonArrayBuilder jsonValues = Json.createArrayBuilder();
			for(String valueKey : this.values.keySet()) {
				JsonObjectBuilder valueInstance = Json.createObjectBuilder();
				valueInstance.add(valueKey, this.values.get(valueKey));
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
	 * @param subQuery
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
	public Path getField() {
		return field;
	}

	/**
	 * Sets the path that is associated with the where clause
	 * 
	 * @param field
	 *            Field
	 */
	public void setField(Path field) {
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
	 * @param predicateType
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
	public Map<String, String> getValues() {
		return values;
	}

	/**
	 * Sets the value that the predicate type operates against if it is needed.
	 * 
	 * @param values
	 *            Value
	 */
	public void setValues(Map<String, String> values) {
		this.values = values;
	}

}
