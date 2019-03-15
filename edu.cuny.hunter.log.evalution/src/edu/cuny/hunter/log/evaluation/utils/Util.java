package edu.cuny.hunter.log.evaluation.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.ui.util.SelectionUtil;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.handlers.HandlerUtil;

@SuppressWarnings("restriction")
public class Util {

	/**
	 * @author Raffi Khatchadourian
	 * @param enclosing
	 *            eclipse method object
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

	public static CSVPrinter createCSVPrinter(String fileName, String[] header) throws IOException {
		return new CSVPrinter(new FileWriter(fileName, true), CSVFormat.EXCEL.withHeader(header));
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
