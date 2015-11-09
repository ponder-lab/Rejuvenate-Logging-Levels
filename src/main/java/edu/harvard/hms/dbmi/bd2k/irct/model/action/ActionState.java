/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.action;

import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultSet;

/**
 * An object used to keep track of an action
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class ActionState {
	private long id;
	private boolean isComplete;
	private ResultSet results;
	private String resourceId;

	/**
	 * Returns the Id
	 * 
	 * @return Id
	 */
	public long getId() {
		return id;
	}

	/**
	 * Sets the Id
	 * 
	 * @param id
	 *            Id
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Returns if the action is complete
	 * 
	 * @return action is complete
	 */
	public boolean isComplete() {
		return isComplete;
	}

	/**
	 * Sets if the actino is complete
	 * 
	 * @param isComplete
	 *            Action is complete
	 */
	public void setComplete(boolean isComplete) {
		this.isComplete = isComplete;
	}

	/**
	 * Returns the result set
	 * 
	 * @return Result Set
	 */
	public ResultSet getResults() {
		return results;
	}

	/**
	 * Sets the result set
	 * 
	 * @param results
	 *            Result Set
	 */
	public void setResults(ResultSet results) {
		this.results = results;
	}

	/**
	 * @return the resourceId
	 */
	public String getResourceId() {
		return resourceId;
	}

	/**
	 * @param resourceId
	 *            the resourceId to set
	 */
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

}
