package edu.cuny.hunter.log.core.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.jdt.internal.ui.util.SelectionUtil;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.core.IDegreeOfInterest;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.internal.context.core.InteractionContextScaling;
import org.eclipse.ui.handlers.HandlerUtil;

import edu.cuny.hunter.log.core.analysis.LogLevel;
import edu.cuny.hunter.log.core.analysis.LoggingFramework;
import edu.cuny.hunter.log.core.refactorings.LogRejuvenatingProcessor;
import edu.cuny.hunter.mylyngit.core.analysis.MylynGitPredictionProvider;
import edu.cuny.hunter.mylyngit.core.utils.NonActiveMylynTaskException;

@SuppressWarnings("restriction")
public final class Util {
	private static final int ENCLOSING_METHOD_DECAY_FACTOR = 256;

	private static Float originalDecay;

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
	public static float getDOIValue(IMethod method) {
		IInteractionElement interactionElement = getInteractionElement(method);

		if (interactionElement == null || interactionElement.getContext() == null)
			// workaround bug ...
			return 0;

		IDegreeOfInterest degreeOfInterest = interactionElement.getInterest();

		if (degreeOfInterest != null && degreeOfInterest.getValue() > 0) {
			return degreeOfInterest.getValue();
		}
		return 0;
	}

	/**
	 * Get DOI value and set decay.
	 */
	public static float getDOIValue(IMethod method, Set<IMethod> enclosingMethods) {
		IInteractionElement interactionElement = getInteractionElement(method);

		if (interactionElement == null || interactionElement.getContext() == null)
			// workaround bug ...
			return 0;

		// we haven't found the decay yet.
		if (originalDecay == null)
			originalDecay = interactionElement.getContext().getScaling().getDecay();

		IDegreeOfInterest degreeOfInterest = interactionElement.getInterest();

		if (degreeOfInterest != null) {
			InteractionContextScaling scaling = (InteractionContextScaling) interactionElement.getContext()
					.getScaling();

			// if the method is an enclosing method.
			if (enclosingMethods.contains(method)) {
				// set a special decay.
				scaling.setDecay(originalDecay / ENCLOSING_METHOD_DECAY_FACTOR);
			} else { // otherwise, it's a non-enclosing method.
				// use the original decay.
				scaling.setDecay(originalDecay);
			}

			if (degreeOfInterest.getValue() > 0)
				return degreeOfInterest.getValue();
		}
		return 0;
	}

	// The element in Mylyn
	private static IInteractionElement getInteractionElement(IMethod method) {
		if (method != null)
			return ContextCore.getContextManager().getElement(method.getHandleIdentifier());
		return null;
	}

	/**
	 * Check whether the logging method contains logging level
	 */
	public static boolean isLoggingLevelMethod(String methodName) {
		if (methodName.equals("config") || methodName.equals("fine") || methodName.equals("finer")
				|| methodName.equals("finest") || methodName.equals("info") || methodName.equals("severe")
				|| methodName.equals("warning"))
			return true;
		return false;
	}

	/**
	 * For slf4j.
	 * 
	 * Check whether the logging method contains logging level
	 */
	public static boolean isLoggingLevelMethodSlf4J(String methodName) {
		if (methodName.equals("error") || methodName.equals("warn") || methodName.equals("info")
				|| methodName.equals("debug") || methodName.equals("trace"))
			return true;
		return false;
	}

	/**
	 * Clear the active task context.
	 * 
	 * @throws NonActiveMylynTaskException
	 */
	public static void clearTaskContext() throws NonActiveMylynTaskException {
		MylynGitPredictionProvider.clearTaskContext();
	}

