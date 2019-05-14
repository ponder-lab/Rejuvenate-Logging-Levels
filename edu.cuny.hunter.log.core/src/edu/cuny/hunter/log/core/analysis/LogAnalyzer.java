package edu.cuny.hunter.log.core.analysis;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.mylyn.context.core.IDegreeOfInterest;
import edu.cuny.hunter.log.core.messages.Messages;
import edu.cuny.hunter.log.core.utils.LoggerNames;
import edu.cuny.hunter.log.core.utils.Util;

public class LogAnalyzer extends ASTVisitor {

	private static final Logger LOGGER = Logger.getLogger(LoggerNames.LOGGER_NAME);

	/**
	 * Set of log invocations not transformed due to if condition.
	 */
	private HashSet<LogInvocation> logInvsNotTransformedInIf = new HashSet<LogInvocation>();

	/**
	 * Set of log invocations that their log levels are not lower in catch
	 * blocks
	 */
	private HashSet<LogInvocation> logInvsNotLoweredInCatch = new HashSet<LogInvocation>();

	private HashSet<LogInvocation> logInvsNotLoweredInIfStatement = new HashSet<LogInvocation>();

	private HashSet<LogInvocation> logInvsNotLoweredByKeywords = new HashSet<LogInvocation>();

	private HashSet<MethodDeclaration> methodDeclarations = new HashSet<>();

	private Set<LogInvocation> logInvocationSet = new HashSet<>();

	private boolean notLowerLogLevelInIfStatement;

	private boolean notLowerLogLevelInCatchBlock;

	private boolean notLowerLogLevelWithKeyWords;

	private boolean useLogCategoryWithConfig;

	private boolean checkIfCondition;

	private boolean useLogCategory;

	private HashSet<Float> DOIValues = new HashSet<>();

	private LinkedList<Float> boundary;

	/**
	 * A set of keywords in log messages.
	 */
	private final Set<String> KEYWORDS_IN_LOG_MESSAGES = Stream.of("fail", "disable", "error", "exception", "collision",
			"reboot", "terminate", "throw", "should", "start", "should", "tried", "empty", "launch", "init", "does not",
			"doesn't", "stop", "shut", "run", "deprecate", "kill", "finish", "ready", "wait")
			.collect(Collectors.toSet());

	private boolean test;

	public LogAnalyzer(boolean isTest) {
		this.test = isTest;
	}

	public LinkedList<Float> getBoundary() {
		return this.boundary;
	}

	public LogAnalyzer(boolean useConfigLogLevelCategory, boolean useLogLevelCategory,
			boolean notLowerLogLevelInCatchBlock, boolean checkIfCondition, boolean notLowerLogLevelInIfStatement,
			boolean notLowerLogLevelWithKeyWords) {
		this.useLogCategoryWithConfig = useConfigLogLevelCategory;
		this.useLogCategory = useLogLevelCategory;
		this.notLowerLogLevelInCatchBlock = notLowerLogLevelInCatchBlock;
		this.notLowerLogLevelInIfStatement = notLowerLogLevelInIfStatement;
		this.notLowerLogLevelWithKeyWords = notLowerLogLevelWithKeyWords;
		this.checkIfCondition = checkIfCondition;
	}

	public LogAnalyzer() {
	}

	/**
	 * Analyze project without git history.
	 */
	public void analyze() {
		this.collectDOIValues(this.methodDeclarations);
		this.analyzeLogInvs();
	}

	/**
	 * Build boundary and analyze log invocations.
	 */
	private void analyzeLogInvs() {
		// build boundary
		boundary = this.buildBoundary(this.DOIValues);
		// check whether action is needed
		for (LogInvocation logInvocation : this.logInvocationSet) {
			if (this.checkCodeModification(logInvocation) && this.checkEnoughData(logInvocation))
				if (this.doAction(logInvocation))
					LOGGER.info("Do action: " + logInvocation.getAction() + "! The changed log expression is "
							+ logInvocation.getExpression());
		}
	}

