/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.exception;

/**
 * An exception occurred in a predicate
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class PredicateException extends Exception {
	private static final long serialVersionUID = 2350119710872195996L;

	/**
	 * Create an exception that an unspecified error occurred in the the predicate.
	 * @param message Message
	 */
	public PredicateException(String message) {
		super(message);
	}
}
