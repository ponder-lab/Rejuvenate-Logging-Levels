/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.controller;

import java.util.Date;
import java.util.logging.Logger;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import edu.harvard.hms.dbmi.bd2k.irct.action.ExecutionPlan;
import edu.harvard.hms.dbmi.bd2k.irct.action.ProcessExecutable;
import edu.harvard.hms.dbmi.bd2k.irct.action.QueryExecutable;
import edu.harvard.hms.dbmi.bd2k.irct.action.process.ExecuteProcess;
import edu.harvard.hms.dbmi.bd2k.irct.action.query.ExecuteQuery;
import edu.harvard.hms.dbmi.bd2k.irct.model.process.IRCTProcess;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Persistable;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultSet;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultStatus;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;

/**
 * The execution controller is a stateless controller that manages the
 * executions of different processes, queries, and joins by creating an
 * execution plan and running it.
 * 
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@Stateless
public class ExecutionController {

	@Inject
	Logger log;

	@Inject
	private EntityManagerFactory objectEntityManager;
	
	/**
	 * Runs the process
	 * 
	 * @param process Process to run
	 * @return result id
	 * @throws PersistableException
	 */
	public Long runProcess(IRCTProcess process) throws PersistableException {
		log.info("Start: " + process.getId());
		Result newResult = new Result();
		
		EntityManager oem = objectEntityManager.createEntityManager();
		oem.persist(newResult);
		
		ExecuteProcess ep = new ExecuteProcess();
		ep.setup(process.getResource(), process);
		
		ProcessExecutable pe = new ProcessExecutable();
		pe.setup(ep);

		ExecutionPlan exp = new ExecutionPlan();
		exp.setup(pe);
		
		runExecutionPlan(exp, newResult);

		log.info("Stop: " + process.getId());

		return newResult.getId();
	}

	/**
	 * Run a query by creating an execution plan
	 * 
	 * @param query
	 *            Query
	 * @return Result Id
	 * @throws PersistableException
	 *             An error occurred
	 */
	public Long runQuery(Query query) throws PersistableException {
		log.info("Start: " + query.getId());
		Result newResult = new Result();

		EntityManager oem = objectEntityManager.createEntityManager();
		oem.persist(newResult);

		ExecuteQuery eq = new ExecuteQuery();
		eq.setup(query.getResources().get(0), query);

		QueryExecutable qe = new QueryExecutable();
		qe.setup(eq);

		ExecutionPlan ep = new ExecutionPlan();
		ep.setup(qe);

		runExecutionPlan(ep, newResult);

		log.info("Stop: " + query.getId());

		return newResult.getId();
	}

	/**
	 * 
	 * @param executionPlan
	 * @param result
	 * @throws PersistableException
	 */
	@Asynchronous
	public void runExecutionPlan(ExecutionPlan executionPlan, Result result)
			throws PersistableException {

		result.setRunTime(new Date());
		executionPlan.run();

		ResultSet rs = executionPlan.getResults();
		if(rs != null) {
			((Persistable) rs).persist("" + result.getId());
		} else {
			
		}
		result.setResultSetLocation("" + result.getId());
		result.setImplementingResultSet(rs);
		result.setResultStatus(ResultStatus.Available);

	}
}
