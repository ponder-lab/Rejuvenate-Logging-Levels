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
	 * @param session Session to run in
	 * @param process Process to be run
	 * @param result Result
	 * @return Results
	 * @throws ResourceInterfaceException A resource exception occurred
	 */
	Result runProcess(SecureSession session, IRCTProcess process, Result result) throws ResourceInterfaceException;
	
	/**
	 * Returns the results of the process if they are available
	 *
	 * @param session Session to run in
	 * @param result Results
	 * @return Results
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
	 * Returns the result type of the process
	 * 
	 * @param process rocess to run
	 * @return Result
	 * 
	 */
	ResultDataType getProcessDataType(IRCTProcess process);
	
}
