/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.action;

import java.util.Map;

import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;

/**
 * The action interface provides a set of basic functionality that all actions must
 * accomplish. An action can be a Join, Query, Process or other. Each action subtype
 * having their own interface that should be used.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public interface Action {
	/**
	 * Run the given action in a secure session if required
	 * 
	 * @param secureSession A secure session (Null if not needed or set)
	 * @throws ResourceInterfaceException A resource interface exception occurred
	 */
	void run(SecureSession secureSession) throws ResourceInterfaceException;
	
	/**
	 * Updates the parameters of the action with the new information. This is typically used in chained actions.
	 * 
	 * @param updatedParams Updated parameters of an action
	 */
	void updateActionParams(Map<String, Result> updatedParams);

	/**
	 * Returns the results from an action
	 * 
	 * @param secureSession A secure session (Null if not needed or set)
	 * @return The results of the action
	 * @throws ResourceInterfaceException A resource interface exception occurred
	 */
	Result getResults(SecureSession secureSession) throws ResourceInterfaceException;
	
	/**
	 * Returns the actions status
	 * 
	 * @return Action Status
	 */
	ActionStatus getStatus();
	
}
