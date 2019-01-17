package edu.cuny.hunter.log.core.refactorings;

import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.refactoring.changes.DynamicValidationRefactoringChange;
import org.eclipse.jdt.internal.corext.refactoring.structure.CompilationUnitRewrite;
import org.eclipse.jdt.internal.corext.refactoring.util.TextEditBasedChangeManager;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import edu.cuny.citytech.refactoring.common.core.RefactoringProcessor;
import edu.cuny.hunter.log.core.analysis.Action;
import edu.cuny.hunter.log.core.analysis.LogAnalyzer;
import edu.cuny.hunter.log.core.analysis.LogInvocation;
import edu.cuny.hunter.log.core.descriptors.LogDescriptor;
import edu.cuny.hunter.log.core.messages.Messages;
import edu.cuny.hunter.log.core.utils.LoggerNames;
import edu.cuny.hunter.mylyngit.core.analysis.MylynGitPredictionProvider;
import edu.cuny.hunter.mylyngit.core.utils.Commit;

@SuppressWarnings({ "restriction", "deprecation" })
public class LogRejuvenatingProcessor extends RefactoringProcessor {

	private IJavaProject[] javaProjects;

	private Set<LogInvocation> logInvocationSet = new HashSet<>();

	private LinkedList<Float> boundary;

	private static final Logger LOGGER = Logger.getLogger(LoggerNames.LOGGER_NAME);

	// Treat CONFIG as category
	private boolean useLogCategoryWithConfig = false;

	// Treat CONFIG/WARNING/SEVERE log levels as category
	private boolean useLogCategory = false;

	// Should we use git history to bump DOI values for all methods?
	private boolean useGitHistory = true;

	// Should we consider logging statements in catch blocks?
	private boolean notConsiderCatchBlock = true;

	// Limit number of commits
	private int NToUseForCommits = 100;

	// It the caller Evaluation plugin?
	private boolean isEvaluation = false;

	private LinkedList<Commit> commits = new LinkedList<>();

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

	public void setNotConsiderCatchBlock(boolean notConsiderCatchBlock) {
		this.notConsiderCatchBlock = notConsiderCatchBlock;
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

	public boolean getNotConsiderCatchBlock() {
		return this.notConsiderCatchBlock;
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
			boolean useConfigLogLevelCategory, boolean useGitHistory, boolean notConsiderCatchBlock,
			int NToUseForCommits, final CodeGenerationSettings settings, Optional<IProgressMonitor> monitor) {
		super(settings);
		try {
			this.javaProjects = javaProjects;
			this.useLogCategoryWithConfig = useConfigLogLevelCategory;
			this.useLogCategory = useLogLevelCategory;
			this.useGitHistory = useGitHistory;
			this.notConsiderCatchBlock = notConsiderCatchBlock;
			this.NToUseForCommits = NToUseForCommits;
		} finally {
			monitor.ifPresent(IProgressMonitor::done);
		}
	}

	public LogRejuvenatingProcessor(IJavaProject[] javaProjects, boolean useLogLevelCategory,
			boolean useConfigLogLevelCategory, boolean useGitHistory, boolean notConsiderCatchBlock,
			int NToUseForCommits, final CodeGenerationSettings settings, Optional<IProgressMonitor> monitor,
			boolean isEvaluation) {
		this(javaProjects, useLogLevelCategory, useConfigLogLevelCategory, useGitHistory, notConsiderCatchBlock,
				NToUseForCommits, settings, monitor);
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
					this.notConsiderCatchBlock);

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

			MylynGitPredictionProvider mylynProvider = null;

			if (this.useGitHistory) {
				// Process git history.
				mylynProvider = new MylynGitPredictionProvider(this.NToUseForCommits);
				this.processGitHistory(mylynProvider, analyzer, jproj);
				this.setCommits(mylynProvider.getCommits());
			}

			analyzer.analyze();

			// Get boundary
			this.boundary = analyzer.getBoundary();

			this.addLogInvocationSet(analyzer.getLogInvocationSet());

			// If we are using the git history.
			if (this.useGitHistory) {
				// then, we must clear the context
				analyzer.clearTaskContext(mylynProvider);
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
				status.addFatalError(Messages.NoInputLogInvs);
			}
		}

		return status;

	}

	/**
	 * Call mylyngit plugin to process git history.
	 * 
	 * @param mylynProvider
	 * @param analyzer
	 * @param jproj
	 */
	private void processGitHistory(MylynGitPredictionProvider mylynProvider, LogAnalyzer analyzer, IJavaProject jproj) {
		try {
			if (this.useGitHistory) {
				mylynProvider.processOneProject(jproj);
				analyzer.updateDOI();
			}
		} catch (GitAPIException | JGitInternalException e) {
			LOGGER.info("Cannot get valid git object! May not a valid git repo.");
		} catch (IOException e) {
			LOGGER.info("Cannot process git commits.");
		}
	}

	/**
	 * get a set of transformed log set
	 */
	public Set<LogInvocation> getTransformedLog() {
		HashSet<LogInvocation> transformedSet = new HashSet<>();
		for (LogInvocation logInvocation : this.logInvocationSet) {
			if (logInvocation.getAction() != null && !logInvocation.getAction().equals(Action.NONE))
				transformedSet.add(logInvocation);
		}
		return transformedSet;
	}

	/**
	 * Get passing log invocations.
	 */
	public HashSet<LogInvocation> getPassingLogInvocation() {
		HashSet<LogInvocation> passingLogInvocations = new HashSet<>();
		this.logInvocationSet.forEach(inv -> {
			if (inv.getAction() != null)
				passingLogInvocations.add(inv);
		});
		return passingLogInvocations;
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
			Set<LogInvocation> allLogs = this.getLogInvocationSet();

			if (allLogs.isEmpty())
				return new NullChange(Messages.NoInputLogInvs);

			pm.beginTask("Transforming logging levels ...", allLogs.size());
			for (LogInvocation logInvocation : allLogs) {
				CompilationUnitRewrite rewrite = this.getCompilationUnitRewrite(
						logInvocation.getEnclosingEclipseMethod().getCompilationUnit(),
						logInvocation.getEnclosingCompilationUnit());
				logInvocation.transform(rewrite);
				pm.worked(1);
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
		return commits;
	}

	private void setCommits(LinkedList<Commit> commits) {
		this.commits = commits;
	}

}
