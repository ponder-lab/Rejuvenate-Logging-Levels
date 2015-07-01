package edu.harvard.hms.dbmi.bd2k.irct.controller;

public class LogicalOperatorNotFound extends Exception {
	private static final long serialVersionUID = 864956518641339000L;

	public LogicalOperatorNotFound(String logicalOperator) {
		super("Logical Operator: " + logicalOperator + " not found");
	}
}
