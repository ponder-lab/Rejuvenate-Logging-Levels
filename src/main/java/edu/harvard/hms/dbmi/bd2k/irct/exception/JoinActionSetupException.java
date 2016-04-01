/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.exception;

public class JoinActionSetupException extends Exception {
	private static final long serialVersionUID = -1448158416944133951L;

	/**
	 * An exception occurred setting up the Join Action
	 * 
	 * @param message Message
	 */
	public JoinActionSetupException(String message) {
		super(message);
	}
}
