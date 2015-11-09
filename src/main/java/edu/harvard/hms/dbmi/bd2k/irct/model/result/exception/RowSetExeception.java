/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.result.exception;

/**
 * An exception occurred in the RowSet
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class RowSetExeception extends ResultSetException {

	private static final long serialVersionUID = 7742706330823781183L;
	
	/**
	 * Creates a row set exception and pass in a message describing it
	 * @param message Message
	 */
	public RowSetExeception(String message) {
		super(message);
	}

}
