package edu.harvard.hms.dbmi.bd2k.irct.controller;

import java.util.Arrays;
import java.util.List;

import javax.ejb.Stateful;
import javax.inject.Inject;

import edu.harvard.hms.dbmi.bd2k.irct.IRCTApplication;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ClauseIsNotTheCorrectType;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ClauseNotFoundException;
import edu.harvard.hms.dbmi.bd2k.irct.exception.JoinTypeNotSupported;
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

@Stateful
public class QueryController {

	@Inject
	private IRCTApplication irctApp;

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
	 *            An array of resources
	 * @return The subquery id
	 * @throws ResourceNotFoundException
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
	 * @param parameters
	 *            An array of parameters
	 * @return The select clause id
	 * @throws SubQueryNotFoundException
	 */
	public Long addSelectClause(Long sqId, Path... parameters)
			throws SubQueryNotFoundException {
		SelectClause sc = new SelectClause(lastId++);
		for (Path parameter : parameters) {
			sc.addParameter(parameter);
		}
		if (sqId != null) {
			SubQuery sq = findSubQuery(sqId);
			sq.addClause(sc.getId(), sc);
		}
		return sc.getId();
	}

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
		checkSubQueryJoinSupport(sq1, jt, fieldId1, relationship);
		checkSubQueryJoinSupport(sq2, jt, fieldId2, relationship);

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

	public Long addWhereClause(Long sqId, String logicalOperator, Path field,
			String predicateName, String value, String additionalValue,
			Long whereId) throws ClauseNotFoundException,
			ClauseIsNotTheCorrectType, SubQueryNotFoundException,
			LogicalOperatorNotFound, PredicateTypeNotSupported {
		if (whereId != null) {
			hasClause(whereId, WhereClause.class);
		} else {
			whereId = lastId++;
		}
		SubQuery sq = findSubQuery(sqId);
		LogicalOperator lo = findLogicalOperator(logicalOperator);
		PredicateType predicate = findPredicateType(predicateName);

		checkSubQueryPredicateSupport(sq, lo, field, predicate, value,
				additionalValue);

		WhereClause wc = new WhereClause();
		wc.setId(whereId);

		wc.setSubQuery(sq);
		wc.setLogicalOperator(lo);
		wc.setField(field);
		wc.setPredicateType(predicate);
		wc.setValue(value);
		wc.setAdditionalValue(additionalValue);

		return null;
	}

	public void cancelQuery() {
		this.query = null;
		this.lastId = 0L;
	}

	public void deleteClause(Long clauseId) throws ClauseNotFoundException {
		if (query.getClauses().containsKey(clauseId)) {
			query.removeClause(clauseId);
		} else if (query.getSubQueries().containsKey(clauseId)) {
			query.removeSubQuery(clauseId);
		} else {
			throw new ClauseNotFoundException(clauseId);
		}
	}

	public Query getQuery() {
		return query;
	}

	public void setQuery(Query query) {
		this.query = query;
	}
	
	
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

	

	private void checkSubQueryJoinSupport(SubQuery sq, JoinType joinType,
			Path field, String relationship) throws SubQueryNotFoundException,
			JoinTypeNotSupported {

		boolean supported = false;
		List<Resource> resources = sq.getResources();
		if (resources.isEmpty()) {
			resources = sq.getParent().getResources();
		}

		for (Resource resource : resources) {
			if (resource.getSupportedJoins().contains(joinType)) {

				if (joinType.isRequireFields()) {
					if (joinType.supportsDataType(field.getDataType())) {
						supported = true;
					}
				} else {
					supported = true;
				}

				if (joinType.isRequireRelationships() && relationship == null) {
					supported = false;
				}

				if (supported) {
					break;
				}
			}
		}

		if (!supported) {
			throw new JoinTypeNotSupported(joinType.getName());
		}

	}

	private void checkSubQueryPredicateSupport(SubQuery sq, LogicalOperator lo,
			Path field, PredicateType predicate, String value,
			String additionalValue) throws PredicateTypeNotSupported {
		boolean supported = false;
		List<Resource> resources = sq.getResources();
		if (resources.isEmpty()) {
			resources = sq.getParent().getResources();
		}

		for (Resource resource : resources) {
			if (resource.getSupportedPredicates().contains(predicate)) {
				if (predicate.isRequiresValue()) {
					if (predicate.isRequiresAdditionalValue()) {
						if (predicate.supportsDataType(field.getDataType())) {
							supported = true;
						}
					}
				} else {
					supported = true;
				}

			}
		}

		if (!supported) {
			throw new PredicateTypeNotSupported(predicate.getName());
		}

	}

	private JoinType findJoinType(String joinType) throws JoinTypeNotSupported {
		JoinType jt = irctApp.getSupportedJoinTypes().get(joinType);
		if (jt == null) {
			throw new JoinTypeNotSupported(joinType);
		}
		return jt;
	}

	private PredicateType findPredicateType(String predicateName)
			throws PredicateTypeNotSupported {

		PredicateType pt = irctApp.getSupportedPredicateTypes().get(
				predicateName);
		if (pt == null) {
			throw new PredicateTypeNotSupported(predicateName);
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

	private ClauseAbstract findClause(Long clauseId,
			Class<ClauseAbstract> clauseType) throws ClauseIsNotTheCorrectType,
			ClauseNotFoundException {
		ClauseAbstract clause = query.getClauses().get(clauseId);
		if (clause == null) {
			throw new ClauseNotFoundException(clauseId);
		}
		if (!clauseType.isInstance(clause)) {
			throw new ClauseIsNotTheCorrectType(clauseId);
		}

		return clause;
	}
}
