package edu.harvard.hms.dbmi.bd2k.irct.exception;

/**
 * Indicates a Process Exception occurred of some type
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class ProcessException extends Exception {
	private static final long serialVersionUID = -5061650218975355817L;

	/**
	 * An exception occurred setting up a process
	 * 
	 * @param message Message
	 */
	public ProcessException(String message) {
		super(message);
	}
}