	/**
	 * Check whether all DOI values are the same.
	 */
	private boolean checkEnoughData(LogInvocation logInvocation) {
		if (this.boundary == null) {
			logInvocation.addStatusEntry(Failure.NO_ENOUGH_DATA,
					"The DOI values are all same or no DOI values. Cannot get valid results.");
			LOGGER.info("The DOI values are all same or no DOI values. Cannot get valid results.");
			return false;
		}
		return true;
	}

	/**
	 * Do transformation action.
	 */
	private boolean doAction(LogInvocation logInvocation) {
		// No enclosing method.
		if (logInvocation.getEnclosingEclipseMethod() == null) {
			logInvocation.addStatusEntry(Failure.CURRENTLY_NOT_HANDLED,
					logInvocation.getExpression() + " has no enclosing method.");
			return false;
		}

		Level currentLogLevel = logInvocation.getLogLevel();
		// Cannot get valid log level from log invocations.
		if (currentLogLevel == null)
			return false;

		// DOI not in intervals
		if (logInvocation.getDegreeOfInterestValue() < this.boundary.get(0)
				|| logInvocation.getDegreeOfInterestValue() > this.boundary.get(this.boundary.size() - 1)) {
			logInvocation.setAction(Action.NONE, null);
			return false;
		}

		if (this.notLowerLogLevelWithKeyWords) {
			if (Util.isLogMessageWithKeywords(logInvocation.getExpression(), KEYWORDS_IN_LOG_MESSAGES)) {
				logInvocation.setAction(Action.NONE, null);
				this.logInvsNotLoweredByKeywords.add(logInvocation);
				return false;
			}
		}

		/**
		 * Do not change a log level in a logging statement if there exists an
		 * immediate if statement whose condition contains a log level.
		 */
		if (this.checkIfCondition) {
			if (checkIfConditionHavingLevel(logInvocation.getExpression())) {
				logInvocation.setAction(Action.NONE, null);
				this.logInvsNotTransformedInIf.add(logInvocation);
				return false;
			}
		}

		Level rejuvenatedLogLevel = getRejuvenatedLogLevel(this.boundary, logInvocation);

		if (rejuvenatedLogLevel == null)
			return false;

		// process not lower log levels in catch blocks
		if (logInvocation.getInCatchBlock() && currentLogLevel.intValue() > rejuvenatedLogLevel.intValue()) {
			this.logInvsNotLoweredInCatch.add(logInvocation);
			logInvocation.setAction(Action.NONE, null);
			return false;
		}

		if (this.notLowerLogLevelInIfStatement)
			// process not lower log levels in if statements.
			if (checkIfBlock(logInvocation.getExpression())
					&& currentLogLevel.intValue() > rejuvenatedLogLevel.intValue()) {
				this.logInvsNotLoweredInIfStatement.add(logInvocation);
				logInvocation.setAction(Action.NONE, null);
				return false;
			}

		if ((currentLogLevel == rejuvenatedLogLevel) // current log level is
														// same to transformed
														// log level

				|| (currentLogLevel == Level.ALL || currentLogLevel == Level.OFF) // not
																					// consider
																					// all
																					// and
																					// off

				|| (this.useLogCategory && (currentLogLevel == Level.CONFIG || currentLogLevel == Level.WARNING
						|| currentLogLevel == Level.SEVERE)) // process log
																// category
																// (CONFIG/WARNING/SERVRE)

				|| (this.useLogCategoryWithConfig && (currentLogLevel == Level.CONFIG)) // process
																						// log
																						// category
																						// (CONFIG)
		) {
			logInvocation.setAction(Action.NONE, null);
			return false;
		}

		if (rejuvenatedLogLevel == Level.FINEST)
			logInvocation.setAction(Action.CONVERT_TO_FINEST, Level.FINEST);
		if (rejuvenatedLogLevel == Level.FINER)
			logInvocation.setAction(Action.CONVERT_TO_FINER, Level.FINER);
		if (rejuvenatedLogLevel == Level.FINE)
			logInvocation.setAction(Action.CONVERT_TO_FINE, Level.FINE);
		if (rejuvenatedLogLevel == Level.CONFIG)
			logInvocation.setAction(Action.CONVERT_TO_CONFIG, Level.CONFIG);
		if (rejuvenatedLogLevel == Level.INFO)
			logInvocation.setAction(Action.CONVERT_TO_INFO, Level.INFO);
		if (rejuvenatedLogLevel == Level.WARNING)
			logInvocation.setAction(Action.CONVERT_TO_WARNING, Level.WARNING);
		if (rejuvenatedLogLevel == Level.SEVERE)
			logInvocation.setAction(Action.CONVERT_TO_SEVERE, Level.SEVERE);

		return true;
	}

