/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.action;

import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultSet;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;

/**
 * An execution plan is series of executable processes that are run by the IRCT.
 * An execution plan is run each time a query, join, or process request for
 * execution is made.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class ExecutionPlan {
	private ExecutableState state;
	private Executable executable;
	private ResultSet results;

	/**
	 * Setup the execution plan with the base executable
	 * 
	 * @param executable
	 *            Base executable
	 */
	public void setup(Executable executable) {
		this.executable = executable;
		this.state = ExecutableState.CREATED;
		this.results = null;
	}

	/**
	 * Run the base execution plan
	 */
	public void run() {
		this.state = ExecutableState.RUNNING;
		try {
			this.executable.run();
			this.results = this.executable.getResults();
		} catch (ResourceInterfaceException e) {
			e.printStackTrace();
		}

		this.state = ExecutableState.COMPLETED;
	}

	/**
	 * Return the results of the execution plan if they are available
	 * 
	 * @return Results
	 */
	public ResultSet getResults() {
		return this.results;
	}

	/**
	 * Returns the current execution state
	 * 
	 * @return Execution state
	 */
	public ExecutableState getState() {
		return this.state;
	}

}
