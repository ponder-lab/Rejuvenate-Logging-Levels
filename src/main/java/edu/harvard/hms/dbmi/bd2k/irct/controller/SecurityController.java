/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.controller;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.Token;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.User;

/**
 * A stateless controller for managing security.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@Stateless
public class SecurityController {

	@PersistenceContext(unitName = "primary")
	EntityManager entityManager;

	@Inject
	Logger log;
	
	@javax.annotation.Resource(mappedName ="java:global/KeyTimeOutInMinutes")
	private String keyTimeOut;

	/**
	 * Creates a secured randomly generated key unique to that user.
	 * 
	 * @param user
	 *            A user that is to be associated with that key
	 * @param token
	 *            A token that is to be associated with that key
	 * @return A secured key
	 */
	public String createKey(User user, Token token) {
		if ((user == null) || (token == null)) {
			return null;
		}
		String key = generateString();

		SecureSession ss = new SecureSession();
		ss.setUser(user);
		ss.setToken(token);
		ss.setAccessKey(key);
		ss.setCreated(new Date());

		if (user.getId() == null) {
			entityManager.persist(ss);
		} else {
			entityManager.merge(ss);
		}

		log.info("Created key for " + user.getName());

		return key;
	}

	/**
	 * Validates a key as being valid and returns a secure session information
	 * 
	 * @param key
	 *            Key to validate
	 * @return A secure session information if the key is valid, null if it is
	 *         not valid
	 */
	public SecureSession validateKey(String key) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<SecureSession> cq = cb.createQuery(SecureSession.class);
		Root<SecureSession> secureSession = cq.from(SecureSession.class);

		Date startTime = new Date();

		Calendar endTime = GregorianCalendar.getInstance();
		endTime.setTime(new Date());
		endTime.add(Calendar.MINUTE, -1 * Integer.parseInt(keyTimeOut));
		endTime.getTime();

		cq.where(cb.and(
				cb.equal(secureSession.get("accessKey"), key),
				cb.between(secureSession.<Date> get("created"),
						endTime.getTime(), startTime)));

		cq.select(secureSession);
		List<SecureSession> ssl = entityManager.createQuery(cq).getResultList();
		if (ssl == null || ssl.isEmpty()) {
			return null;
		}

		SecureSession ss = ssl.get(0);

		log.info("Found valid key for " + ss.getUser().getName());
		return ss;
	}

	/**
	 * Get a given user from a database from a user id
	 * 
	 * @param userId
	 *            User Id
	 * @return User
	 */
	public User getUser(String userId) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<User> cq = cb.createQuery(User.class);
		Root<User> userRoot = cq.from(User.class);
		cq.where(cb.equal(userRoot.get("userId"), userId));
		cq.select(userRoot);
		List<User> users = entityManager.createQuery(cq).getResultList();

		if (users == null || users.isEmpty()) {
			return new User((String) userId);
		}

		return users.get(0);
	}

	private final String generateString() {
		SecureRandom random = new SecureRandom();
		return new BigInteger(130, random).toString(32);
	}
}
