package edu.cuny.hunter.log.core.analysis;

public enum Failure {

	/**
	 * The issues that currently are not handled.
	 * The most common reason is that our tool is missing data flow analysis.
	 */
	CURRENTLY_NOT_HANDLED(1),
	/**
	 * All DOI values of methods are same. It is always caused by missing active task.
	 */
	NO_ENOUGH_DATA(2),
	/**
	 * Cannot detect elements of Java model.
	 */
	MISSING_JAVA_ELEMENT(3),
	/**
	 * Element is read-only.
	 */
	READ_ONLY_ELEMENT(4),
	/**
	 * Element is from a CLASS file.
	 */
	BINARY_ELEMENT(5),
	/**
	 * This resource subtree is marked as derived. 
	 */
	GENERATED_ELEMENT(6);
	
	private int code;

	private Failure(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}
}
