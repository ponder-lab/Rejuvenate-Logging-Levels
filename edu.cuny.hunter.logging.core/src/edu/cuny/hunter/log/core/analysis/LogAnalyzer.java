package edu.cuny.hunter.log.core.analysis;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;

import edu.cuny.hunter.log.core.untils.Util;

@SuppressWarnings("restriction")
public class LogAnalyzer extends ASTVisitor {

	private Set<LogInvocation> loggingSet = new HashSet<>();

	public void analyze() {

		// collect the projects to be analyzed.
		Map<IJavaProject, Set<LogInvocation>> projectToLoggings = this.getLoggingSet().stream()
				.collect(Collectors.groupingBy(LogInvocation::getExpressionJavaProject, Collectors.toSet()));

		this.getLoggingSet().forEach(e -> {
			e.logInfo();
		});

		// TODO: analyze logging here.

	}

	public Set<LogInvocation> getLoggingSet() {
		return this.loggingSet;
	}

	/**
	 * This method is used to find a set of logging objects
	 */
	@Override
	public boolean visit(MethodInvocation node) {
		
		Level logLevel = Util.isLogExpression(node);
		
		if (logLevel != null) {
			LogInvocation logInvocation = new LogInvocation(node, logLevel);
			this.getLoggingSet().add(logInvocation);
		}
		
		return super.visit(node);
	}

}
