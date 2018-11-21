package edu.cuny.hunter.mylyngit.core.analysis;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import edu.cuny.hunter.mylyngit.core.utils.GitMethod;
import edu.cuny.hunter.mylyngit.core.utils.Graph;
import edu.cuny.hunter.mylyngit.core.utils.Vertex;

public class GitMylynAnalyzer extends ASTVisitor {

	private Set<MylynMethodDeclaration> methodDeclarations = new HashSet<>();

	public GitMylynAnalyzer() {

	}

	/**
	 * This method is used to find a set of logging objects
	 */
	@Override
	public boolean visit(MethodDeclaration node) {
		methodDeclarations.add(new MylynMethodDeclaration(node));
		return super.visit(node);
	}

	public Set<MylynMethodDeclaration> getMethodDeclarations() {
		return this.methodDeclarations;
	}

	public void analyze(File repo) {
		GitHistoryAnalyzer gitHistoryAnalyzer = new GitHistoryAnalyzer(repo);
		LinkedList<GitMethod> gitMethods = gitHistoryAnalyzer.getGitMethods();
		// Get the method renaming graph
		Graph renaming = gitHistoryAnalyzer.getRenaming();

		// Get all method and their operations in the git history to bump the DOI values
		for (GitMethod gitMethod : gitMethods) {
			// Get methods, files, and method change operations
			String method = gitMethod.getMethodSignature();
			String file = gitMethod.getFilePath();
			TypesOfMethodOperations methodOp = gitMethod.getMethodOp();

			MylynMethodDeclaration methodDec = checkHistoricalMethodExist(method, file);

			if (methodDec != null) {
				bumpDOIByVertex(methodDec, methodOp);
			} else {

				Vertex vertex = new Vertex(method, file);
				Vertex head = null;

				// Get the head of vertex
				for (Vertex v : renaming.getVertices()) {
					if (v.equals(vertex)) {
						head = v.getHead();
						break;
					}
				}

				// The method exists in the graph
				while (head != null) {
					methodDec = checkHistoricalMethodExist(head.getMethod(), head.getFile());
					if (methodDec != null) {
						bumpDOIByVertex(methodDec, methodOp);
						break;
					}
					head = head.getNextVertex();
				}
			}
		}
	}

	/**
	 * Check whether a historical method exists in the current source code.
	 */
	private MylynMethodDeclaration checkHistoricalMethodExist(String method, String file) {
		for (MylynMethodDeclaration methodDeclaration : methodDeclarations) {
			if (methodDeclaration.getMethodSignature().equals(method) && methodDeclaration.getFilePath().equals(file))
				return methodDeclaration;
		}
		return null;
	}

	private void bumpDOIByVertex(MylynMethodDeclaration mylynMethodDeclaration, TypesOfMethodOperations methodOp) {
		switch (methodOp) {
		case ADD:
			mylynMethodDeclaration.setDegreeOfInterestValue(0);
			mylynMethodDeclaration.bumpDOI();
			break;
		case DELETE:
			mylynMethodDeclaration.setDegreeOfInterestValue(0);
			break;
		case RENAME:
		case CHANGE:
		case CHANGEPARAMETER:
			mylynMethodDeclaration.bumpDOI();
			break;
		}
	}
}
