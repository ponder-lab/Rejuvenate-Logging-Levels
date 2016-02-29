/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import edu.harvard.hms.dbmi.bd2k.irct.model.query.JoinType;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;

/**
 * Manages supported resources and join types for this instance of the IRCT
 * application
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@Singleton
@ApplicationScoped
@Startup
public class IRCTApplication {

	private Map<String, Resource> resources;
	private Map<String, JoinType> supportedJoinTypes;

	@Inject
	Logger log;

	@Inject
	private EntityManagerFactory objectEntityManager;

	private EntityManager oem;

	/**
	 * Initiates the IRCT Application and loading of the joins, resources, and
	 * predicates.
	 * 
	 */
	@PostConstruct
	public void init() {
		log.info("Starting IRCT Application");

		this.oem = objectEntityManager.createEntityManager();
		this.oem.setFlushMode(FlushModeType.COMMIT);

		log.info("Loading Join Types");
		loadJoins();
		log.info("Finished Loading Join Types");

		log.info("Loading Resources");
		loadResources();
		log.info("Finished Loading Resources");

		log.info("Finished Starting IRCT Application");
	}

	/**
	 * Loads all the joins from the persistence manager
	 */
	private void loadJoins() {

		setSupportedJoinTypes(new HashMap<String, JoinType>());
		// Run JPA Query to load the resources
		CriteriaBuilder cb = oem.getCriteriaBuilder();
		CriteriaQuery<JoinType> criteria = cb.createQuery(JoinType.class);
		Root<JoinType> load = criteria.from(JoinType.class);
		criteria.select(load);
		for (JoinType jt : oem.createQuery(criteria).getResultList()) {
			jt.setup();
			this.supportedJoinTypes.put(jt.getName(), jt);
		}
		log.info("Loaded " + this.supportedJoinTypes.size() + " joins");
	}

	/**
	 * 
	 * Loads all the resources from the persistence manager
	 * 
	 */
	private void loadResources() {
		setResources(new HashMap<String, Resource>());
		// Run JPA Query to load the resources
		CriteriaBuilder cb = oem.getCriteriaBuilder();
		CriteriaQuery<Resource> criteria = cb.createQuery(Resource.class);
		Root<Resource> load = criteria.from(Resource.class);
		criteria.select(load);
		for (Resource resource : oem.createQuery(criteria).getResultList()) {
			resource.setup();
			this.resources.put(resource.getName(), resource);
		}
		log.info("Loaded " + this.resources.size() + " resources");
	}

	/**
	 * Adds a given resource to the IRCT application
	 * 
	 * @param name
	 *            Resource name
	 * @param resource
	 *            Resource
	 */
	public void addResource(String name, Resource resource) {
		// Persist the new resource
		oem.persist(resource);

		this.resources.put(name, resource);
	}

	/**
	 * Removes a resource from the IRCT application
	 * 
	 * @param name
	 *            Resource name
	 */
	public void removeResource(String name) {
		// Remove the resource from persistence manager
		oem.remove(this.resources.remove(name));
	}

	/**
	 * Returns true if the resource exists
	 * 
	 * True if resource exists, false otherwise
	 * 
	 * @param name
	 *            Resource name
	 * @return If resource exists
	 */
	public boolean doesResourceExist(String name) {
		return this.resources.containsKey(name);
	}

	/**
	 * Returns a map of the resources where the Resource name is the key, and
	 * the Resource itself is the value
	 * 
	 * @return Resources
	 */
	public Map<String, Resource> getResources() {
		return resources;
	}

	/**
	 * Sets a map of the resources.
	 * 
	 * @param resources
	 *            Resources
	 */
	public void setResources(Map<String, Resource> resources) {
		this.resources = resources;
	}

	/**
	 * Returns a map of the supported joins where the Join name is the key, and
	 * the JoinType itself is the value
	 * 
	 * @return Supported join types
	 */
	public Map<String, JoinType> getSupportedJoinTypes() {
		return supportedJoinTypes;
	}

	/**
	 * Sets a map of the supported join types
	 * 
	 * @param supportedJoinTypes
	 *            Supported join types
	 */
	public void setSupportedJoinTypes(Map<String, JoinType> supportedJoinTypes) {
		this.supportedJoinTypes = supportedJoinTypes;
	}

	/**
	 * Adds a join to the list of supported joins
	 * 
	 * @param name
	 *            Join Name
	 * @param join
	 *            Join
	 */
	public void addJoin(String name, JoinType join) {
		// Persist the join
		oem.persist(join);
		this.supportedJoinTypes.put(name, join);
	}

	/**
	 * Removes a join from the list of supported joins
	 * 
	 * @param name
	 *            Join name
	 */
	public void removeJoin(String name) {
		// Removes the supported joins from the persistence manager
		oem.remove(this.supportedJoinTypes.remove(name));
	}

	/**
	 * Returns true if the join type is supported
	 * 
	 * True if join type is supported, otherwise it false
	 * 
	 * @param name
	 *            Resource name
	 * @return If resource exists
	 */
	public boolean doesJoinExists(String name) {
		return this.supportedJoinTypes.containsKey(name);
	}
}
