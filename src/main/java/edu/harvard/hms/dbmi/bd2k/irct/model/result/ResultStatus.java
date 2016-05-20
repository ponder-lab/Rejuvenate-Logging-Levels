/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.result;

/**
 * An enum representing all the different states a result can be in.
 * 
 * CREATED - The result has been created but not run
 * RUNNING - The result is currently running but has not completed
 * COMPLETE - The result is ready
 * AVAILABLE - The result is available to the user
 * ERROR - An error occurred
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public enum ResultStatus {
	CREATED, RUNNING, AVAILABLE, COMPLETE, ERROR;
}