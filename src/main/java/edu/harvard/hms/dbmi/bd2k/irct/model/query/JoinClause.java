/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.query;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Entity;

/**
 * Creates a clause to support joins done on a remote resource
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@javax.persistence.Entity
public class JoinClause extends ClauseAbstract implements Serializable {

	private static final long serialVersionUID = -8136813552982367867L;
	
	@OneToOne(cascade=CascadeType.ALL)
	private Entity field;
	
	@ManyToOne
	private JoinType joinType;
	
	@ElementCollection
	@CollectionTable(name="join_values", joinColumns=@JoinColumn(name="JOIN_VALUE"))
	@MapKeyColumn(name="join_id")
	@Column(name="join_value")
	private Map<String, String> stringValues;
	
	@Transient
	private Map<String, Object> objectValues;
	
	/**
	 * @return the field
	 */
	public Entity getField() {
		return field;
	}

	/**
	 * @param field the field to set
	 */
	public void setField(Entity field) {
		this.field = field;
	}

	/**
	 * @return the joinType
	 */
	public JoinType getJoinType() {
		return joinType;
	}

	/**
	 * @param joinType the joinType to set
	 */
	public void setJoinType(JoinType joinType) {
		this.joinType = joinType;
	}

	/**
	 * Creates an empty join clause
	 */
	public JoinClause() {
		this.stringValues = new HashMap<String, String>();
	}

	/**
	 * Returns a map of strings that represent the fields for the join clause
	 *  
	 * @return String Value Map
	 */
	public Map<String, String> getStringValues() {
		return stringValues;
	}

	/**
	 * Sets a mpa of string that represent the fields for the join clause
	 * @param stringValues String Value Map
	 */
	public void setStringValues(Map<String, String> stringValues) {
		this.stringValues = stringValues;
	}
	
	/**
	 * Returns a map of the values of the fields for the process
	 * 
	 * @return the objectValues
	 */
	public Map<String, Object> getObjectValues() {
		return objectValues;
	}

	/**
	 * Sets a map of the values of the fields for the process
	 * 
	 * @param objectValues the objectValues to set
	 */
	public void setObjectValues(Map<String, Object> objectValues) {
		this.objectValues = objectValues;
	}

}
