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
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.core.IDegreeOfInterest;
import org.eclipse.mylyn.context.core.IInteractionElement;

import edu.cuny.hunter.log.core.refactorings.LogRejuvenatingProcessor;
import edu.cuny.hunter.mylyngit.core.analysis.MylynGitPredictionProvider;
import edu.cuny.hunter.mylyngit.core.utils.NonActiveMylynTaskException;

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

	/**
	 * Get DOI value.
	 */
	public static float getDOIValue(IDegreeOfInterest degreeOfInterest) {
		if (degreeOfInterest != null) {
			if (degreeOfInterest.getValue() > 0)
				return degreeOfInterest.getValue();
		}
		return 0;
	}

	/**
	 * Get DOI
	 */
	public static IDegreeOfInterest getDegreeOfInterest(IMethod method) {
		IInteractionElement interactionElement = getInteractionElement(method);
		if (interactionElement == null || interactionElement.getContext() == null) // workaround bug ...
			return null;
		return interactionElement.getInterest();
	}

	// The element in Mylyn
	private static IInteractionElement getInteractionElement(IMethod method) {
		if (method != null)
			return ContextCore.getContextManager().getElement(method.getHandleIdentifier());
		return null;
	}

	/**
	 * Clear the active task context.
	 * @throws NonActiveMylynTaskException 
	 */
	public static void clearTaskContext() throws NonActiveMylynTaskException {
		MylynGitPredictionProvider.clearTaskContext();
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
	// if isTest == 1, then it is junit test
	@SuppressWarnings("unchecked")
	public static Level isLogExpression(MethodInvocation node, boolean isTest) {
		if (!isTest) {
			IMethodBinding methodBinding = node.resolveMethodBinding();

			if (methodBinding == null
					|| !methodBinding.getDeclaringClass().getQualifiedName().equals("java.util.logging.Logger"))
				return null;
		}

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
			Level loggingLevel = getLogLevel(firstArgument, isTest);
			if (loggingLevel == null) {
				throw new IllegalStateException("The log level cannot be detected.");
			}
			if (loggingLevel == Level.ALL || loggingLevel == Level.OFF)
				return null;
			return loggingLevel;
		}

		if (methodName.equals("logp") || methodName.equals("logrb")) {
			Level loggingLevel = getLogLevel(firstArgument, isTest);
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
	public static Level getLogLevel(Expression firstArg, boolean isTest) {
		ITypeBinding typeBinding = firstArg.resolveTypeBinding();
		if (isTest)
			return getLogLevel(firstArg.toString());
		if (typeBinding == null || !typeBinding.getQualifiedName().equals("java.util.logging.Level"))
			return null;
		return getLogLevel(firstArg.toString());
	}

	private static Level getLogLevel(String argument) {
		if (argument.contains("SEVERE"))
			return Level.SEVERE;
		if (argument.contains("WARNING"))
			return Level.WARNING;
		if (argument.contains("INFO"))
			return Level.INFO;
		if (argument.contains("CONFIG"))
			return Level.CONFIG;
		if (argument.contains("FINER"))
			return Level.FINER;
		if (argument.contains("FINEST"))
			return Level.FINEST;
		if (argument.contains("FINE"))
			return Level.FINE;
		if (argument.contains("ALL"))
			return Level.ALL;
		if (argument.contains("OFF"))
			return Level.OFF;
		return null;
	}

	public static boolean isGeneratedCode(IJavaElement element) throws JavaModelException {
		return element.getResource().isDerived(IResource.CHECK_ANCESTORS);
	}

}
