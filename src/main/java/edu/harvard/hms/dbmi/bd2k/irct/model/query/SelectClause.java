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

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Path;

/**
 * The select clause provides a list of parameters that should be returned by
 * the query.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class SelectClause extends ClauseAbstract {
	private Path parameter;
	private String alias;

	public SelectClause(Long id) {
		super(id);
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
	public Path getParameter() {
		return parameter;
	}

	/**
	 * Sets the parameters for the select clause
	 * 
	 * @param parameter
	 *            Select parameters
	 */
	public void setParameters(Path parameter) {
		this.parameter = parameter;
	}

	/**
	 * Returns the alias for the select clause
	 * 
	 * @return Alias
	 */
	public String getAlias() {
		return alias;
	}

	/**
	 * Sets the alias for the select clause
	 * @param alias Alias
	 */
	public void setAlias(String alias) {
		this.alias = alias;
	}

}
