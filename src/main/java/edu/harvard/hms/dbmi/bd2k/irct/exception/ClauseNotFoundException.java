/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.exception;

/**
 * Signals that the clause could not be found
 * @author Jeremy R. Easton-Marks
 *
 */
public class ClauseNotFoundException extends Exception {
	private static final long serialVersionUID = 476770172910966728L;

	/**
	 * Creates an exception indicating that the clause could not be found
	 * 
	 * @param clauseId Clause id
	 */
	public ClauseNotFoundException(Long clauseId) {
		super("Unable to find clause " + clauseId);
	}
}
