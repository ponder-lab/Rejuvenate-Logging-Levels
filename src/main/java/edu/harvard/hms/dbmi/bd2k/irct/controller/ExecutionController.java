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
package edu.harvard.hms.dbmi.bd2k.irct.controller;

import java.util.Date;
import java.util.logging.Logger;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import edu.harvard.hms.dbmi.bd2k.irct.action.ExecutionPlan;
import edu.harvard.hms.dbmi.bd2k.irct.action.QueryExecutable;
import edu.harvard.hms.dbmi.bd2k.irct.action.query.ExecuteQuery;
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

		// ep.run();
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
		((Persistable) rs).persist("" + result.getId());
		result.setResultSetLocation("" + result.getId());
		result.setImplementingResultSet(rs);
		result.setResultStatus(ResultStatus.Available);

	}
}
