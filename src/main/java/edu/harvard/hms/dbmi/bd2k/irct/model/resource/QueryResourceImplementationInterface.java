/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.resource;


import java.util.List;

import edu.harvard.hms.dbmi.bd2k.irct.model.action.ActionState;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Path;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultSet;
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
	ActionState runQuery(Query qep) throws ResourceInterfaceException;

	/**
	 * Returns the results of the query if they are available
	 * 
	 * @param actionState actionState
	 * @return Results
	 * @throws ResourceInterfaceException A resource exception occurred
	 */
	ResultSet getResults(ActionState actionState) throws ResourceInterfaceException;

	/**
	 * Returns the state of the resource
	 * 
	 * @return Resource State
	 */
	ResourceState getState();

	/**
	 * Returns the the default entity that is returned with a query
	 * 
	 * @return Return Entity
	 */
	List<Path> getReturnEntity();
	
	/**
	 * Returns if the default entity is editable
	 * 
	 * @return Return Entity Editable
	 */
	Boolean editableReturnEntity();
}
