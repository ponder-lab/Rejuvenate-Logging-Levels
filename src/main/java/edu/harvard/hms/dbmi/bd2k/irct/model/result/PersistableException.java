package edu.harvard.hms.dbmi.bd2k.irct.model.result;

import java.io.IOException;

public class PersistableException extends Exception {

	

	private static final long serialVersionUID = -8653407809077481304L;
	public PersistableException(String message) {
		super(message);
	}
	
	public PersistableException(String message, Exception exception) {
		super(message, exception);
	}

}
