/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.find;

import java.util.HashMap;
import java.util.Map;

public class FindByPath implements FindInformationInterface {
	Map<String, String> values;

	public FindByPath() {
		values = new HashMap<String, String>();
	}

	@Override
	public void setValue(String parameter, String value) {
		values.put(parameter, value);
	}

	@Override
	public String getType() {
		return "FindPath";
	}

	@Override
	public String[] getRequiredParameters() {
		return new String[] { "term" };
	}

	@Override
	public Map<String, String> getValues() {
		return values;
	}

	@SuppressWarnings("unchecked")
	@Override
	public FindByPath copy() {
		FindByPath copyFind = new FindByPath();
		for (String key : values.keySet()) {
			copyFind.setValue(key, values.get(key));
		}
		return copyFind;
	}

}
