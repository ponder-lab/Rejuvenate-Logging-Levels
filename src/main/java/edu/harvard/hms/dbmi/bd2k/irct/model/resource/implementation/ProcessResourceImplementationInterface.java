/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation;

import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.model.action.ActionState;
import edu.harvard.hms.dbmi.bd2k.irct.model.process.IRCTProcess;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.ResourceState;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultSet;

/**
 * Provides an implementation that describes the API for any resource that has
 * processes that can be run
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public interface ProcessResourceImplementationInterface extends ResourceImplementationInterface {
	/**
	 * Run the given process
	 * 
	 * @param pep Process to be run
	 * @return The id of the process that is running
	 */
	ActionState runProcess(IRCTProcess pep);
	
	/**
	 * Returns the results of the process if they are available
	 *
	 * @param actionState Action State
	 * @return Results Results
	 * @throws ResourceInterfaceException A resource exception occurred
	 */
	ResultSet getResults(ActionState actionState) throws ResourceInterfaceException;

	/**
	 * Returns the status of the resource
	 * 
	 * @return Resource State
	 */
	ResourceState getState();
	
}
