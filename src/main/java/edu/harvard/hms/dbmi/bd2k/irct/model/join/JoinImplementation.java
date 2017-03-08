/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.join;

import java.util.Map;

import edu.harvard.hms.dbmi.bd2k.irct.exception.JoinActionSetupException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.ResultSetException;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;

/**
 * The JoinAction interface extends the Action interface for Joins. All joins
 * must implement this interface.
 * 
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public interface JoinImplementation {
	/**
	 * Sets up the join with the given parameters.
	 * 
	 * @param parameters
	 *            Join Parameters
	 * @throws JoinActionSetupException An exception occurred
	 */
	void setup(Map<String, Object> parameters) throws JoinActionSetupException;

	Result run(SecureSession session, Join join, Result result) throws ResultSetException, PersistableException;
	
	Result getResults(Result result);
	
	/**
	 * Returns the result data type
	 * 
	 * @return Result data type
	 */
	ResultDataType getJoinDataType();
	
}
