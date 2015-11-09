/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.controller;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;

import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyRelationship;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyType;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Path;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.PathResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;
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
	
	/**
	 * Gets a list of relationships available for a given resource
	 * 
	 * @param resource Resource
	 * @return List of available relationships
	 */
	public List<OntologyRelationship> getRelationships(Resource resource) {
		return ((PathResourceImplementationInterface) resource
				.getImplementingInterface()).relationships();
	}

	/**
	 * Get relationship from a string
	 * 
	 * @param resource Resource
	 * @param relationshipName Relationship string
	 * @return Relationship
	 */
	public OntologyRelationship getRelationshipFromString(Resource resource,
			String relationshipName) {
		return ((PathResourceImplementationInterface) resource
				.getImplementingInterface())
				.getRelationshipFromString(relationshipName);
	}

	/**
	 * Returns the root of the resource
	 * 
	 * @param resource Resource
	 * @return Root path
	 */
	public List<Path> getPathRoot(Resource resource) {
		return ((PathResourceImplementationInterface) resource
				.getImplementingInterface()).getPathRoot();
	}

	/**
	 * Gets the paths of the relationship with a given path on a specific resource
	 * 
	 * @param resource Resource
	 * @param path Path
	 * @param relationship Relationship type
	 * @return List of paths of that relationship type
	 * @throws ResourceInterfaceException An error occurred with the resource
	 */
	public List<Path> getPathRelationship(Resource resource, Path path,
			OntologyRelationship relationship)
			throws ResourceInterfaceException {
		if (path == null) {
			return new ArrayList<Path>();
		}
		return ((PathResourceImplementationInterface) resource
				.getImplementingInterface()).getPathRelationship(path,
				relationship);
	}

	/**
	 * Returns the ontology type resource
	 * 
	 * @param resource Resource
	 * @return Ontology type
	 */
	public OntologyType getPathType(Resource resource) {
		return ((PathResourceImplementationInterface) resource
				.getImplementingInterface()).getOntologyType();
	}

	/**
	 * Returns the path given the string
	 * 
	 * @param resource Resource
	 * @param path Path String
	 * @return Path
	 */
	public Path getPathFromString(Resource resource, String path) {
		return ((PathResourceImplementationInterface) resource
				.getImplementingInterface()).getPathFromString(path);
	}
}
