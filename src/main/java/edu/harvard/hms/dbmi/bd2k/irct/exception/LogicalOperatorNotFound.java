/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.exception;

/**
 * Signals that the logical operator was not found
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class LogicalOperatorNotFound extends Exception {
	private static final long serialVersionUID = 864956518641339000L;

	/**
	 * Creates an exception indicating that the logical operator was not found
	 * 
	 * @param logicalOperator Logical Operator
	 */
	public LogicalOperatorNotFound(String logicalOperator) {
		super("Logical Operator: " + logicalOperator + " not found");
	}
}
