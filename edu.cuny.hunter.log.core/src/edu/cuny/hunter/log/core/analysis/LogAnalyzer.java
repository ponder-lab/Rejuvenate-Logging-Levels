package edu.cuny.hunter.log.core.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
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
	 * Set of log invocations that their log levels are not lower in catch blocks
	 */
	private HashSet<LogInvocation> logInvsNotLoweredInCatch = new HashSet<LogInvocation>();

	private HashSet<LogInvocation> logInvsNotLoweredInIfStatement = new HashSet<LogInvocation>();

	private HashSet<LogInvocation> logInvsNotLoweredByKeywords = new HashSet<LogInvocation>();

	private HashSet<LogInvocation> logInvsNotRaisedByKeywords = new HashSet<LogInvocation>();

	private Set<LogInvocation> logInvocationSet = new HashSet<>();

	private HashMap<Level, Integer> levelToInt = new HashMap<Level, Integer>();

	private boolean notLowerLogLevelInIfStatement;

	private boolean notLowerLogLevelInCatchBlock;

	private boolean notLowerLogLevelWithKeyWords;

	private boolean notRaiseLogLevelWithoutKeyWords;

	private boolean useLogCategoryWithConfig;

	private boolean checkIfCondition;

	private boolean useLogCategory;

	private int maxTransDistance;

	private Map<IMethod, Float> methodToDOI = new HashMap<>();

	private ArrayList<Float> boundary;

	/**
	 * A set of keywords in log messages for lowering log levels.
	 */
	private static final Set<String> KEYWORDS_IN_LOG_MESSAGES_FOR_LOWERING = Stream
			.of("fail", "disabl", "error", "exception", "collision", "reboot", "terminat", "throw", "should", "start",
					"tried", "try", "empty", "launch", "init", "does not", "doesn't", "did not", "didn't", "stop",
					"shut", "run", "deprecat", "kill", "finish", "ready", "wait", "dead", "alive", "creat", "debug",
					"info", "warn", "fatal", "severe", "config ", "fine", "trace", "FYI", "unknown", "could not",
					"cannot", "couldn't", "can't", "can not", "interrupt", "have no", "has no", "had no","unsupport", "not support",
					"wrong", "reject", "cancel", "not recognize", "invalid", "timed out", "unable", "trigger", "expected",
					"unavailable", "not available")
			.collect(Collectors.toSet());
	/**
	 * A set of keywords in log messages for raising log levels.
	 */
	private static final Set<String> KEYWORDS_IN_LOG_MESSAGES_FOR_RAISING = Stream
			.of("fail", "disabl", "error", "exception", "collision", "reboot", "terminat", "throw", "should have",
					"should've", "tried", "empty", "does not", "doesn't", "stop", "shut", "kill", "dead", "not alive")
			.collect(Collectors.toSet());

	private boolean test;

	public LogAnalyzer(boolean isTest) {
		this.test = isTest;
	}

	public ArrayList<Float> getBoundary() {
		return this.boundary;
	}

	public LogAnalyzer(boolean useConfigLogLevelCategory, boolean useLogLevelCategory,
			boolean notLowerLogLevelInCatchBlock, boolean checkIfCondition, boolean notLowerLogLevelInIfStatement,
			boolean notLowerLogLevelWithKeyWords, boolean notRaiseLogLevelWithoutKeywords, int maxTransDistance) {
		this.useLogCategoryWithConfig = useConfigLogLevelCategory;
		this.useLogCategory = useLogLevelCategory;
		this.notLowerLogLevelInCatchBlock = notLowerLogLevelInCatchBlock;
		this.notLowerLogLevelInIfStatement = notLowerLogLevelInIfStatement;
		this.notLowerLogLevelWithKeyWords = notLowerLogLevelWithKeyWords;
		this.notRaiseLogLevelWithoutKeyWords = notRaiseLogLevelWithoutKeywords;
		this.checkIfCondition = checkIfCondition;
		this.maxTransDistance = maxTransDistance;
	}

	public LogAnalyzer() {
	}

	public void analyze(HashSet<MethodDeclaration> methodDecsForAnalyzedMethod) {
		this.collectDOIValues(methodDecsForAnalyzedMethod);
		this.analyzeLogInvs(methodDecsForAnalyzedMethod);
	}

	/**
	 * Analyze project without git history.
	 */
	public void analyze() {
		this.collectDOIValues(Collections.emptySet());
		this.analyzeLogInvs(Collections.emptySet());
	}

	/**
	 * Build boundary and analyze log invocations.
	 */
	private void analyzeLogInvs(Set<MethodDeclaration> methodDecsForAnalyzedMethod) {
		// build boundary
		boundary = this.buildBoundary(this.methodToDOI.values());

		Set<IMethod> validMethods = this.methodToDOI.keySet();

		// It's used by the limitation of max transformation distance.
		this.createLevelToInt();

		// check whether action is needed
		for (LogInvocation logInvocation : this.logInvocationSet) {
			// Methods not analyzed will not be considered for transformation.
			if (!validMethods.contains(logInvocation.getEnclosingEclipseMethod()))
				logInvocation.setAction(Action.NONE, null);
			else {
				if (this.checkCodeModification(logInvocation) && this.checkEnoughData(logInvocation))
					if (this.doAction(logInvocation))
						LOGGER.info("Do action: " + logInvocation.getAction() + "! The changed log expression is "
								+ logInvocation.getExpression());
			}
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

		/**
		 * Do not change a log level in a logging statement if there exists an immediate
		 * if statement whose condition contains a log level or a case label mentioning
		 * a log level.
		 */
		if (this.checkIfCondition) {
			if (checkIfConditionHavingLevel(logInvocation.getExpression())
					|| checkCaseHavingLevel(logInvocation.getExpression())) {
				logInvocation.setAction(Action.NONE, null);
				this.logInvsNotTransformedInIf.add(logInvocation);
				return false;
			}
		}

		Level rejuvenatedLogLevel = getRejuvenatedLogLevel(this.boundary, logInvocation);

		if (rejuvenatedLogLevel == null)
			return false;

		if (this.notLowerLogLevelWithKeyWords) {
			if (Util.isLogMessageWithKeywords(logInvocation.getExpression(), KEYWORDS_IN_LOG_MESSAGES_FOR_LOWERING)
					&& currentLogLevel.intValue() > rejuvenatedLogLevel.intValue()) {
				logInvocation.setAction(Action.NONE, null);
				this.logInvsNotLoweredByKeywords.add(logInvocation);
				return false;
			}
		}

		if (this.notRaiseLogLevelWithoutKeyWords) {
			if (!Util.isLogMessageWithKeywords(logInvocation.getExpression(), KEYWORDS_IN_LOG_MESSAGES_FOR_RAISING)
					&& currentLogLevel.intValue() < rejuvenatedLogLevel.intValue()) {
				logInvocation.setAction(Action.NONE, null);
				this.logInvsNotRaisedByKeywords.add(logInvocation);
				return false;
			}
		}

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

		// Adjust transformations by the max transformation distance.
		rejuvenatedLogLevel = this.adjustTransformationByDistance(currentLogLevel, rejuvenatedLogLevel);

		if (rejuvenatedLogLevel == null) {
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
	 * Adjust transformations if their transformation distance is over the distance
	 * limitation.
	 * 
	 * @return Log level: after adjusting.
	 */
	private Level adjustTransformationByDistance(Level currentLevel, Level rejuvenatedLogLevel) {

		// Discard all transformations if allowed max transformation distance is
		// negative or equal to 0.
		if (maxTransDistance <= 0)
			return null;

		int transformationDistance = this.levelToInt.get(rejuvenatedLogLevel) - this.levelToInt.get(currentLevel);

		// if it's over the max transformation distance which are allowed by users
		if (Math.abs(transformationDistance) > this.maxTransDistance) {
			if (transformationDistance > 0)
				transformationDistance = this.maxTransDistance;
			else
				transformationDistance = -this.maxTransDistance;
		}

		int rejuveLogLevelValue = this.levelToInt.get(currentLevel) + transformationDistance;
		for (Map.Entry<Level, Integer> entry : this.levelToInt.entrySet()) {
			if (entry.getValue() == rejuveLogLevelValue)
				return entry.getKey();
		}

		return null;
	}

	/**
	 * Maintain a map from log level to an integer value. We are using this map to
	 * calculate transformation distance.
	 */
	private void createLevelToInt() {
		if (this.useLogCategory) {
			this.levelToInt.put(Level.FINEST, 0);
			this.levelToInt.put(Level.FINER, 1);
			this.levelToInt.put(Level.FINE, 2);
			this.levelToInt.put(Level.INFO, 3);
		} else if (this.useLogCategoryWithConfig) {
			this.levelToInt.put(Level.FINEST, 0);
			this.levelToInt.put(Level.FINER, 1);
			this.levelToInt.put(Level.FINE, 2);
			this.levelToInt.put(Level.INFO, 3);
			this.levelToInt.put(Level.WARNING, 4);
			this.levelToInt.put(Level.SEVERE, 5);
		} else {
			this.levelToInt.put(Level.FINEST, 0);
			this.levelToInt.put(Level.FINER, 1);
			this.levelToInt.put(Level.FINE, 2);
			this.levelToInt.put(Level.CONFIG, 3);
			this.levelToInt.put(Level.INFO, 4);
			this.levelToInt.put(Level.WARNING, 5);
			this.levelToInt.put(Level.SEVERE, 6);
		}
	}

	/**
	 * Get the rejuvenated log level based on boundary.
	 * 
	 * @param boundary
	 * @param DOI
	 * @return the rejuvenated log level
	 */
	private Level getRejuvenatedLogLevel(ArrayList<Float> boundary, LogInvocation logInvocation) {
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
	 * Build a list of boundary. The DOI values could be divided into 7 groups by
	 * this boundary. 7 groups are corresponding to 7 logging levels
	 * 
	 * @param degreeOfInterests
	 * @return a list of boundary
	 */
	private ArrayList<Float> buildBoundary(Collection<Float> degreeOfInterests) {
		float min = getMinDOI(degreeOfInterests);
		float max = getMaxDOI(degreeOfInterests);
		ArrayList<Float> boundary = new ArrayList<>();
		if (min < max) {
			if (this.useLogCategory) {
				float interval = (max - min) / 4;
				IntStream.range(0, 4).forEach(i -> boundary.add(min + i * interval));
				boundary.add(max);
			} else if (this.useLogCategoryWithConfig) {
				float interval = (max - min) / 6;
				IntStream.range(0, 6).forEach(i -> boundary.add(min + i * interval));
				boundary.add(max);
			} else {
				float interval = (max - min) / 7;
				IntStream.range(0, 7).forEach(i -> boundary.add(min + i * interval));
				boundary.add(max);
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
	private float getMinDOI(Collection<Float> degreeOfInterests) {
		float min = 0;
		for (float d : degreeOfInterests)
			if (d < min)
				min = d;
		return min;
	}

	private float getMaxDOI(Collection<Float> degreeOfInterests) {
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
				Expression condition = ((IfStatement) node).getExpression();
				if (containingKeywords(condition)) {
					LOGGER.info("We meet a logging wrapping: \n" + node);
					return true;
				}
			}
			node = node.getParent();
		}
		return false;
	}

	/**
	 * Check case label mentions log levels.
	 */
	private static boolean checkCaseHavingLevel(ASTNode node) {
		int nodePosition = node.getStartPosition();
		while (node != null) {
			if (node instanceof SwitchStatement) {
				SwitchStatement switchStatement = (SwitchStatement) node;

				Map<Integer, SwitchCase> postionToSwitchCase = new HashMap<Integer, SwitchCase>();
				// get a list of positions for switch cases
				switchStatement.accept(new ASTVisitor() {
					@Override
					public boolean visit(SwitchCase switchCase) {
						postionToSwitchCase.put(switchCase.getStartPosition(), switchCase);
						return true;
					}
				});

				// get the corresponding case label for the log invocation.
				Set<Integer> positions = postionToSwitchCase.keySet();
				// tmp is the max poisition before nodePosition
				int tmp = 0;
				for (int p : positions)
					if (p < nodePosition && p >= tmp) {
						tmp = p;
					}
				if (tmp == 0)
					return false;
				SwitchCase switchCase = postionToSwitchCase.get(tmp);

				// check whether the case label contains the keywords
				if (containingKeywords(switchCase.toString())) {
					LOGGER.info("We meet a logging wrapping: \n" + node);
					return true;
				}
				return false;
			}
			node = node.getParent();
		}
		return false;
	}

	/**
	 * Check whether if condition or case label contains a particular log level.
	 */
	private static boolean containingKeywords(Object expression) {
		String label = expression.toString().toUpperCase();
		if (label.contains("CONFIG") || label.contains("FINE") || label.contains("FINER") || label.contains("FINEST")
				|| label.contains("SEVERE") || label.contains("WARN") || label.contains("INFO")
				|| label.contains("FATAL") || label.contains("ERROR") || label.contains("DEBUG")
				|| label.contains("TRACE"))
			return true;
		else
			return false;

	}

	/**
	 * Returns true if the given logging expression is immediately contained within
	 * an if statement not having an else clause (i.e., guarded) and false
	 * otherwise.
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
	 * We only consider analyzed methods.
	 */
	private void collectDOIValues(Set<MethodDeclaration> methods) {
		Set<IMethod> enclosingMethods = getEnclosingMethods();
		methods.forEach(m -> {
			IMethodBinding methodBinding = m.resolveBinding();
			if (methodBinding != null) {
				IMethod method = (IMethod) methodBinding.getJavaElement();
				float doiValue = Util.getDOIValue(method, enclosingMethods);
				this.methodToDOI.put(method, doiValue);
			}
		});
	}

	public Set<IMethod> getEnclosingMethods() {
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

	public HashSet<LogInvocation> getLogInvsNotRaisedByKeywords() {
		return this.logInvsNotRaisedByKeywords;
	}

	public Map<IMethod, Float> getMethodToDOI() {
		return this.methodToDOI;
	}
}
