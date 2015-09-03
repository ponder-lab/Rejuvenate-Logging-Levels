package edu.harvard.hms.dbmi.bd2k.irct.exception;

/**
 * Signals that the join type is not supported
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class JoinTypeNotSupported extends Exception {

	private static final long serialVersionUID = -6100576565318538938L;

	/**
	 * Creates an exception indicating that the join type is not supported
	 * @param joinType Join Type
	 */
	public JoinTypeNotSupported(String joinType) {
		super(joinType + " is not supported");
	}
}
