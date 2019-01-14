package edu.cuny.hunter.mylyngit.core.utils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
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
						if (javaElement.getElementType() == IJavaElement.COMPILATION_UNIT) {
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
	 */
	public static void clearTaskContext() {
		IInteractionContextManager contextManager = ContextCore.getContextManager();
		String handleIdentifier = contextManager.getActiveContext().getHandleIdentifier();

		// try to delete the active context.
		try {
			contextManager.deleteContext(handleIdentifier);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Context may not active.");
		}
	}
}
