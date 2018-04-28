package edu.cuny.hunter.logging.core.analysis;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.internal.corext.util.JdtFlags;
import edu.cuny.hunter.logging.core.untils.Util;

@SuppressWarnings("restriction")
public class LoggingAnalyzer extends ASTVisitor {

	private Set<Logging> loggingSet = new HashSet<>();

	public void analyze() {

		// collect the projects to be analyzed.
		Map<IJavaProject, Set<Logging>> projectToLoggings = this.getLoggingSet().stream()
				.filter(s -> s.getStatus().isOK())
				.collect(Collectors.groupingBy(Logging::getCreationJavaProject, Collectors.toSet()));

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
		ITypeBinding returnType = methodBinding.getReturnType();
		boolean returnTypeImplementsLogging = Util.implementsLogging(returnType);

		ITypeBinding declaringClass = methodBinding.getDeclaringClass();
		boolean declaringClassImplementsLogging = Util.implementsLogging(declaringClass);

		// TODO: This could be problematic if the API
		// implementation treats itself as a "client."
		String[] declaringClassPackageNameComponents = declaringClass.getPackage().getNameComponents();
		boolean isFromAPI = declaringClassPackageNameComponents.length > 0
				&& declaringClassPackageNameComponents[0].equals("java");

		boolean instanceMethod = !JdtFlags.isStatic(methodBinding);
		boolean intermediateOperation = instanceMethod && declaringClassImplementsLogging;

		// java.util.logging is the top-level interface for all logging.
		if (returnTypeImplementsLogging && !intermediateOperation && isFromAPI) {
			Logging logging = null;
			logging = new Logging(node);

			this.getLoggingSet().add(logging);
		}

		return super.visit(node);
	}

}
