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
package edu.harvard.hms.dbmi.bd2k.irct.model.resource;

import java.util.Map;

import javax.json.JsonObject;

import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Path;

/**
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
	 * @return
	 */
	String getType();

	/**
	 * Returns a path representation of the default object that is returned
	 * 
	 * @return The default returned object
	 */
	Path getReturnEntity();

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
