package edu.cuny.hunter.mylyngit.core.analysis;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.core.IInteractionContext;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.internal.context.core.ContextCorePlugin;
import org.eclipse.mylyn.monitor.core.InteractionEvent.Kind;

import edu.cuny.hunter.mylyngit.core.utils.GitMethod;
import edu.cuny.hunter.mylyngit.core.utils.Util;

/**
 * @author tangy
 *
 */
@SuppressWarnings("restriction")
public class MylynGitPredictionProvider {

	private IJavaProject[] javaProjects;

	private HashSet<IMethod> methods = new HashSet<>();

	private final static String ID = "mylyngit.git";

	/**
	 * The entry point
	 */
	public void processProjects() {
		for (IJavaProject javaProject : javaProjects) {
			List<ICompilationUnit> cUnits = Util.getCompilationUnits(javaProject);
			cUnits.forEach(cUnit -> {
				methods.addAll(this.getIMethodsInSouceCode(cUnit));
			});
			bumpDOIValuesForAllMethods(javaProject);
		}
	}

	/**
	 * Given a java project, get the repository.
	 */
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
	 * Get the element in Mylyn.
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
	private void bumpDOIValuesForAllMethods(IJavaProject javaProject) {
		File repo = getRepoFile(javaProject);
		GitHistoryAnalyzer gitHistoryAnalyzer = new GitHistoryAnalyzer(repo);
		LinkedList<GitMethod> gitMethods = gitHistoryAnalyzer.getGitMethods();
		gitMethods.forEach(method -> {
			bumpDOIValuesForMethod(method);
		});
	}

	/**
	 * Bump DOI value for a method in the current source code when its historical
	 * method has a change.
	 */
	private void bumpDOIValuesForMethod(GitMethod gitMethod) {
		TypesOfMethodOperations methodOp = gitMethod.getMethodOp();
		IMethod method = getJavaMethod(gitMethod);
		// The historical has its corresponding method in the current source code.
		if (method != null)
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
		for (IMethod m : this.methods) {
			if (Util.getMethodSignatureForJavaMethod(m).equals(method.getMethodSignature())
					&& (Util.getMethodFilePath(m).equals(method.getFilePath())))
				return m;
		}
		return null;
	}

	private HashSet<IMethod> getIMethodsInSouceCode(ICompilationUnit icu) {
		HashSet<IMethod> methods = new HashSet<>();
		final CompilationUnit cu = this.getCompilationUnit(icu);

		cu.accept(new ASTVisitor() {
			@Override
			public boolean visit(MethodDeclaration methodDeclaration) {
				try {
					methods.add((IMethod) methodDeclaration.resolveBinding().getJavaElement());
					return true;
				} catch (Exception e) {
					return false;
				}
			}
		});

		return methods;
	}

	/**
	 * Make the method uninteresting here.
	 */
	public void resetDOIValue(IMethod method) {
		ContextCorePlugin.getContextManager().manipulateInterestForElement(getInteractionElement(method), false, false,
				true, ID, true);

	}
}
