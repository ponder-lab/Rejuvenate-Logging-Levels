/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.exception;
/**
 * Indicates a Resource Interface Exception occurred of some type
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class ResourceInterfaceException extends Exception {
	private static final long serialVersionUID = -4688536480746747740L;
	
	/**
	 * Create a Resource Interface Exception with the given message
	 * 
	 * @param message Messsage
	 */
	public ResourceInterfaceException(String message) {
		super(message);
	}

	/**
	 * Create a Resource Interface Exception
	 * 
	 * @param exception Exception
	 */
	public ResourceInterfaceException(Exception exception) {
		super(exception);
	}
	
	/**
	 * Create a Resource Interface Exception with the given message
	 * 
	 * @param message Message
	 * @param exception Exception
	 */
	public ResourceInterfaceException(String message, Exception exception) {
		super(message, exception);
	}
}
