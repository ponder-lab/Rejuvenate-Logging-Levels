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
import edu.harvard.hms.dbmi.bd2k.irct.model.result.PersistableException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultSet;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.ResultSetException;

@Stateless
public class ResultController {
	@Inject
	private EntityManagerFactory objectEntityManager;

	public List<Result> availableResults() {
		EntityManager oem = objectEntityManager.createEntityManager();
		CriteriaBuilder cb = oem.getCriteriaBuilder();
		CriteriaQuery<Result> criteria = cb.createQuery(Result.class);
		Root<Result> load = criteria.from(Result.class);
		criteria.select(load);
		return oem.createQuery(criteria).getResultList();
	}

	public Result getResult(Long id) {
		EntityManager oem = objectEntityManager.createEntityManager();
		return oem.find(Result.class, id);

	}

	public ResultSet getResultSet(Long id) throws ResultSetException,
			PersistableException {
		 EntityManager oem = objectEntityManager.createEntityManager();
		 Result result = oem.find(Result.class, id);

		 ResultSet rs = result.getImplementingResultSet();
		 ((Persistable) rs).load(result.getResultSetLocation());

		return rs;
	}
}
