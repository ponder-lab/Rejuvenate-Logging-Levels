package edu.cuny.hunter.log.core.analysis;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;

import edu.cuny.hunter.log.core.utils.Util;

@SuppressWarnings("restriction")
public class LogAnalyzer extends ASTVisitor {

	private Set<LogInvocation> logInvocationSet = new HashSet<>();
	
	private static boolean test;

	public void analyze() {

		// collect the projects to be analyzed.
		Map<IJavaProject, Set<LogInvocation>> projectToLoggings = this.getLogInvocationSet().stream()
				.collect(Collectors.groupingBy(LogInvocation::getExpressionJavaProject, Collectors.toSet()));

		this.getLogInvocationSet().forEach(e -> {
			e.logInfo();
		});

		// TODO: analyze logging here.

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
		
		Level logLevel = Util.isLogExpression(node, test);
		
		if (logLevel != null) {
			LogInvocation logInvocation = new LogInvocation(node, logLevel);
			this.getLogInvocationSet().add(logInvocation);
		}
		
		return super.visit(node);
	}

}