	/**
	 * Get the rejuvenated log level based on boundary.
	 * 
	 * @param boundary
	 * @param DOI
	 * @return the rejuvenated log level
	 */
	private Level getRejuvenatedLogLevel(LinkedList<Float> boundary, LogInvocation logInvocation) {
		float DOI = logInvocation.getDegreeOfInterestValue();
		if (boundary == null)
			return null;

		if (this.useLogCategory) {
			LOGGER.info("Use log category: do not consider config/warning/severe.");
			if (DOI >= boundary.get(0) && DOI < boundary.get(1))
				return Level.FINEST;
			if (DOI < boundary.get(2))
				return Level.FINER;
			if (DOI < boundary.get(3))
				return Level.FINE;
			if (DOI <= boundary.get(4))
				return Level.INFO;
		} else if (this.useLogCategoryWithConfig) {
			LOGGER.info("Use log category: do not consider config.");
			if (DOI >= boundary.get(0) && DOI < boundary.get(1))
				return Level.FINEST;
			if (DOI < boundary.get(2))
				return Level.FINER;
			if (DOI < boundary.get(3))
				return Level.FINE;
			if (DOI < boundary.get(4))
				return Level.INFO;
			if (DOI < boundary.get(5))
				return Level.WARNING;
			if (DOI <= boundary.get(6))
				return Level.SEVERE;
		} else {
			LOGGER.info("Default log category.");
			if (DOI >= boundary.get(0) && DOI < boundary.get(1))
				return Level.FINEST;
			if (DOI < boundary.get(2))
				return Level.FINER;
			if (DOI < boundary.get(3))
				return Level.FINE;
			if (DOI < boundary.get(4))
				return Level.CONFIG;
			if (DOI < boundary.get(5))
				return Level.INFO;
			if (DOI < boundary.get(6))
				return Level.WARNING;
			if (DOI <= boundary.get(7))
				return Level.SEVERE;
		}
		return null;
	}

	/**
	 * Build a list of boundary. The DOI values could be divided into 7 groups
	 * by this boundary. 7 groups are corresponding to 7 logging levels
	 * 
	 * @param degreeOfInterests
	 * @return a list of boundary
	 */
	private LinkedList<Float> buildBoundary(HashSet<Float> degreeOfInterests) {
		float min = getMinDOI(degreeOfInterests);
		float max = getMaxDOI(degreeOfInterests);
		LinkedList<Float> boundary = new LinkedList<>();
		if (min < max) {
			if (this.useLogCategory || this.useLogCategoryWithConfig) {
				// The DOI boundaries should be built as if the "treat CONFIG as
				// a log category" was selected. This will produce a boundaries
				// table that includes the levels FINEST, FINER, FINE, INFO,
				// WARNING, and SEVERE #185.
				float interval = (max - min) / 6;
				IntStream.range(0, 7).forEach(i -> boundary.add(min + i * interval));

				if (this.useLogCategory) {
					// The DOI boundaries should then be *modified* to *remove*
					// the boundaries for WARNING and SEVERE #185.
					LOGGER.info(() -> "Original boundaries are: " + boundary);
					IntStream.range(0, 2).forEach(i -> boundary.remove(boundary.size() - 1));
					LOGGER.info(() -> "New boundaries are: " + boundary);
				}
			} else {
				float interval = (max - min) / 7;
				IntStream.range(0, 8).forEach(i -> boundary.add(min + i * interval));
			}
			return boundary;
		} else
			return null;
	}

