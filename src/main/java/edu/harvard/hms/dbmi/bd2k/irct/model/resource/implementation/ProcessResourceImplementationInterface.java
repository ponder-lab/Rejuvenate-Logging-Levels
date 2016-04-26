/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation;

import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.model.process.IRCTProcess;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.ResourceState;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;

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
	Result runProcess(SecureSession session, IRCTProcess pep, Result result) throws ResourceInterfaceException;
	
	/**
	 * Returns the results of the process if they are available
	 *
	 * @param actionState Action State
	 * @return Results Results
	 * @throws ResourceInterfaceException A resource exception occurred
	 */ 
	Result getResults(SecureSession session, Result result) throws ResourceInterfaceException;

	/**
	 * Returns the state of the resource
	 * 
	 * @return Resource State
	 */
	ResourceState getState();
	
	/**
	 * Returns the result data type
	 * 
	 * @return Result data type
	 */
	ResultDataType getProcessDataType(IRCTProcess process);
	
}
