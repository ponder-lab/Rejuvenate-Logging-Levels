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

import java.util.ArrayList;
import java.util.List;

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
	private List<Path> parameters;

	public SelectClause(Long id) {
		super(id);
		this.parameters = new ArrayList<Path>();
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
	public List<Path> getParameters() {
		return parameters;
	}

	/**
	 * Sets the parameters for the select clause
	 * 
	 * @param parameters
	 *            Select parameters
	 */
	public void setParameters(List<Path> parameters) {
		this.parameters = parameters;
	}

	/**
	 * Adds a new parameter to the select clause
	 * 
	 * @param parameter
	 *            Select parameter
	 */
	public void addParameter(Path parameter) {
		this.parameters.add(parameter);
	}

	/**
	 * Removes a parameter from the select clause
	 * 
	 * @param parameter
	 *            Select parameter
	 */
	public void removeParameter(Path parameter) {
		this.parameters.remove(parameter);
	}

}
