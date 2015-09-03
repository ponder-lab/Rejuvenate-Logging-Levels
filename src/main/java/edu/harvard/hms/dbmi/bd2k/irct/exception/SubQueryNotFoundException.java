package edu.harvard.hms.dbmi.bd2k.irct.exception;

/**
 * Signals that the subquery was not found
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class SubQueryNotFoundException extends Exception {

	private static final long serialVersionUID = 3783973832912306772L;

	/**
	 * Creates an exception that states that the subquery was not found
	 * 
	 * @param sqId Subquery id
	 */
	public SubQueryNotFoundException(Long sqId) {
		super("SubQuery " + sqId + " was not found");
	}

}
