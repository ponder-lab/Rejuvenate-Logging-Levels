package edu.cuny.hunter.mylyngit.core.analysis;

import java.util.HashSet;
import java.util.Set;

public class GitMylynAnalyzer {
	
	private Set<MylynMethodDeclaration> methodDeclarations = new HashSet<>();
	
	public Set<MylynMethodDeclaration> getSetOfMylynMethodDeclarations() {
		return this.methodDeclarations;
	}

}
