package edu.cuny.hunter.mylyngit.tests;

import java.util.LinkedList;

import edu.cuny.hunter.mylyngit.core.analysis.TypesOfMethodOperations;

public class GitAnalysisExpectedResult {
	String methodSig;
	LinkedList<TypesOfMethodOperations> methodOperations = new LinkedList<>();
	
	public GitAnalysisExpectedResult(String methodSig, TypesOfMethodOperations... methodOps) {
		this.methodSig = methodSig;
		for (TypesOfMethodOperations methodOp : methodOps) {
			methodOperations.add(methodOp);
		}
	}
	
	public String getMethodSignature() {
		return this.methodSig;
	}
	
	public LinkedList<TypesOfMethodOperations> getMethodOperations() {
		return this.methodOperations;
	}
}
