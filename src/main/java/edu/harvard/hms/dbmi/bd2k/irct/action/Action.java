/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.action;

import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultSet;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;

/**
 * The action interface provides a set of basic functionality that all actions must
 * accomplish. An action can be a Join, Query, Process or other. Each action subtype
 * having their own interface that should be used.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public interface Action {
	/**
	 * Runs the given action
	 * @throws ResourceInterfaceException An error occurred 
	 */
	void run() throws ResourceInterfaceException;

	/**
	 * Gets the results
	 * 
	 * @return Results ResultSet
	 * @throws ResourceInterfaceException An error occurred
	 */
	ResultSet getResults() throws ResourceInterfaceException;
}
