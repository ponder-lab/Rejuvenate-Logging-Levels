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
package edu.harvard.hms.dbmi.bd2k.irct.model.resource;

import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultSet;

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
	 * @param pep Process to be run
	 * @return The id of the process that is running
	 */
	Long run(Process pep);

	/**
	 * Returns the results of the process if they are available
	 * 
	 * @param processId Process ID
	 * @return Results
	 */
	ResultSet getResults(Long processId);

	/**
	 * Returns the status of the resource
	 * 
	 * @return Resource State
	 */
	ResourceState getState();
}
