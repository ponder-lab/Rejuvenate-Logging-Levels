package edu.harvard.hms.dbmi.bd2k.irct.exception;

public class JoinTypeNotSupported extends Exception {

	private static final long serialVersionUID = -6100576565318538938L;

	public JoinTypeNotSupported(String resource) {
		super(resource + " is not supported");
	}
}
