/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.event;

import java.util.Map;

/**
 * The IRCT event listener base that other event listeners extend.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public interface IRCTEvent {
	/**
	 * Initializes the event listener
	 *  
	 * @param parameters Map of parameters
	 */
	public void init(Map<String, String> parameters);
}