package edu.harvard.hms.dbmi.bd2k.irct.exception;

/**
 * Signals that the predicate type is not supported
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class PredicateTypeNotSupported extends Exception {

	private static final long serialVersionUID = -7674396916771461548L;
	
	/**
	 * Creates a predicate type is not supported exception
	 * 
	 * @param predicate Predicate name
	 */
	public PredicateTypeNotSupported(String predicate) {
		super(predicate + " is not supported");
	}

}
