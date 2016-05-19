/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.security;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

/**
 * A secure session contains information about a user, the token associated with
 * that user, and the unique key generated for that combination.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@Entity
public class SecureSession implements Serializable {
	private static final long serialVersionUID = 8282963150870479994L;

	@Id
	@GeneratedValue(generator = "secureSessionSequencer")
	@SequenceGenerator(name = "secureSessionSequencer", sequenceName = "secsurSeq")
	private Long id;

	@ManyToOne(cascade = CascadeType.ALL)
	private Token token;

	@ManyToOne(cascade = CascadeType.ALL)
	private User user;

	private String accessKey;

	@Temporal(TemporalType.TIMESTAMP)
	private Date created;
	
	@Transient
	private Map<String, Token> delegated;
	
	
	public SecureSession() {
		this.delegated = new HashMap<String, Token>();
	}

	/**
	 * Returns the id of this secure session object
	 * 
	 * @return Id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Sets the id of this secure session object
	 * 
	 * @param id
	 *            Id
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Returns the token associated with this secure session
	 * 
	 * @return Token
	 */
	public Token getToken() {
		return token;
	}

	/**
	 * Sets the token associated with this secure session
	 * 
	 * @param token
	 *            Token
	 */
	public void setToken(Token token) {
		this.token = token;
	}

	/**
	 * Returns the user associated with this secure session
	 * 
	 * @return User
	 */
	public User getUser() {
		return user;
	}

	/**
	 * Sets the user associated with this secure session
	 * 
	 * @param user
	 *            User
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * Returns the access key associated with this secure session
	 * 
	 * @return Access Key
	 */
	public String getAccessKey() {
		return accessKey;
	}

	/**
	 * Sets the access key associated with this secure session
	 * @param accessKey
	 *            Access Key
	 */
	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	/**
	 * Returns the date the key was created
	 * 
	 * @return Creation date
	 */
	public Date getCreated() {
		return created;
	}

	/**
	 * Sets the date the key was created
	 * 
	 * @param created
	 *            Date created
	 */
	public void setCreated(Date created) {
		this.created = created;
	}

	/**
	 * @return the delegated
	 */
	public Map<String, Token> getDelegated() {
		return delegated;
	}

	/**
	 * @param delegated the delegated to set
	 */
	public void setDelegated(Map<String, Token> delegated) {
		this.delegated = delegated;
	}

}
