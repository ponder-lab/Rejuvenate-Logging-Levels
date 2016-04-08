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
import edu.harvard.hms.dbmi.bd2k.irct.model.query.PredicateType;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.WhereClause;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Field;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.LogicalOperator;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;

@Stateful
public class QueryController {
	
	@PersistenceContext
	EntityManager entityManager;
	
	@Inject
	Logger log;
	
	private Query query;
	private Long lastId;
	
	public void createQuery() {
		this.query = new Query();
		this.lastId = 0L;
	}
	
	public Long addWhereClause(Long clauseId, Resource resource, Entity field, PredicateType predicate, LogicalOperator logicalOperator, Map<String, String> fields) throws QueryException {
		
		// Is valid where clause
		validateWhereClause(resource, field, predicate, logicalOperator, fields);
		
		// Create the where Clause
		WhereClause wc = new WhereClause();
		wc.setField(field);
		wc.setLogicalOperator(logicalOperator);
		wc.setPredicateType(predicate);
		wc.setValues(fields);
		
		// Assign the where clause an id if it doesn't have one
		if(clauseId == null) {
			clauseId = this.lastId;
			this.lastId++;
		}
		
		// Add the where clause to the query
		query.addClause(clauseId, wc);
		
		return clauseId;
	}
	
	public void deleteWhereClause(Long clauseId) {
		this.query.getClauses().remove(clauseId);
	}
	
	public void addSelectClause() throws QueryException {
		validateSelectClause();
	}
	
	public void addJoinClause() throws QueryException {
		validateJoinClause();
	}
	
	public void deleteQuery() {
		this.query = null;
	}
	
	private void validateWhereClause(Resource resource, Entity field, PredicateType predicate, LogicalOperator logicalOperator, Map<String, String> queryFields) throws QueryException {
		//Is resource part of query?
		if(this.query.getResources().isEmpty()) {
			this.query.getResources().add(resource);
		} else if (!this.query.getResources().contains(resource)) {
			throw new QueryException("Queries only support one resource");
		}
		//Does the resource support the logical operator
		if((logicalOperator != null) && (!resource.getLogicalOperators().contains(logicalOperator))) {
			throw new QueryException("Logical operator is not supported by the resource");
		}
		//Does the resource support the predicate?
		if(!resource.getSupportedPredicates().contains(predicate)) {
			throw new QueryException("Predicate is not supported by the resource");
		}
		//Does the predicate support the entity?
		if((!predicate.getDataTypes().isEmpty()) && (!predicate.getDataTypes().contains(field.getDataType()))) {
			throw new QueryException("Predicate does not support this type of field");
		}
		//Are all the fields valid?
		validateFields(predicate.getFields(), queryFields);
	}
	
	private void validateSelectClause() throws QueryException {
	}
	
	private void validateJoinClause() throws QueryException {
	}
	
	private void validateFields(List<Field> fields, Map<String, String> valueFields) throws QueryException {
		for(Field predicateField : fields) {
			//Is the predicate field required and is in the query fields
			if(predicateField.isRequired() && (!valueFields.containsKey(predicateField.getPath()))) {
				throw new QueryException("Required field is not set");
			}
			String queryFieldValue = valueFields.get(predicateField.getPath());
			
			if(queryFieldValue != null) {
				//Is the predicate field data type allowed for this query field
				if(!predicateField.getDataTypes().isEmpty()) {
					boolean validateFieldValue = false;
					
					for(DataType dt : predicateField.getDataTypes()) {
						if(dt.validate(queryFieldValue)) {
							validateFieldValue = true;
							break;
						}
					}
					
					if(!validateFieldValue) {
						throw new QueryException("The field value set is not a supported type for this field");
					}
				}
				//Is the predicate field of allowedTypes
				if(!predicateField.getPath().isEmpty() && (!predicateField.getPath().contains(queryFieldValue))) {
					throw new QueryException("The field value set is not a required field");
				}
			}
		}
	}

	public void saveQuery() throws QueryException {
		if(this.query == null) {
			throw new QueryException("No query to save.");
		}
		if(this.query.getId() == null) {
			entityManager.persist(this.query);
		} else {
			entityManager.merge(this.query);
		}
		log.info("Query " + this.query.getId() + " saved");
		
	}

	public void loadQuery(Long queryId) throws QueryException {
		if(queryId == null) {
			throw new QueryException("No query id.");
		}
		
		this.query = entityManager.find(Query.class, queryId);
		if(this.query == null) {
			throw new QueryException("No query to load.");
		}
		log.info("Query " + this.query.getId() + " loaded");
	}
	
	public Query getQuery() {
		return query;
	}

	public void setQuery(Query query) {
		this.query = query;
	}
}
