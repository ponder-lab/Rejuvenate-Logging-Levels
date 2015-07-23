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
package edu.harvard.hms.dbmi.bd2k.irct.model.query.predicate;

import javax.json.JsonObject;

import edu.harvard.hms.dbmi.bd2k.irct.exception.PredicateException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultSet;

/**
 * Provides an implementation that describes the API for any predicate that
 * needs to be run.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public interface PredicateImplementationInterface {
	/**
	 * Sets the input result set that the predicate will be run on
	 * 
	 * @param resultset
	 *            The input result set
	 */
	void setResultSet(ResultSet resultset);

	/**
	 * Sets the value (if needed) for the predicate action
	 * 
	 * @param value
	 *            Value
	 */
	void setValue(String value);

	/**
	 * Sets the additional value (if needed) for the predicate action
	 * 
	 * @param additionalValue
	 *            Additional Value
	 */
	void setAdditionalValue(String additionalValue);

	/**
	 * Runs the predicate action and returns the computed results
	 * 
	 * @return Results
	 * @throws PredicateException
	 *             An error occurred running the predicate
	 */
	ResultSet run() throws PredicateException;
	
	/**
	 * Returns a JSON representation of the implementing interface
	 * 
	 * Equivalent to toJson(1);
	 * 
	 * @return JSON Representation
	 */
	JsonObject toJson();

	/**
	 * Returns a JSON representation of the implementing interface while
	 * converting children to JSON of a given depth
	 * 
	 * 
	 * @param depth Depth to travel
	 * @return JSON Representation
	 */
	JsonObject toJson(int depth);
}
