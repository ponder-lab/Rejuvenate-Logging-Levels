/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.action.join;

import java.util.Map;

import edu.harvard.hms.dbmi.bd2k.irct.action.Action;
import edu.harvard.hms.dbmi.bd2k.irct.exception.JoinActionSetupException;

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
	 * @throws Exception An exception occurred
	 */
	void setup(Map<String, Object> parameters) throws JoinActionSetupException;

	/**
	 * Returns the join type
	 * 
	 * @return Join Type
	 */
	String getType();
	
}
