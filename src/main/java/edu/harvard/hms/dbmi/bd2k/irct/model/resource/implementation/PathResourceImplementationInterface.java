/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation;

import java.util.List;

import edu.harvard.hms.dbmi.bd2k.irct.model.find.FindInformationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyRelationship;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Entity;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;
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
	 * Given a path give all the associated paths of that type of relationship
	 * 
	 * @param path
	 *            Path
	 * @param relationship
	 *            Relationships
	 * @param session Secure Session
	 * @return Paths
	 * @throws ResourceInterfaceException
	 *             A resource exception occurred
	 */
	List<Entity> getPathRelationship(Entity path, OntologyRelationship relationship, SecureSession session)
			throws ResourceInterfaceException;

	/**
	 * Given a path find all entities that match the finds
	 * 
	 * @param path Path
	 * @param find Find Data
	 * @param session Session
	 * @return Paths
	 * @throws ResourceInterfaceException A resource exception occurred
	 */
	List<Entity> find(Entity path, FindInformationInterface findInformation, SecureSession session) throws ResourceInterfaceException;

}
