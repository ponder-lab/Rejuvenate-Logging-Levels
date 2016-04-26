/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation;


import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.ResourceState;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;

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
	 * @throws ResourceInterfaceException A resource exception occurred
	 */
	Result runQuery(SecureSession session, Query qep, Result result) throws ResourceInterfaceException;

	/**
	 * Returns the results of the query if they are available
	 * 
	 * @param actionState actionState
	 * @return Results
	 * @throws ResourceInterfaceException A resource exception occurred
	 */
	Result getResults(SecureSession session, Result result) throws ResourceInterfaceException;

	/**
	 * Returns the state of the resource
	 * 
	 * @return Resource State
	 */
	ResourceState getState();
	
	/**
	 * Returns the result data type
	 * 
	 * @return Result data type
	 */
	ResultDataType getQueryDataType(Query query);
}
