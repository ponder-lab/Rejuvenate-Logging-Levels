/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.event;

/**
 * The IRCT event listener base that other event listeners extend.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public interface IRCTEvent {
	/**
	 * Returns the name of the event
	 * 
	 * @return Event Name
	 */
	public String getName();
	
	
	/**
	 * Initializes the event listener
	 */
	public void init();
}