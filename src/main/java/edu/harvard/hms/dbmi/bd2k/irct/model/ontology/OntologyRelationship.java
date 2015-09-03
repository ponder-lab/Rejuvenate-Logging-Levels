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
}
