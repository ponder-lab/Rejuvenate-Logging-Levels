package edu.harvard.hms.dbmi.bd2k.irct.model.ontology;

public enum OntologyRelationship {
	CHILD, PARENT, SIBLING;
	
	private OntologyRelationship inverse;
	
	static {
		CHILD.inverse = PARENT;
		PARENT.inverse = CHILD;
		SIBLING.inverse = SIBLING;
	}
	
	public OntologyRelationship getInverse() {
		return inverse;
	}
	
}
