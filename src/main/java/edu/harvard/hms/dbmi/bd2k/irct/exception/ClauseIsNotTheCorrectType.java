/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.exception;

/**
 * Signals that the clause is not of the correct type
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class ClauseIsNotTheCorrectType extends Exception {
	private static final long serialVersionUID = -7647212583226916787L;
	
	/**
	 * Constructs an exception with information about clause id
	 * 
	 * @param clauseId The clause id
	 */
	public ClauseIsNotTheCorrectType(Long clauseId) {
		super("Clause " + clauseId + " is not of the expected type");
	}

}
