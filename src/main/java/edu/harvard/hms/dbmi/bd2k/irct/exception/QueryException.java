package edu.harvard.hms.dbmi.bd2k.irct.exception;

public class QueryException extends Exception {
	private static final long serialVersionUID = -5061650218975355817L;

	/**
	 * An exception occurred setting up the Join Action
	 * 
	 * @param message Message
	 */
	public QueryException(String message) {
		super(message);
	}
}
