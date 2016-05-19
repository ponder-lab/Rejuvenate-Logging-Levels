/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.controller;

import java.util.Map;

import javax.ejb.Stateful;
//import javax.inject.Inject;

import edu.harvard.hms.dbmi.bd2k.irct.exception.ActionNotSetException;
import edu.harvard.hms.dbmi.bd2k.irct.exception.FieldException;
import edu.harvard.hms.dbmi.bd2k.irct.exception.JoinActionSetupException;
import edu.harvard.hms.dbmi.bd2k.irct.model.join.IRCTJoin;

/**
 * A stateless controller for creating and running joins.
 * 
 * NOTE: THIS CONTROLLER HAS NOT BEEN FULL IMPLEMENTED
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@Stateful
public class JoinController {
	
//	@Inject
//	private ResultController rc;

	private IRCTJoin joinType;

	/**
	 * Creates a new join of the type passed in.
	 * 
	 * @param joinType
	 *            The name of the join to create
	 */
	public void createJoin(IRCTJoin joinType) {
		this.setJoinType(joinType);
	}
	
	
	public void setup(Map<String, String> parameters) throws ActionNotSetException, FieldException, JoinActionSetupException {

		if(this.joinType == null) {
			throw new ActionNotSetException("Join has not been created");
		}
		//TODO: FILL IN
		
//		Map<String, Object> actionParameters = Utilities.createActionParametersFromStringMap(this.joinType.getFields(), parameters, rc);
//		if(actionParameters != null) {
//			this.joinType.getJoinImplementation().setup(actionParameters);
//		}
	}


	/**
	 * @return the joinType
	 */
	public IRCTJoin getJoinType() {
		return joinType;
	}


	/**
	 * @param joinType the joinType to set
	 */
	public void setJoinType(IRCTJoin joinType) {
		this.joinType = joinType;
	}
}
