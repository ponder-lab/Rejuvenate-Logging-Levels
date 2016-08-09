/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.query;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;

/**
 * Creates a clause to support joins done on a remote resource
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@Entity
public class JoinClause extends ClauseAbstract implements Serializable {

	private static final long serialVersionUID = -8136813552982367867L;
	
	@ElementCollection
	@CollectionTable(name="join_values", joinColumns=@JoinColumn(name="JOIN_VALUE"))
	@MapKeyColumn(name="join_id")
	@Column(name="join_value")
	private Map<String, String> stringValues;
	
	@ManyToOne
	private JoinType joinType;
	
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
	
	

}
