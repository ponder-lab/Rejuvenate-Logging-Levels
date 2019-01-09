package edu.cuny.hunter.log.core.analysis;

public enum Failure {

	CURRENTLY_NOT_HANDLED(1),
	NO_ENOUGH_DATA(2),
	MISSING_JAVA_ELEMENT(3),
	READ_ONLY_ELEMENT(4),
	BINARY_ELEMENT(5),
	GENERATED_ELEMENT(6);
	
	private int code;

	private Failure(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}
}
