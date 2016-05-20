/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.security;

import java.io.Serializable;
import java.security.Principal;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.SequenceGenerator;

/**
 * A basic user representation. It can be associated with a session in EE 7. The
 * userId, and name are the same in this implementation.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class User implements Principal, Serializable {
	private static final long serialVersionUID = 225027371671010450L;

	@Id
	@GeneratedValue(generator = "userSequencer")
	@SequenceGenerator(name = "userSequencer", sequenceName = "userSeq")
	private Long id;

	private String userId;
	
	/**
	 * Creates a new user
	 */
	public User() {
		
	}

	/**
	 * Creates a new user with the given User Id
	 * 
	 * @param userId User Id
	 */
	public User(String userId) {
		this.userId = userId;
	}

	/**
	 * Returns the id of this User object
	 * 
	 * @return Id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Sets the id of this User object
	 * 
	 * @param id Id
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Returns the user id
	 * 
	 * @return User Id
	 */
	public String getUserId() {
		return this.userId;
	}

	/**
	 * Sets the user id
	 * 
	 * @param userId User Id
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	@Override
	public String getName() {
		return this.userId;
	}
}
