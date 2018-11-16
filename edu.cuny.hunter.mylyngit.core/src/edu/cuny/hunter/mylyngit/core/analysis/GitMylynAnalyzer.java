package edu.cuny.hunter.mylyngit.core.analysis;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
public class GitMylynAnalyzer extends ASTVisitor {
	
	private Set<MylynMethodDeclaration> methodDeclarations = new HashSet<>();
	
	public void GitMylynAnalyzer() {
		
	}
	
	/**
	 * This method is used to find a set of logging objects
	 */
	@Override
	public boolean visit(MethodDeclaration node) {
		return  super.visit(node);
	}
}
