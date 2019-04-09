package edu.cuny.hunter.mylyngit.core.utils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.core.IInteractionContextManager;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.internal.context.core.ContextCorePlugin;

@SuppressWarnings("restriction")
public class Util {
	public final static String LOGGER_NAME = "edu.cuny.hunter.logging";
	private static final Logger LOGGER = Logger.getLogger(LOGGER_NAME);

	/**
	 * Return a method signature
	 */
	@SuppressWarnings("unchecked")
	public static String getMethodSignature(MethodDeclaration methodDeclaration) {
		String signature = "";
		signature += methodDeclaration.getName() + "(";

		Iterator<SingleVariableDeclaration> parameterIterator = methodDeclaration.parameters().iterator();
		if (parameterIterator.hasNext())
			signature += parameterIterator.next().getType();
		while (parameterIterator.hasNext()) {
			signature += ", " + parameterIterator.next().getType();
		}
		signature += ")";
		return signature;
	}

	/**
	 * Return the file path for a method.
	 */
	public static String getMethodFilePath(IMethod m) {
		if (m != null) {
			return m.getResource().getLocation().makeAbsolute().toString();
		}
		return "";
	}

	public static List<ICompilationUnit> getCompilationUnits(IJavaProject javaProject) {
		List<ICompilationUnit> units = new LinkedList<ICompilationUnit>();
		try {
			IPackageFragmentRoot[] packageFragmentRoots = javaProject.getAllPackageFragmentRoots();
			for (int i = 0; i < packageFragmentRoots.length; i++) {
				IPackageFragmentRoot packageFragmentRoot = packageFragmentRoots[i];
				IJavaElement[] fragments = packageFragmentRoot.getChildren();
				for (int j = 0; j < fragments.length; j++) {
					IPackageFragment fragment = (IPackageFragment) fragments[j];
					IJavaElement[] javaElements = fragment.getChildren();
					for (int k = 0; k < javaElements.length; k++) {
						IJavaElement javaElement = javaElements[k];
						if (javaElement.getElementType() == IJavaElement.COMPILATION_UNIT
								&& javaElement.getJavaProject().equals(javaProject)) {
							units.add((ICompilationUnit) javaElement);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return units;
	}

	/**
	 * Make the method uninteresting here.
	 */
	public static void resetDOIValue(IMethod method, String ID) {
		ContextCorePlugin.getContextManager().manipulateInterestForElement(getInteractionElement(method), false, false,
				true, ID, true);
	}

	/**
	 * Get the element in Mylyn.
	 */
	private static IInteractionElement getInteractionElement(IMethod method) {
		return ContextCore.getContextManager().getElement(method.getHandleIdentifier());
	}

	/**
	 * Treat negative DOI values as uninteresting.
	 */
	public static float getDOIValue(IMethod method) {
		IInteractionElement element = ContextCore.getContextManager().getElement(method.getHandleIdentifier());

		if (element == null || element.getContext() == null) {
			return 0;
		}

		if (element.getInterest().getValue() <= 0) {
			resetDOIValue(method, "Java");
			return 0;
		}
		return element.getInterest().getValue();
	}

	/**
	 * This method is used to clear mylyn task context. It should be called after
	 * processing one project.
	 * @throws NonActiveMylynTaskException When the task is not active.
	 */
	public static void clearTaskContext() throws NonActiveMylynTaskException {
		IInteractionContextManager contextManager = ContextCore.getContextManager();
		String handleIdentifier = contextManager.getActiveContext().getHandleIdentifier();

		// try to delete the active context.
		try {
			contextManager.deleteContext(handleIdentifier);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Context may not active.");
			throw new NonActiveMylynTaskException("Mylyn task being considered is not active.");
		}
	}

	private static File findEvaluationPropertiesFile(IJavaProject project, String fileName) throws JavaModelException {
		IPath location = project.getCorrespondingResource().getLocation();
		return findEvaluationPropertiesFile(location.toFile(), fileName);
	}
	
	/**
	 * Create CompilationUnit from ICompilationUnit.
	 */
	public static CompilationUnit getCompilationUnit(ICompilationUnit unit) {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setResolveBindings(true);
		parser.setSource(unit);
		return (CompilationUnit) parser.createAST(null);
	}


	private static File findEvaluationPropertiesFile(File directory, String fileName) {
		if (directory == null)
			return null;

		if (!directory.isDirectory())
			throw new IllegalArgumentException("Expecting directory: " + directory + ".");

		File evaluationFile = directory.toPath().resolve(fileName).toFile();

		if (evaluationFile != null && evaluationFile.exists())
			return evaluationFile;
		else
			return findEvaluationPropertiesFile(directory.getParentFile(), fileName);
	}

	public static List<Integer> getNToUseForCommits(IJavaProject project, String key, int value, String fileName)
			throws IOException, JavaModelException {
		Properties properties = new Properties();
		File file = findEvaluationPropertiesFile(project, fileName);

		if (file != null && file.exists())
			try (Reader reader = new FileReader(file)) {
				properties.load(reader);

				String nToUseForStreams = properties.getProperty(key);

				if (nToUseForStreams == null) {
					List<Integer> ret = Stream.of(value).collect(Collectors.toList());
					LOGGER.info("Using default N for commit number: " + ret + ".");
					return ret;
				} else {
					String[] strings = nToUseForStreams.split(",");
					List<Integer> ret = Arrays.stream(strings).map(Integer::parseInt).collect(Collectors.toList());
					LOGGER.info("Using properties file N for commit number: " + ret + ".");
					return ret;
				}
			}
		else {
			List<Integer> ret = Stream.of(value).collect(Collectors.toList());
			LOGGER.info("Using default N for commit number: " + ret + ".");
			return ret;
		}
	}
}
