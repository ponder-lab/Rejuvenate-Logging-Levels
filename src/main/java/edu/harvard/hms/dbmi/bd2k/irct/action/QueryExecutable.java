/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.action;

import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultSet;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;

/**
 * The query executable implements the Executable interface for executing
 * queries.
 * 
 * NOTE: The current implementation does not support any child processes.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class QueryExecutable implements Executable {
	private Resource resource;

	private ExecutableState state;
	private Action action;

	public void setup(Action action) {
		this.action = action;
		this.state = ExecutableState.CREATED;

	}

	public void run() throws ResourceInterfaceException {
		this.state = ExecutableState.RUNNING;
		this.action.run();
		this.state = ExecutableState.COMPLETED;
	}

	public ResultSet getResults() throws ResourceInterfaceException {
		return this.action.getResults();
	}

	public ExecutableState getState() {
		return this.state;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public Resource getResource() {
		return this.resource;
	}
}
