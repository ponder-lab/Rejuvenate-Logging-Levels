/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.action;

import edu.harvard.hms.dbmi.bd2k.irct.action.join.JoinAction;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultSet;

/**
 * The query executable implements the Executable interface for executing local
 * joins. It also supports binary tree executions through the
 * BinaryTreeExecutable interface.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class JoinExecutable implements Executable {
	private Resource resource;
	private JoinAction action;
	private ExecutableState state;

	public void setup(Action action) {
		this.action = (JoinAction) action;
	}

	public void run() throws ResourceInterfaceException {
		this.state = ExecutableState.RUNNING;
		this.action.run();
		this.state = ExecutableState.COMPLETED;

	}

	public ExecutableState getState() {
		return this.state;
	}

	public ResultSet getResults() throws ResourceInterfaceException {
		return this.action.getResults();
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public Resource getResource() {
		return this.resource;
	}

}
