/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.query;

import java.io.Serializable;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.persistence.CascadeType;
import javax.persistence.OneToOne;

import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Entity;

/**
 * The select clause provides a list of parameters that should be returned by
 * the query.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@javax.persistence.Entity
public class SelectClause extends ClauseAbstract implements Serializable {
	private static final long serialVersionUID = 3728919497144122930L;
	@OneToOne(cascade=CascadeType.ALL)
	private Entity parameter;
	
	private String alias;

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
	 * @return the alias
	 */
	public String getAlias() {
		return alias;
	}

	/**
	 * @param alias the alias to set
	 */
	public void setAlias(String alias) {
		this.alias = alias;
	}
}
