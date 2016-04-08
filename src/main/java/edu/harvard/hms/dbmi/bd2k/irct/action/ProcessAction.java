/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.action;


import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.model.process.IRCTProcess;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.ProcessResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.QueryResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultStatus;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;

/**
 * Implements the ProcessAction interface to run a process
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class ProcessAction implements Action {
	private IRCTProcess process ;
	private Resource resource;
	private ActionStatus status;
	private Result result;
	
	public void setup(Resource resource, IRCTProcess process) {
		this.resource = resource;
		this.process = process;
		
	}
	
	@Override
	public void run(SecureSession session) {
		this.status = ActionStatus.RUNNING;
		try {
			result = ((ProcessResourceImplementationInterface)resource.getImplementingInterface()).runProcess(session, process);
			this.status = ActionStatus.COMPLETE;
		} catch (ResourceInterfaceException e) {
			this.status = ActionStatus.ERROR;
		}
	}

	@Override
	public Result getResults(SecureSession session) throws ResourceInterfaceException {
		if(this.result.getResultStatus() != ResultStatus.ERROR && this.result.getResultStatus() != ResultStatus.COMPLETE) {
			this.result = ((QueryResourceImplementationInterface)resource.getImplementingInterface()).getResults(session, result);
		}
		return this.result;
	}

	public IRCTProcess getProcess() {
		return this.process;
	}

	public void setProcess(IRCTProcess process) {
		this.process = process;
	}

	@Override
	public ActionStatus getStatus() {
		return status;
	}
}
