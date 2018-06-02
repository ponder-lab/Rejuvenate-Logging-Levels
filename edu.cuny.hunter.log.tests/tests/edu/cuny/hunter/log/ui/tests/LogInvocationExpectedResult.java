package edu.cuny.hunter.log.ui.tests;

import java.util.logging.Level;

public class LogInvocationExpectedResult {
	private String logExpression;
	Level logLevel;

	public LogInvocationExpectedResult(String logExpression, Level logLevel) {
		this.logExpression = logExpression;
		this.logLevel = logLevel;
	}

	public String getLogExpression() {
		return this.logExpression;
	}

	public Level getLogLevel() {
		return this.logLevel;
	}
}
