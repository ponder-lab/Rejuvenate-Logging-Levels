package edu.cuny.hunter.mylyngit.core.analysis;

import java.io.File;
import java.util.LinkedList;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.core.IInteractionContext;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.internal.context.core.ContextCorePlugin;
import org.eclipse.mylyn.monitor.core.InteractionEvent.Kind;

import edu.cuny.hunter.mylyngit.core.utils.GitMethod;

/**
 * @author tangy
 *
 */
@SuppressWarnings("restriction")
public class MylynGitPredictionProvider {

	private IJavaProject[] javaProjects;

	private final static String ID = "mylyngit.git";


	public void processProjects() {
		for (IJavaProject javaProject : javaProjects) {
			File repo = getRepoFile(javaProject);
			bumpDOIValuesForAllMethods();	
		}
	}
	
	private File getRepoFile(IJavaProject javaProject) {
		return javaProject.getResource().getLocation().toFile();
	}
	
	public void setJavaProjects(IJavaProject[] javaProjects) {
		this.javaProjects = javaProjects;
	}
	
	/**
	 * Create CompilationUnit from ICompilationUnit.
	 */
	protected CompilationUnit getCompilationUnit(ICompilationUnit unit) {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setResolveBindings(true);
		parser.setSource(unit);

		return (CompilationUnit) parser.createAST(null);
	}

	/**
	 * The element in Mylyn
	 */
	private IInteractionElement getInteractionElement(IMethod method) {
		return ContextCore.getContextManager().getElement(method.getHandleIdentifier());
	}

	/**
	 * Bump DOI when there is a method change.
	 */
	public void bumpDOI(IMethod method) {
		IInteractionContext activeContext = ContextCore.getContextManager().getActiveContext();
		ContextCorePlugin.getContextManager().processInteractionEvent(method, Kind.PREDICTION, ID, activeContext);
	}
	
	/**
	 * Process all methods in the git history and bump the DOI values
	 */
	private void bumpDOIValuesForAllMethods() {
		GitHistoryAnalyzer gitHistoryAnalyzer = new GitHistoryAnalyzer();
		LinkedList<GitMethod> gitMethods = gitHistoryAnalyzer.getGitMethods();
		gitMethods.forEach(method -> {
			bumpDOIValuesForMethod(method);
		});
	}

	private void bumpDOIValuesForMethod(GitMethod gitMethod) {
		TypesOfMethodOperations methodOp = gitMethod.getMethodOp();
		IMethod method = getJavaMethod(gitMethod);
		switch (methodOp) {
		case ADD:
		case RENAME:
		case CHANGE:
		case CHANGEPARAMETER:
			this.bumpDOI(method);
			break;
		case DELETE:
			resetDOIValue(method);
			break;
		}
	}

	/**
	 * Given a method information, return a IMethod instance whose methods exist in
	 * the current source code.
	 */
	private IMethod getJavaMethod(GitMethod method) {

	}

	/**
	 * Make the method uninteresting here.
	 */
	public void resetDOIValue(IMethod method) {
		ContextCorePlugin.getContextManager().manipulateInterestForElement(getInteractionElement(method), false, false,
				true, ID, true);

	}
}
