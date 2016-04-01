/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import edu.harvard.hms.dbmi.bd2k.irct.IRCTApplication;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.PathResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.ProcessResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.QueryResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.VisualizationResourceImplementationInterface;

/**
 * A stateless controller that provides access to all resources in the IRCT
 * Application
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@Stateless
public class ResourceController {
	@Inject
	private IRCTApplication irctApp;

	@PersistenceContext
	EntityManager entityManager;

	private List<String> categories;

	@PostConstruct
	public void init() {

	}

	/**
	 * Returns a list of available resources
	 * 
	 * @return Available Resources
	 */
	public List<Resource> getResources() {
		return new ArrayList<Resource>(irctApp.getResources().values());
	}

	/**
	 * Returns a specific resource
	 * 
	 * @param resource
	 *            Resource ID
	 * @return Resource
	 */
	public Resource getResource(String resource) {
		return irctApp.getResources().get(resource);
	}

	/**
	 * Returns a list of all resources that implement the
	 * QueryResourceImplementationInterface and there for can have queries run
	 * against them.
	 * 
	 * @return Query Resources
	 */
	public List<Resource> getQueryResources() {
		List<Resource> queryResources = new ArrayList<Resource>();
		for (Resource resource : irctApp.getResources().values()) {
			if (resource.getImplementingInterface() instanceof QueryResourceImplementationInterface) {
				queryResources.add(resource);

			}
		}
		return queryResources;
	}

	/**
	 * Returns a list of all resources that implement the
	 * ProcessResourceImplementationInterface and there for can have process run
	 * on them.
	 * 
	 * @return Process Resources
	 */
	public List<Resource> getProcessResources() {
		List<Resource> processResources = new ArrayList<Resource>();
		for (Resource resource : irctApp.getResources().values()) {
			if (resource.getImplementingInterface() instanceof ProcessResourceImplementationInterface) {
				processResources.add(resource);
			}
		}
		return processResources;
	}

	/**
	 * Returns a list of all resources that implement the
	 * VisualizationResourceImplementationInterface and there for can have
	 * visualizations run on them.
	 * 
	 * @return Process Resources
	 */
	public List<Resource> getVisualizationResources() {
		List<Resource> visualizationResources = new ArrayList<Resource>();
		for (Resource resource : irctApp.getResources().values()) {
			if (resource.getImplementingInterface() instanceof VisualizationResourceImplementationInterface) {
				visualizationResources.add(resource);
			}
		}
		return visualizationResources;
	}

	/**
	 * Returns a list of all resources that implement the
	 * PathResourceImplementationInterface and there for can be traversed
	 * 
	 * @return Path Resources.
	 */
	public List<Resource> getPathResources() {
		List<Resource> pathResources = new ArrayList<Resource>();
		for (Resource resource : irctApp.getResources().values()) {
			if (resource.getImplementingInterface() instanceof PathResourceImplementationInterface) {
				pathResources.add(resource);
			}
		}
		return pathResources;
	}
	
	/**
	 * Get all categories for searching
	 * 
	 * @return Search Category
	 */
	public List<String> getCategories() {
		return this.categories;
	}

	/**
	 * Returns if this is a valid category or not
	 * 
	 * @param categoryName Category name
	 * @return Category Validity
	 */
	public boolean isValidCategory(String categoryName) {
		return this.categories.contains(categoryName);
	}

	/**
	 * Returns a list of all resources that match the search parameters
	 * @param searchParams Search Parameters
	 * @return Matching Resources
	 */
	public List<Resource> search(Map<String, List<String>> searchParams) {
		// TODO Auto-generated method stub
		return null;
	}

}
