/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.exception;

/**
 * Signals that the join type is not supported
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class JoinTypeNotSupported extends Exception {

	private static final long serialVersionUID = -6100576565318538938L;

	/**
	 * Creates an exception indicating that the join type is not supported
	 * @param joinType Join Type
	 */
	public JoinTypeNotSupported(String joinType) {
		super(joinType + " is not supported");
	}
}
