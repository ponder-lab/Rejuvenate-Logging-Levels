/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.result.exception;

/**
 * Signals that a persistable exception occurred
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class PersistableException extends Exception {
	private static final long serialVersionUID = -8653407809077481304L;

	/**
	 * Create a persistable exception and pass in a message describing it
	 * 
	 * @param message
	 *            Message
	 */
	public PersistableException(String message) {
		super(message);
	}

	/**
	 * Create a persistable exception and pass in a message describing the
	 * exception, and any additional exceptions in the stack trace
	 * 
	 * @param message Message
	 * @param exception Origin Exception
	 */
	public PersistableException(String message, Exception exception) {
		super(message, exception);
	}

}
