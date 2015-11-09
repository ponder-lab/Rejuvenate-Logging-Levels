/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.resource;

import java.util.List;

import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyRelationship;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyType;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Path;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;

/**
 * Provides an implemntation that descripes tha API for any resource that has
 * paths that can be traversed.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public interface PathResourceImplementationInterface extends
		ResourceImplementationInterface {
	/**
	 * Get the root of the resource
	 * 
	 * @return Root
	 */
	List<Path> getPathRoot();
	
	/**
	 * Returns the Ontology relationships 
	 * @return Ontology Relationships
	 */
	List<OntologyRelationship> relationships();
	
	/**
	 * Turns a string into a relationship
	 * 
	 * @param relationship String Representation
	 * @return Relationship
	 */
	OntologyRelationship getRelationshipFromString(String relationship);

	/**
	 * Given a path give all the associated paths of that type of relationship
	 * 
	 * @param path Path
	 * @param relationship Relationships
	 * @return Paths 
	 * @throws ResourceInterfaceException A resource exception occurred 
	 */
	List<Path> getPathRelationship(Path path, OntologyRelationship relationship) throws ResourceInterfaceException;

	/**
	 * Get the ontology type of this resource
	 * 
	 * @return Ontology Type
	 */
	OntologyType getOntologyType();

	/**
	 * Turns a string into a path
	 * 
	 * @param path String representation
	 * @return Path
	 */
	Path getPathFromString(String path);
}
