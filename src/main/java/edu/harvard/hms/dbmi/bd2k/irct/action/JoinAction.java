/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.action;

import java.util.Map;

import javax.naming.NamingException;

import edu.harvard.hms.dbmi.bd2k.irct.model.join.Join;
import edu.harvard.hms.dbmi.bd2k.irct.model.join.JoinImplementation;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultStatus;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.ResultSetException;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;
import edu.harvard.hms.dbmi.bd2k.irct.util.Utilities;
import edu.harvard.hms.dbmi.bd2k.irct.event.IRCTEventListener;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;

/**
 * Implements the Action interface to run a join
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class JoinAction implements Action {
	private Join join;
	private ActionStatus status;
	private Result result;
	
	private IRCTEventListener irctEventListener;

	/**
	 * Sets up the IRCT Join Action
	 * 
	 * @param joinType The join to run
	 */
	public void setup(Join join) {
		this.status = ActionStatus.CREATED;
		this.join = join;
		this.irctEventListener = Utilities.getIRCTEventListener();
	}
	
	@Override
	public void updateActionParams(Map<String, Result> updatedParams) {
		for(String key : updatedParams.keySet()) {
			this.join.getObjectValues().put(key, updatedParams.get(key).getId().toString());
		}
	}
	
	@Override
	public void run(SecureSession session) {
		irctEventListener.beforeJoin(session, join);
		this.status = ActionStatus.RUNNING;

		try {
			JoinImplementation joinImplementation = (JoinImplementation) join.getJoinImplementation();
			result = ActionUtilities.createResult(joinImplementation.getJoinDataType());
			if(session != null) {
				result.setUser(session.getUser());
			}
			
			join.getObjectValues().putAll(ActionUtilities.convertResultSetFieldToObject(session.getUser(), join.getJoinType().getFields(), join.getStringValues()));
			
			result = joinImplementation.run(session, join, result);
			this.status = ActionStatus.COMPLETE;
			ActionUtilities.mergeResult(result);
		} catch (PersistableException | NamingException | ResultSetException e) {
			result.setMessage(e.getMessage());
			this.status = ActionStatus.ERROR;
		}
		
		this.status = ActionStatus.COMPLETE;
		irctEventListener.afterJoin(session, join);
	}

	@Override
	public Result getResults(SecureSession session) throws ResourceInterfaceException {
		if(this.result.getResultStatus() != ResultStatus.ERROR && this.result.getResultStatus() != ResultStatus.COMPLETE) {
			this.result = this.join.getJoinImplementation().getResults(this.result);
		}
		try {
			ActionUtilities.mergeResult(result);
			this.status = ActionStatus.COMPLETE;
		} catch (NamingException e) {
			result.setMessage(e.getMessage());
			this.status = ActionStatus.ERROR;
		}
		return this.result;
	}

	@Override
	public ActionStatus getStatus() {
		return status;
	}
}
