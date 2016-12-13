/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.join;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.Transient;

@Entity
public class Join implements Serializable {

	private static final long serialVersionUID = 4869490865776072674L;

	@Id
	@GeneratedValue
	private Long id;

	private JoinImplementation joinImplementation;
	
	private IRCTJoin joinType;

	@ElementCollection(fetch = FetchType.EAGER)
	@MapKeyColumn(name = "name")
	@Column(name = "value")
	@CollectionTable(name = "where_values", joinColumns = @JoinColumn(name = "where_id"))
	private Map<String, String> stringValues;

	@Transient
	private Map<String, Object> objectValues;
	
	public Join() {
		this.stringValues = new HashMap<String, String>();
		this.objectValues = new HashMap<String, Object>();
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}


	/**
	 * @return the joinImplementation
	 */
	public JoinImplementation getJoinImplementation() {
		return joinImplementation;
	}

	/**
	 * @param joinImplementation the joinImplementation to set
	 */
	public void setJoinImplementation(JoinImplementation joinImplementation) {
		this.joinImplementation = joinImplementation;
	}

	/**
	 * @return the joinType
	 */
	public IRCTJoin getJoinType() {
		return joinType;
	}

	/**
	 * @param joinType the joinType to set
	 */
	public void setJoinType(IRCTJoin joinType) {
		this.joinType = joinType;
	}

	/**
	 * @return the stringValues
	 */
	public Map<String, String> getStringValues() {
		return stringValues;
	}

	/**
	 * @param stringValues
	 *            the stringValues to set
	 */
	public void setStringValues(Map<String, String> stringValues) {
		this.stringValues = stringValues;
	}

	/**
	 * @return the objectValues
	 */
	public Map<String, Object> getObjectValues() {
		return objectValues;
	}

	/**
	 * @param objectValues
	 *            the objectValues to set
	 */
	public void setObjectValues(Map<String, Object> objectValues) {
		this.objectValues = objectValues;
	}

}
