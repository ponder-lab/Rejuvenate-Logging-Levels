/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.controller;

import java.util.Arrays;
import java.util.Map;

import javax.ejb.Stateful;
import javax.inject.Inject;

import edu.harvard.hms.dbmi.bd2k.irct.IRCTApplication;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ClauseIsNotTheCorrectType;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ClauseNotFoundException;
import edu.harvard.hms.dbmi.bd2k.irct.exception.JoinTypeNotSupported;
import edu.harvard.hms.dbmi.bd2k.irct.exception.LogicalOperatorNotFound;
import edu.harvard.hms.dbmi.bd2k.irct.exception.PredicateTypeNotSupported;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceNotFoundException;
import edu.harvard.hms.dbmi.bd2k.irct.exception.SubQueryNotFoundException;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Path;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.ClauseAbstract;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.JoinClause;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.JoinType;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.LogicalOperator;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.PredicateType;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.SelectClause;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.SubQuery;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.WhereClause;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;

/**
 * This a stateful controller that creates queries and ensures that the
 * predicates, selects, and other attributes added to them are created
 * correctly.
 * 
 * NOTE: NOT ALL CHECKS ARE IMPLEMENTED 
 * NOTE: NOT ALL METHODS HAVE BEEN FULLY IMPLEMENTED AND VETTED
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@Stateful
public class QueryController {

	@Inject
	private IRCTApplication irctApp;

	private Long lastId;
	private Query query;

	/**
	 * Initiates the creation of a query
	 * 
	 * @param conversationId Conversation id
	 * 
	 */
	public void createQuery(String conversationId) {
		this.query = new Query();
		this.query.setId(Long.parseLong(conversationId));
		lastId = 0L;
	}

	/**
	 * Initiates the creation of a subQuery
	 *
	 * @param resources
	 *            An array of resources
	 * @return The subquery id
	 * @throws ResourceNotFoundException Resource not found exception
	 */
	public Long createSubQuery(Resource... resources)
			throws ResourceNotFoundException {
		SubQuery sq = new SubQuery();
		sq.setResources(Arrays.asList(resources));
		sq.setId(lastId++);
		sq.setParent(query);
		query.addSubQuery(sq.getId(), sq);

		return sq.getId();
	}

	/**
	 * Adds a select clause to a subQuery
	 * 
	 * 
	 * @param sqId
	 *            The subQuery Id
	 * @param parameter
	 *            A parameters
	 * @param resource Resouce
	 * @return The select clause id
	 * @throws SubQueryNotFoundException SubQuery not found
	 */
	public Long addSelectClause(Long sqId, Path parameter, Resource resource)
			throws SubQueryNotFoundException {
		SelectClause sc = new SelectClause(lastId++);
		sc.setParameters(parameter);
		if (sqId != null) {
			SubQuery sq = findSubQuery(sqId);
			sq.addClause(sc.getId(), sc);
		}
		query.getResources().add(resource);
		query.getClauses().put(sc.getId(), sc);
		return sc.getId();
	}

	/**
	 * Adds a join clause between to different subqueries
	 *
	 * NOTE: NOT CURRENTLY TESTED OR FULLY IMPEMENTED
	 *
	 * 
	 * @param sqId1 SubQuery 1
	 * @param sqId2 SubQuery 2
	 * @param joinType Join Type
	 * @param fieldId1 Field 1
	 * @param fieldId2 Field 2
	 * @param relationship Relationship
	 * @param joinId Join Id
	 * @return Id
	 * @throws ClauseNotFoundException Clause not found
	 * @throws ClauseIsNotTheCorrectType Clause is not correct
	 * @throws SubQueryNotFoundException Sub query not found
	 * @throws JoinTypeNotSupported Join type not supported
	 */
	public Long addJoinClause(Long sqId1, Long sqId2, String joinType,
			Path fieldId1, Path fieldId2, String relationship, Long joinId)
			throws ClauseNotFoundException, ClauseIsNotTheCorrectType,
			SubQueryNotFoundException, JoinTypeNotSupported {
		if (joinId != null) {
			hasClause(joinId, JoinClause.class);
		} else {
			joinId = lastId++;
		}
		SubQuery sq1 = findSubQuery(sqId1);
		SubQuery sq2 = findSubQuery(sqId2);
		JoinType jt = findJoinType(joinType);

		// Has subqueries
//		checkSubQueryJoinSupport(sq1, jt, fieldId1, relationship);
//		checkSubQueryJoinSupport(sq2, jt, fieldId2, relationship);

		JoinClause jc = new JoinClause();
		jc.setId(joinId);

		jc.setSubQuery1(sq1);
		jc.setSubQuery2(sq2);
		jc.setJoinType(jt);
		jc.setField1(fieldId1);
		jc.setField2(fieldId2);
		jc.setRelationship(relationship);

		query.addClause(joinId, jc);
		return joinId;
	}

	/**
	 * Adds a where clause to a given query or subquery
	 * 
	 * @param sqId
	 *            Subquery Id if applicable
	 * @param logicalOperator
	 *            Logical Operator
	 * @param field
	 *            Field
	 * @param predicateName
	 *            Name of the predicate
	 * @param values
	 *            A map of values
	 * @param whereId
	 *            If replacing an existing one
	 * @param resource
	 *            Resource to run against
	 * @return The where clause id
	 * @throws ClauseNotFoundException
	 *             Clause not found
	 * @throws ClauseIsNotTheCorrectType
	 *             Clause is not of the correct type
	 * @throws SubQueryNotFoundException
	 *             Could not find the subQuery requested
	 * @throws LogicalOperatorNotFound
	 *             Logical Operator not found
	 * @throws PredicateTypeNotSupported
	 *             Predicate type is not supported by the resource
	 */
	public Long addWhereClause(Long sqId, String logicalOperator, Path field,
			String predicateName, Map<String, String> values, Long whereId,
			Resource resource) throws ClauseNotFoundException,
			ClauseIsNotTheCorrectType, SubQueryNotFoundException,
			LogicalOperatorNotFound, PredicateTypeNotSupported {
		if (whereId != null) {
			hasClause(whereId, WhereClause.class);
		} else {
			whereId = lastId++;
		}

		WhereClause wc = new WhereClause();
		wc.setId(whereId);

		LogicalOperator lo = findLogicalOperator(logicalOperator);
		if (predicateName != null) {
			wc.setPredicateType(findPredicateType(resource, predicateName));
		}

		wc.setLogicalOperator(lo);
		wc.setField(field);
		wc.setValues(values);

		if (sqId != null) {
			SubQuery whereQuery = findSubQuery(sqId);
			// checkSubQueryPredicateSupport(whereQuery, lo, field, predicate,
			// value,
			// additionalValue);
			wc.setSubQuery(whereQuery);
			whereQuery.getResources().add(resource);
			whereQuery.getClauses().put(whereId, wc);
		} else {
			query.getResources().add(resource);
			query.getClauses().put(whereId, wc);
		}

		return wc.getId();
	}

	/**
	 * Cancels the query
	 * 
	 */
	public void cancelQuery() {
		this.query = null;
		this.lastId = 0L;
	}

	/**
	 * Deletes the last clause
	 * 
	 * @param clauseId
	 *            Clause Id
	 * @throws ClauseNotFoundException
	 *             Clause not found
	 */
	public void deleteClause(Long clauseId) throws ClauseNotFoundException {
		if (query.getClauses().containsKey(clauseId)) {
			query.removeClause(clauseId);
		} else if (query.getSubQueries().containsKey(clauseId)) {
			query.removeSubQuery(clauseId);
		} else {
			throw new ClauseNotFoundException(clauseId);
		}
	}

	/**
	 * Returns the query
	 * 
	 * @return Query
	 */
	public Query getQuery() {
		return query;
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

	/**
	 * Returns the subquery
	 * 
	 * @param sqId
	 *            The subquery id
	 * @return The Subquery
	 * @throws SubQueryNotFoundException
	 *             No subQuery found
	 */
	public SubQuery findSubQuery(Long sqId) throws SubQueryNotFoundException {
		SubQuery sq = query.getSubQueries().get(sqId);
		if (sq == null) {
			throw new SubQueryNotFoundException(sqId);
		}

		return sq;
	}

	// Utility Methods
	private LogicalOperator findLogicalOperator(String logicalOperator)
			throws LogicalOperatorNotFound {
		LogicalOperator lo;
		try {
			lo = LogicalOperator.valueOf(logicalOperator);
		} catch (IllegalArgumentException e) {
			throw new LogicalOperatorNotFound(logicalOperator);
		}

		return lo;
	}

//	private void checkSubQueryJoinSupport(SubQuery sq, JoinType joinType,
//			Path field, String relationship) throws SubQueryNotFoundException,
//			JoinTypeNotSupported {
//
//		boolean supported = false;
//		List<Resource> resources = sq.getResources();
//		if (resources.isEmpty()) {
//			resources = sq.getParent().getResources();
//		}
//
//		for (Resource resource : resources) {
//			if (resource.getSupportedJoins().contains(joinType)) {
//
//				if (joinType.isRequireFields()) {
//					if (joinType.supportsDataType(field.getDataType())) {
//						supported = true;
//					}
//				} else {
//					supported = true;
//				}
//
//				if (joinType.isRequireRelationships() && relationship == null) {
//					supported = false;
//				}
//
//				if (supported) {
//					break;
//				}
//			}
//		}
//
//		if (!supported) {
//			throw new JoinTypeNotSupported(joinType.getName());
//		}
//
//	}

	// private void checkSubQueryPredicateSupport(SubQuery sq, LogicalOperator
	// lo,
	// Path field, PredicateType predicate, String value,
	// String additionalValue) throws PredicateTypeNotSupported {
	// boolean supported = false;
	// List<Resource> resources = sq.getResources();
	// if (resources.isEmpty()) {
	// resources = sq.getParent().getResources();
	// }

	// for (Resource resource : resources) {
	// if (resource.getSupportedPredicates().contains(predicate)) {
	// if (predicate.isRequiresValue()) {
	// if (predicate.isRequiresAdditionalValue()) {
	// if (predicate.supportsDataType(field.getDataType())) {
	// supported = true;
	// }
	// }
	// } else {
	// supported = true;
	// }

	// }
	// }

	// if (!supported) {
	// throw new PredicateTypeNotSupported(predicate.getName());
	// }

	// }

	private JoinType findJoinType(String joinType) throws JoinTypeNotSupported {
		JoinType jt = irctApp.getSupportedJoinTypes().get(joinType);
		if (jt == null) {
			throw new JoinTypeNotSupported(joinType);
		}
		return jt;
	}

	private PredicateType findPredicateType(Resource resource,
			String predicateName) throws PredicateTypeNotSupported {

		for (PredicateType predicateType : resource.getSupportedPredicates()) {
			if (predicateType.getName().equals(predicateName)) {
				return predicateType;
			}
		}

		return null;
	}

	private boolean hasClause(Long clauseId,
			Class<? extends ClauseAbstract> clauseType)
			throws ClauseNotFoundException, ClauseIsNotTheCorrectType {
		ClauseAbstract clause = query.getClauses().get(clauseId);
		if (clause == null) {
			throw new ClauseNotFoundException(clauseId);
		}
		if (!clauseType.isInstance(clause)) {
			throw new ClauseIsNotTheCorrectType(clauseId);
		}
		return true;
	}

	// private ClauseAbstract findClause(Long clauseId,
	// Class<ClauseAbstract> clauseType) throws ClauseIsNotTheCorrectType,
	// ClauseNotFoundException {
	// ClauseAbstract clause = query.getClauses().get(clauseId);
	// if (clause == null) {
	// throw new ClauseNotFoundException(clauseId);
	// }
	// if (!clauseType.isInstance(clause)) {
	// throw new ClauseIsNotTheCorrectType(clauseId);
	// }
	//
	// return clause;
	// }
}
