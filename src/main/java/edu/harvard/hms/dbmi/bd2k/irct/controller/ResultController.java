/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.controller;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import edu.harvard.hms.dbmi.bd2k.irct.model.result.Persistable;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultSet;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.ResultSetException;

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

		ResultSet rs = result.getImplementingResultSet();
		((Persistable) rs).load(result.getResultSetLocation());

		return rs;
	}
}
