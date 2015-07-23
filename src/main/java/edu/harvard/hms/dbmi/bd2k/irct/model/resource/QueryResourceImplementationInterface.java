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

import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Path;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultSet;
import edu.harvard.hms.dbmi.bd2k.irct.ri.exception.ResourceInterfaceException;

/**
 * Provides an implementation that describes the API for any resource that has
 * the ability to handle a query
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public interface QueryResourceImplementationInterface extends
		ResourceImplementationInterface {
	/**
	 * Runs the given query
	 * 
	 * @param qep
	 *            Query to be run
	 * @return The id of the query that is running
	 * @throws ResourceInterfaceException 
	 */
	Long run(Query qep) throws ResourceInterfaceException;

	/**
	 * Returns the results of the query if they are available
	 * 
	 * @param queryId
	 *            Query ID
	 * @return Results
	 * @throws ResourceInterfaceException 
	 */
	ResultSet getResults(Long queryId) throws ResourceInterfaceException;

	/**
	 * Returns the state of the resource
	 * 
	 * @return Resource State
	 */
	ResourceState getState();

	/**
	 * Returns the the default entity that is returned with a query
	 */
	Path getReturnEntity();

}
