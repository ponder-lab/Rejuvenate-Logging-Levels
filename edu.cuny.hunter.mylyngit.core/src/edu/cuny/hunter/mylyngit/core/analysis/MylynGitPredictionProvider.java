package edu.cuny.hunter.mylyngit.core.analysis;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.mylyn.context.core.AbstractContextStructureBridge;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.core.IInteractionContext;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.internal.context.core.ContextCorePlugin;
import org.eclipse.mylyn.monitor.core.InteractionEvent;
import org.eclipse.mylyn.monitor.core.InteractionEvent.Kind;

import edu.cuny.hunter.mylyngit.core.utils.GitMethod;

/**
 * @author tangy
 *
 */
@SuppressWarnings("restriction")
public class MylynGitPredictionProvider {

	private Set<File> repoFiles = new HashSet<>();

	private IJavaProject[] javaProjects;

	private final static String ID = "mylyngit.git";

	// TODO: consider this carefully
	private void setJavaProjectsInRepo(LinkedList<IJavaProject> javaProjectsInRepo) {
		this.javaProjectsInRepo = javaProjectsInRepo;
	}

	private void setRepoFiles() {
		for (IJavaProject javaProject : javaProjects) {
			this.repoFiles.add(getRepoFile(javaProject));
		}
	}

	/**
	 * After the user checks the option to analyze the git history, the tool should
	 * get a repository file.
	 */
	private File getRepoFile(IJavaProject project) {
		return project.getResource().getLocation().toFile();
	}

	private Set<File> getRepoFiles() {
		return this.repoFiles;
	}

	/**
	 * Compute all Java projects in the workspace.
	 */
	private void computeJavaProjects() {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = workspaceRoot.getProjects();
		for (IProject project : projects) {
			for (File repoFile : repoFiles)
				if (isJavaProjectInRepo(project, repoFile))
					this.javaProjectsInRepo.add(project);
		}
	}

	private LinkedList<IJavaProject> getJavaProjectsInRepo() {
		return this.javaProjectsInRepo;
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
	 * Is Java project in the repository?
	 */
	private boolean isJavaProjectInRepo(IProject project, File repoFile) {
		if (repoFile.getAbsolutePath().contains(project.getProjectRelativePath().makeAbsolute().toString())) {
			return true;
		}
		return false;
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
	
	private IMethod getJavaMethod(String methodSignature) {
		
	}

	/**
	 * Make the method uninteresting here.
	 */
	public void resetDOIValue(IMethod method) {
		ContextCorePlugin.getContextManager().manipulateInterestForElement(getInteractionElement(method), false, false,
				true, ID, true);

	}
}
