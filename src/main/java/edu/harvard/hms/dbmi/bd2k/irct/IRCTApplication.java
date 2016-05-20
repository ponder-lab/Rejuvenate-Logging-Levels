/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.servlet.ServletContext;

import edu.harvard.hms.dbmi.bd2k.irct.dataconverter.ResultDataConverter;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.model.join.IRCTJoin;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.DataConverterImplementation;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultDataType;

/**
 * Manages supported resources and join types for this instance of the IRCT
 * application
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@ApplicationScoped
public class IRCTApplication {

	private Map<String, Resource> resources;
	private Map<String, IRCTJoin> supportedJoinTypes;
	private Map<ResultDataType, List<DataConverterImplementation>> resultDataConverters;

	private Properties properties;

	@Inject
	Logger log;

	@Inject
	private EntityManagerFactory objectEntityManager;

	@Inject
	private ServletContext context;

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

		log.info("Loading Properties");
		loadProperties();
		log.info("Finished Loading Properties");

		log.info("Loading Data Converters");
		loadDataConverters();
		log.info("Finished Data Converters");

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
	 * 
	 * Load all the properties from the servlet context or from the
	 * IRCT.properties file
	 * 
	 */
	private void loadProperties() {
		this.properties = new Properties();
		if (context != null) {
			Enumeration<String> propertyNameEnumeration = context
					.getInitParameterNames();
			while (propertyNameEnumeration.hasMoreElements()) {
				String propertyName = propertyNameEnumeration.nextElement();
				this.properties.put(propertyName,
						context.getInitParameter(propertyName));
			}

		} else {
			InputStream inputStream = IRCTApplication.class.getClassLoader()
					.getResourceAsStream("IRCT.properties");

			try {
				this.properties.load(inputStream);
			} catch (IOException e) {
				log.info("Error loading properties: " + e.getMessage());
				log.log(Level.FINE, "Error loading properties", e);
			}
		}

	}

	/**
	 * Load all the Output Data Converters
	 * 
	 */
	private void loadDataConverters() {
		this.resultDataConverters = new HashMap<ResultDataType, List<DataConverterImplementation>>();
		CriteriaBuilder cb = oem.getCriteriaBuilder();
		CriteriaQuery<DataConverterImplementation> criteria = cb
				.createQuery(DataConverterImplementation.class);
		Root<DataConverterImplementation> load = criteria
				.from(DataConverterImplementation.class);
		criteria.select(load);
		List<DataConverterImplementation> allDCI = oem.createQuery(criteria)
				.getResultList();
		for (DataConverterImplementation dci : allDCI) {
			if (this.resultDataConverters.containsKey(dci.getResultDataType())) {
				this.resultDataConverters.get(dci.getResultDataType()).add(dci);
			} else {
				List<DataConverterImplementation> dciList = new ArrayList<DataConverterImplementation>();
				dciList.add(dci);
				this.resultDataConverters.put(dci.getResultDataType(), dciList);
			}

		}

		log.info("Loaded " + allDCI.size() + " result data converters");
	}

	/**
	 * Loads all the joins from the persistence manager
	 */
	private void loadJoins() {

		setSupportedJoinTypes(new HashMap<String, IRCTJoin>());
		// Run JPA Query to load the resources
		CriteriaBuilder cb = oem.getCriteriaBuilder();
		CriteriaQuery<IRCTJoin> criteria = cb.createQuery(IRCTJoin.class);
		Root<IRCTJoin> load = criteria.from(IRCTJoin.class);
		criteria.select(load);
		for (IRCTJoin jt : oem.createQuery(criteria).getResultList()) {
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
			try {
				resource.setup();
				this.resources.put(resource.getName(), resource);
			} catch (ResourceInterfaceException e) {
				e.printStackTrace();
			}

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
	public Map<String, IRCTJoin> getSupportedJoinTypes() {
		return supportedJoinTypes;
	}

	/**
	 * Sets a map of the supported join types
	 * 
	 * @param supportedJoinTypes
	 *            Supported join types
	 */
	public void setSupportedJoinTypes(Map<String, IRCTJoin> supportedJoinTypes) {
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
	public void addJoin(String name, IRCTJoin join) {
		// Persist the join
		oem.persist(join);
		this.supportedJoinTypes.put(name, join);
	}

	/**
	 * @return the resultDataConverters
	 */
	public Map<ResultDataType, List<DataConverterImplementation>> getResultDataConverters() {
		return resultDataConverters;
	}

	/**
	 * @param resultDataConverters
	 *            the resultDataConverters to set
	 */
	public void setResultDataConverters(
			Map<ResultDataType, List<DataConverterImplementation>> resultDataConverters) {
		this.resultDataConverters = resultDataConverters;
	}

	/**
	 * Returns a dataconveter for a given datatype and format
	 * 
	 * @param dataType
	 *            DataType
	 * @param format
	 *            Format
	 * @return DataConverter
	 */
	public ResultDataConverter getResultDataConverter(ResultDataType dataType,
			String format) {
		List<DataConverterImplementation> dciList = this.resultDataConverters
				.get(dataType);

		for (DataConverterImplementation dci : dciList) {
			if (dci.getFormat().equals(format)) {

				return dci.getDataConverter();
			}
		}
		return null;
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

	/**
	 * Get the properties
	 * 
	 * @return Properties
	 */
	public Properties getProperties() {
		return properties;
	}

	/**
	 * Set the properties
	 * 
	 * @param properties
	 *            Properties
	 */
	public void setProperties(Properties properties) {
		this.properties = properties;
	}
}
