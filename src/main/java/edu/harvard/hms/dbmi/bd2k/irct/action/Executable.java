/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.action;

import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultSet;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;

/**
 * The executable interface provides a set of methods that an executable must
 * implement. An executable is the implementing class for any type of process,
 * query, or action.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public interface Executable {
	/**
	 * Set up the executable with an action
	 * 
	 * @param action Action to be executed
	 */
	void setup(Action action);

	/**
	 * Run the executable
	 * 
	 * @throws ResourceInterfaceException An error occurred
	 */
	void run() throws ResourceInterfaceException;

	/**
	 * Get the current state of the executable
	 * 
	 * @return Executable State
	 */
	ExecutableState getState();

	/**
	 * Returns the result set
	 * 
	 * @return Result Set
	 * @throws ResourceInterfaceException An error occurred
	 */
	ResultSet getResults() throws ResourceInterfaceException;

	/**
	 * Returns the resource the executable will be run on
	 * 
	 * @return Resource to run on
	 */
	Resource getResource();

	/**
	 * Sets the resource this executable will be run on
	 * 
	 * @param resource Resource to run on
	 */
	void setResource(Resource resource);
}
