/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.query;

import javax.json.JsonObject;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * A sub query may be part of a query or another sub query. If it is part of a
 * query then it may reference a different resource then sibling sub queries.
 * The sub queries can be combined using the Join clause in a query.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@Entity
public class SubQuery extends Query {
	private static final long serialVersionUID = -4698577393371222525L;
	@ManyToOne
	private Query parent;

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
		JsonObject parentJson = super.toJson(depth);
		if (depth > 1) {
			parentJson.put("parent", parent.toJson(depth));
		}
		return parentJson;
	}

	// -------------------------------------------------------------------------
	// SETTERS AND GETTERS
	// -------------------------------------------------------------------------

	/**
	 * Returns the subQueries parent
	 * 
	 * @return Parent Query
	 */
	public Query getParent() {
		return parent;
	}

	/**
	 * Sets the parent query that is associated with this subQuery
	 * 
	 * @param parent
	 *            Parent Query
	 */
	public void setParent(Query parent) {
		this.parent = parent;
	}

}
