/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;

import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyRelationship;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Entity;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.PathResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;

/**
 * A stateless controller that manages the relationships, and paths for a
 * resource
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@Stateless
public class PathController {

	@Inject
	private ResourceController rc;

	@Inject
	Logger log;

	/**
	 * Traverses the path in the resource with the given relationship
	 * 
	 * @param resource
	 *            Resource
	 * @param resourcePath
	 *            Path in the resource
	 * @param relationship
	 *            Relationship type
	 * @param session
	 *            Session to run it in
	 * @return Paths
	 * @throws ResourceInterfaceException
	 *             A resource interface exception occurred
	 */
	public List<Entity> traversePath(Resource resource, Entity resourcePath,
			OntologyRelationship relationship, SecureSession session)
			throws ResourceInterfaceException {
		if (resource.getImplementingInterface() instanceof PathResourceImplementationInterface) {
			return ((PathResourceImplementationInterface) resource
					.getImplementingInterface()).getPathRelationship(
					resourcePath, relationship, session);
		}
		return null;
	}

	/**
	 * Searches a resource for a given searchTerm. If the resource is null it
	 * searches all resources for that term regardless of path.
	 * 
	 * @param resource
	 *            Resource
	 * @param resourcePath
	 *            Resource Path
	 * @param searchTerm
	 *            Search Term
	 * @param session
	 *            Session to run it in
	 * @return Paths
	 * @throws ResourceInterfaceException
	 *             A resource interface exception occurred
	 */
	public List<Entity> searchForTerm(Resource resource, Entity resourcePath,
			String searchTerm, SecureSession session)
			throws ResourceInterfaceException {
		List<Entity> returns = new ArrayList<Entity>();
		if (resource == null) {
			for (Resource searchResource : rc.getPathResources()) {
				returns.addAll(searchResourceForTerm(
						(PathResourceImplementationInterface) searchResource
								.getImplementingInterface(), null, searchTerm,
						session));
			}
			return returns;
		} else {
			if (resource.getImplementingInterface() instanceof PathResourceImplementationInterface) {
				return searchResourceForTerm(
						(PathResourceImplementationInterface) resource
								.getImplementingInterface(),
						resourcePath, searchTerm, session);
			}
		}
		return null;
	}

	/**
	 * Searches a resource for a given ontology term from a given ontology. If
	 * the resource is null it searches all resources for that term regardless
	 * of path.
	 * 
	 * @param resource
	 *            Resource
	 * @param resourcePath
	 *            Resource Path
	 * @param ontologyType
	 *            Ontology Type
	 * @param ontologyTerm
	 *            Ontology Term
	 * @param session
	 *            Session to run it in
	 * @return Paths
	 * @throws ResourceInterfaceException
	 *             A resource interface exception occurred
	 */
	public List<Entity> searchForOntology(Resource resource,
			Entity resourcePath, String ontologyType, String ontologyTerm,
			SecureSession session) throws ResourceInterfaceException {

		List<Entity> returns = new ArrayList<Entity>();
		if (resource == null) {
			for (Resource searchResource : rc.getPathResources()) {
				returns.addAll(searchResourceForOntologyTerm(
						(PathResourceImplementationInterface) searchResource
								.getImplementingInterface(), null,
						ontologyType, ontologyTerm, session));
			}
			return returns;
		} else {
			if (resource.getImplementingInterface() instanceof PathResourceImplementationInterface) {
				return searchResourceForOntologyTerm(
						(PathResourceImplementationInterface) resource
								.getImplementingInterface(),
						resourcePath, ontologyType, ontologyTerm, session);
			}
		}
		return null;
	}

	private List<Entity> searchResourceForTerm(
			PathResourceImplementationInterface resource, Entity resourcePath,
			String searchTerm, SecureSession session)
			throws ResourceInterfaceException {
		List<Entity> entities = new ArrayList<Entity>();
		try {
			entities = resource.searchPaths(resourcePath, searchTerm, session);
		} catch (Exception e) {
			log.info("Unable to search for term on resource " + resource
					+ " message: " + e.getMessage());
		}
		return entities;
	}

	private List<Entity> searchResourceForOntologyTerm(
			PathResourceImplementationInterface resource, Entity resourcePath,
			String ontologyType, String ontologyTerm, SecureSession session)
			throws ResourceInterfaceException {
		List<Entity> entities = new ArrayList<Entity>();
		try {
			entities = resource.searchOntology(resourcePath, ontologyType,
					ontologyTerm, session);
		} catch (Exception e) {
			log.info("Unable to search for ontology term on resource "
					+ resource + " message: " + e.getMessage());
		}
		return entities;
	}

	/**
	 * Returns a list of entities that represent all the resources that
	 * implement the Path Resource Interface
	 * 
	 * @return Available Path Resources
	 */
	public List<Entity> getAllResourcePaths() {
		List<Entity> returns = new ArrayList<Entity>();

		for (Resource resource : rc.getPathResources()) {
			Entity entity = new Entity("/" + resource.getName());
			entity.setName(resource.getName());
			entity.setDisplayName(resource.getName());
			returns.add(entity);
		}

		return returns;
	}
}
