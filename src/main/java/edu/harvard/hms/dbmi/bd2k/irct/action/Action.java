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
	void run(SecureSession secureSession) throws ResourceInterfaceException;
	
	void updateActionParams(Map<String, Result> updatedParams);

	Result getResults(SecureSession secureSession) throws ResourceInterfaceException;
	
	ActionStatus getStatus();
	
}
