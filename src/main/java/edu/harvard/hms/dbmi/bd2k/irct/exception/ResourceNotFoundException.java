package edu.harvard.hms.dbmi.bd2k.irct.exception;

public class ResourceNotFoundException extends Exception {
	private static final long serialVersionUID = -5584542985464086513L;

	public ResourceNotFoundException(String resource) {
		super(resource + " could not be found");
	}
}
