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

import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultSet;
import edu.harvard.hms.dbmi.bd2k.irct.ri.exception.ResourceInterfaceException;

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
