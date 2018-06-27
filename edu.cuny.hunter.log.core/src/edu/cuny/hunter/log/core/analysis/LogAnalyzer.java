package edu.cuny.hunter.log.core.analysis;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;

import edu.cuny.hunter.log.core.utils.LoggerNames;
import edu.cuny.hunter.log.core.utils.Util;

public class LogAnalyzer extends ASTVisitor {

	private static final Logger LOGGER = Logger.getLogger(LoggerNames.LOGGER_NAME);

	private Set<LogInvocation> logInvocationSet = new HashSet<>();

	private boolean test;

	public LogAnalyzer(boolean b) {
		this.test = b;
	}

	public LogAnalyzer() {
	}

	public void analyze() {

		// collect the projects to be analyzed.
		Map<IJavaProject, Set<LogInvocation>> projectToLoggings = this.getLogInvocationSet().stream()
				.collect(Collectors.groupingBy(LogInvocation::getExpressionJavaProject, Collectors.toSet()));

		HashSet<Float> degreeOfInterests = new HashSet<>();
		this.getLogInvocationSet().forEach(e -> {
			e.logInfo();
			degreeOfInterests.add(e.getDegreeOfInterestValue());
		});

		// TODO: analyze logging here.

	}
	
	/**
	 * Build a list of boundary. The DOI values could be divided into 7 groups by
	 * this boundary. 7 groups are corresponding to 7 logging levels
	 * 
	 * @param degreeOfInterests
	 * @return a list of boundary
	 */
	private LinkedList<Float> buildBoundary(HashSet<Float> degreeOfInterests) {
		//TODO: implementation
		return null;
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
			createLogInvocation(node, null);
		}

		if (logLevel != null)
			createLogInvocation(node, logLevel);

		return super.visit(node);
	}

	private void createLogInvocation(MethodInvocation node, Level logLevel) {
		LogInvocation logInvocation = new LogInvocation(node, logLevel);
		this.getLogInvocationSet().add(logInvocation);
	}

}
