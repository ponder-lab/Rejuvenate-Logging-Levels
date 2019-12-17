package edu.cuny.hunter.log.core.analysis;

import java.util.logging.Logger;

import org.eclipse.jdt.core.dom.MethodInvocation;
import org.slf4j.event.Level;

import edu.cuny.hunter.log.core.utils.LoggerNames;

public class LogInvocationSlf4j extends AbstractLogInvocation {
	private static final Logger LOGGER = Logger.getLogger(LoggerNames.LOGGER_NAME);
	Level level;

	/**
	 * The current log level
	 */
	Level logLevel;

	public LogInvocationSlf4j(MethodInvocation logExpression, Level loggingLevel, boolean inCatchBlock) {
		this.setLogExpression(logExpression);
		this.setInCatchBlock(inCatchBlock);

		this.logLevel = loggingLevel;

		if (loggingLevel == null) {
			this.addStatusEntry(Failure.CURRENTLY_NOT_HANDLED, this.getExpression()
					+ " has argument LogRecord or log level variable which cannot be handled yet.");
		}

		this.updateDOI();
	}

	public void logInfo() {
		LOGGER.info("Find a log expression." + this.getExpression().toString() + " The logging level: " + this.logLevel
				+ ". Degree of Interest " + this.getDegreeOfInterestValue() + ". ");
	}
}