	/**
	 * Get the minimum of DOIs
	 * 
	 * @param degreeOfInterests
	 */
	private float getMinDOI(HashSet<Float> degreeOfInterests) {
		float min = 0;
		for (float d : degreeOfInterests)
			if (d < min)
				min = d;
		return min;
	}

	private float getMaxDOI(HashSet<Float> degreeOfInterests) {
		float max = 0;
		for (float d : degreeOfInterests)
			if (d > max)
				max = d;
		return max;
	}

	public Set<LogInvocation> getLogInvocationSet() {
		return this.logInvocationSet;
	}

	public void setTest(boolean test) {
		this.test = test;
	}

	/**
	 * This method is used to find a set of logging objects
	 */
	@Override
	public boolean visit(MethodInvocation node) {

		Level logLevel = null;

		try {
			logLevel = Util.isLogExpression(node, test);
		} catch (IllegalStateException e) {
			LOGGER.warning("Need to process the variable of logging level or LogRecord!");
			this.createLogInvocation(node, null, false);
			return super.visit(node);
		}

		if (logLevel != null)
			this.createLogInvocation(node, logLevel,
					this.notLowerLogLevelInCatchBlock && this.checkLogInCatchBlock(node));

		return super.visit(node);
	}

	/**
	 * Check whether logging statements are in catch blocks.
	 */
	private boolean checkLogInCatchBlock(ASTNode node) {
		while (node != null) {
			if (node instanceof CatchClause)
				return true;
			node = node.getParent();
		}
		return false;
	}

	/**
	 * Check if condition mentions log levels.
	 */
	private static boolean checkIfConditionHavingLevel(ASTNode node) {
		while (node != null) {
			if (node instanceof IfStatement) {
				String condition = ((IfStatement) node).getExpression().toString().toUpperCase();
				if (condition.contains("CONFIG") || condition.contains("FINE") || condition.contains("FINER")
						|| condition.contains("FINEST") || condition.contains("SEVERE") || condition.contains("WARN")
						|| condition.contains("INFO") || condition.contains("FATAL") || condition.contains("ERROR")
						|| condition.contains("DEBUG") || condition.contains("TRACE")) {
					LOGGER.info("We meet a logging wrapping: \n" + node);
					return true;
				}
			}
			node = node.getParent();
		}
		return false;
	}

	/**
	 * Returns true if the given logging expression is immediately contained
	 * within an if statement not having an else clause (i.e., guarded) and
	 * false otherwise.
	 */
	private static boolean checkIfBlock(MethodInvocation loggingExpression) {
		ASTNode loggingStatement = loggingExpression.getParent();
		ASTNode parent = loggingStatement.getParent();

		if (parent instanceof Block)
			parent = parent.getParent();

		if (parent instanceof IfStatement) {
			IfStatement ifStatement = (IfStatement) parent;
			Statement elseStatement = ifStatement.getElseStatement();

			// if there's no else clause
			if (elseStatement == null) {
				// is it the first statement of the then statement?
				Statement thenStatement = ifStatement.getThenStatement();

				// if it's a block.
				if (thenStatement.getNodeType() == ASTNode.BLOCK) {
					Block block = (Block) thenStatement;

					// if the block is non-empty.
					if (block.statements().size() > 0) {
						// if it's in the first statement of the block.
						Statement firstStatement = (Statement) block.statements().get(0);

						return firstStatement.getNodeType() != ASTNode.BLOCK
								&& contains(firstStatement, loggingExpression);
					} else
						// it's an empty block.
						throw new IllegalStateException("Block shouldn't be empty.");
				} else
					// it's not a block. Just check the statement.
					return contains(thenStatement, loggingExpression);
			} else
				// there's an else clause. It's not a guarded comment.
				return false;
		} else
			return false;
	}

