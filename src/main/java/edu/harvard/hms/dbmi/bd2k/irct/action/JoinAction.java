/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.action;

import java.util.Map;

import edu.harvard.hms.dbmi.bd2k.irct.model.join.IRCTJoin;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultStatus;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;

/**
 * Implements the QueryAction interface to run a query on a specific instance
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class JoinAction implements Action {
	private IRCTJoin joinType;
	private ActionStatus status;
	private Result result;

	public void setup(IRCTJoin joinType) {
		this.status = ActionStatus.CREATED;
		this.joinType = joinType;
	}
	
	public void updateActionParams(Map<String, Result> updatedParams) {
		for(String key : updatedParams.keySet()) {
			this.joinType.getValues().put(key, updatedParams.get(key).getId().toString());
		}
	}

	public void run(SecureSession session) {
		this.status = ActionStatus.RUNNING;
//		try {
		this.result = this.joinType.getJoinImplementation().run();
//		} catch (ResourceInterfaceException e) {
//			this.status = ActionStatus.ERROR;
//		}
		this.status = ActionStatus.COMPLETE;
	}

	public Result getResults(SecureSession session) throws ResourceInterfaceException {
		if(this.result.getResultStatus() != ResultStatus.ERROR && this.result.getResultStatus() != ResultStatus.COMPLETE) {
			this.result = this.joinType.getJoinImplementation().getResults();
		}
		return this.result;
	}

	@Override
	public ActionStatus getStatus() {
		return status;
	}
}
