/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.action;

import java.util.Date;
import java.util.Map;

import javax.naming.NamingException;

import edu.harvard.hms.dbmi.bd2k.irct.model.query.ClauseAbstract;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.WhereClause;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.QueryResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Persistable;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultStatus;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;
import edu.harvard.hms.dbmi.bd2k.irct.util.Utilities;
import edu.harvard.hms.dbmi.bd2k.irct.event.IRCTEventListener;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;

/**
 * Implements the Action interface to run a query on a specific instance
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class QueryAction implements Action {

	private Query query;
	private Resource resource;
	private ActionStatus status;
	private Result result;
	
	private IRCTEventListener irctEventListener;

	/**
	 * Sets up the action to run a given query on a resource
	 * 
	 * @param resource
	 *            Resource to run the query
	 * @param query
	 *            Run the query
	 */
	public void setup(Resource resource, Query query) {
		this.query = query;
		this.resource = resource;
		this.status = ActionStatus.CREATED;
		this.irctEventListener = Utilities.getIRCTEventListener();
	}

	@Override
	public void updateActionParams(Map<String, Result> updatedParams) {
		for (String key : updatedParams.keySet()) {
			Long clauseId = Long.valueOf(key.split(".")[0]);
			String parameterId = key.split(".")[1];

			ClauseAbstract clause = this.query.getClauses().get(clauseId);
			if (clause instanceof WhereClause) {
				WhereClause whereClause = (WhereClause) clause;
				whereClause.getStringValues().put(parameterId,
						updatedParams.get(key).getId().toString());
			}
		}
	}

	@Override
	public void run(SecureSession session) {
		irctEventListener.beforeQuery(session, resource, query);
		this.status = ActionStatus.RUNNING;
		try {
			QueryResourceImplementationInterface queryInterface = (QueryResourceImplementationInterface) resource
					.getImplementingInterface();

			this.result = ActionUtilities.createResult(queryInterface
					.getQueryDataType(query));

			if (session != null) {
				this.result.setUser(session.getUser());
			}

			this.result = queryInterface.runQuery(session, query, result);

			// Update the result in the database
			ActionUtilities.mergeResult(this.result);
		} catch (Exception e) {
			this.result.setResultStatus(ResultStatus.ERROR);
			this.result.setMessage(e.getMessage());
			this.status = ActionStatus.ERROR;
		}
		irctEventListener.afterQuery(session, resource, query);
	}

	@Override
	public Result getResults(SecureSession session)
			throws ResourceInterfaceException {
		if(this.result.getResultStatus() == ResultStatus.COMPLETE || this.result.getResultStatus() == ResultStatus.ERROR) {
			return this.result;
		}
		try {			
			this.result = ((QueryResourceImplementationInterface) resource
					.getImplementingInterface()).getResults(session, result);
			
			while ((this.result.getResultStatus() != ResultStatus.ERROR)
					&& (this.result.getResultStatus() != ResultStatus.COMPLETE)) {
				Thread.sleep(3000);
				this.result = ((QueryResourceImplementationInterface) resource
						.getImplementingInterface())
						.getResults(session, result);
			}
			
			if(this.result.getResultStatus() == ResultStatus.COMPLETE) {
				if(((Persistable) result.getData()).isPersisted()) {
					((Persistable) result.getData()).merge();
				} else {
					((Persistable) result.getData()).persist();
				}
				
			}
			
			result.getData().close();
		} catch (Exception e) {
			this.result.setResultStatus(ResultStatus.ERROR);
			this.result.setMessage(e.getMessage());
		}

		result.setEndTime(new Date());
		// Save the query Action
		try {
			ActionUtilities.mergeResult(result);
			this.status = ActionStatus.COMPLETE;
		} catch (NamingException e) {
			result.setMessage(e.getMessage());
			this.status = ActionStatus.ERROR;
		}

		return this.result;
	}

	/**
	 * Returns the query
	 * 
	 * @return Query
	 */
	public Query getQuery() {
		return this.query;

	}

	/**
	 * Sets the query
	 * 
	 * @param query
	 *            Query
	 */
	public void setQuery(Query query) {
		this.query = query;
	}

	@Override
	public ActionStatus getStatus() {
		return status;
	}
}
