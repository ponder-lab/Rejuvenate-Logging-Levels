package edu.harvard.hms.dbmi.bd2k.irct.exception;

/**
 * Signals that the clause is not of the correct type
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class ClauseIsNotTheCorrectType extends Exception {
	private static final long serialVersionUID = -7647212583226916787L;
	
	/**
	 * Constructs an exception with information about clause id
	 * 
	 * @param clauseId The clause id
	 */
	public ClauseIsNotTheCorrectType(Long clauseId) {
		super("Clause " + clauseId + " is not of the expected type");
	}

}
