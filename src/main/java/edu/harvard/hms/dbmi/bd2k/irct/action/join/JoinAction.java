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
