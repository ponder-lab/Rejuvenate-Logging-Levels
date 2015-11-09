/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.util;

import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * A set of utilities that can be used for creating JSON objects
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@SuppressWarnings("rawtypes")
public class JsonUtilities {

	/**
	 * Given a map returns a JSON object with attributes where the attribute
	 * name is the string value of the map key, and the attribute value is the
	 * string value of map value.
	 * 
	 * @param map Map to turn into JSON
	 * @return JSON representation
	 */
	public static JsonObject mapToJson(Map map) {
		JsonObjectBuilder mapBuilder = Json.createObjectBuilder();

		for (Object key : map.keySet()) {
			mapBuilder.add(key.toString(), map.get(key).toString());
		}

		return mapBuilder.build();
	}
}
