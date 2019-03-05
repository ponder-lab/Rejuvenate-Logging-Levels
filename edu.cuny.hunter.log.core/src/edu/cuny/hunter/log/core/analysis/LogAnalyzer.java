package edu.cuny.hunter.log.core.analysis;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.mylyn.context.core.IDegreeOfInterest;
import edu.cuny.hunter.log.core.messages.Messages;
import edu.cuny.hunter.log.core.utils.LoggerNames;
import edu.cuny.hunter.log.core.utils.Util;
import edu.cuny.hunter.mylyngit.core.analysis.MylynGitPredictionProvider;

public class LogAnalyzer extends ASTVisitor {

	private static final Logger LOGGER = Logger.getLogger(LoggerNames.LOGGER_NAME);

	private HashSet<MethodDeclaration> methodDeclarations = new HashSet<>();

	private Set<LogInvocation> logInvocationSet = new HashSet<>();

	private boolean notLowerLogLevelInCatchBlock = false;

	private boolean useLogCategoryWithConfig = false;

	private boolean checkIfCondition = false;

	private boolean useLogCategory = false;

	private HashSet<Float> DOIValues = new HashSet<>();

	private LinkedList<Float> boundary;

	private boolean test;

	public LogAnalyzer(boolean isTest) {
		this.test = isTest;
	}

	public LinkedList<Float> getBoundary() {
		return this.boundary;
	}

	public LogAnalyzer(boolean useConfigLogLevelCategory, boolean useLogLevelCategory,
			boolean notLowerLogLevelInCatchBlock, boolean checkIfCondition) {
		this.useLogCategoryWithConfig = useConfigLogLevelCategory;
		this.useLogCategory = useLogLevelCategory;
		this.notLowerLogLevelInCatchBlock = notLowerLogLevelInCatchBlock;
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

	private boolean doAction(LogInvocation logInvocation) {

		Level currentLogLevel = logInvocation.getLogLevel();

		/**
		 * Do not change a log level in a logging statement if there exists an immediate
		 * if statement whose condition contains a log level that matches the log level
		 * of the logging statement in question.
		 */
		if (this.checkIfCondition) {
			Level logLevelInIfCond = this.checkIfBlock(logInvocation.getExpression());
			if ((logLevelInIfCond != null) && (logLevelInIfCond == currentLogLevel)) {
				logInvocation.setAction(Action.NONE, null);
				return false;
			}
		}

		Level rejuvenatedLogLevel = getRejuvenatedLogLevel(this.boundary, logInvocation);

		if (rejuvenatedLogLevel == null || currentLogLevel == null)
			return false;

		if ((currentLogLevel == rejuvenatedLogLevel) // current log level is same to transformed log level

				|| (currentLogLevel == Level.ALL || currentLogLevel == Level.OFF) // not consider all and off

				|| (this.useLogCategory && (currentLogLevel == Level.CONFIG || currentLogLevel == Level.WARNING
						|| currentLogLevel == Level.SEVERE)) // process log category (CONFIG/WARNING/SERVRE)

				|| (this.useLogCategoryWithConfig && (currentLogLevel == Level.CONFIG)) // process log category (CONFIG)

				|| (logInvocation.getInCatchBlock() // process not lower log levels in catch blocks
						&& (currentLogLevel.intValue() > rejuvenatedLogLevel.intValue()))) {
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
	 * Build a list of boundary. The DOI values could be divided into 7 groups by
	 * this boundary. 7 groups are corresponding to 7 logging levels
	 * 
	 * @param degreeOfInterests
	 * @return a list of boundary
	 */
	private LinkedList<Float> buildBoundary(HashSet<Float> degreeOfInterests) {
		float min = getMinDOI(degreeOfInterests);
		float max = getMaxDOI(degreeOfInterests);
		LinkedList<Float> boundary = new LinkedList<>();
		if (min < max) {
			if (this.useLogCategory) {
				float interval = (max - min) / 4;
				IntStream.range(0, 5).forEach(i -> boundary.add(min + i * interval));
			} else if (this.useLogCategoryWithConfig) {
				float interval = (max - min) / 6;
				IntStream.range(0, 7).forEach(i -> boundary.add(min + i * interval));
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
	private Level checkIfBlock(ASTNode node) {
		while (node != null) {
			if (node instanceof IfStatement) {
				String condition = ((IfStatement) node).getExpression().toString();
				if (condition.contains("CONFIG"))
					return Level.CONFIG;
				if (condition.contains("FINE"))
					return Level.FINE;
				if (condition.contains("FINER"))
					return Level.FINER;
				if (condition.contains("FINEST"))
					return Level.FINEST;
				if (condition.contains("SEVERE"))
					return Level.SEVERE;
				if (condition.contains("WARNING"))
					return Level.WARNING;
				if (condition.contains("INFO"))
					return Level.INFO;
			}
			node = node.getParent();
		}
		return null;
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
		methods.forEach(m -> {
			IMethodBinding methodBinding = m.resolveBinding();
			if (methodBinding != null) {
				IDegreeOfInterest degreeOfInterest = Util.getDegreeOfInterest((IMethod) methodBinding.getJavaElement());
				this.DOIValues.add(Util.getDOIValue(degreeOfInterest));
			}
		});
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

	/**
	 * Clear the active task context.
	 */
	public void clearTaskContext(MylynGitPredictionProvider mylynProvider) {
		mylynProvider.clearTaskContext();
	}

	public void updateDOI() {
		this.getLogInvocationSet().forEach(inv -> {
			inv.updateDOI();
		});
	}

}
