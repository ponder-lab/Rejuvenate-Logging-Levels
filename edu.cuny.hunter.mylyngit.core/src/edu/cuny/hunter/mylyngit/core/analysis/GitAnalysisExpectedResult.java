package edu.cuny.hunter.mylyngit.core.analysis;

import java.util.LinkedList;

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
