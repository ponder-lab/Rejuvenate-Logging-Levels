package edu.harvard.hms.dbmi.bd2k.irct.exception;

public class ClauseNotFoundException extends Exception {
	private static final long serialVersionUID = 476770172910966728L;

	public ClauseNotFoundException(Long clauseId) {
		super("Unable to find clause " + clauseId);
	}
}
