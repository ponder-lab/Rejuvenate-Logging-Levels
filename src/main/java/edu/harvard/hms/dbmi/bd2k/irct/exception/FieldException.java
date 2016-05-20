/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.exception;

/**
 * Indicates a Field Exception occurred of some type
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class FieldException extends Exception {
	private static final long serialVersionUID = 3729644518492681421L;

	/**
	 * Create an exception that an unspecified error occurred in the field.
	 * @param message Message
	 */
	public FieldException(String message) {
		super(message);
	}
}
