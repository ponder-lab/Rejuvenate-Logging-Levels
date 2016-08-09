/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.find;

import java.util.Map;

public interface FindInformationInterface {
	public String getType();
	public String[] getRequiredParameters();
	public void setValue(String parameter, String value);
	public Map<String, String> getValues();
	public <T extends FindInformationInterface> T copy();
}
