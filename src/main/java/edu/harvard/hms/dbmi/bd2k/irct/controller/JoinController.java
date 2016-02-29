package edu.harvard.hms.dbmi.bd2k.irct.controller;

import javax.ejb.Stateless;

import edu.harvard.hms.dbmi.bd2k.irct.model.query.JoinType;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;

/**
 * A stateless controller for creating and running joins.
 * 
 * NOTE: THIS CONTROLLER HAS NOT BEEN FULL IMPLEMENTED
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@Stateless
public class JoinController {

	/**
	 * Creates a new join of the type passed in.
	 * 
	 * @param joinName
	 *            The name of the join to create
	 * @return The created join
	 */
	public JoinType createJoin(String joinName) {
		// TODO: Fill in
		return null;
	}

	/**
	 * Determines if the join that has been passed in is valid or not. A join is
	 * considered valid if it contains valid values for all the required fields.
	 * 
	 * 
	 * @param join
	 *            The join to check
	 * @return True if the join is valid, false if it is not valid
	 */
	public boolean validJoin(JoinType join) {
		// TODO: Fill in
		return false;
	}

	/**
	 * Runs a join that has been passed to it.
	 * 
	 * @param join The join to run
	 * @return Results of the join
	 */
	public Result runJoin(JoinType join) {
		// TODO: Fill in
		return null;
	}

}
