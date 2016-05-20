/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.action;

/**
 * An enumeration of different statuses an action can have. 
 * 
 * CREATED - The action has been created, but not run
 * RUNNING - The action is currently running and no results has been created
 * COMPLETE - The action has been run and is complete
 * ERROR - The action has an encountered an error
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public enum ActionStatus {
	CREATED, RUNNING, COMPLETE, ERROR
}
