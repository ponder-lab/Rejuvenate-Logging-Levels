package edu.cuny.hunter.mylyngit.core.analysis;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.core.IInteractionContext;
import org.eclipse.mylyn.internal.context.core.ContextCorePlugin;
import org.eclipse.mylyn.internal.context.core.InteractionContextScaling;
import org.eclipse.mylyn.internal.java.ui.search.AbstractJavaRelationProvider;
import org.eclipse.mylyn.monitor.core.InteractionEvent.Kind;

import edu.cuny.hunter.mylyngit.core.utils.Commit;
import edu.cuny.hunter.mylyngit.core.utils.GitMethod;
import edu.cuny.hunter.mylyngit.core.utils.NonActiveMylynTaskException;
import edu.cuny.hunter.mylyngit.core.utils.Util;
import edu.cuny.hunter.mylyngit.core.utils.Vertex;

/**
 * @author tangy
 *
 */
@SuppressWarnings("restriction")
public class MylynGitPredictionProvider extends AbstractJavaRelationProvider {

	private static final String Name = "mylyn git";

	private int actualNumberOfCommits;
	
	private String repoURL = "";
	
	private HashSet<IMethod> enclosingMethods;
	
	private InteractionContextScaling scaling = (InteractionContextScaling) ContextCorePlugin.getContextManager()
			.getActiveContext().getScaling();

	public MylynGitPredictionProvider() {
		super("java", ID);
	}

	public MylynGitPredictionProvider(int N, HashSet<IMethod> enclosingMethods) {
		this();
		this.enclosingMethods = enclosingMethods;
		NToUseForCommits = N;
	}
	
	public MylynGitPredictionProvider(int N) {
		this();
		NToUseForCommits = N;
	}

	private static int NToUseForCommits;

	private IJavaProject[] javaProjects;

	private HashSet<MethodDeclaration> methodDeclarations = new HashSet<>();

	private HashMap<MethodDeclaration, IMethod> methodDecToIMethod = new HashMap<>();

	private static String ID = MylynGitPredictionProvider.class.getName();

	private LinkedList<Commit> commits;

	PrintWriter writer;

	/**
	 * The entry point
	 * 
	 * @throws GitAPIException
	 * @throws IOException
	 * @throws NoHeadException
	 * @throws NonActiveMylynTaskException
	 */
	public void processProjects() throws NoHeadException, IOException, GitAPIException, NonActiveMylynTaskException {
		clearTaskContext();
		for (IJavaProject javaProject : javaProjects) {
			this.processOneProject(javaProject);
			clearTaskContext();
		}
	}

	public void processOneProject(IJavaProject javaProject) throws NoHeadException, IOException, GitAPIException {
		this.methodDeclarations.clear();
		this.methodDecToIMethod.clear();

		List<ICompilationUnit> cUnits = Util.getCompilationUnits(javaProject);
		cUnits.forEach(cUnit -> {
			this.methodDeclarations.addAll(this.getIMethodsInSouceCode(cUnit));
		});

		this.methodDeclarations.forEach(m -> {
			IMethodBinding methodBinding = m.resolveBinding();
			if (methodBinding != null)
				this.methodDecToIMethod.put(m, (IMethod) methodBinding.getJavaElement());
		});

		writer = new PrintWriter("DOI_value.csv", "UTF-8");
		this.bumpDOIValuesForAllGitMethods(javaProject);
		writer.close();
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
	 * Bump DOI when there is a method change.
	 */
	public void bumpDOI(IMethod method) {	
		
		if (this.enclosingMethods.contains(method)) {
			this.scaling.set(Kind.EDIT, (float) 5.6);
		} else this.scaling.set(Kind.EDIT, (float) 0.7);
		
		IInteractionContext activeContext = ContextCore.getContextManager().getActiveContext();
		ContextCorePlugin.getContextManager().processInteractionEvent(method, Kind.EDIT, ID, activeContext);

		writer.println("\"" + method.getHandleIdentifier() + "\"," + ContextCore.getContextManager().getElement(
				"=client/src<org.openqa.selenium.net{UrlChecker.java[UrlChecker~waitUntilAvailable~J~QTimeUnit;~\\[QURL;")
				.getInterest().getValue());

	}

	/**
	 * Process all methods in the git history and bump the DOI values
	 * 
	 * @throws GitAPIException
	 * @throws IOException
	 * @throws NoHeadException
	 */
	private void bumpDOIValuesForAllGitMethods(IJavaProject javaProject) throws GitAPIException, IOException {
		File repo = this.getRepoFile(javaProject);
		GitHistoryAnalyzer gitHistoryAnalyzer = new GitHistoryAnalyzer(repo, NToUseForCommits);
		// store commits and remote URL of repository
		this.setCommits(gitHistoryAnalyzer.getCommits());
		this.setRepoURL(gitHistoryAnalyzer.getRepoURL());
		this.setActualNumberOfCommits(gitHistoryAnalyzer.getActualNumberOfCommits());

		LinkedList<GitMethod> gitMethods = gitHistoryAnalyzer.getGitMethods();
		gitMethods.forEach(method -> {
			bumpDOIValuesForMethod(method, gitHistoryAnalyzer);
		});
	}

	/**
	 * Clear intermediate data.
	 */
	public static void clearMappingData() {
		GitHistoryAnalyzer.clearMappingData();
	}

	/**
	 * Bump DOI value for a method in the current source code when its historical
	 * method has a change.
	 */
	private void bumpDOIValuesForMethod(GitMethod gitMethod, GitHistoryAnalyzer gitHistoryAnalyzer) {
		TypesOfMethodOperations methodOp = gitMethod.getMethodOp();
		MethodDeclaration methodDec = this.getMethodDeclaration(gitMethod, gitHistoryAnalyzer);
		if (methodDec == null)
			return;
		IMethod method = this.methodDecToIMethod.get(methodDec);
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
				Util.resetDOIValue(method, ID);
				break;
			}
	}

