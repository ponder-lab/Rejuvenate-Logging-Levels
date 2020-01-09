package edu.cuny.hunter.log.core.analysis;

import java.util.logging.Logger;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.corext.refactoring.structure.CompilationUnitRewrite;
import org.slf4j.event.Level;

import edu.cuny.hunter.log.core.utils.LoggerNames;
import edu.cuny.hunter.log.core.utils.Util;

@SuppressWarnings("restriction")
public class LogInvocationSlf4j extends AbstractLogInvocation {
	private static final Logger LOGGER = Logger.getLogger(LoggerNames.LOGGER_NAME);

	/**
	 * The current log level
	 */
	Level logLevel;

	/**
	 * Current log level.
	 */
	private Level newLogLevel;

	ActionSlf4j action;

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

	public void setAction(ActionSlf4j action, Level newLogLevel) {
		this.action = action;
		this.newLogLevel = newLogLevel;
	}

	/**
	 * Get the log level for this log invocation. It's a log level of slf4j.
	 */
	public Level getLogLevel() {
		return this.logLevel;
	}

	public void setLogLevel(Level logLevel) {
		this.logLevel = logLevel;
	}

	public ActionSlf4j getAction() {
		return this.action;
	}

	public Level getNewLogLevel() {
		return newLogLevel;
	}

	/**
	 * Do transformation!
	 * 
	 * @param rewrite
	 */
	public void transform(CompilationUnitRewrite rewrite) {
		switch (this.getAction()) {
		case CONVERT_TO_TRACE:
			this.convertTrace(rewrite);
			break;
		case CONVERT_TO_DEBUG:
			this.convertDebug(rewrite);
			break;
		case CONVERT_TO_INFO:
			this.convertToInfo(rewrite);
			break;
		case CONVERT_TO_WARN:
			this.convertWarn(rewrite);
			break;
		case CONVERT_TO_ERROR:
			this.convertError(rewrite);
			break;
		default:
			break;
		}
	}

	private void convertTrace(CompilationUnitRewrite rewrite) {
		this.convert("trace", "TRACE", rewrite);
	}

	private void convertError(CompilationUnitRewrite rewrite) {
		this.convert("severe", "SEVERE", rewrite);
	}

	private void convertWarn(CompilationUnitRewrite rewrite) {
		this.convert("warn", "WARN", rewrite);
	}

	private void convertToInfo(CompilationUnitRewrite rewrite) {
		this.convert("info", "INFO", rewrite);
	}

	private void convertDebug(CompilationUnitRewrite rewrite) {
		this.convert("debug", "DEBUG", rewrite);
	}

	/**
	 * Basic method to do transformation.
	 */
	@Override
	public void convert(String target, String targetLogLevel, CompilationUnitRewrite rewrite) {

		MethodInvocation expression = this.getExpression();

		if (expression != null)
			if (expression.getNodeType() == ASTNode.METHOD_INVOCATION) {

				String identifier = expression.getName().getIdentifier();
				AST ast = expression.getAST();

				ASTRewrite astRewrite = rewrite.getASTRewrite();

				// The methods (e.g., warning() -> critical()).
				if (Util.isLoggingLevelMethodSlf4J(identifier)) {

					SimpleName newMethodName = ast.newSimpleName(target);
					astRewrite.replace(expression.getName(), newMethodName, null);
					this.setNames(expression.getName(), newMethodName);
				}
			}
	}

}