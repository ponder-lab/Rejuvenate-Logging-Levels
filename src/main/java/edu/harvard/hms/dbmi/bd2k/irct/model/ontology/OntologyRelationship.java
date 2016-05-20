/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.ontology;

/**
 * An implementable interface of relationship types that is implemented by a
 * given resource as an enum
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public interface OntologyRelationship {
	/**
	 * Returns the inverse of the given relationship. If no inverse exists it returns itself
	 * 
	 * @return Inverse if it exists
	 */
	public OntologyRelationship getInverse();
	
	
	/**
	 * Returns the name of the given relationship type
	 * @return Name
	 */
	public String getName();
}
