package edu.cuny.hunter.log.core.refactorings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
import org.eclipse.jdt.core.dom.MethodDeclaration;
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
import edu.cuny.hunter.log.core.analysis.AbstractLogInvocation;
import edu.cuny.hunter.log.core.analysis.Action;
import edu.cuny.hunter.log.core.analysis.ActionSlf4j;
import edu.cuny.hunter.log.core.analysis.LogAnalyzer;
import edu.cuny.hunter.log.core.analysis.LogInvocation;
import edu.cuny.hunter.log.core.analysis.LogInvocationSlf4j;
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

	private LinkedList<Commit> commits = new LinkedList<>();

	/**
	 * A set of candidates for J.U.L.
	 */
	private Set<LogInvocation> candidates = new HashSet<LogInvocation>();

	/**
	 * A set of candidates for slf4j.
	 */
	private Set<LogInvocationSlf4j> candidateSlf4j = new HashSet<LogInvocationSlf4j>();

	private IJavaProject[] javaProjects;

	/**
	 * Boundary for DOI values of enclosing methods
	 */
	private ArrayList<Float> boundary;

	/**
	 * For Slf4j. Boundary for DOI values of enclosing methods.
	 */
	private ArrayList<Float> boundarySlf4j;

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
	 * 
	 * The default value is true.
	 */
	private boolean useGitHistory = true;

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
	 * Not raise logs without particular keywords in their messages.
	 */
	private boolean notRaiseLogLevelWithoutKeywords;

	/**
	 * Log level transformations between overriding methods should be consistent.
	 */
	private boolean consistentLevelInInheritance;

	/**
	 * Limit number of commits
	 */
	private int NToUseForCommits = 100;

	/**
	 * Limit transformation Distance, e.g., if the max transformation is 1, the
	 * transformation from INFO to FINEST should be adjusted to the transformation
	 * from INFO to FINER.
	 */
	private int maxTransDistance = 10;

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

	// Sets of log invocations for J.U.L.
	private HashSet<LogInvocation> logInvsNotTransformedInIf;
	private HashSet<LogInvocation> logInvsNotLoweredInCatch;
	private HashSet<LogInvocation> logInvsNotLoweredInIf;
	private HashSet<LogInvocation> logInvsAdjustedByDis;
	private HashSet<LogInvocation> logInvsNotLoweredWithKeywords;
	private HashSet<LogInvocation> logInvsNotRaisedWithoutKeywords;
	private HashSet<LogInvocation> logInvsAdjustedByInheritance = new HashSet<LogInvocation>();

	private Set<LogInvocation> logInvocationSet = new HashSet<>();

	// Sets of log invocations for slf4j.
	private HashSet<LogInvocationSlf4j> logInvsNotTransformedInIfSlf4j = new HashSet<LogInvocationSlf4j>();
	private HashSet<LogInvocationSlf4j> logInvsNotLoweredInCatchSlf4j = new HashSet<LogInvocationSlf4j>();
	private HashSet<LogInvocationSlf4j> logInvsNotLoweredInIfStatementSlf4j = new HashSet<LogInvocationSlf4j>();
	private HashSet<LogInvocationSlf4j> logInvsAdjustedByDistanceSlf4j = new HashSet<LogInvocationSlf4j>();
	private HashSet<LogInvocationSlf4j> logInvsNotLoweredByKeywordsSlf4j = new HashSet<LogInvocationSlf4j>();
	private HashSet<LogInvocationSlf4j> logInvsNotRaisedWithoutKeywordsSlf4j = new HashSet<LogInvocationSlf4j>();
	private HashSet<LogInvocationSlf4j> logInvsAdjustedByInheritanceSlf4j = new HashSet<LogInvocationSlf4j>();

	private Set<LogInvocationSlf4j> logInvocationSlf4j = new HashSet<LogInvocationSlf4j>();

	private Map<IMethod, Float> methodToDOI;
	private Set<IMethod> enclosingMethods;

	private String repoURL;

	public LogRejuvenatingProcessor(final CodeGenerationSettings settings) {
		super(settings);
	}

	public void setConsistentLevelInInheritance(boolean consistentLevelInInheritance) {
		this.consistentLevelInInheritance = consistentLevelInInheritance;
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

	public void setMaxTransDistance(int maxTransDistance) {
		this.maxTransDistance = maxTransDistance;
	}

	public int getMaxTransDistance() {
		return this.maxTransDistance;
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
			boolean notLowerLogLevelInIfStatement, boolean notLowerLogLevelWithKeywords,
			boolean notRaiseLogLevelWithoutKeywords, boolean checkIfCondtion, boolean consistentLevelInInheritance,
			int maxTransDistance, int NToUseForCommits, final CodeGenerationSettings settings,
			Optional<IProgressMonitor> monitor) {
		super(settings);
		try {
			this.javaProjects = javaProjects;
			this.useLogCategoryWithConfig = useConfigLogLevelCategory;
			this.useLogCategory = useLogLevelCategory;
			this.useGitHistory = useGitHistory;
			this.notLowerLogLevelInCatchBlock = notLowerLogLevelInCatchBlock;
			this.notLowerLogLevelInIfStatement = notLowerLogLevelInIfStatement;
			this.notLowerLogLevelWithKeyWords = notLowerLogLevelWithKeywords;
			this.notRaiseLogLevelWithoutKeywords = notRaiseLogLevelWithoutKeywords;
			this.consistentLevelInInheritance = consistentLevelInInheritance;
			this.checkIfCondition = checkIfCondtion;
			this.NToUseForCommits = NToUseForCommits;
			this.maxTransDistance = maxTransDistance;
		} finally {
			monitor.ifPresent(IProgressMonitor::done);
		}
	}

	public LogRejuvenatingProcessor(IJavaProject[] javaProjects, boolean useLogLevelCategory,
			boolean useConfigLogLevelCategory, boolean useGitHistory, boolean notLowerLogLevelInCatchBlock,
			boolean notLowerLogLevelInIfStatement, boolean notLowerLogLevelWithKeywords,
			boolean notRaiseLogLevelWithoutKeywords, boolean checkIfCondtion, boolean consistentLevelInInheritance,
			int maxTransDistance, int NToUseForCommits, final CodeGenerationSettings settings,
			Optional<IProgressMonitor> monitor, boolean isEvaluation) {
		this(javaProjects, useLogLevelCategory, useConfigLogLevelCategory, useGitHistory, notLowerLogLevelInCatchBlock,
				notLowerLogLevelInIfStatement, notLowerLogLevelWithKeywords, notRaiseLogLevelWithoutKeywords,
				checkIfCondtion, consistentLevelInInheritance, maxTransDistance, NToUseForCommits, settings, monitor);
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
					this.notLowerLogLevelWithKeyWords, this.notRaiseLogLevelWithoutKeywords, this.maxTransDistance,
					monitor);

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

			MylynGitPredictionProvider mylynProvider = null;
			HashSet<MethodDeclaration> methodDeclsForAnalyzedMethod = null;

			if (this.useGitHistory) {
				// Process git history.
				mylynProvider = new MylynGitPredictionProvider(this.NToUseForCommits);

				methodDeclsForAnalyzedMethod = mylynProvider.getMethodDecsForAnalyzedMethod();

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

			analyzer.analyze(methodDeclsForAnalyzedMethod);

			// Store all not transformed log invocations due to the settings.
			this.setDataForJUL(analyzer);
			this.setDataForSlf4j(analyzer);

			// Get all log invocations.
			this.addLogInvocationSet(analyzer.getLogInvocationSet());
			this.addLogInvocationSlf4j(analyzer.getLogInvocationSlf4js());

			this.setMethodToDOI(analyzer.getMethodToDOI());
			this.setEnclosingMethods(analyzer.getEnclosingMethods());

			// Get boundary
			this.boundary = analyzer.getBoundary();
			this.boundarySlf4j = analyzer.getBoundarySlf4j();

			this.estimateCandidates();
			this.estimateCandidatesSlf4j();

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

		// get the status of each J.U.L log invocation.
		RefactoringStatus collectedStatus = this.getLogInvocationSet().stream().map(LogInvocation::getStatus)
				.collect(() -> new RefactoringStatus(), (a, b) -> a.merge(b), (a, b) -> a.merge(b));

		status.merge(collectedStatus);

		// get the status of each slf4j log invocation.
		RefactoringStatus collectedStatusSlf4j = this.getLogInvocationSlf4j().stream()
				.map(LogInvocationSlf4j::getStatus)
				.collect(() -> new RefactoringStatus(), (a, b) -> a.merge(b), (a, b) -> a.merge(b));

		status.merge(collectedStatusSlf4j);

		if (!status.hasFatalError()) {
			if (logInvocationSet.isEmpty() && logInvocationSlf4j.isEmpty()) {
				status.addWarning(Messages.NoInputLogInvs);
			}
		}

		return status;
	}

	private void addLogInvocationSlf4j(Set<LogInvocationSlf4j> logInvocationSlf4js) {
		this.logInvocationSlf4j.addAll(logInvocationSlf4js);
	}

	/**
	 * Store all not transformed log invocations due to the settings for
	 * Java.Util.Logging framework.
	 */
	private void setDataForJUL(LogAnalyzer analyzer) {
		this.setLogInvsAdjustedByDis(analyzer.getLogInvsAdjustedByDis());
		this.setLogInvsNotLoweredInCatch(analyzer.getLogInvsNotLoweredInCatch());
		this.setLogInvsNotTransformedInIf(analyzer.getLogInvsNotTransformedInIf());
		this.setLogInvsNotLoweredInIf(analyzer.getLogInvsNotLoweredInIfStatement());
		this.setLogInvsNotLoweredWithKeywords(analyzer.getLogInvsNotLoweredByKeywords());
		this.setLogInvsNotRaisedWithoutKeywords(analyzer.getLogInvsNotRaisedByKeywords());
		this.setLogInvsAdjustedByInheritance(analyzer.getLogInvsAdjustedByInheritance());
	}

	/**
	 * Store all not transformed log invocations due to the settings for slf4j.
	 */
	private void setDataForSlf4j(LogAnalyzer analyzer) {
		this.setLogInvsAdjustedByDistanceSlf4j(analyzer.getLogInvsAdjustedByDisSlf4j());
		this.setLogInvsNotLoweredInCatchSlf4j(analyzer.getLogInvsNotLoweredInCatchSlf4j());
		this.setLogInvsNotTransformedInIfSlf4j(analyzer.getLogInvsNotTransformedInIfSlf4j());
		this.setLogInvsNotLoweredInIfStatementSlf4j(analyzer.getLogInvsNotLoweredInIfStatementsSlf4j());
		this.setLogInvsNotLoweredByKeywordsSlf4j(analyzer.getLogInvsNotLoweredByKeywordsSlf4j());
		this.setLogInvsNotRaisedWithoutKeywordsSlf4j(analyzer.getLogInvsNotRaisedByKeywordsSlf4j());
		this.setLogInvsAdjustedByInheritanceSlf4j(analyzer.getLogInvsAdjustedByInheritanceSlf4j());
	}

	/**
	 * Remove log invocations in candidate sets whose enclosing methods are not
	 * analyzed.
	 * 
	 * @param methodDeclsForAnalyzedMethod: A set of method declarations is analyzed
	 *                                      before.
	 */
	private void estimateCandidates() {
		Set<IMethod> validMethods = this.methodToDOI.keySet();

		this.candidates.addAll(this.logInvocationSet);
		for (LogInvocation candidate : this.logInvocationSet) {
			// if its enclosing method is not analyzed in the git history
			if (!validMethods.contains(candidate.getEnclosingEclipseMethod()))
				this.candidates.remove(candidate);
		}
	}

	/**
	 * For Slf4j.
	 * 
	 * Remove log invocations in candidate sets whose enclosing methods are not
	 * analyzed.
	 */
	private void estimateCandidatesSlf4j() {
		Set<IMethod> validMethods = this.methodToDOI.keySet();

		this.candidateSlf4j.addAll(this.logInvocationSlf4j);
		for (LogInvocationSlf4j candidate : this.logInvocationSlf4j) {
			// if its enclosing method is not analyzed in the git history
			if (!validMethods.contains(candidate.getEnclosingEclipseMethod()))
				this.candidateSlf4j.remove(candidate);
		}
	}

	public Set<LogInvocation> getRoughCandidateSet() {
		return this.candidates;
	}

	public Set<LogInvocationSlf4j> getRoughCandidateSetSlf4j() {
		return this.candidateSlf4j;
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
			LOGGER.severe("Cannot get valid git object! May not be a valid git repo.");
			throw e;
		} catch (IOException e) {
			LOGGER.severe("Cannot process git commits.");
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * Get a set of transformed log set.
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

	/**
	 * Get a set of slf4j transformed log set.
	 */
	public Set<LogInvocationSlf4j> getSlf4jTransformedLog() {
		HashSet<LogInvocationSlf4j> transformedSet = new HashSet<>();
		for (LogInvocationSlf4j logInvocation : this.logInvocationSlf4j) {
			if (logInvocation.getAction() != null && !logInvocation.getAction().equals(ActionSlf4j.NONE)
					&& logInvocation.getStatus().isOK())
				transformedSet.add(logInvocation);
		}
		return transformedSet;
	}

	private IJavaProject[] getJavaProjects() {
		return this.javaProjects;
	}

	public ArrayList<Float> getBoundary() {
		return this.boundary;
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		try {
			final TextEditBasedChangeManager manager = new TextEditBasedChangeManager();

			Set<LogInvocation> transformedLogs = this.getTransformedLog();
			Set<LogInvocationSlf4j> transformedSlf4jLogs = this.getSlf4jTransformedLog();

			pm.beginTask("Transforming logging levels ...", transformedLogs.size() + transformedSlf4jLogs.size());

			transformedLogs.forEach(log -> {
				this.createTransformation(log, pm, "java.util.logging.Level");
			});
			transformedSlf4jLogs.forEach(log -> {
				this.createTransformation(log, pm, "org.slf4j.event.Level");
			});

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

	private void createTransformation(AbstractLogInvocation logInvocation, IProgressMonitor pm, String importPrefix) {
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
				String matchName = importPrefix + "." + logInvocation.getReplacedName().getFullyQualifiedName();

				if (name.getFullyQualifiedName().equals(matchName)) {
					// deal with imports.
					// register the removed name.
					rewrite.getImportRemover().registerRemovedNode(logInvocation.getReplacedName());

					// we are replacing a log level that has been
					// statically imported.
					// then, add a static import for new log level.
					rewrite.getImportRewrite().addStaticImport(importPrefix,
							logInvocation.getNewTargetName().getFullyQualifiedName(), true, context);
				}
			}
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

	public boolean isConsistentLevelInInheritance() {
		return this.consistentLevelInInheritance;
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

	public boolean isNotRaisedLogLevelWithoutKeywords() {
		return this.notRaiseLogLevelWithoutKeywords;
	}

	public void setNotLowerLogLevelWithKeyWords(boolean notLowerLogLevelWithKeyWords) {
		this.notLowerLogLevelWithKeyWords = notLowerLogLevelWithKeyWords;
	}

	public void setNotRaiseLogLevelWithoutKeyWords(boolean notRaiseLogLevelWithoutKeyWords) {
		this.notRaiseLogLevelWithoutKeywords = notRaiseLogLevelWithoutKeyWords;
	}

	public HashSet<LogInvocation> getLogInvsNotLoweredWithKeywords() {
		return this.logInvsNotLoweredWithKeywords;
	}

	public HashSet<LogInvocation> getLogInvsNotRaisedWithoutKeywords() {
		return this.logInvsNotRaisedWithoutKeywords;
	}

	private void setLogInvsNotLoweredWithKeywords(HashSet<LogInvocation> logInvsNotLoweredWithKeywords) {
		this.logInvsNotLoweredWithKeywords = logInvsNotLoweredWithKeywords;
	}

	private void setLogInvsNotRaisedWithoutKeywords(HashSet<LogInvocation> logInvsNotRaisedWithoutKeywords) {
		this.logInvsNotRaisedWithoutKeywords = logInvsNotRaisedWithoutKeywords;
	}

	private void setMethodToDOI(Map<IMethod, Float> methodToDOI) {
		this.methodToDOI = methodToDOI;
	}

	private void setEnclosingMethods(Set<IMethod> enclosingMethods) {
		this.enclosingMethods = enclosingMethods;
	}

	public Map<IMethod, Float> getNonenclosingMethodToDOI() {
		return this.methodToDOI.keySet().stream().filter(m -> !this.enclosingMethods.contains(m))
				.collect(Collectors.toMap(Function.identity(), m -> this.methodToDOI.get(m)));
	}

	public int getDecayFactor() {
		return Util.getDecayFactor();
	}

	public HashSet<LogInvocationSlf4j> getLogInvsNotTransformedInIfSlf4j() {
		return logInvsNotTransformedInIfSlf4j;
	}

	public void setLogInvsNotTransformedInIfSlf4j(HashSet<LogInvocationSlf4j> logInvsNotTransformedInIfSlf4j) {
		this.logInvsNotTransformedInIfSlf4j = logInvsNotTransformedInIfSlf4j;
	}

	public HashSet<LogInvocationSlf4j> getLogInvsNotLoweredInCatchSlf4j() {
		return logInvsNotLoweredInCatchSlf4j;
	}

	public void setLogInvsNotLoweredInCatchSlf4j(HashSet<LogInvocationSlf4j> logInvsNotLoweredInCatchSlf4j) {
		this.logInvsNotLoweredInCatchSlf4j = logInvsNotLoweredInCatchSlf4j;
	}

	public HashSet<LogInvocationSlf4j> getLogInvsNotLoweredInIfStatementSlf4j() {
		return logInvsNotLoweredInIfStatementSlf4j;
	}

	public void setLogInvsNotLoweredInIfStatementSlf4j(
			HashSet<LogInvocationSlf4j> logInvsNotLoweredInIfStatementSlf4j) {
		this.logInvsNotLoweredInIfStatementSlf4j = logInvsNotLoweredInIfStatementSlf4j;
	}

	public HashSet<LogInvocationSlf4j> getLogInvsNotLoweredByKeywordsSlf4j() {
		return logInvsNotLoweredByKeywordsSlf4j;
	}

	public void setLogInvsNotLoweredByKeywordsSlf4j(HashSet<LogInvocationSlf4j> logInvsNotLoweredByKeywordsSlf4j) {
		this.logInvsNotLoweredByKeywordsSlf4j = logInvsNotLoweredByKeywordsSlf4j;
	}

	public HashSet<LogInvocationSlf4j> getLogInvsNotRaisedWithoutKeywordsSlf4j() {
		return logInvsNotRaisedWithoutKeywordsSlf4j;
	}

	public void setLogInvsNotRaisedWithoutKeywordsSlf4j(HashSet<LogInvocationSlf4j> logInvsNotRaisedByKeywordsSlf4j) {
		this.logInvsNotRaisedWithoutKeywordsSlf4j = logInvsNotRaisedByKeywordsSlf4j;
	}

	public Set<LogInvocationSlf4j> getLogInvocationSlf4j() {
		return logInvocationSlf4j;
	}

	public void setLogInvocationSlf4j(Set<LogInvocationSlf4j> logInvocationSlf4j) {
		this.logInvocationSlf4j = logInvocationSlf4j;
	}

	public ArrayList<Float> getBoundarySlf4j() {
		return this.boundarySlf4j;
	}

	public void setBoundarySlf4j(ArrayList<Float> boundarySlf4j) {
		this.boundarySlf4j = boundarySlf4j;
	}

	public HashSet<LogInvocation> getLogInvsAdjustedByDis() {
		return logInvsAdjustedByDis;
	}

	public void setLogInvsAdjustedByDis(HashSet<LogInvocation> logInvsAdjustedByDis) {
		this.logInvsAdjustedByDis = logInvsAdjustedByDis;
	}

	private void setLogInvsAdjustedByInheritance(HashSet<LogInvocation> logInvsAdjustedByInheritance) {
		this.logInvsAdjustedByInheritance = logInvsAdjustedByInheritance;
	}

	public HashSet<LogInvocation> getLogInvsAdjustedByInheritance() {
		return this.logInvsAdjustedByInheritance;
	}

	public HashSet<LogInvocationSlf4j> getLogInvsAdjustedByDistanceSlf4j() {
		return this.logInvsAdjustedByDistanceSlf4j;
	}

	public void setLogInvsAdjustedByDistanceSlf4j(HashSet<LogInvocationSlf4j> logInvsAdjustedByDistanceSlf4j) {
		this.logInvsAdjustedByDistanceSlf4j = logInvsAdjustedByDistanceSlf4j;
	}

	public HashSet<LogInvocationSlf4j> getLogInvsAdjustedByInheritanceSlf4j() {
		return this.logInvsAdjustedByInheritanceSlf4j;
	}

	public void setLogInvsAdjustedByInheritanceSlf4j(HashSet<LogInvocationSlf4j> logInvsAdjustedByInheritanceSlf4j) {
		this.logInvsAdjustedByInheritanceSlf4j = logInvsAdjustedByInheritanceSlf4j;
	}
}
