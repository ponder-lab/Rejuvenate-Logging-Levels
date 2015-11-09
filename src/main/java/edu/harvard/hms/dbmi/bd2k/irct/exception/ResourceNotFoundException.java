/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.exception;

/**
 * Signals that the resource could not be found
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class ResourceNotFoundException extends Exception {
	private static final long serialVersionUID = -5584542985464086513L;

	/**
	 * Creates an exception that the resource was not found 
	 * 
	 * @param resource Resource id
	 */
	public ResourceNotFoundException(String resource) {
		super(resource + " could not be found");
	}
}
