package edu.cuny.hunter.mylyngit.core.utils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

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
	 * Return a method signature
	 */
	public static String getMethodSignatureForJavaMethod(IMethod method) {
		String signature = "";
		signature += method.getElementName() + "(";

		ITypeParameter[] parameterTypes;
		try {
			parameterTypes = method.getTypeParameters();
			if (parameterTypes.length >= 0)
				signature += parameterTypes[0];
			for (int i = 1; i < parameterTypes.length; ++i) {
				signature += ", " + parameterTypes[i];
			}
			signature += ")";
		} catch (JavaModelException e) {
			LOGGER.info("Cannot get parameter types!");
		}
		return signature;
	}

	/**
	 * Return the file path for a method.
	 */
	public static String getMethodFilePath(IMethod method) {
		return method.getClassFile().getPath().toString();
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
}
