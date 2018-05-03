package edu.cuny.hunter.logging.core.analysis;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import edu.cuny.hunter.logging.core.untils.Util;

@SuppressWarnings("restriction")
public class LoggingAnalyzer extends ASTVisitor {

	private Set<Logging> loggingSet = new HashSet<>();

	public void analyze() {

		// collect the projects to be analyzed.
		Map<IJavaProject, Set<Logging>> projectToLoggings = this.getLoggingSet().stream()
				.collect(Collectors.groupingBy(Logging::getCreationJavaProject, Collectors.toSet()));

		this.getLoggingSet().forEach(e -> {
			e.logInfo();
		});

		// TODO: analyze logging here.

	}

	public Set<Logging> getLoggingSet() {
		return this.loggingSet;
	}

	/**
	 * This method is used to find a set of logging objects
	 */
	@Override
	public boolean visit(MethodInvocation node) {
		IMethodBinding methodBinding = node.resolveMethodBinding();

		ITypeBinding declaringClass = methodBinding.getDeclaringClass();
		boolean declaringClassExtendsLogging = Util.isLoggingClass(declaringClass);

		if (declaringClassExtendsLogging) {
			Level loggingLevel = Util.isLoggingMethod(methodBinding.getName());

			if (loggingLevel != null) {
				Logging logging = new Logging(node, loggingLevel);
				this.getLoggingSet().add(logging);
			}

		}

		return super.visit(node);
	}

}
