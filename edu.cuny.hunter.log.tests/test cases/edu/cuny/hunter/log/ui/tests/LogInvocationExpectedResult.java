package edu.cuny.hunter.log.ui.tests;

import java.util.logging.Level;

import edu.cuny.hunter.log.core.analysis.PreconditionFailure;

public class LogInvocationExpectedResult {
	private String logExpression;
	private Level logLevel;
	private PreconditionFailure expectedFailure;

	public LogInvocationExpectedResult(String logExpression, Level logLevel, PreconditionFailure expectedFailure) {
		this.logExpression = logExpression;
		this.logLevel = logLevel;
		this.setExpectedFailure(expectedFailure);
	}

	public String getLogExpression() {
		return this.logExpression;
	}

	public Level getLogLevel() {
		return this.logLevel;
	}

	public PreconditionFailure getExpectedFailure() {
		return expectedFailure;
	}

	public void setExpectedFailure(PreconditionFailure expectedFailure) {
		this.expectedFailure = expectedFailure;
	}
}
