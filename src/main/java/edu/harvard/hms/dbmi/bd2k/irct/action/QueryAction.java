/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.action;

import java.util.Map;

import edu.harvard.hms.dbmi.bd2k.irct.model.query.ClauseAbstract;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.WhereClause;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.QueryResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultStatus;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;

/**
 * Implements the QueryAction interface to run a query on a specific instance
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class QueryAction implements Action {
	private Query query;
	private Resource resource;
	private ActionStatus status;
	private Result result;

	public void setup(Resource resource, Query query) {
		this.query = query;
		this.resource = resource;
		this.status = ActionStatus.CREATED;
	}
	@Override
	public void updateActionParams(Map<String, Result> updatedParams) {
		for(String key : updatedParams.keySet()) {
			Long clauseId = Long.valueOf(key.split(".")[0]);
			String parameterId = key.split(".")[1];
			
			ClauseAbstract clause = this.query.getClauses().get(clauseId);
			if (clause instanceof WhereClause) {
				WhereClause whereClause = (WhereClause) clause;
				whereClause.getValues().put(parameterId, updatedParams.get(key).getId().toString());
			}
		}
	}
	@Override
	public void run(SecureSession session) {
		this.status = ActionStatus.RUNNING;
		try {
			result = ((QueryResourceImplementationInterface) resource.getImplementingInterface()).runQuery(session, query);
			this.status = ActionStatus.COMPLETE;
		} catch (ResourceInterfaceException e) {
			this.status = ActionStatus.ERROR;
		}
	}
	@Override
	public Result getResults(SecureSession session) throws ResourceInterfaceException {
		if(this.result.getResultStatus() != ResultStatus.ERROR && this.result.getResultStatus() != ResultStatus.COMPLETE) {
			this.result = ((QueryResourceImplementationInterface)resource.getImplementingInterface()).getResults(session, result);
		}
		return this.result;
	}

	public Query getQuery() {
		return this.query;

	}

	public void setQuery(Query query) {
		this.query = query;
	}

	@Override
	public ActionStatus getStatus() {
		return status;
	}
}
