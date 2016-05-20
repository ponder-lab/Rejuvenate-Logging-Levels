package edu.harvard.hms.dbmi.bd2k.irct.exception;

/**
 * Indicates a Query Exception occurred of some type
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class QueryException extends Exception {
	private static final long serialVersionUID = -5061650218975355817L;

	/**
	 * An exception occurred setting up a query
	 * 
	 * @param message Message
	 */
	public QueryException(String message) {
		super(message);
	}
}
