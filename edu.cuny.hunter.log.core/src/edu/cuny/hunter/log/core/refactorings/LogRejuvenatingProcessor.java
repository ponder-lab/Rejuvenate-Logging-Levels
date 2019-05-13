package edu.cuny.hunter.log.core.refactorings;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite.ImportRewriteContext;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.codemanipulation.ContextSensitiveImportRewriteContext;
import org.eclipse.jdt.internal.corext.refactoring.changes.DynamicValidationRefactoringChange;
import org.eclipse.jdt.internal.corext.refactoring.structure.CompilationUnitRewrite;
import org.eclipse.jdt.internal.corext.refactoring.util.TextEditBasedChangeManager;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;

import edu.cuny.citytech.refactoring.common.core.RefactoringProcessor;
import edu.cuny.hunter.log.core.analysis.Action;
import edu.cuny.hunter.log.core.analysis.LogAnalyzer;
import edu.cuny.hunter.log.core.analysis.LogInvocation;
import edu.cuny.hunter.log.core.descriptors.LogDescriptor;
import edu.cuny.hunter.log.core.messages.Messages;
import edu.cuny.hunter.log.core.utils.LoggerNames;
import edu.cuny.hunter.log.core.utils.Util;
import edu.cuny.hunter.mylyngit.core.analysis.MylynGitPredictionProvider;
import edu.cuny.hunter.mylyngit.core.utils.Commit;
import edu.cuny.hunter.mylyngit.core.utils.NonActiveMylynTaskException;

@SuppressWarnings({ "restriction", "deprecation" })
public class LogRejuvenatingProcessor extends RefactoringProcessor {

	private static final Logger LOGGER = Logger.getLogger(LoggerNames.LOGGER_NAME);

	private Set<LogInvocation> logInvocationSet = new HashSet<>();

	private LinkedList<Commit> commits = new LinkedList<>();

	private IJavaProject[] javaProjects;

	/**
	 * Boundary for DOI values of enclosing methods
	 */
	private LinkedList<Float> boundary;

	/**
	 * Treat CONFIG as category
	 */
	private boolean useLogCategoryWithConfig;

	/**
	 * Treat CONFIG/WARNING/SEVERE log levels as category
	 */
	private boolean useLogCategory;

	/**
	 * Should we use git history to bump DOI values for all methods?
	 */
	private boolean useGitHistory;

	/**
	 * Should we consider logging statements in catch blocks?
	 */
	private boolean notLowerLogLevelInCatchBlock;

	/**
	 * We should not lower log level in immediate if statement.
	 */
	private boolean notLowerLogLevelInIfStatement;

	/**
	 * Not lower logs with particular keywords in their messages.
	 */
	private boolean notLowerLogLevelWithKeyWords;

	/**
	 * Limit number of commits
	 */
	private int NToUseForCommits = 100;

	/**
	 * The actual number of commits in repo.
	 */
	private int actualNumberOfCommits;

	/**
	 * Keep if condition and log levels inside if statement consistent
	 */
	private boolean checkIfCondition;

	/**
	 * It the caller Evaluation plugin?
	 */
	private boolean isEvaluation;

	private HashSet<LogInvocation> logInvsNotTransformedInIf = new HashSet<LogInvocation>();
	private HashSet<LogInvocation> logInvsNotLoweredInCatch = new HashSet<LogInvocation>();
	private HashSet<LogInvocation> logInvsNotLoweredInIf = new HashSet<LogInvocation>();
	private HashSet<LogInvocation> logInvsNotLoweredWithKeywords = new HashSet<LogInvocation>();

	private String repoURL = "";

	public LogRejuvenatingProcessor(final CodeGenerationSettings settings) {
		super(settings);
	}

	public void setParticularConfigLogLevel(boolean useConfigLogLevelCategory) {
		this.useLogCategoryWithConfig = useConfigLogLevelCategory;
	}

	public void setUseGitHistory(boolean useGitHistory) {
		this.useGitHistory = useGitHistory;
	}

	public void setParticularLogLevel(boolean useLogLevelCategory) {
		this.useLogCategory = useLogLevelCategory;
	}

	public void setNToUseForCommits(int NToUseForCommits) {
		this.NToUseForCommits = NToUseForCommits;
	}

	public void setNotLowerLogLevelInCatchBlock(boolean notLowerLogLevelInCatchBlock) {
		this.notLowerLogLevelInCatchBlock = notLowerLogLevelInCatchBlock;
	}

	public int getNToUseForCommits() {
		return this.NToUseForCommits;
	}

	public boolean getParticularConfigLogLevel() {
		return this.useLogCategoryWithConfig;
	}

	public boolean getParticularLogLevel() {
		return this.useLogCategory;
	}

