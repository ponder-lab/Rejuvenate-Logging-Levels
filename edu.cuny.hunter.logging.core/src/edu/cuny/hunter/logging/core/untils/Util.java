package edu.cuny.hunter.logging.core.untils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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

	static Set<ITypeBinding> getImplementedInterfaces(ITypeBinding type) {
		Set<ITypeBinding> ret = new HashSet<>();

		if (type.isInterface())
			ret.add(type);

		ret.addAll(getAllInterfaces(type));
		return ret;
	}

	static Set<ITypeBinding> getAllInterfaces(ITypeBinding type) {
		Set<ITypeBinding> ret = new HashSet<>();
		ITypeBinding[] interfaces = type.getInterfaces();
		ret.addAll(Arrays.asList(interfaces));

		for (ITypeBinding interfaceBinding : interfaces)
			ret.addAll(getAllInterfaces(interfaceBinding));

		return ret;
	}

	public static boolean implementsLogging(ITypeBinding type) {
		Set<ITypeBinding> implementedInterfaces = getImplementedInterfaces(type);
		return implementedInterfaces.stream()
				.anyMatch(i -> i.getErasure().getQualifiedName().equals("java.util.logging"));
	}

}
