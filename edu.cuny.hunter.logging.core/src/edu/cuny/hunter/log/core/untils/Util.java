package edu.cuny.hunter.logging.core.untils;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;

import edu.cuny.hunter.logging.core.refactorings.LoggingRefactoringProcessor;

@SuppressWarnings("restriction")
public final class Util {
	public static ProcessorBasedRefactoring createRefactoring(IJavaProject[] projects,
			Optional<IProgressMonitor> monitor) throws JavaModelException {
		LoggingRefactoringProcessor processor = createLoggingProcessor(projects, monitor);
		return new ProcessorBasedRefactoring(processor);
	}

	public static LoggingRefactoringProcessor createLoggingProcessor(IJavaProject[] projects,
			Optional<IProgressMonitor> monitor) throws JavaModelException {
		if (projects.length < 1)
			throw new IllegalArgumentException("No projects.");

		CodeGenerationSettings settings = JavaPreferencesSettings.getCodeGenerationSettings(projects[0]);
		LoggingRefactoringProcessor processor = new LoggingRefactoringProcessor(projects, settings, monitor);
		return processor;
	}

	static Set<ITypeBinding> getExtendedClasses(ITypeBinding type) {
		Set<ITypeBinding> ret = new HashSet<>();
		ret.add(type);
		ret.addAll(getAllClasses(type));
		return ret;
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

	public static boolean isLoggingStatement(String methodName) {
		System.out.println(methodName);
		return true;
	}

	public static boolean isLoggingClass(ITypeBinding declaringClass) {
		Set<ITypeBinding> extendedClasses = getExtendedClasses(declaringClass);
		return extendedClasses.stream()
				.anyMatch(i -> i.getErasure().getQualifiedName().equals("java.util.logging.Logger"));
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
		IMethodBinding methodBinding = node.resolveMethodBinding();

		if (methodBinding == null
				|| !methodBinding.getDeclaringClass().getQualifiedName().equals("java.util.logging.Logger"))
			return null;

		String methodName = methodBinding.getName();

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
			if (loggingLevel == null)
				System.out.println("Need to process LogRecord.");
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
	 * @param argument
	 * @return
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
		return null;
	}

}