	public boolean getGitHistory() {
		return this.useGitHistory;
	}

	public boolean getNotLowerLogLevelInCatchBlock() {
		return this.notLowerLogLevelInCatchBlock;
	}

	public LogRejuvenatingProcessor(IJavaProject[] javaProjects, final CodeGenerationSettings settings,
			Optional<IProgressMonitor> monitor) {
		super(settings);
		try {
			this.javaProjects = javaProjects;
		} finally {
			monitor.ifPresent(IProgressMonitor::done);
		}
	}

	public LogRejuvenatingProcessor(IJavaProject[] javaProjects, boolean useLogLevelCategory,
			boolean useConfigLogLevelCategory, boolean useGitHistory, boolean notLowerLogLevelInCatchBlock,
			boolean notLowerLogLevelInIfStatement, boolean notLowerLogLevelWithKeywords, boolean checkIfCondtion,
			int NToUseForCommits, final CodeGenerationSettings settings, Optional<IProgressMonitor> monitor) {
		super(settings);
		try {
			this.javaProjects = javaProjects;
			this.useLogCategoryWithConfig = useConfigLogLevelCategory;
			this.useLogCategory = useLogLevelCategory;
			this.useGitHistory = useGitHistory;
			this.notLowerLogLevelInCatchBlock = notLowerLogLevelInCatchBlock;
			this.notLowerLogLevelInIfStatement = notLowerLogLevelInIfStatement;
			this.notLowerLogLevelWithKeyWords = notLowerLogLevelWithKeywords;
			this.checkIfCondition = checkIfCondtion;
			this.NToUseForCommits = NToUseForCommits;
		} finally {
			monitor.ifPresent(IProgressMonitor::done);
		}
	}

	public LogRejuvenatingProcessor(IJavaProject[] javaProjects, boolean useLogLevelCategory,
			boolean useConfigLogLevelCategory, boolean useGitHistory, boolean notLowerLogLevelInCatchBlock,
			boolean notLowerLogLevelInIfStatement, boolean notLowerLogLevelWithKeywords, boolean checkIfCondtion,
			int NToUseForCommits, final CodeGenerationSettings settings, Optional<IProgressMonitor> monitor,
			boolean isEvaluation) {
		this(javaProjects, useLogLevelCategory, useConfigLogLevelCategory, useGitHistory, notLowerLogLevelInCatchBlock,
				notLowerLogLevelInIfStatement, notLowerLogLevelWithKeywords, checkIfCondtion, NToUseForCommits,
				settings, monitor);
		this.isEvaluation = isEvaluation;
	}

	@Override
	public String getIdentifier() {
		return LogDescriptor.REFACTORING_ID;
	}

	@Override
	public String getProcessorName() {
		return Messages.Name;
	}

	@Override
	public RefactoringStatus checkFinalConditions(final IProgressMonitor monitor, final CheckConditionsContext context)
			throws JavaModelException {

		final RefactoringStatus status = new RefactoringStatus();

		for (IJavaProject jproj : this.getJavaProjects()) {

			LogAnalyzer analyzer = new LogAnalyzer(this.useLogCategoryWithConfig, this.useLogCategory,
					this.notLowerLogLevelInCatchBlock, this.checkIfCondition, this.notLowerLogLevelInIfStatement,
					this.notLowerLogLevelWithKeyWords);

			// If we are using the git history.
			if (this.useGitHistory) {
				try {
					Util.clearTaskContext();
				} catch (NonActiveMylynTaskException e) {
					status.addFatalError("No active Mylyn task found.");
					return status;
				}
			}

			IPackageFragmentRoot[] roots = jproj.getPackageFragmentRoots();
			for (IPackageFragmentRoot root : roots) {
				IJavaElement[] children = root.getChildren();
				for (IJavaElement child : children)
					if (child.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
						IPackageFragment fragment = (IPackageFragment) child;
						ICompilationUnit[] units = fragment.getCompilationUnits();
						for (ICompilationUnit unit : units) {
							CompilationUnit compilationUnit = super.getCompilationUnit(unit, monitor);
							compilationUnit.accept(analyzer);
						}
					}
			}

			this.addLogInvocationSet(analyzer.getLogInvocationSet());

			MylynGitPredictionProvider mylynProvider = null;

			if (this.useGitHistory) {
				// Process git history.
				mylynProvider = new MylynGitPredictionProvider(this.NToUseForCommits, this.getEnclosingMethods());

				try {
					this.processGitHistory(mylynProvider, analyzer, jproj);
				} catch (JGitInternalException | GitAPIException | IOException e) {
					status.addFatalError("Error reading git repository.");
					return status;
				}

				this.setActualNumberOfCommits(mylynProvider.getActualNumberOfCommits());
				this.setCommits(mylynProvider.getCommits());
				this.setRepoURL(mylynProvider.getRepoURL());
			}

			analyzer.analyze();

			this.setLogInvsNotLoweredInCatch(analyzer.getLogInvsNotLoweredInCatch());
			this.setLogInvsNotTransformedInIf(analyzer.getLogInvsNotTransformedInIf());
			this.setLogInvsNotLoweredInIf(analyzer.getLogInvsNotLoweredInIfStatement());
			this.setLogInvsNotLoweredWithKeywords(analyzer.getLogInvsNotLoweredByKeywords());

			// Get boundary
			this.boundary = analyzer.getBoundary();

			// If we are using the git history.
			if (this.useGitHistory) {
				// then, we must clear the context
				try {
					Util.clearTaskContext();
				} catch (NonActiveMylynTaskException e) {
					status.addFatalError("No active Mylyn task found.");
					return status;
				}
			}
		}

		// It is not called by evaluation plugin.
		if (!isEvaluation)
			MylynGitPredictionProvider.clearMappingData();

		// get the status of each log invocation.
		RefactoringStatus collectedStatus = this.getLogInvocationSet().stream().map(LogInvocation::getStatus)
				.collect(() -> new RefactoringStatus(), (a, b) -> a.merge(b), (a, b) -> a.merge(b));
		status.merge(collectedStatus);

		if (!status.hasFatalError()) {
			if (logInvocationSet.isEmpty()) {
				status.addWarning(Messages.NoInputLogInvs);
			}
		}

		return status;
	}

