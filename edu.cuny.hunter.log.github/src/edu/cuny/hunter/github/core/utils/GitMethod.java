package edu.cuny.hunter.github.core.utils;

import edu.cuny.hunter.github.core.analysis.TypesOfMethodOperations;

public class GitMethod {

	String methodSignature;
	TypesOfMethodOperations methodOp;

	String filePath;
	String fileOp;

	int commitIndex;
	String commitName;

	public GitMethod(String methodSignature, TypesOfMethodOperations methodOp, String filePath, String fileOp,
			int commitIndex, String commitName) {
		this.methodSignature = methodSignature;
		this.methodOp = methodOp;
		this.filePath = filePath;
		this.fileOp = fileOp;
		this.commitIndex = commitIndex;
		this.commitName = commitName;

	}

	public String getMethodSignature() {
		return this.methodSignature;
	}

	public TypesOfMethodOperations getMethodOp() {
		return this.methodOp;
	}

	public String getFilePath() {
		return this.filePath;
	}

	public String getFileOp() {
		return this.fileOp;
	}

	public int getCommitIndex() {
		return this.commitIndex;
	}

	public String getCommitName() {
		return this.commitName;
	}
}