	@SuppressWarnings("unused")
	private static boolean contains(Statement statement, MethodInvocation loggingExpression) {
		if (statement == null || loggingExpression == null)
			return false;
		else {
			int elseStart = statement.getStartPosition();
			int elseEnd = elseStart + statement.getLength();

			int loggingExpressionStart = loggingExpression.getStartPosition();
			int loggingExpressionEnd = loggingExpressionStart + loggingExpression.getLength();

			return elseStart <= loggingExpressionStart && elseEnd >= loggingExpressionEnd;
		}
	}

	/**
	 * This method is used to find a set of logging objects
	 */
	@Override
	public boolean visit(MethodDeclaration node) {
		this.methodDeclarations.add(node);
		return super.visit(node);
	}

	public void collectDOIValues(HashSet<MethodDeclaration> methods) {
		Set<IMethod> enclosingMethods = getEnclosingMethods();

		methods.forEach(m -> {
			IMethodBinding methodBinding = m.resolveBinding();
			if (methodBinding != null) {
				float doiValue = Util.getDOIValue((IMethod) methodBinding.getJavaElement(), enclosingMethods);
				this.DOIValues.add(doiValue);
			}
		});
	}

	private Set<IMethod> getEnclosingMethods() {
		return this.getLogInvocationSet().parallelStream().map(LogInvocation::getEnclosingEclipseMethod)
				.filter(Objects::nonNull).collect(Collectors.toSet());
	}

	/**
	 * Whether the code is read-only, generated code and in a .class file.
	 */
	public boolean checkCodeModification(LogInvocation logInvocation) {

		CompilationUnit cu = logInvocation.getEnclosingCompilationUnit();
		if (cu != null) {
			IJavaElement element = cu.getJavaElement();
			if (element.isReadOnly()) {
				logInvocation.addStatusEntry(Failure.READ_ONLY_ELEMENT, Messages.ReadOnlyElement);
				return false;
			}

			IMethod method = logInvocation.getEnclosingEclipseMethod();
			if (method != null && method.isBinary()) {
				logInvocation.addStatusEntry(Failure.BINARY_ELEMENT, Messages.BinaryElement);
				return false;
			}

			try {
				if (Util.isGeneratedCode(element)) {
					logInvocation.addStatusEntry(Failure.GENERATED_ELEMENT, Messages.GeneratedElement);
					return false;
				}
			} catch (JavaModelException e) {
				logInvocation.addStatusEntry(Failure.MISSING_JAVA_ELEMENT, Messages.MissingJavaElement);
				return false;
			}
		} else {
			LOGGER.warning("Can't find enclosing compilation unit for: " + logInvocation + ".");
			logInvocation.addStatusEntry(Failure.MISSING_JAVA_ELEMENT, Messages.MissingJavaElement);
			return false;
		}
		return true;

	}

	private void createLogInvocation(MethodInvocation node, Level logLevel, boolean inCatchBlock) {
		LogInvocation logInvocation = new LogInvocation(node, logLevel, inCatchBlock);
		this.getLogInvocationSet().add(logInvocation);
	}

	public void updateDOI() {
		this.getLogInvocationSet().forEach(inv -> {
			inv.updateDOI();
		});
	}

	public HashSet<LogInvocation> getLogInvsNotTransformedInIf() {
		return this.logInvsNotTransformedInIf;
	}

	public HashSet<LogInvocation> getLogInvsNotLoweredInCatch() {
		return this.logInvsNotLoweredInCatch;
	}

	public HashSet<LogInvocation> getLogInvsNotLoweredInIfStatement() {
		return this.logInvsNotLoweredInIfStatement;
	}

	public HashSet<LogInvocation> getLogInvsNotLoweredByKeywords() {
		return this.logInvsNotLoweredByKeywords;
	}

}
