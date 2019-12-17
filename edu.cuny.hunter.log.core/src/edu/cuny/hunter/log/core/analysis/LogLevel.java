package edu.cuny.hunter.log.core.analysis;

import org.slf4j.event.Level;

/**
 * Store log level of an log invocation.
 * 
 * @author Yiming Tang
 *
 */
public class LogLevel {

	java.util.logging.Level level = null;
	Level slf4jLevel = null;
	LoggingFramework framework = LoggingFramework.NONE;

	public java.util.logging.Level getLogLevel() {
		return this.level;
	}

	public void setLogLevel(java.util.logging.Level level) {
		this.level = level;
	}

	public Level getSlf4jLevel() {
		return this.slf4jLevel;
	}

	public void setSlf4jLevel(Level slf4jLevel) {
		this.slf4jLevel = slf4jLevel;
	}

	public void setLoggingFramework(LoggingFramework framework) {
		this.framework = framework;
	}

	public LoggingFramework getLoggingFramework() {
		return this.framework;
	}
}
