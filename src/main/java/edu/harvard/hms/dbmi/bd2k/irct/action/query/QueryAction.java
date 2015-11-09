/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.action.query;

import edu.harvard.hms.dbmi.bd2k.irct.action.Action;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;

/**
 * This interface extends the action interface for running queries
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public interface QueryAction extends Action {
	/**
	 * Sets up a query to be run on a specific resource
	 * 
	 * @param resource Resource to run on
	 * @param query Query to run
	 */
	void setup(Resource resource, Query query);
	
	/**
	 * Returns the query to be run
	 * 
	 * @return Query to be run
	 */
	Query getQuery();
	
	/**
	 * Sets the query to be run
	 * 
	 * @param query Query to be run
	 */
	void setQuery(Query query);
}
