/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.action.process;


import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.model.action.ActionState;
import edu.harvard.hms.dbmi.bd2k.irct.model.process.IRCTProcess;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.ProcessResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.QueryResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultSet;

/**
 * Implements the ProcessAction interface to run a process
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class ExecuteProcess implements ProcessAction {
	private IRCTProcess process ;
	private Resource resource;
	private ActionState actionState;
	
	@Override
	public void setup(Resource resource, IRCTProcess process) {
		this.resource = resource;
		this.process = process;
		
	}
	
	@Override
	public void run() {
		actionState = ((ProcessResourceImplementationInterface)resource.getImplementingInterface()).runProcess(process);
	}

	@Override
	public ResultSet getResults() throws ResourceInterfaceException {
		if(actionState.isComplete()) {
			return actionState.getResults();
		}
		return ((QueryResourceImplementationInterface)resource.getImplementingInterface()).getResults(actionState);
	}

	@Override
	public IRCTProcess getProcess() {
		return this.process;
	}

	@Override
	public void setProcess(IRCTProcess process) {
		this.process = process;
	}
}
