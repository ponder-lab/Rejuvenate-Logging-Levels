package edu.harvard.hms.dbmi.bd2k.irct.exception;

public class SubQueryNotFoundException extends Exception {

	private static final long serialVersionUID = 3783973832912306772L;

	public SubQueryNotFoundException(Long sqId) {
		super("SubQuery " + sqId + " was not found");
	}

}
