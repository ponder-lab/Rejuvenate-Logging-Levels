/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.exception;

/**
 * Signals that the predicate type is not supported
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class PredicateTypeNotSupported extends Exception {

	private static final long serialVersionUID = -7674396916771461548L;
	
	/**
	 * Creates a predicate type is not supported exception
	 * 
	 * @param predicate Predicate name
	 */
	public PredicateTypeNotSupported(String predicate) {
		super(predicate + " is not supported");
	}

}
