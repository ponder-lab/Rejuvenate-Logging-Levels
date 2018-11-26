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
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodInvocation;
import edu.cuny.hunter.log.core.messages.Messages;
import edu.cuny.hunter.log.core.utils.LoggerNames;
import edu.cuny.hunter.log.core.utils.Util;
import edu.cuny.hunter.mylyngit.core.analysis.MylynGitPredictionProvider;

public class LogAnalyzer extends ASTVisitor {

	private static final Logger LOGGER = Logger.getLogger(LoggerNames.LOGGER_NAME);

	private Set<LogInvocation> logInvocationSet = new HashSet<>();

	private static LinkedList<Float> boundary;

	private static boolean useLogCategoryWithConfig = false;

	private static boolean useLogCategory = false;

	private int test;

	public LogAnalyzer(int isTest) {
		this.test = isTest;
	}

	public LogAnalyzer(boolean useConfigLogLevelCategory, boolean useLogLevelCategory) {
		useLogCategoryWithConfig = useConfigLogLevelCategory;
		useLogCategory = useLogLevelCategory;
	}

	public LogAnalyzer() {
	}

	public void analyze() {
		// check failed preconditions.
		this.checkCodeModification();

		HashSet<Float> degreeOfInterests = new HashSet<>();
		for (LogInvocation logInvocation : this.logInvocationSet) {
			logInvocation.logInfo();
			if (logInvocation.getDegreeOfInterest() != null) {
				if ((useLogCategory && (logInvocation.getLogLevel() != Level.CONFIG
						&& logInvocation.getLogLevel() != Level.SEVERE && logInvocation.getLogLevel() != Level.WARNING))
						|| (useLogCategoryWithConfig && logInvocation.getLogLevel() != Level.CONFIG)
						|| (!useLogCategory && !useLogCategoryWithConfig))
					degreeOfInterests.add(logInvocation.getDegreeOfInterestValue());
			}
		}

		// build boundary
		boundary = buildBoundary(degreeOfInterests);
		// check whether action is needed
		for (LogInvocation logInvocation : this.logInvocationSet)
			if (this.doAction(logInvocation))
				LOGGER.info("Do action: " + logInvocation.getAction() + "! The changed log expression is "
						+ logInvocation.getExpression());

	}

	private boolean doAction(LogInvocation logInvocation) {
		Level currentLogLevel = logInvocation.getLogLevel();
		Level rejuvenatedLogLevel = getRejuvenatedLogLevel(boundary, logInvocation);

		if (currentLogLevel == rejuvenatedLogLevel)
			return false;
		if (rejuvenatedLogLevel == null || currentLogLevel == null)
			return false;
		// get rid of log level ALL and OFF
		if (currentLogLevel == Level.ALL || currentLogLevel == Level.OFF)
			return false;
		if (useLogCategory && (currentLogLevel == Level.CONFIG || currentLogLevel == Level.WARNING
				|| currentLogLevel == Level.SEVERE))
			return false;
		if (useLogCategoryWithConfig && (currentLogLevel == Level.CONFIG))
			return false;

		if (rejuvenatedLogLevel == Level.FINEST)
			logInvocation.setAction(Action.CONVERT_TO_FINEST);
		if (rejuvenatedLogLevel == Level.FINER)
			logInvocation.setAction(Action.CONVERT_TO_FINER);
		if (rejuvenatedLogLevel == Level.FINE)
			logInvocation.setAction(Action.CONVERT_TO_FINE);
		if (rejuvenatedLogLevel == Level.CONFIG)
			logInvocation.setAction(Action.CONVERT_TO_CONFIG);
		if (rejuvenatedLogLevel == Level.INFO)
			logInvocation.setAction(Action.CONVERT_TO_INFO);
		if (rejuvenatedLogLevel == Level.WARNING)
			logInvocation.setAction(Action.CONVERT_TO_WARNING);
		if (rejuvenatedLogLevel == Level.SEVERE)
			logInvocation.setAction(Action.CONVERT_TO_SEVERE);

		return true;
	}

	/**
	 * Get the rejuvenated log level based on boundary.
	 * 
	 * @param boundary
	 * @param DOI
	 * @return the rejuvenated log level
	 */
	private static Level getRejuvenatedLogLevel(LinkedList<Float> boundary, LogInvocation logInvocation) {
		float DOI = logInvocation.getDegreeOfInterestValue();
		if (boundary == null)
			return null;
		if (Float.compare(boundary.getFirst(), boundary.getLast()) == 0) {
			logInvocation.addStatusEntry(PreconditionFailure.NO_ENOUGH_DATA,
					"The DOI values are all same or no DOI values. Cannot get valid results.");
			LOGGER.info("The DOI values are all same or no DOI values. Cannot get valid results.");
			return null;
		}

		if (useLogCategory) {
			LOGGER.info("Use log category: config/warning/severe.");
			if (DOI >= boundary.get(0) && DOI < boundary.get(1))
				return Level.FINEST;
			if (DOI < boundary.get(2))
				return Level.FINER;
			if (DOI < boundary.get(3))
				return Level.FINE;
			if (DOI <= boundary.get(4))
				return Level.INFO;
		} else if (useLogCategoryWithConfig) {
			LOGGER.info("Use log category: config.");
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
		if (min <= max) {
			if (useLogCategory) {
				float interval = (max - min) / 4;
				IntStream.range(0, 5).forEach(i -> boundary.add(min + i * interval));
			} else if (useLogCategoryWithConfig) {
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
		float min = Float.MAX_VALUE;
		for (float d : degreeOfInterests)
			if (d < min)
				min = d;
		return min;
	}

	private float getMaxDOI(HashSet<Float> degreeOfInterests) {
		float max = Float.MIN_VALUE;
		for (float d : degreeOfInterests)
			if (d > max)
				max = d;
		return max;
	}

	public Set<LogInvocation> getLogInvocationSet() {
		return this.logInvocationSet;
	}

	public void setTest(int test) {
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
			createLogInvocation(node, null);
		}

		if (logLevel != null)
			createLogInvocation(node, logLevel);

		return super.visit(node);
	}

	/**
	 * Whether the code is read-only, generated code and in a .class file.
	 */
	public void checkCodeModification() {
		for (LogInvocation logInvocation : this.getLogInvocationSet()) {
			CompilationUnit cu = logInvocation.getEnclosingCompilationUnit();
			if (cu != null) {
				IJavaElement element = cu.getJavaElement();
				if (element.isReadOnly())
					logInvocation.addStatusEntry(PreconditionFailure.READ_ONLY_ELEMENT, Messages.ReadOnlyElement);

				IMethod method = logInvocation.getEnclosingEclipseMethod();
				if (method != null && method.isBinary())
					logInvocation.addStatusEntry(PreconditionFailure.BINARY_ELEMENT, Messages.BinaryElement);

				try {
					if (Util.isGeneratedCode(element))
						logInvocation.addStatusEntry(PreconditionFailure.GENERATED_ELEMENT, Messages.GeneratedElement);
				} catch (JavaModelException e) {
					logInvocation.addStatusEntry(PreconditionFailure.MISSING_JAVA_ELEMENT, Messages.MissingJavaElement);
				}
			} else
				LOGGER.warning("Can't find enclosing compilation unit for: " + logInvocation + ".");
		}
	}

	private void createLogInvocation(MethodInvocation node, Level logLevel) {
		LogInvocation logInvocation = new LogInvocation(node, logLevel);
		this.getLogInvocationSet().add(logInvocation);
	}

}
