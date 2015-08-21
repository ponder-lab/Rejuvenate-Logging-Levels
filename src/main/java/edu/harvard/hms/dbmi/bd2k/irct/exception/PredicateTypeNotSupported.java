package edu.harvard.hms.dbmi.bd2k.irct.exception;

public class PredicateTypeNotSupported extends Exception {

	private static final long serialVersionUID = -7674396916771461548L;
	

	public PredicateTypeNotSupported(String predicate) {
		super(predicate + " is not supported");
	}

}
