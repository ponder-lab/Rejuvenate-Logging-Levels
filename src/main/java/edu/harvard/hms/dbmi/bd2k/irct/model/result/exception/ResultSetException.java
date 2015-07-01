package edu.harvard.hms.dbmi.bd2k.irct.model.result.exception;


public class ResultSetException extends Exception {
	private static final long serialVersionUID = -2674146066302094719L;
	
	public ResultSetException(String message) {
		super(message);
	}

	public ResultSetException(String message, Exception exception) {
		super(message, exception);
	}

}
