/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.action.query;

import edu.harvard.hms.dbmi.bd2k.irct.model.action.ActionState;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.QueryResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultSet;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;

/**
 * Implements the QueryAction interface to run a query on a specific instance
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class ExecuteQuery implements QueryAction {
	private ActionState actionState;
	private Query query;
	private Resource resource;

	public void setup(Resource resource, Query query) {
		setQuery(query);
		this.resource = resource;
	}

	public void run() throws ResourceInterfaceException {
		actionState = ((QueryResourceImplementationInterface)resource.getImplementingInterface()).runQuery(query);
	}

	public ResultSet getResults() throws ResourceInterfaceException {
		if(actionState.isComplete()) {
			return actionState.getResults();
		}
		return ((QueryResourceImplementationInterface)resource.getImplementingInterface()).getResults(actionState);
	}

	public Query getQuery() {
		return this.query;

	}

	public void setQuery(Query query) {
		this.query = query;
	}

}
