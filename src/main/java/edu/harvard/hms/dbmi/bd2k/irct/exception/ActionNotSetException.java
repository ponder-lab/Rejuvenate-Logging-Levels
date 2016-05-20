/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.exception;

/**
 * Indicates a Action not set Exception occurred of some type
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class ActionNotSetException extends Exception {
	private static final long serialVersionUID = -1519310685687710074L;

	/**
	 * Create an exception that an action has not been setup
	 * @param message Message
	 */
	public ActionNotSetException(String message) {
		super(message);
	}
}
