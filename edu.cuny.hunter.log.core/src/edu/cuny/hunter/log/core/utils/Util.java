package edu.cuny.hunter.log.core.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;

import edu.cuny.hunter.log.core.refactorings.LogRejuvenatingProcessor;

@SuppressWarnings("restriction")
public final class Util {
	public static ProcessorBasedRefactoring createRejuvenating(IJavaProject[] projects,
			Optional<IProgressMonitor> monitor) throws JavaModelException {
		LogRejuvenatingProcessor processor = createLoggingProcessor(projects, monitor);
		return new ProcessorBasedRefactoring(processor);
	}

	public static LogRejuvenatingProcessor createLoggingProcessor(IJavaProject[] projects,
			Optional<IProgressMonitor> monitor) throws JavaModelException {
		if (projects.length < 1)
			throw new IllegalArgumentException("No projects.");

		CodeGenerationSettings settings = JavaPreferencesSettings.getCodeGenerationSettings(projects[0]);
		LogRejuvenatingProcessor processor = new LogRejuvenatingProcessor(projects, settings, monitor);
		return processor;
	}

	static Set<ITypeBinding> getAllClasses(ITypeBinding type) {
		Set<ITypeBinding> ret = new HashSet<>();
		ITypeBinding superClass = type.getSuperclass();

		if (superClass != null) {
			ret.add(superClass);
			ret.addAll(getAllClasses(superClass));
		}

		return ret;
	}

	// if isTest == 1, then it is junit test
	public static Level isLogExpression(MethodInvocation node, int isTest) {
		if (isTest != 1) {
			IMethodBinding methodBinding = node.resolveMethodBinding();

			if (methodBinding == null
					|| !methodBinding.getDeclaringClass().getQualifiedName().equals("java.util.logging.Logger"))
				return null;
		}
		return isLogExpression(node);
	}

	/**
	 * We only focus on the logging level, which is set by the developer. Hence, we
	 * do not record the logging level which is embedded by the logging package.
	 * e.g. each time we call method entering, a logging record which has "FINER"
	 * level is created.
	 * 
	 * @param node
	 * @return logging level
	 */
	public static Level isLogExpression(MethodInvocation node) {

		String methodName = node.getName().toString();

		// Get rid of all and off here.
		if (methodName.equals("config"))
			return Level.CONFIG;
		if (methodName.equals("fine"))
			return Level.FINE;
		if (methodName.equals("finer"))
			return Level.FINER;
		if (methodName.equals("finest"))
			return Level.FINEST;
		if (methodName.equals("info"))
			return Level.INFO;
		if (methodName.equals("severe"))
			return Level.SEVERE;
		if (methodName.equals("warning"))
			return Level.WARNING;

		List<Expression> arguments = node.arguments();
		if (arguments.size() == 0)
			return null;
		Expression firstArgument = arguments.get(0);

		// TODO: may need wala?
		// They should not be null
		if (methodName.equals("log")) {
			Level loggingLevel = getLogLevel(firstArgument);
			if (loggingLevel == null) {
				throw new IllegalStateException("The log level cannot be detected.");
			}
			if (loggingLevel == Level.ALL || loggingLevel == Level.OFF)
				return null;
			return loggingLevel;
		}

		if (methodName.equals("logp") || methodName.equals("logrb")) {
			Level loggingLevel = getLogLevel(firstArgument);
			if (loggingLevel == Level.ALL || loggingLevel == Level.OFF)
				return null;
			return loggingLevel;
		}

		return null;
	}

	/**
	 * Return a corresponding logging level of a string
	 * 
	 * @param argument
	 * @return logging level
	 */
	public static Level getLogLevel(Expression firstArg) {
		if (!firstArg.resolveTypeBinding().getQualifiedName().equals("java.util.logging.Level"))
			return null;
		String argument = firstArg.toString();
		if (argument.contains("Level.SEVERE"))
			return Level.SEVERE;
		if (argument.contains("Level.WARNING"))
			return Level.WARNING;
		if (argument.contains("Level.INFO"))
			return Level.INFO;
		if (argument.contains("Level.CONFIG"))
			return Level.CONFIG;
		if (argument.contains("Level.FINE"))
			return Level.FINE;
		if (argument.contains("Level.FINER"))
			return Level.FINER;
		if (argument.contains("Level.FINEST"))
			return Level.FINEST;
		if (argument.contains("Level.ALL"))
			return Level.ALL;
		if (argument.contains("Level.OFF"))
			return Level.OFF;
		return null;
	}

	public static boolean isGeneratedCode(IJavaElement element) throws JavaModelException {
		return element.getResource().isDerived(IResource.CHECK_ANCESTORS);
	}

}
