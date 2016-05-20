/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import edu.harvard.hms.dbmi.bd2k.irct.IRCTApplication;
import edu.harvard.hms.dbmi.bd2k.irct.dataconverter.ResultDataConverter;
import edu.harvard.hms.dbmi.bd2k.irct.dataconverter.ResultDataStream;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.DataConverterImplementation;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultStatus;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular.FileResultSet;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.User;

/**
 * A stateless controller for retrieving the status of a result as well as the
 * result themselves.
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
	 * Returns a list of results that are available for the user to download
	 * 
	 * @param user
	 *            User
	 * @return List of Resutls
	 */
	public List<Result> getAvailableResults(User user) {
		EntityManager oem = objectEntityManager.createEntityManager();
		CriteriaBuilder cb = oem.getCriteriaBuilder();

		CriteriaQuery<Result> criteria = cb.createQuery(Result.class);
		Root<Result> result = criteria.from(Result.class);
		criteria.select(result);

		Predicate restrictions = cb.conjunction();
		restrictions = cb.and(restrictions, cb.isNull(result.get("user")));
		restrictions = cb.or(restrictions, cb.equal(result.get("user"), user));
		restrictions = cb.and(restrictions,
				cb.equal(result.get("resultStatus"), ResultStatus.AVAILABLE));
		criteria.where(restrictions);

		return oem.createQuery(criteria).getResultList();
	}

	/**
	 * Returns the status of a result if the user has access to it
	 * 
	 * @param user
	 *            User
	 * @param resultId
	 *            Result Id
	 * @return Status of the result
	 */
	public ResultStatus getResultStatus(User user, Long resultId) {
		List<Result> results = getResults(user, resultId);
		if ((results == null) || (results.isEmpty())) {
			return null;
		}
		return results.get(0).getResultStatus();
	}

	/**
	 * Returns a list of available formats that the user can use to download a
	 * given result if they have access to it
	 * 
	 * @param user
	 *            User
	 * @param resultId
	 *            Result Id
	 * @return Available Formats
	 */
	public List<String> getAvailableFormats(User user, Long resultId) {
		List<Result> results = getResults(user, resultId);

		if (results == null) {
			return null;
		}
		List<DataConverterImplementation> rdc = irctApp
				.getResultDataConverters().get(results.get(0).getDataType());

		List<String> converterNames = new ArrayList<String>();
		for (DataConverterImplementation rd : rdc) {
			converterNames.add(rd.getFormat());
		}
		return converterNames;
	}

	/**
	 * Returns a datastream object for the given format and result if the user
	 * has access to the result, and the format is of the correct type
	 * 
	 * @param user
	 *            User
	 * @param resultId
	 *            Result Id
	 * @param format
	 *            Form
	 * @return Result Data Stream
	 */
	public ResultDataStream getResultDataStream(User user, Long resultId,
			String format) {
		ResultDataStream rds = new ResultDataStream();
		List<Result> results = getResults(user, resultId);

		if (results == null) {
			rds.setMessage("Unable to find result");
			return rds;
		}
		Result result = results.get(0);
		ResultDataConverter rdc = irctApp.getResultDataConverter(
				result.getDataType(), format);
		if (rdc == null) {
			rds.setMessage("Unable to find format");
			return rds;
		}

		rds.setMediaType(rdc.getMediaType());
		rds.setResult(rdc.createStream(result));
		rds.setFileExtension(rdc.getFileExtension());

		return rds;
	}

	/**
	 * Returns the results if the user has access to it
	 * 
	 * @param user
	 *            user
	 * @param resultId
	 *            Result Id
	 * @return Result
	 */
	public Result getResult(User user, Long resultId) {
		List<Result> results = getResults(user, resultId);

		if ((results == null) || (results.isEmpty())) {
			return null;
		}

		return results.get(0);
	}

	private List<Result> getResults(User user, Long resultId) {
		EntityManager oem = objectEntityManager.createEntityManager();
		CriteriaBuilder cb = oem.getCriteriaBuilder();

		CriteriaQuery<Result> criteria = cb.createQuery(Result.class);
		Root<Result> resultRoot = criteria.from(Result.class);
		criteria.select(resultRoot);

		Predicate restrictions = cb.conjunction();
		restrictions = cb.and(restrictions, cb.isNull(resultRoot.get("user")));
		restrictions = cb.or(restrictions,
				cb.equal(resultRoot.get("user"), user));
		restrictions = cb.and(restrictions,
				cb.equal(resultRoot.get("id"), resultId));
		criteria.where(restrictions);

		List<Result> results = oem.createQuery(criteria).getResultList();

		if ((results == null) || (results.isEmpty())) {
			return null;
		}
		return results;
	}

	/**
	 * Creates a new result that is associated with a default Result Data Type
	 * 
	 * @param resultDataType
	 *            Data Type
	 * @return Result
	 * @throws PersistableException
	 *             An error occurred creating the Result
	 */
	public Result createResult(ResultDataType resultDataType)
			throws PersistableException {
		EntityManager oem = objectEntityManager.createEntityManager();
		Result result = new Result();
		oem.persist(result);
		result.setDataType(resultDataType);
		result.setStartTime(new Date());
		if (resultDataType == ResultDataType.TABULAR) {
			FileResultSet frs = new FileResultSet();
			frs.persist(irctApp.getProperties().getProperty("ResultDataFolder")
					+ "/" + result.getId());
			result.setResultSetLocation(irctApp.getProperties().getProperty(
					"ResultDataFolder")
					+ "/" + result.getId());
			result.setData(frs);
		} else if (resultDataType == ResultDataType.JSON) {

		} else {
			result.setResultStatus(ResultStatus.ERROR);
			result.setMessage("Unknown Result Data Type");
			return result;
		}
		result.setResultStatus(ResultStatus.CREATED);
		oem.merge(result);
		return result;
	}

	/**
	 * Updates the given result with the new information
	 * 
	 * @param result
	 *            Result
	 */
	public void mergeResult(Result result) {
		EntityManager oem = objectEntityManager.createEntityManager();
		oem.merge(result);
	}

}
