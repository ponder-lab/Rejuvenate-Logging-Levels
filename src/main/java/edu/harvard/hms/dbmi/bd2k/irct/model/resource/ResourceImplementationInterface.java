/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.resource;

import java.util.List;
import java.util.Map;

import javax.json.JsonObject;

import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Path;

/**
 * Provides a base interface that is used by a resource implementation 
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public interface ResourceImplementationInterface {
	/**
	 * A set of parameters that can used to setup the Resource Implementation
	 * 
	 * @param parameters
	 *            Setup parameters
	 */
	void setup(Map<String, String> parameters);

	/**
	 * A string representation of the type of resource implementation this is
	 * 
	 * @return Type
	 */
	String getType();

	/**
	 * Returns a path representation of the default object that is returned
	 * 
	 * @return The default returned object
	 */
	List<Path> getReturnEntity();

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
