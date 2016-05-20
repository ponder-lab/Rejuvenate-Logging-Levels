/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.executable;

/**
 * An enumeration of the available executable states
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public enum ExecutableStatus {
	/**
	 * Created but not running
	 */
	CREATED,
	/**
	 * Waiting for the next action
	 */
	WAITING,
	/**
	 * Currently running
	 * 
	 */
	RUNNING,
	/**
	 * Currently blocked
	 */
	BLOCKED,
	/**
	 * The executable has been terminated
	 */
	TERMINATED, 
	/**
	 * The executable has completed
	 */
	COMPLETED
}
