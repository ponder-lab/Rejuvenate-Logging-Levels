/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.controller;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.ejb.Stateful;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import edu.harvard.hms.dbmi.bd2k.irct.exception.QueryException;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.DataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Entity;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.JoinClause;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.JoinType;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.PredicateType;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.SelectClause;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.WhereClause;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Field;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.LogicalOperator;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;

/**
 * A stateless controller for creating a query
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@Stateful
public class QueryController {

	@PersistenceContext(unitName = "primary")
	EntityManager entityManager;

	@Inject
	Logger log;

	private Query query;
	private Long lastId;

	/**
	 * Deletes the current query and creates a new one
	 * 
	 */
	public void createQuery() {
		this.query = new Query();
		this.lastId = 0L;
	}

	/**
	 * Adds or updates a where clause to the query
	 * 
	 * @param clauseId
	 *            Clause Id
	 * @param resource
	 *            Resource
	 * @param field
	 *            Field
	 * @param predicate
	 *            Predicate
	 * @param logicalOperator
	 *            Logical Operator
	 * @param fields
	 *            Map of Field values
	 * @return Id of the clause
	 * @throws QueryException
	 *             An exception occurred adding the where clause
	 */
	public Long addWhereClause(Long clauseId, Resource resource, Entity field,
			PredicateType predicate, LogicalOperator logicalOperator,
			Map<String, String> fields) throws QueryException {

		// Is valid where clause
		validateWhereClause(resource, field, predicate, logicalOperator, fields);

		// Create the where Clause
		WhereClause wc = new WhereClause();
		wc.setField(field);
		wc.setLogicalOperator(logicalOperator);
		wc.setPredicateType(predicate);
		wc.setStringValues(fields);

		// Assign the where clause an id if it doesn't have one
		if (clauseId == null) {
			clauseId = this.lastId;
			this.lastId++;
		}

		// Add the where clause to the query
		query.addClause(clauseId, wc);

		return clauseId;
	}

	/**
	 * Deletes a where clause
	 * 
	 * @param clauseId
	 *            Clause Id to delete
	 */
	public void deleteWhereClause(Long clauseId) {
		this.query.getClauses().remove(clauseId);
	}

	/**
	 * Adds or updates a select clause
	 * 
	 * @param clauseId
	 *            Clause Id
	 * @param resource
	 *            Resource
	 * @param field
	 *            Field
	 * @param alias
	 *            Alias for the column
	 * @return Clause Id
	 * @throws QueryException
	 *             An exception occurred adding the select clause
	 */
	public Long addSelectClause(Long clauseId, Resource resource, Entity field,
			String alias) throws QueryException {
		// Is this a valid select clause
		validateSelectClause(resource);

		// Crete the select clause
		SelectClause sc = new SelectClause();
		sc.setParameters(field);
		sc.setAlias(alias);

		// Assign the where clause an id if it doesn't have one
		if (clauseId == null) {
			clauseId = this.lastId;
			this.lastId++;
		}

		// Add the where clause to the query
		query.addClause(clauseId, sc);

		return clauseId;
	}

	/**
	 * Adds a join clause
	 * 
	 * @return
	 * @throws QueryException
	 *             An occurred adding the join clause
	 */
	public Long addJoinClause(Long clauseId, Resource resource, JoinType joinType, Map<String, String> joinFields) throws QueryException {
		validateJoinClause(resource, joinType, joinFields);
		
		
		
		JoinClause jc = new JoinClause();
		jc.setJoinType(joinType);
		jc.setStringValues(joinFields);
		
		// Assign the where clause an id if it doesn't have one
		if (clauseId == null) {
			clauseId = this.lastId;
			this.lastId++;
		}

		// Add the where clause to the query
		query.addClause(clauseId, jc);

		return clauseId;
	}

	/**
	 * Deletes the current query
	 * 
	 */
	public void deleteQuery() {
		this.query = null;
	}

	private void validateWhereClause(Resource resource, Entity field,
			PredicateType predicate, LogicalOperator logicalOperator,
			Map<String, String> queryFields) throws QueryException {
		// Is resource part of query?
		if (this.query.getResources().isEmpty()) {
			this.query.getResources().add(resource);
		} else if (!this.query.getResources().contains(resource)) {
			throw new QueryException("Queries only support one resource");
		}
		// Does the resource support the logical operator
		if ((logicalOperator != null)
				&& (!resource.getLogicalOperators().contains(logicalOperator))) {
			throw new QueryException(
					"Logical operator is not supported by the resource");
		}
		// Does the resource support the predicate?
		if (!resource.getSupportedPredicates().contains(predicate)) {
			throw new QueryException(
					"Predicate is not supported by the resource");
		}
		// Does the predicate support the entity?
		if ((!predicate.getDataTypes().isEmpty())
				&& (!predicate.getDataTypes().contains(field.getDataType()))) {
			throw new QueryException(
					"Predicate does not support this type of field");
		}
		// Are all the fields valid?
		validateFields(predicate.getFields(), queryFields);
	}

	private void validateSelectClause(Resource resource) throws QueryException {
		// Is resource part of query?
		if (this.query.getResources().isEmpty()) {
			this.query.getResources().add(resource);
		} else if (!this.query.getResources().contains(resource)) {
			throw new QueryException("Queries only support one resource");
		}
	}
	
	private void validateJoinClause(Resource resource, JoinType joinType,
			Map<String, String> joinFields) throws QueryException {
		// Is the resource part of query?
		if (this.query.getResources().isEmpty()) {
			this.query.getResources().add(resource);
		} else if (!this.query.getResources().contains(resource)) {
			throw new QueryException("Queries only support one resource");
		}
		
		// Does the resource support the join type
		if(!resource.getSupportedJoins().contains(joinType)) {
			throw new QueryException(
					"Join Type is not supported by the resource");
		}
		
		// Are all the fields valid?
		validateFields(joinType.getFields(), joinFields);
	}

	private void validateFields(List<Field> fields,
			Map<String, String> valueFields) throws QueryException {
		for (Field predicateField : fields) {
			// Is the predicate field required and is in the query fields
			if (predicateField.isRequired()
					&& (!valueFields.containsKey(predicateField.getPath()))) {
				throw new QueryException("Required field is not set");
			}
			String queryFieldValue = valueFields.get(predicateField.getPath());

			if (queryFieldValue != null) {
				// Is the predicate field data type allowed for this query field
				if (!predicateField.getDataTypes().isEmpty()) {
					boolean validateFieldValue = false;

					for (DataType dt : predicateField.getDataTypes()) {
						if (dt.validate(queryFieldValue)) {
							validateFieldValue = true;
							break;
						}
					}

					if (!validateFieldValue) {
						throw new QueryException(
								"The field value set is not a supported type for this field");
					}
				}
				// Is the predicate field of allowedTypes
				if (!predicateField.getPermittedValues().isEmpty()
						&& (!predicateField.getPermittedValues().contains(
								queryFieldValue))) {
					throw new QueryException(
							"The field value is not of an allowed type");
				}
			}
		}
	}

	/**
	 * Save the query
	 * 
	 * @throws QueryException
	 *             An exception occurred saving the query
	 */
	public void saveQuery() throws QueryException {
		if (this.query == null) {
			throw new QueryException("No query to save.");
		}
		if (this.query.getId() == null) {
			entityManager.persist(this.query);
		} else {
			entityManager.merge(this.query);
		}
		log.info("Query " + this.query.getId() + " saved");

	}

	/**
	 * Load the query
	 * 
	 * @param queryId
	 *            Query to load
	 * @throws QueryException
	 *             An exception occurred loading the query
	 */
	public void loadQuery(Long queryId) throws QueryException {
		if (queryId == null) {
			throw new QueryException("No query id.");
		}

		this.query = entityManager.find(Query.class, queryId);
		if (this.query == null) {
			throw new QueryException("No query to load.");
		}
		log.info("Query " + this.query.getId() + " loaded");
	}

	/**
	 * Returns the given query
	 * 
	 * @return Query
	 */
	public Query getQuery() {
		return query;
	}

	/**
	 * Sets the given query
	 * 
	 * @param query
	 *            Query
	 */
	public void setQuery(Query query) {
		this.query = query;
	}
}
