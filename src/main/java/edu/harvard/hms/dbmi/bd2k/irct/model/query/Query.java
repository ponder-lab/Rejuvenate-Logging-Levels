/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;

/**
 * The query class represents any query against any individual or group of
 * resources. A Query can have many subQueries, and clauses (Joins, Selects,
 * Wheres).
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class Query {
	private Long id;

	private HashMap<Long, SubQuery> subQueries;
	private HashMap<Long, ClauseAbstract> clauses;

	private List<Resource> resources;

	public Query() {
		this.setSubQueries(new LinkedHashMap<Long, SubQuery>());
		this.setClauses(new LinkedHashMap<Long, ClauseAbstract>());
		this.setResources(new ArrayList<Resource>());
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
	 * Returns the id of the query
	 * 
	 * @return ID
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Sets the id of the query
	 * 
	 * @param id
	 *            ID
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Adds a new subQuery to the query
	 * 
	 * @param id
	 *            SubQuery ID
	 * @param subQuery
	 *            SubQuery
	 */
	public final void addSubQuery(Long id, SubQuery subQuery) {
		this.subQueries.put(id, subQuery);
	}

	/**
	 * Removes a subQuery from the query
	 * 
	 * @param id
	 *            SubQuery ID
	 */
	public final void removeSubQuery(Long id) {
		this.subQueries.remove(id);
	}

	/**
	 * Returns a map of SubQueries where the key is the subQuery ID, and the
	 * value is the subQuery
	 * 
	 * @return SubQuery Map
	 */
	public final HashMap<Long, SubQuery> getSubQueries() {
		return subQueries;
	}

	/**
	 * Sets a map of SubQueries where the key is the subQuery ID, and the value
	 * is the subQuery
	 * 
	 * @param subQueries
	 *            SubQuery Map
	 */
	public final void setSubQueries(LinkedHashMap<Long, SubQuery> subQueries) {
		this.subQueries = subQueries;
	}

	/**
	 * Adds a new clause to the subQuery
	 * 
	 * @param id
	 *            Clause ID
	 * @param clause
	 *            Clause
	 */
	public void addClause(Long id, ClauseAbstract clause) {
		this.clauses.put(id, clause);
	}

	/**
	 * Removes a clause from the subQuery
	 * 
	 * @param id
	 *            Clause ID
	 */
	public void removeClause(Long id) {
		this.clauses.remove(id);
	}

	/**
	 * Returns a map of Clauses where the key is the clause id, and the Clause
	 * is the value
	 * 
	 * @return Clause Map
	 */
	public HashMap<Long, ClauseAbstract> getClauses() {
		return clauses;
	}

	/**
	 * Sets a map of Clauses where the key is the clause id, and the Clause is
	 * the value
	 * 
	 * @param clauses Map of Clauses
	 */
	public void setClauses(LinkedHashMap<Long, ClauseAbstract> clauses) {
		this.clauses = clauses;
	}

	/**
	 * Adds a resource to the query
	 * 
	 * @param resource
	 *            Resource
	 */
	public void addResource(Resource resource) {
		this.resources.add(resource);
	}

	/**
	 * Removes a resource from the query
	 * 
	 * @param resource
	 *            Resource
	 */
	public void removeResource(Resource resource) {
		this.resources.remove(resource);
	}

	/**
	 * Returns a list of resources that are associated with the query
	 * 
	 * @return Resources
	 */
	public List<Resource> getResources() {
		return resources;
	}

	/**
	 * Sets a list of resources that are associated with the query
	 * 
	 * @param resources
	 *            Resources
	 */
	public void setResources(List<Resource> resources) {
		this.resources = resources;
	}
}