	/**
	 * Given a set of log invocations, return a set of enclosing methods.
	 * 
	 * @return a set of enclosing methods
	 */
	private HashSet<IMethod> getEnclosingMethods() {
		HashSet<IMethod> methods = new HashSet<IMethod>();
		this.logInvocationSet.forEach(inv -> {
			if (inv.getEnclosingEclipseMethod() != null)
				methods.add(inv.getEnclosingEclipseMethod());
		});
		return methods;
	}

	/**
	 * Call mylyngit plugin to process git history.
	 * 
	 * @param mylynProvider
	 * @param analyzer
	 * @param jproj
	 */
	private void processGitHistory(MylynGitPredictionProvider mylynProvider, LogAnalyzer analyzer, IJavaProject jproj)
			throws GitAPIException, JGitInternalException, IOException {
		try {
			if (this.useGitHistory) {
				mylynProvider.processOneProject(jproj);
				analyzer.updateDOI();
			}
		} catch (GitAPIException | JGitInternalException e) {
			LOGGER.severe("Cannot get valid git object! May not a valid git repo.");
			throw e;
		} catch (IOException e) {
			LOGGER.severe("Cannot process git commits.");
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * get a set of transformed log set
	 */
	public Set<LogInvocation> getTransformedLog() {
		HashSet<LogInvocation> transformedSet = new HashSet<>();
		for (LogInvocation logInvocation : this.logInvocationSet) {
			if (logInvocation.getAction() != null && !logInvocation.getAction().equals(Action.NONE)
					&& logInvocation.getStatus().isOK())
				transformedSet.add(logInvocation);
		}
		return transformedSet;
	}

	private IJavaProject[] getJavaProjects() {
		return this.javaProjects;
	}

	public LinkedList<Float> getBoundary() {
		return this.boundary;
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		try {
			final TextEditBasedChangeManager manager = new TextEditBasedChangeManager();
			Set<LogInvocation> transformedLogs = this.getTransformedLog();

			pm.beginTask("Transforming logging levels ...", transformedLogs.size());
			for (LogInvocation logInvocation : transformedLogs) {
				CompilationUnitRewrite rewrite = this.getCompilationUnitRewrite(
						logInvocation.getEnclosingEclipseMethod().getCompilationUnit(),
						logInvocation.getEnclosingCompilationUnit());

				ImportRewriteContext context = new ContextSensitiveImportRewriteContext(
						logInvocation.getEnclosingCompilationUnit(), rewrite.getImportRewrite());

				logInvocation.transform(rewrite);
				pm.worked(1);

				// add static imports if necessary.
				// for each import statement in the enclosing compilation unit.
				List<?> imports = logInvocation.getEnclosingCompilationUnit().imports();
				for (Object obj : imports) {
					ImportDeclaration importDeclaration = (ImportDeclaration) obj;
					// if the import is static (this is the only case we need to
					// worry about).
					if (importDeclaration.isStatic()) {
						Name name = importDeclaration.getName();
						String matchName = "java.util.logging.Level."
								+ logInvocation.getReplacedName().getFullyQualifiedName();

						if (name.getFullyQualifiedName().equals(matchName)) {
							// deal with imports.
							// register the removed name.
							rewrite.getImportRemover().registerRemovedNode(logInvocation.getReplacedName());

							// we are replacing a log level that has been
							// statically imported.
							// then, add a static import for new log level.
							rewrite.getImportRewrite().addStaticImport("java.util.logging.Level",
									logInvocation.getNewTargetName().getFullyQualifiedName(), true, context);
						}
					}
				}
			}

			// save the source changes.
			ICompilationUnit[] units = this.getCompilationUnitToCompilationUnitRewriteMap().keySet().stream()
					.filter(cu -> !manager.containsChangesIn(cu)).toArray(ICompilationUnit[]::new);

			for (ICompilationUnit cu : units) {
				CompilationUnit compilationUnit = this.getCompilationUnit(cu, pm);
				this.manageCompilationUnit(manager, this.getCompilationUnitRewrite(cu, compilationUnit),
						Optional.of(new SubProgressMonitor(pm, IProgressMonitor.UNKNOWN)));
			}

			final Map<String, String> arguments = new HashMap<>();
			int flags = RefactoringDescriptor.STRUCTURAL_CHANGE | RefactoringDescriptor.MULTI_CHANGE;

			// TODO: Fill in description.

			LogDescriptor descriptor = new LogDescriptor(null, "TODO", null, arguments, flags);

			return new DynamicValidationRefactoringChange(descriptor, this.getProcessorName(), manager.getAllChanges());
		} finally {
			pm.done();
			this.clearCaches();
		}
	}

	protected void addLogInvocationSet(Set<LogInvocation> logInvocationSet) {
		this.logInvocationSet.addAll(logInvocationSet);
	}

	public Set<LogInvocation> getLogInvocationSet() {
		return this.logInvocationSet;
	}

	@Override
	public Object[] getElements() {
		return null;
	}

	@Override
	public boolean isApplicable() throws CoreException {
		return false;
	}

	@Override
	public RefactoringParticipant[] loadParticipants(RefactoringStatus status, SharableParticipants sharedParticipants)
			throws CoreException {
		return null;
	}

	public LinkedList<Commit> getCommits() {
		return this.commits;
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

	public boolean isCheckIfCondition() {
		return checkIfCondition;
	}

	public void setCheckIfCondition(boolean checkIfCondition) {
		this.checkIfCondition = checkIfCondition;
	}

	public HashSet<LogInvocation> getLogInvsNotTransformedInIf() {
		return this.logInvsNotTransformedInIf;
	}

	public void setLogInvsNotTransformedInIf(HashSet<LogInvocation> logLevelNotTransformedInIf) {
		this.logInvsNotTransformedInIf = logLevelNotTransformedInIf;
	}

	public HashSet<LogInvocation> getLogInvsNotLoweredInCatch() {
		return this.logInvsNotLoweredInCatch;
	}

	public void setLogInvsNotLoweredInCatch(HashSet<LogInvocation> logLevelNotLoweredInCatch) {
		this.logInvsNotLoweredInCatch = logLevelNotLoweredInCatch;
	}

	public int getActualNumberOfCommits() {
		return actualNumberOfCommits;
	}

	public void setActualNumberOfCommits(int actualNumberOfCommits) {
		this.actualNumberOfCommits = actualNumberOfCommits;
	}

	public boolean isNotLowerLogLevelInIfStatement() {
		return notLowerLogLevelInIfStatement;
	}

	public void setNotLowerLogLevelInIfStatement(boolean notLowerLogLevelInIfStatement) {
		this.notLowerLogLevelInIfStatement = notLowerLogLevelInIfStatement;
	}

	public HashSet<LogInvocation> getLogInvsNotLoweredInIf() {
		return logInvsNotLoweredInIf;
	}

	public void setLogInvsNotLoweredInIf(HashSet<LogInvocation> logInvsNotLoweredInIf) {
		this.logInvsNotLoweredInIf = logInvsNotLoweredInIf;
	}

	public boolean isNotLowerLogLevelWithKeyWords() {
		return this.notLowerLogLevelWithKeyWords;
	}

	public void setNotLowerLogLevelWithKeyWords(boolean notLowerLogLevelWithKeyWords) {
		this.notLowerLogLevelWithKeyWords = notLowerLogLevelWithKeyWords;
	}

	public HashSet<LogInvocation> getLogInvsNotLoweredWithKeywords() {
		return logInvsNotLoweredWithKeywords;
	}

	private void setLogInvsNotLoweredWithKeywords(HashSet<LogInvocation> logInvsNotLoweredWithKeywords) {
		this.logInvsNotLoweredWithKeywords = logInvsNotLoweredWithKeywords;
	}

}
