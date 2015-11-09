/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.result.exception;

/**
 * Signals that the result set exception occurred
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class ResultSetException extends Exception {
	private static final long serialVersionUID = -2674146066302094719L;
	/**
	 * Creates a result set exception with a describing message
	 * 
	 * @param message Message
	 */
	public ResultSetException(String message) {
		super(message);
	}

	/**
	 * Creates a result set exception with a describing message, and origin exception
	 * 
	 * @param message Message
	 * @param exception Origin Exception
	 */
	public ResultSetException(String message, Exception exception) {
		super(message, exception);
	}

}
