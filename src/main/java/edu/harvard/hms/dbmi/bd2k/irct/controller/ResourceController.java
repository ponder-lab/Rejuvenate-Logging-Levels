/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.controller;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import edu.harvard.hms.dbmi.bd2k.irct.IRCTApplication;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.ProcessResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.QueryResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;

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
	private IRCTApplication ipctApp;

	/**
	 * Returns a list of available resources
	 * 
	 * @return Available Resources
	 */
	public List<Resource> getResources() {
		return new ArrayList<Resource>(ipctApp.getResources().values());
	}

	/**
	 * Returns a specific resource
	 * 
	 * @param resource
	 *            Resource ID
	 * @return Resource
	 */
	public Resource getResource(String resource) {
		return ipctApp.getResources().get(resource);
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
		for (Resource resource : ipctApp.getResources().values()) {
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
		for (Resource resource : ipctApp.getResources().values()) {
			if (resource.getImplementingInterface() instanceof ProcessResourceImplementationInterface) {
				processResources.add(resource);
			}
		}
		return processResources;
	}

}
