package edu.harvard.hms.dbmi.bd2k.irct.exception;

public class ClauseIsNotTheCorrectType extends Exception {
	private static final long serialVersionUID = -7647212583226916787L;
	
	public ClauseIsNotTheCorrectType(Long clauseId) {
		super("Clause " + clauseId + " is not of the expected type");
	}

}
