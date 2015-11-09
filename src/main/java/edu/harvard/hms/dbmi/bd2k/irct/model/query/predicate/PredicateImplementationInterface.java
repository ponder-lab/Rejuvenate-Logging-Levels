/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
