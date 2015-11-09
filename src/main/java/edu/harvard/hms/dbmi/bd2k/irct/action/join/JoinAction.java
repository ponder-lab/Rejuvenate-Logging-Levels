/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.action.join;

import java.util.Map;

import javax.json.JsonObject;

import edu.harvard.hms.dbmi.bd2k.irct.action.Action;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Joinable;

/**
 * The JoinAction interface extends the Action interface for Joins. All joins
 * must implement this interface.
 * 
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public interface JoinAction extends Action {
	/**
	 * Sets up the join with the given parameters.
	 * 
	 * @param parameters
	 *            Join Parameters
	 */
	void setup(Map<String, String> parameters);

	/**
	 * Sets the joinable resultSets that are to be combined and joins them. The
	 * results of these joins can then be retrieved with the getResults()
	 * method.
	 * 
	 * @param joinables
	 *            All RrsultSets that are to be joined
	 * @throws Exception
	 *             An exception occurred joining the resultSets
	 */
	void setJoins(Joinable... joinables) throws Exception;

	/**
	 * Returns the join type
	 * 
	 * @return Join Type
	 */
	String getType();
	
	/**
	 * Returns a JSONObject representation of the object. This returns only the
	 * attributes associated with this object and not their representation.
	 * 
	 * This is equivalent of toJson(1);
	 * 
	 * @return JSON Representation
	 */
	JsonObject toJson();

	/**
	 * Returns a JSONObject representation of the object. This returns only the
	 * attributes associated with this object and not their representation.
	 * 
	 * 
	 * @param depth
	 *            Depth to travel
	 * @return JSON Representation
	 */
	JsonObject toJson(int depth);
}
