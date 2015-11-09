/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.query;

/**
 * Logical Operator enumerations provide a choice of three different operators
 * that can be used to combine different where clauses or other parts of a query
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public enum LogicalOperator {
	AND, OR, NOT
}