	public IMethod getIMethod(MethodDeclaration method) {
		return this.methodDecToIMethod.get(method);
	}

	/**
	 * Given a method information, return a IMethod instance whose methods exist in
	 * the current source code.
	 */
	private MethodDeclaration getMethodDeclaration(GitMethod method, GitHistoryAnalyzer gitHistoryAnalyzer) {
		MethodDeclaration methodDec = this.checkMethods(method.getMethodSignature(), method.getFilePath());
		if (methodDec != null)
			return methodDec;

		// if it exists in the renaming graph
		HashMap<Vertex, Vertex> historicalMethodToCurrentMethods = gitHistoryAnalyzer
				.getHistoricalMethodToCurrentMethods();
		Vertex vertex = new Vertex(method.getMethodSignature(), method.getFilePath(), -1);
		Vertex targetVertex = null;
		Set<Vertex> historicalMethods = historicalMethodToCurrentMethods.keySet();
		for (Vertex historicalMethod : historicalMethods) {
			if (historicalMethod.equals(vertex)) {
				targetVertex = historicalMethod;
				break;
			}
		}
		// find the vertex exists in the renaming graph
		if (targetVertex != null) {
			Vertex currentMethod = historicalMethodToCurrentMethods.get(targetVertex);
			return this.checkMethods(currentMethod.getMethod(), currentMethod.getFile());
		}

		return null;
	}

	/**
	 * Check whether there is a method in the list of IMethod
	 */
	private MethodDeclaration checkMethods(String method, String filePath) {
		for (MethodDeclaration m : this.methodDeclarations) {
			if (Util.getMethodSignature(m).equals(method)) {
				String path = Util.getMethodFilePath(this.methodDecToIMethod.get(m));
				if ((path.length() - filePath.length() >= 0)
						&& (path.substring(path.length() - filePath.length()).equals(filePath)))
					return m;
			}
		}
		return null;
	}

	private HashSet<MethodDeclaration> getIMethodsInSouceCode(ICompilationUnit icu) {
		HashSet<MethodDeclaration> methodDecs = new HashSet<>();
		final CompilationUnit cu = Util.getCompilationUnit(icu);

		cu.accept(new ASTVisitor() {
			@Override
			public boolean visit(MethodDeclaration methodDeclaration) {
				try {
					if (methodDeclaration != null)
						methodDecs.add(methodDeclaration);
					return true;
				} catch (Exception e) {
					return false;
				}
			}
		});

		return methodDecs;
	}

	public HashSet<MethodDeclaration> getMethods() {
		return this.methodDeclarations;
	}

	public static void clearTaskContext() throws NonActiveMylynTaskException {
		Util.clearTaskContext();
	}

	@Override
	public String getName() {
		return Name;
	}

	@Override
	protected String getSourceId() {
		return ID;
	}

	public LinkedList<Commit> getCommits() {
		return commits;
	}

	private void setCommits(LinkedList<Commit> commits) {
		this.commits = commits;
	}

	public String getRepoURL() {
		return repoURL;
	}

	public void setRepoURL(String repoURL) {
		this.repoURL = repoURL;
	}

	public int getActualNumberOfCommits() {
		return actualNumberOfCommits;
	}

	public void setActualNumberOfCommits(int actualNumberOfCommits) {
		this.actualNumberOfCommits = actualNumberOfCommits;
	}

}
