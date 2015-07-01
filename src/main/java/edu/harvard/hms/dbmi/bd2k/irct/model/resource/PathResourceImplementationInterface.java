/*
 *  This file is part of Inter-Resource Communication Tool (IRCT).
 *
 *  IRCT is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  IRCT is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with IRCT.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.harvard.hms.dbmi.bd2k.irct.model.resource;

import java.util.List;

import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyRelationship;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyType;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Path;

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
	Path getPathRoot();

	/**
	 * Given a path give all the associated paths of that type of relationship
	 * 
	 * @param path Path
	 * @param relationship Relationships
	 * @return Paths
	 */
	List<Path> getPathRelationship(Path path, OntologyRelationship relationship);

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
