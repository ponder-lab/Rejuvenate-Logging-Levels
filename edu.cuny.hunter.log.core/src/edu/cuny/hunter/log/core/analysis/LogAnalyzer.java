package edu.cuny.hunter.log.core.analysis;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodInvocation;

import edu.cuny.hunter.log.core.messages.Messages;
import edu.cuny.hunter.log.core.utils.LoggerNames;
import edu.cuny.hunter.log.core.utils.Util;

public class LogAnalyzer extends ASTVisitor {

	private static final Logger LOGGER = Logger.getLogger(LoggerNames.LOGGER_NAME);

	private Set<LogInvocation> logInvocationSet = new HashSet<>();

	private static LinkedList<Float> boundary;

	private static boolean useLogCategory = false;

	private int test;

	public LogAnalyzer(int isTest) {
		this.test = isTest;
	}

	public LogAnalyzer(boolean configLogLevel) {
		useLogCategory = configLogLevel;
	}

	public LogAnalyzer() {
	}

	public void analyze() {

		// collect the projects to be analyzed.
		Map<IJavaProject, Set<LogInvocation>> projectToLoggings = this.getLogInvocationSet().stream()
				.collect(Collectors.groupingBy(LogInvocation::getExpressionJavaProject, Collectors.toSet()));

		HashSet<Float> degreeOfInterests = new HashSet<>();
		for (LogInvocation logInvocation : this.logInvocationSet) {
			logInvocation.logInfo();
			degreeOfInterests.add(logInvocation.getDegreeOfInterestValue());
		}

		// build boundary
		boundary = buildBoundary(degreeOfInterests, useLogCategory);
		// check whether action is needed
		for (LogInvocation logInvocation : this.logInvocationSet)
			if (this.doAction(logInvocation, useLogCategory))
				LOGGER.info("Do action: " + logInvocation.getAction() + "! The changed log expression is "
						+ logInvocation.getExpression());

	}

	private boolean doAction(LogInvocation logInvocation, boolean useLogCategory) {
		Level currentLogLevel = logInvocation.getLogLevel();
		Level suggestedLogLevel = getSuggestedLogLevel(boundary, logInvocation, useLogCategory);

		if (currentLogLevel == suggestedLogLevel)
			return false;
		if (suggestedLogLevel == null || currentLogLevel == null)
			return false;
		if (useLogCategory && (currentLogLevel == Level.CONFIG || currentLogLevel == Level.WARNING
				|| currentLogLevel == Level.SEVERE))
			return false;

		if (suggestedLogLevel == Level.ALL)
			logInvocation.setAction(Action.CONVERT_TO_ALL);
		if (suggestedLogLevel == Level.FINEST)
			logInvocation.setAction(Action.CONVERT_TO_FINEST);
		if (suggestedLogLevel == Level.FINER)
			logInvocation.setAction(Action.CONVERT_TO_FINER);
		if (suggestedLogLevel == Level.FINE)
			logInvocation.setAction(Action.CONVERT_TO_FINE);
		if (suggestedLogLevel == Level.CONFIG)
			logInvocation.setAction(Action.CONVERT_TO_CONFIG);
		if (suggestedLogLevel == Level.INFO)
			logInvocation.setAction(Action.CONVERT_TO_INFO);
		if (suggestedLogLevel == Level.WARNING)
			logInvocation.setAction(Action.CONVERT_TO_WARNING);
		if (suggestedLogLevel == Level.SEVERE)
			logInvocation.setAction(Action.CONVERT_TO_SEVERE);
		if (suggestedLogLevel == Level.OFF)
			logInvocation.setAction(Action.CONVERT_TO_OFF);
		return true;
	}

	/**
	 * Get the suggested log level based on boundary.
	 * 
	 * @param boundary
	 * @param DOI
	 * @return the suggested log level
	 */
	private static Level getSuggestedLogLevel(LinkedList<Float> boundary, LogInvocation logInvocation,
			boolean useLogCategory) {
		float DOI = logInvocation.getDegreeOfInterestValue();
		if (boundary == null)
			return null;
		if (Float.compare(boundary.getFirst(), boundary.getLast()) == 0) {
			logInvocation.addStatusEntry(PreconditionFailure.NO_ENOUGH_DATA,
					"The DOI values are all same or no DOI values. Cannot get valid results.");
			LOGGER.info("The DOI values are all same or no DOI values. Cannot get valid results.");
			return null;
		}
		if (DOI >= boundary.get(0) && DOI < boundary.get(1))
			return Level.ALL;
		if (DOI < boundary.get(2))
			return Level.FINEST;
		if (DOI < boundary.get(3))
			return Level.FINER;
		if (DOI < boundary.get(4))
			return Level.FINE;
		if (!useLogCategory) {
			LOGGER.info("CONFIG logging level could be refactored.");
			if (DOI < boundary.get(5))
				return Level.CONFIG;
			if (DOI < boundary.get(6))
				return Level.INFO;
			if (DOI < boundary.get(7))
				return Level.WARNING;
			if (DOI < boundary.get(8))
				return Level.SEVERE;
			if (DOI <= boundary.get(9))
				return Level.OFF;
		} else {
			LOGGER.info("CONFIG/WARNING/SEVERE logging level could not be refactored.");
			if (DOI < boundary.get(5))
				return Level.INFO;
			if (DOI <= boundary.get(6))
				return Level.OFF;
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
	private LinkedList<Float> buildBoundary(HashSet<Float> degreeOfInterests, boolean useLogCategory) {
		float min = getMinDOI(degreeOfInterests);
		float max = getMaxDOI(degreeOfInterests);
		LinkedList<Float> boundary = new LinkedList<>();
		if (min <= max) {
			if (!useLogCategory) {
				float interval = (max - min) / 9;
				IntStream.range(0, 10).forEach(i -> boundary.add(min + i * interval));
			} else {
				float interval = (max - min) / 6;
				IntStream.range(0, 7).forEach(i -> boundary.add(min + i * interval));
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
			}
		}
	}

	private void createLogInvocation(MethodInvocation node, Level logLevel) {
		LogInvocation logInvocation = new LogInvocation(node, logLevel);
		this.getLogInvocationSet().add(logInvocation);
	}

}
