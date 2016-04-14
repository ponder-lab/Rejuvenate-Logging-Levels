/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.controller;

import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import edu.harvard.hms.dbmi.bd2k.irct.IRCTApplication;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Persistable;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultStatus;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.ResultSetException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular.FileResultSet;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular.ResultSet;

/**
 * A stateless controller for retrieving available results as well as individual
 * results.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@Stateless
public class ResultController {
	@Inject
	private EntityManagerFactory objectEntityManager;

	@Inject
	private IRCTApplication irctApp;
	/**
	 * Returns a list of available results
	 * 
	 * @return Available results
	 */
	public List<Result> availableResults() {
		EntityManager oem = objectEntityManager.createEntityManager();
		CriteriaBuilder cb = oem.getCriteriaBuilder();
		CriteriaQuery<Result> criteria = cb.createQuery(Result.class);
		Root<Result> load = criteria.from(Result.class);
		criteria.select(load);
		return oem.createQuery(criteria).getResultList();
	}

	/**
	 * Returns a result from the entity manager
	 * 
	 * @param id The result id
	 * @return Result
	 */
	public Result getResult(Long id) {
		EntityManager oem = objectEntityManager.createEntityManager();
		return oem.find(Result.class, id);

	}

	/**
	 * Gets a result set from the entity manager
	 * 
	 * @param id Result Id
	 * @return Result Set
	 * @throws ResultSetException An error occurred in the result set
	 * @throws PersistableException An error occurred loading the resultset
	 */
	public ResultSet getResultSet(Long id) throws ResultSetException,
			PersistableException {
		EntityManager oem = objectEntityManager.createEntityManager();
		Result result = oem.find(Result.class, id);
		
		if(result.getResultStatus() == ResultStatus.RUNNING) {
			throw new ResultSetException("Result set is still running");
		} else if(result.getResultStatus() != ResultStatus.AVAILABLE) {
			throw new ResultSetException("Result set is not available");
		} else {
			Persistable rs = (Persistable) result.getData();
			rs.load(result.getResultSetLocation());
			return (ResultSet) rs;
		}
	}
	
	
	public Result createResult(ResultDataType resultDataType) throws PersistableException {
		EntityManager oem = objectEntityManager.createEntityManager();
		Result result = new Result();
		oem.persist(result);
		result.setDataType(resultDataType);
		result.setStartTime(new Date());
		if(resultDataType == ResultDataType.TABULAR) {
			FileResultSet frs = new FileResultSet();
			frs.persist(irctApp.getProperties().getProperty("ResultDataFolder") + "/" + result.getId());
			result.setResultSetLocation(irctApp.getProperties().getProperty("ResultDataFolder") + "/" +  result.getId());
			result.setData(frs);
		}
		result.setResultStatus(ResultStatus.CREATED);
		oem.merge(result);
		return result;
	}
	
	public void mergeResult(Result result) {
		EntityManager oem = objectEntityManager.createEntityManager();
		oem.merge(result);
	}
}
