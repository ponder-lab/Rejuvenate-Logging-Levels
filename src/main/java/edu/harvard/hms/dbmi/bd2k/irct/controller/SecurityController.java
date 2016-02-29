package edu.harvard.hms.dbmi.bd2k.irct.controller;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Date;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
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

	@PersistenceContext
	EntityManager entityManager;

	@Inject
	Logger log;

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
		entityManager.persist(ss);

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
		cq.select(secureSession);

		// Predicate key = cb.equal(arg0, arg1)
		// cq.where(arg0)

		// log.info("Found valid key for " + user.getName());
		return null;
	}

	private final String generateString() {
		SecureRandom random = new SecureRandom();
		return new BigInteger(130, random).toString(32);
	}
}
