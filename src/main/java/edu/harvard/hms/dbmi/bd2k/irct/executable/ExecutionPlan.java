/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.executable;

import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;
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
	private ExecutableStatus status;
	private Executable executable;
	private Result results;
	private SecureSession session;

	/**
	 * Setup the execution plan with the base executable
	 * 
	 * @param executable
	 *            Base executable
	 */
	public void setup(Executable executable, SecureSession session) {
		this.executable = executable;
		this.session = session;
		this.status = ExecutableStatus.CREATED;
		this.results = null;
	}

	/**
	 * Run the base execution plan
	 */
	public void run() {
		this.status = ExecutableStatus.RUNNING;
		try {
			this.executable.setup(session);
			this.executable.run();
			this.results = this.executable.getResults();
		} catch (ResourceInterfaceException e) {
			e.printStackTrace();
		}

		this.status = ExecutableStatus.COMPLETED;
	}

	/**
	 * Return the results of the execution plan if they are available
	 * 
	 * @return Results
	 */
	public Result getResults() {
		return this.results;
	}

	/**
	 * Returns the current execution state
	 * 
	 * @return Execution state
	 */
	public ExecutableStatus getState() {
		return this.status;
	}

}
