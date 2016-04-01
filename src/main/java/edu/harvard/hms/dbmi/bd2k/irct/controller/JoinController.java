/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.controller;

import java.util.Map;

import javax.ejb.Stateful;
import javax.inject.Inject;

import edu.harvard.hms.dbmi.bd2k.irct.action.join.JoinAction;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ActionNotSetException;
import edu.harvard.hms.dbmi.bd2k.irct.exception.FieldException;
import edu.harvard.hms.dbmi.bd2k.irct.exception.JoinActionSetupException;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.JoinType;
import edu.harvard.hms.dbmi.bd2k.irct.util.Utilities;

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
	
	@Inject
	private ResultController rc;

	private JoinType joinType;
	private JoinAction joinAction;
	/**
	 * Creates a new join of the type passed in.
	 * 
	 * @param joinName
	 *            The name of the join to create
	 * @return The created join
	 */
	public void createJoin(JoinType joinType) {
		this.setJoinType(joinType);
		this.setJoinAction(joinType.getAction());
	}
	
	
	public void setup(Map<String, String> parameters) throws ActionNotSetException, FieldException, JoinActionSetupException {
		if((this.getJoinAction() == null) || (this.getJoinType() == null)) {
			throw new ActionNotSetException("Join has not been created");
		}
		
		Map<String, Object> actionParameters = Utilities.createActionParametersFromStringMap(this.joinType.getFields(), parameters, rc);
		if(actionParameters != null) {
			joinAction.setup(actionParameters);
		}
	}


	/**
	 * @return the joinType
	 */
	public JoinType getJoinType() {
		return joinType;
	}


	/**
	 * @param joinType the joinType to set
	 */
	public void setJoinType(JoinType joinType) {
		this.joinType = joinType;
	}


	/**
	 * @return the joinAction
	 */
	public JoinAction getJoinAction() {
		return joinAction;
	}


	/**
	 * @param joinAction the joinAction to set
	 */
	public void setJoinAction(JoinAction joinAction) {
		this.joinAction = joinAction;
	}
	
}
