package edu.cuny.hunter.log.ui.tests;

import java.util.logging.Level;

import edu.cuny.hunter.log.core.analysis.Failure;

public class LogInvocationExpectedResult {
	private String logExpression;
	private Level logLevel;
	private Failure expectedFailure;

	public LogInvocationExpectedResult(String logExpression, Level logLevel, Failure expectedFailure) {
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

	public Failure getExpectedFailure() {
		return expectedFailure;
	}

	public void setExpectedFailure(Failure expectedFailure) {
		this.expectedFailure = expectedFailure;
	}
}
