package edu.cuny.hunter.logging.core.analysis;

public enum PreconditionFailure {
	CURRENTLY_NOT_HANDLED(1);

	private int code;

	private PreconditionFailure(int code) {
		this.code = code;
	}

	public int getCode() {
		return this.code;
	}
}
