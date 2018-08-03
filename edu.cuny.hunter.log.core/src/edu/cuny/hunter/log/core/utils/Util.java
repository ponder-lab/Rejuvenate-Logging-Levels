package edu.cuny.hunter.log.core.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;

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

import edu.cuny.hunter.log.core.refactorings.LogRefactoringProcessor;

@SuppressWarnings("restriction")
public final class Util {
	public static ProcessorBasedRefactoring createRefactoring(IJavaProject[] projects,
			Optional<IProgressMonitor> monitor) throws JavaModelException {
		LogRefactoringProcessor processor = createLoggingProcessor(projects, monitor);
		return new ProcessorBasedRefactoring(processor);
	}

	public static LogRefactoringProcessor createLoggingProcessor(IJavaProject[] projects,
			Optional<IProgressMonitor> monitor) throws JavaModelException {
		if (projects.length < 1)
			throw new IllegalArgumentException("No projects.");

		CodeGenerationSettings settings = JavaPreferencesSettings.getCodeGenerationSettings(projects[0]);
		LogRefactoringProcessor processor = new LogRefactoringProcessor(projects, settings, monitor);
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

		if (methodName.equals("all"))
			return Level.ALL;
		if (methodName.equals("off"))
			return Level.OFF;
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
		String firstArgument = arguments.get(0).toString();

		// TODO: may need wala?
		// They should not be null
		if (methodName.equals("log")) {
			Level loggingLevel = getLogLevel(firstArgument);
			if (loggingLevel == null) {
				throw new IllegalStateException("The log level cannot be detected.");
			}
			return loggingLevel;
		}
		if (methodName.equals("logp")) {
			Level loggingLevel = getLogLevel(firstArgument);
			return loggingLevel;
		}
		if (methodName.equals("logrb")) {
			Level loggingLevel = getLogLevel(firstArgument);
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
	public static Level getLogLevel(String argument) {
		if (argument.equals("Level.SEVERE"))
			return Level.SEVERE;
		if (argument.equals("Level.WARNING"))
			return Level.WARNING;
		if (argument.equals("Level.INFO"))
			return Level.INFO;
		if (argument.equals("Level.CONFIG"))
			return Level.CONFIG;
		if (argument.equals("Level.FINE"))
			return Level.FINE;
		if (argument.equals("Level.FINER"))
			return Level.FINER;
		if (argument.equals("Level.FINEST"))
			return Level.FINEST;
		if (argument.equals("Level.ALL"))
			return Level.ALL;
		if (argument.equals("Level.OFF"))
			return Level.OFF;
		return null;
	}

	public static boolean isGeneratedCode(IJavaElement element) throws JavaModelException {
		return element.getCorrespondingResource().isDerived();
	}

}
