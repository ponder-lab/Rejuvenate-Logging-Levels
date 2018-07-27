package edu.cuny.hunter.log.core.analysis;

public enum PreconditionFailure {

	CURRENTLY_NOT_HANDLED(1),
	NOT_ENOUGH_DATA(2);

	private int code;

	private PreconditionFailure(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}
}
