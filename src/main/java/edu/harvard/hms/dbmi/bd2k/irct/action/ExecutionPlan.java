/*
 *  This file is part of Inter-Resource Communication Tool (IRCT).
 *
 *  IRCT is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  IRCT is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with IRCT.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.harvard.hms.dbmi.bd2k.irct.action;

import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultSet;
import edu.harvard.hms.dbmi.bd2k.irct.ri.exception.ResourceInterfaceException;

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
