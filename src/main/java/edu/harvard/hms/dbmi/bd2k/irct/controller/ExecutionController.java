/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.controller;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import edu.harvard.hms.dbmi.bd2k.irct.action.JoinAction;
import edu.harvard.hms.dbmi.bd2k.irct.action.ProcessAction;
import edu.harvard.hms.dbmi.bd2k.irct.action.QueryAction;
import edu.harvard.hms.dbmi.bd2k.irct.executable.ExecutableChildNode;
import edu.harvard.hms.dbmi.bd2k.irct.executable.ExecutionPlan;
import edu.harvard.hms.dbmi.bd2k.irct.join.JoinImplementation;
import edu.harvard.hms.dbmi.bd2k.irct.model.process.IRCTProcess;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Persistable;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultSet;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultStatus;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;

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

	@PersistenceContext
	EntityManager entityManager;

	@Resource
	private ManagedExecutorService mes;

	/**
	 * Runs the process
	 * 
	 * @param process
	 *            Process to run
	 * @return result id
	 * @throws PersistableException
	 *             Persistable exception occurred
	 */
	public Long runProcess(IRCTProcess process, SecureSession secureSession) throws PersistableException {
		log.info("Start: " + process.getId());
		Result newResult = new Result();


		newResult.setResultStatus(ResultStatus.RUNNING);
		entityManager.persist(newResult);

		ProcessAction pa = new ProcessAction();
		pa.setup(process.getResource(), process);
		
		ExecutableChildNode eln = new ExecutableChildNode();
		eln.setAction(pa);
		
		ExecutionPlan exp = new ExecutionPlan();
		exp.setup(eln, secureSession);

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
	public Long runQuery(Query query, SecureSession secureSession) throws PersistableException {
		log.info("Start: " + query.getId());
		Result newResult = new Result();

		newResult.setResultStatus(ResultStatus.RUNNING);
		entityManager.persist(newResult);

		QueryAction qa = new QueryAction();
		qa.setup(query.getResources().get(0), query);

		ExecutableChildNode eln = new ExecutableChildNode();
		eln.setAction(qa);
		
		ExecutionPlan exp = new ExecutionPlan();
		exp.setup(eln, secureSession);

		runExecutionPlan(exp, newResult);

		log.info("Stop: " + query.getId());

		return newResult.getId();
	}

	/**
	 * Run a join by creating an execution plan
	 * 
	 * @param joinAction
	 *            Join to run
	 * @return Result Id
	 * @throws PersistableException
	 *             An error occurred
	 */
	public Long runJoin(JoinImplementation joinAction, SecureSession secureSession) throws PersistableException {
		log.info("Starting: " + joinAction.getType());
		Result newResult = new Result();
		newResult.setResultStatus(ResultStatus.RUNNING);

		JoinAction ja = new JoinAction();
		ja.setup(joinAction);

		ExecutableChildNode eln = new ExecutableChildNode();
		eln.setAction(ja);
		
		ExecutionPlan exp = new ExecutionPlan();
		exp.setup(eln, secureSession);
		runExecutionPlan(exp, newResult);

		log.info("Stop: " + joinAction.getType());
		return newResult.getId();

	}

	/**
	 * Runs an execution plan
	 * 
	 * @param executionPlan
	 *            Execution Plan
	 * @param result
	 *            Result
	 * @throws PersistableException
	 *             A persistable exception occurred
	 */
	@Asynchronous
	public void runExecutionPlan(final ExecutionPlan executionPlan,
			final Result result) throws PersistableException {

		Callable<Result> runPlan = new Callable<Result>() {
			@Override
			public Result call() throws Exception {
				result.setRunTime(new Date());
				executionPlan.run();

				ResultSet rs = executionPlan.getResults().getResultSet();
				if (rs != null) {
					((Persistable) rs).persist("" + result.getId());
					result.setResultSetLocation("" + result.getId());
					result.setResultSet(rs);
					result.setResultStatus(ResultStatus.AVAILABLE);
				} else {
					result.setResultStatus(ResultStatus.ERROR);
				}

				UserTransaction userTransaction = lookup();
				userTransaction.begin();
				entityManager.merge(result);
				userTransaction.commit();

				return result;
			}
		};

		mes.submit(runPlan);

	}

	private UserTransaction lookup() throws NamingException {
		InitialContext ic = new InitialContext();
		return (UserTransaction) ic.lookup("java:comp/UserTransaction");
	}
}
