package edu.harvard.hms.dbmi.bd2k.irct.controller;

import java.util.List;

import javax.ejb.Stateful;
import javax.inject.Inject;

import edu.harvard.hms.dbmi.bd2k.irct.IRCTApplication;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ClauseIsNotTheCorrectType;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ClauseNotFoundException;
import edu.harvard.hms.dbmi.bd2k.irct.exception.JoinTypeNotSupported;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceNotFoundException;
import edu.harvard.hms.dbmi.bd2k.irct.exception.SubQueryNotFoundException;
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

@Stateful
public class QueryController {

	@Inject
	private IRCTApplication ipctApp;

	private Long lastId;
	private Query query;

	/**
	 * Initiates the creation of a query
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
	 *            A list of resources
	 * @return The subquery id
	 * @throws ResourceNotFoundException
	 */
	public Long createSubQuery(String... resources)
			throws ResourceNotFoundException {
		SubQuery sq = new SubQuery();
		for (String resourceName : resources) {
			sq.addResource(findResource(resourceName));
		}

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
	 * @param parameters
	 *            An array of parameters
	 * @return The select clause id
	 * @throws SubQueryNotFoundException
	 */
//	public Long addSelectClause(Long sqId, String... parameters)
//			throws SubQueryNotFoundException {
//		SelectClause sc = new SelectClause(lastId++);
//		for (String parameter : parameters) {
//			sc.addParameter(parameter);
//		}
//		if (sqId != null) {
//			SubQuery sq = findSubQuery(sqId);
//			sq.addClause(sc.getId(), sc);
//		}
//		return sc.getId();
//	}

//	public Long addJoinClause(Long sqId1, Long sqId2, String joinType,
//			Long fieldId1, Long fieldId2, String relationship, Long joinId)
//			throws ClauseNotFoundException, ClauseIsNotTheCorrectType,
//			SubQueryNotFoundException, JoinTypeNotSupported {
//		if (joinId != null) {
//			hasClause(joinId, JoinClause.class);
//		} else {
//			joinId = lastId++;
//		}
//		SubQuery sq1 = findSubQuery(sqId1);
//		SubQuery sq2 = findSubQuery(sqId2);
//		JoinType jt = findJoinType(joinType);
//
//		
//
//		// Has subqueries
//		checkSubQueryJoinSupport(sq1, jt, fieldId1, relationship);
//		checkSubQueryJoinSupport(sq2, jt, fieldId2, relationship);
//
//		JoinClause jc = new JoinClause();
//		jc.setId(joinId);
//		
//		jc.setSubQuery1(sq1);
//		jc.setSubQuery2(sq2);
//		jc.setJoinType(jt);
//		jc.setFieldId1(fieldId1);
//		jc.setFieldId2(fieldId2);
//		jc.setRelationship(relationship);
//
//		query.addClause(joinId, jc);
//		return joinId;
//	}

//	public Long addWhereClause(Long sqId, String logicalOperator, Long fieldId,
//			String predicateName, String value, String additionalValue,
//			Long whereId) throws ClauseNotFoundException,
//			ClauseIsNotTheCorrectType, SubQueryNotFoundException,
//			LogicalOperatorNotFound, PredicateTypeNotSupported {
//		if (whereId != null) {
//			hasClause(whereId, WhereClause.class);
//		} else {
//			whereId = lastId++;
//		}
//		SubQuery sq = findSubQuery(sqId);
//		LogicalOperator lo = findLogicalOperator(logicalOperator);
//		PredicateType predicate = findPredicateType(predicateName);
//
//		checkSubQueryPredicateSupport(sq, lo, fieldId, predicate, value, additionalValue);
//		
//		WhereClause wc = new WhereClause();
//		wc.setId(whereId);
//		
//		wc.setSubQuery(sq);
//		wc.setLogicalOperator(lo);
//		wc.setFieldId(fieldId);
//		wc.setPredicateType(predicate);
//		wc.setValue(value);
//		wc.setAdditionalValue(additionalValue);
//
//		return null;
//	}

//	public void cancelQuery() {
//		this.query = null;
//		this.lastId = 0L;
//	}

//	public void deleteClause(Long clauseId) throws ClauseNotFoundException {
//		if (query.getClauses().containsKey(clauseId)) {
//			query.removeClause(clauseId);
//		} else if (query.getSubQueries().containsKey(clauseId)) {
//			query.removeSubQuery(clauseId);
//		} else {
//			throw new ClauseNotFoundException(clauseId);
//		}
//	}

	public Query getQuery() {
		return query;
	}

	public void setQuery(Query query) {
		this.query = query;
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

	private Resource findResource(String resourceName)
			throws ResourceNotFoundException {
		if (!ipctApp.doesResourceExist(resourceName)) {
			throw new ResourceNotFoundException(resourceName);
		}
		return ipctApp.getResources().get(resourceName);
	}

//	private void checkSubQueryJoinSupport(SubQuery sq, JoinType joinType,
//			Long fieldId, String relationship)
//			throws SubQueryNotFoundException, JoinTypeNotSupported {
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
//					Field field = resource.getImplementingInterface().getField(
//							fieldId);
//
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

//	private void checkSubQueryPredicateSupport(SubQuery sq, LogicalOperator lo,
//			Long fieldId, PredicateType predicate, String value, String additionalValue)
//			throws PredicateTypeNotSupported {
//		boolean supported = false;
//		List<ResourceAbstract> resources = sq.getResources();
//		if (resources.isEmpty()) {
//			resources = sq.getParent().getResources();
//		}
//
//		for (Resource resource : resources) {
//			if(resource.getSupportedPredicates().contains(predicate)) {
//				if(predicate.isRequiresValue()) {
//					if(predicate.isRequiresAdditionalValue()) {
//						Field field = resource.getImplementingInterface().getField(fieldId);
//						if(predicate.supportsDataType(field.getDataType())) {
//							supported = true;
//						}
//					}
//				} else {
//					supported = true;
//				}
//				
//			}
//		}
//		
//
//		if (!supported) {
//			throw new PredicateTypeNotSupported(predicate.getName());
//		}
//
//	}

	private JoinType findJoinType(String joinType) throws JoinTypeNotSupported {
		JoinType jt = ipctApp.getSupportedJoinTypes().get(joinType);
		if (jt == null) {
			throw new JoinTypeNotSupported(joinType);
		}
		return jt;
	}

//	private PredicateType findPredicateType(String predicateName)
//			throws PredicateTypeNotSupported {
//		PredicateType pt = ipctApp.getSupportedPredicateTypes().get(
//				predicateName);
//		if (pt == null) {
//			throw new PredicateTypeNotSupported(predicateName);
//		}
//		return null;
//	}

	private SubQuery findSubQuery(Long sqId) throws SubQueryNotFoundException {
		SubQuery sq = query.getSubQueries().get(sqId);
		if (sq == null) {
			throw new SubQueryNotFoundException(sqId);
		}

		return sq;
	}

	private boolean hasClause(Long clauseId, Class<? extends ClauseAbstract> clauseType)
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

	// private Clause findClause(Long clauseId, Class<Clause> clauseType)
	// throws ClauseIsNotTheCorrectType, ClauseNotFoundException {
	// Clause clause = query.getClauses().get(clauseId);
	// if (clause == null) {
	// throw new ClauseNotFoundException(clauseId);
	// }
	// if (!clauseType.isInstance(clause)) {
	// throw new ClauseIsNotTheCorrectType(clauseId);
	// }
	//
	// return clause;
	// }

	// /joinClause
	// Adds a join between two subqueries, or two parts of a query. Joins are
	// expected to perform in the order that they are entered. Joins can updated
	// at any time.
	// /selectClause
	// Selects which parameters to return from the completed query. The default
	// setting is to return a count of results from the query. If the select has
	// already been set then callign this function again in the same query or
	// subquery or replace it. This can be added to a subQuery or a full query.
	// /whereClause
	// This call adds predicates that will be be used as a filter for the query
	// or subquery. This can be called multiple times for each query allowing
	// the user to filter on many different fields.
	// /deleteClause
	// Deletes a clause in a query or subQuery. Deleting a clause deletes all
	// its children. A clause can be any where, select, join, or subQuery.
	// /runQuery
	// Runs the query, but does not return the results.
	// /cancelQuery
	// Cancels and deletes the query. If the query is running it attempts to
	// cancel it.
	// /getResults

}
