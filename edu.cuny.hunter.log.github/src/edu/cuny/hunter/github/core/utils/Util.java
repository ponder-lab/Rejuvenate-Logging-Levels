package edu.cuny.hunter.github.core.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

public class Util {
	public final static String LOGGER_NAME = "edu.cuny.hunter.logging";

	// Return a method signature
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
}
