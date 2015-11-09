/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.exception;

/**
 * Signals that the subquery was not found
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class SubQueryNotFoundException extends Exception {

	private static final long serialVersionUID = 3783973832912306772L;

	/**
	 * Creates an exception that states that the subquery was not found
	 * 
	 * @param sqId Subquery id
	 */
	public SubQueryNotFoundException(Long sqId) {
		super("SubQuery " + sqId + " was not found");
	}

}