	@SuppressWarnings("unchecked")
	public static boolean isLogMessageWithKeywords(MethodInvocation node, Set<String> keyWordsInLogMessages) {

		String identifier = node.getName().getIdentifier();
		String logMessage = "";

		List<Expression> arguments = new ArrayList<Expression>();
		arguments.addAll(node.arguments());

		// log(Level.INFO, "...");
		if (!Util.isLoggingLevelMethod(identifier)) {
			arguments.remove(0);
		}

		for (Expression argument : arguments)
			logMessage += argument.toString().toLowerCase();

		for (String key : keyWordsInLogMessages) {
			if (logMessage.contains(key.toLowerCase()))
				return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public static boolean isLogMessageWithKeywordsSlf4j(MethodInvocation node, Set<String> keyWordsInLogMessages) {

		String logMessage = "";

		List<Expression> arguments = new ArrayList<Expression>();
		arguments.addAll(node.arguments());

		for (Expression argument : arguments)
			logMessage += argument.toString().toLowerCase();

		for (String key : keyWordsInLogMessages) {
			if (logMessage.contains(key.toLowerCase()))
				return true;
		}
		return false;
	}

	/**
	 * We only focus on the logging level, which is set by the developer. Hence, we
	 * do not record the logging level which is embedded by the logging package.
	 * e.g. each time we call method entering, a logging record which has "FINER"
	 * level is created.
	 * 
	 * @param method invocation
	 * @return log level
	 */
	// if isTest == 1, then it is jUnit test
	public static LogLevel isLogExpression(MethodInvocation node, boolean isTest) {
		if (!isTest) {
			IMethodBinding methodBinding = node.resolveMethodBinding();

			if (methodBinding == null)
				return null;

			if (methodBinding.getDeclaringClass().getQualifiedName().equals("java.util.logging.Logger"))
				return getLogLevelForLogging(node, isTest);

			if (methodBinding.getDeclaringClass().getQualifiedName().equals("org.slf4j.Logger"))
				return getLogLevelForSlf4J(node);

			return null;

		} else
			return getLogLevelForLogging(node, isTest);

	}

	/**
	 * Get the slf4j log level.
	 */
	private static LogLevel getLogLevelForSlf4J(MethodInvocation node) {
		LogLevel logLevel = new LogLevel();
		logLevel.setLoggingFramework(LoggingFramework.valueOf("SLF4J"));
		String methodName = node.getName().toString();
		switch (methodName) {
		case "debug":
			logLevel.setSlf4jLevel(org.slf4j.event.Level.DEBUG);
			return logLevel;

		case "error":
			logLevel.setSlf4jLevel(org.slf4j.event.Level.ERROR);
			return logLevel;

		case "info":
			logLevel.setSlf4jLevel(org.slf4j.event.Level.INFO);
			return logLevel;

		case "trace":
			logLevel.setSlf4jLevel(org.slf4j.event.Level.TRACE);
			return logLevel;

		case "warn":
			logLevel.setSlf4jLevel(org.slf4j.event.Level.WARN);
			return logLevel;

		default:
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private static LogLevel getLogLevelForLogging(MethodInvocation node, boolean isTest) {

		String methodName = node.getName().toString();

		LogLevel logLevel = new LogLevel();

		logLevel.setLoggingFramework(LoggingFramework.valueOf("JAVA_UTIL_LOGGING"));

		switch (methodName) {
		case "config":
			logLevel.setLogLevel(Level.CONFIG);
			return logLevel;

		case "fine":
			logLevel.setLogLevel(Level.FINE);
			return logLevel;

		case "finer":
			logLevel.setLogLevel(Level.FINER);
			return logLevel;

		case "finest":
			logLevel.setLogLevel(Level.FINEST);
			return logLevel;

		case "info":
			logLevel.setLogLevel(Level.INFO);
			return logLevel;

		case "severe":
			logLevel.setLogLevel(Level.SEVERE);
			return logLevel;

		case "warning":
			logLevel.setLogLevel(Level.WARNING);
			return logLevel;

		default:
			break;
		}

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

			logLevel.setLogLevel(loggingLevel);
			return logLevel;
		}

		if (methodName.equals("logp") || methodName.equals("logrb")) {
			Level loggingLevel = getLogLevel(firstArgument, isTest);
			if (loggingLevel == Level.ALL || loggingLevel == Level.OFF)
				return null;
			logLevel.setLogLevel(loggingLevel);
			return logLevel;
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
			return getLogLevel(firstArg);
		if (typeBinding == null || !typeBinding.getQualifiedName().equals("java.util.logging.Level"))
			return null;
		return getLogLevel(firstArg);
	}

	private static Level getLogLevel(Expression firstArg) {
		String identifier;
		// if it's not a name.
		if (!(firstArg instanceof Name))
			return null;
		else {
			Name name = (Name) firstArg;
			if (name.isSimpleName()) {
				SimpleName simpleName = (SimpleName) name;
				identifier = simpleName.getIdentifier();
			} else if (name.isQualifiedName()) {
				QualifiedName qualifiedName = (QualifiedName) name;
				SimpleName simpleName = qualifiedName.getName();
				identifier = simpleName.getIdentifier();
			} else
				throw new IllegalArgumentException("Expecting simple or qualified name: " + firstArg);
		}

		if (identifier.contains("SEVERE"))
			return Level.SEVERE;
		if (identifier.contains("WARNING"))
			return Level.WARNING;
		if (identifier.contains("INFO"))
			return Level.INFO;
		if (identifier.contains("CONFIG"))
			return Level.CONFIG;
		if (identifier.contains("FINER"))
			return Level.FINER;
		if (identifier.contains("FINEST"))
			return Level.FINEST;
		if (identifier.contains("FINE"))
			return Level.FINE;
		if (identifier.contains("ALL"))
			return Level.ALL;
		if (identifier.contains("OFF"))
			return Level.OFF;
		return null;
	}

	public static boolean isGeneratedCode(IJavaElement element) throws JavaModelException {
		return element.getResource().isDerived(IResource.CHECK_ANCESTORS);
	}

	public static int getDecayFactor() {
		return ENCLOSING_METHOD_DECAY_FACTOR;
	}

	/**
	 * @author Raffi Khatchadourian
	 * @param enclosing eclipse method object
	 * @return method identifier
	 */
	public static String getMethodIdentifier(IMethod method) throws JavaModelException {
		if (method == null)
			return null;

		StringBuilder sb = new StringBuilder();
		sb.append((method.getElementName()) + "(");
		ILocalVariable[] parameters = method.getParameters();
		for (int i = 0; i < parameters.length; i++) {
			sb.append(getQualifiedNameFromTypeSignature(parameters[i].getTypeSignature(), method.getDeclaringType()));
			if (i != (parameters.length - 1)) {
				sb.append(",");
			}
		}
		sb.append(")");
		return sb.toString();
	}

	/**
	 * @author Raffi Khatchadourian
	 */
	public static String getQualifiedNameFromTypeSignature(String typeSignature, IType declaringType)
			throws JavaModelException {
		typeSignature = Signature.getTypeErasure(typeSignature);
		String signatureQualifier = Signature.getSignatureQualifier(typeSignature);
		String signatureSimpleName = Signature.getSignatureSimpleName(typeSignature);
		String simpleName = signatureQualifier.isEmpty() ? signatureSimpleName
				: signatureQualifier + '.' + signatureSimpleName;

		// workaround https://bugs.eclipse.org/bugs/show_bug.cgi?id=494209.
		boolean isArray = false;
		if (simpleName.endsWith("[]")) {
			isArray = true;
			simpleName = simpleName.substring(0, simpleName.lastIndexOf('['));
		}

		String[][] allResults = declaringType.resolveType(simpleName);
		String fullName = null;
		if (allResults != null) {
			String[] nameParts = allResults[0];
			if (nameParts != null) {
				StringBuilder fullNameBuilder = new StringBuilder();
				for (String part : nameParts) {
					if (fullNameBuilder.length() > 0)
						fullNameBuilder.append('.');
					if (part != null)
						fullNameBuilder.append(part);
				}
				fullName = fullNameBuilder.toString();
			}
		} else
			fullName = simpleName;

		// workaround https://bugs.eclipse.org/bugs/show_bug.cgi?id=494209.
		if (isArray)
			fullName += "[]";

		return fullName;
	}

	public static IJavaProject[] getSelectedJavaProjectsFromEvent(ExecutionEvent event) throws ExecutionException {
		ISelection currentSelection = HandlerUtil.getCurrentSelectionChecked(event);
		List<?> list = SelectionUtil.toList(currentSelection);
		IJavaProject[] javaProjects = list.stream().filter(e -> e instanceof IJavaProject)
				.toArray(length -> new IJavaProject[length]);

		return javaProjects;
	}
}
