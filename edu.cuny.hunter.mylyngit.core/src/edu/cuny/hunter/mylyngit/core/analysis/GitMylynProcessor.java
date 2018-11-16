package edu.cuny.hunter.mylyngit.core.analysis;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * This class works on repository level.
 * 
 * @author tangy
 *
 */
public class GitMylynProcessor {

	private Set<File> repoFiles = new HashSet<>();

	private IJavaProject[] javaProjects;

	private LinkedList<IJavaProject> javaProjectsInRepo = new LinkedList<>();

	private Set<MylynMethodDeclaration> methodDeclarations = new HashSet<>();

	public Set<MylynMethodDeclaration> getSetOfMylynMethodDeclarations() {
		return this.methodDeclarations;
	}

	public void setSetOfMylynMethodDeclarations(Set<MylynMethodDeclaration> methodDeclarations) {
		this.methodDeclarations = methodDeclarations;
	}

	// TODO: consider this carefully
	private void setJavaProjectsInRepo(LinkedList<IJavaProject> javaProjectsInRepo) {
		this.javaProjectsInRepo = javaProjectsInRepo;
	}

//	private void setRepoFiles() {
//		for (IJavaProject javaProject : javaProjects) {
//			this.repoFiles.add(getRepoFile(javaProject));
//		}
//	}

//	/**
//	 * After the user checks the option to analyze the git history, the tool should
//	 * get a repository file.
//	 */
//	private File getRepoFile(IJavaProject project) {
//		return project.getResource().getLocation().toFile();
//	}
//	
//	private Set<File> getRepoFiles(){
//		return this.repoFiles;
//	}

//	/**
//	 * Compute all Java projects in the workspace.
//	 */
//	private void computeJavaProjects() {
//		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
//		IProject[] projects = workspaceRoot.getProjects();
//		for (IProject project : projects) {
//			for (File repoFile : repoFiles)
//				if (isJavaProjectInRepo(project, repoFile)) this.javaProjectsInRepo.add(project);
//		}
//	}
//	
	private LinkedList<IJavaProject> getJavaProjectsInRepo() {
		return this.javaProjectsInRepo;
	}

	private void computDOIValuesFromGit() throws JavaModelException {
		for (IJavaProject jproj : this.getJavaProjectsInRepo()) {
			IPackageFragmentRoot[] roots = jproj.getPackageFragmentRoots();
			for (IPackageFragmentRoot root : roots) {
				IJavaElement[] children = root.getChildren();
				for (IJavaElement child : children)
					if (child.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
						IPackageFragment fragment = (IPackageFragment) child;
						ICompilationUnit[] units = fragment.getCompilationUnits();
						for (ICompilationUnit unit : units) {
							CompilationUnit compilationUnit = getCompilationUnit(unit);
							compilationUnit.accept(new GitMylynAnalyzer());
						}
					}
			}
		}
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

//	/**
//	 * Is Java project in the repository?
//	 */
//	private boolean isJavaProjectInRepo(IProject project, File repoFile) {
//		if (repoFile.getAbsolutePath().contains(project.getProjectRelativePath().makeAbsolute().toString())) {
//			return true;
//		}
//		return false;
//	}
}
