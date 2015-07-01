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

import javax.json.JsonObject;

/**
 * A sub query may be part of a query or another sub query. If it is part of a
 * query then it may reference a different resource then sibling sub queries.
 * The sub queries can be combined using the Join clause in a query.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class SubQuery extends Query {
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
