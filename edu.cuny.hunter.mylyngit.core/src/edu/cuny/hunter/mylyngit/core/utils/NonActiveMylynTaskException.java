package edu.cuny.hunter.mylyngit.core.utils;

public class NonActiveMylynTaskException extends Exception {

	private static final long serialVersionUID = -139367659219681243L;

	public NonActiveMylynTaskException() {
		super();
	}

	public NonActiveMylynTaskException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public NonActiveMylynTaskException(String message, Throwable cause) {
		super(message, cause);
	}

	public NonActiveMylynTaskException(String message) {
		super(message);
	}

	public NonActiveMylynTaskException(Throwable cause) {
		super(cause);
	}
}
