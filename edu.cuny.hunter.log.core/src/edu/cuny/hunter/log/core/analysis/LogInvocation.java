package edu.cuny.hunter.log.core.analysis;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.corext.refactoring.structure.CompilationUnitRewrite;

import edu.cuny.hunter.log.core.utils.LoggerNames;
import edu.cuny.hunter.log.core.utils.Util;

@SuppressWarnings("restriction")
public class LogInvocation extends AbstractLogInvocation {

	private static final Logger LOGGER = Logger.getLogger(LoggerNames.LOGGER_NAME);

	/**
	 * The current log level
	 */
	Level logLevel;

	/**
	 * Current log level.
	 */
	private Level newLogLevel;

	Action action;

	public LogInvocation(MethodInvocation logExpression, Level loggingLevel, boolean inCatchBlock) {
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
	
	public void setAction(Action action, Level newLogLevel) {
		this.action = action;
		this.newLogLevel = newLogLevel;
	}

	public Level getLogLevel() {
		return logLevel;
	}

	public Action getAction() {
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
	@Override
	public void transform(CompilationUnitRewrite rewrite) {
		switch (this.getAction()) {
		case CONVERT_TO_FINEST:
			this.convertToFinest(rewrite);
			break;
		case CONVERT_TO_FINER:
			this.convertToFiner(rewrite);
			break;
		case CONVERT_TO_FINE:
			this.convertToFine(rewrite);
			break;
		case CONVERT_TO_INFO:
			this.convertToInfo(rewrite);
			break;
		case CONVERT_TO_CONFIG:
			this.convertToConfig(rewrite);
			break;
		case CONVERT_TO_WARNING:
			this.convertToWarning(rewrite);
			break;
		case CONVERT_TO_SEVERE:
			this.convertToSevere(rewrite);
			break;
		default:
			break;
		}
	}

	private void convertToFinest(CompilationUnitRewrite rewrite) {
		this.convert("finest", "FINEST", rewrite);
	}

	private void convertToSevere(CompilationUnitRewrite rewrite) {
		this.convert("severe", "SEVERE", rewrite);
	}

	private void convertToWarning(CompilationUnitRewrite rewrite) {
		this.convert("warning", "WARNING", rewrite);
	}

	private void convertToConfig(CompilationUnitRewrite rewrite) {
		this.convert("config", "CONFIG", rewrite);
	}

	private void convertToInfo(CompilationUnitRewrite rewrite) {
		this.convert("info", "INFO", rewrite);
	}

	private void convertToFine(CompilationUnitRewrite rewrite) {
		this.convert("fine", "FINE", rewrite);
	}

	private void convertToFiner(CompilationUnitRewrite rewrite) {
		this.convert("finer", "FINER", rewrite);
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
				if (Util.isLoggingLevelMethod(identifier)) {

					SimpleName newMethodName = ast.newSimpleName(target);
					astRewrite.replace(expression.getName(), newMethodName, null);
					this.setNames(expression.getName(), newMethodName);
				} else // The parameters (e.g., log(Level.WARNING) ->
						// log(Level.CRITICAL).
				if (isLogMethod(identifier)) {
					Name firstArgument = (Name) expression.arguments().get(0);
					// log(WARNING, ...)
					if (firstArgument.isSimpleName()) {
						Name newLevelName = ast.newSimpleName(targetLogLevel);
						astRewrite.replace(firstArgument, newLevelName, null);
						this.setNames(firstArgument, newLevelName);
					} else {

						QualifiedName argument = (QualifiedName) firstArgument;
						Name qualifier = argument.getQualifier();

						QualifiedName newParaName = null;
						// log(java.util.logging.Level.warning, ...)
						if (qualifier.isQualifiedName())
							newParaName = ast
									.newQualifiedName(
											ast.newQualifiedName(
													ast.newQualifiedName(
															ast.newQualifiedName(ast.newSimpleName("java"),
																	ast.newSimpleName("util")),
															ast.newSimpleName("logging")),
													ast.newSimpleName("Level")),
											ast.newSimpleName(targetLogLevel));
						// log(Level.warning,...)
						if (qualifier.isSimpleName()) {
							newParaName = ast.newQualifiedName(ast.newSimpleName("Level"),
									ast.newSimpleName(targetLogLevel));
						}
						astRewrite.replace(argument, newParaName, null);
						this.setNames(argument, newParaName);
					}
				}

			}
	}

	/**
	 * Check whether the log method could have the parameter for logging level
	 */
	private static boolean isLogMethod(String methodName) {
		if (methodName.equals("log") || methodName.equals("logp") || methodName.equals("logrb"))
			return true;
		return false;
	}
}
