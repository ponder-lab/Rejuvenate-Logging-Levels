package edu.cuny.hunter.log.core.refactorings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
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
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
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

	private Set<LogInvocation> candidates = new HashSet<LogInvocation>();

	private IJavaProject[] javaProjects;

	/**
	 * Boundary for DOI values of enclosing methods
	 */
	private ArrayList<Float> boundary;

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

	private HashSet<LogInvocation> logInvsNotTransformedInIf;
	private HashSet<LogInvocation> logInvsNotLoweredInCatch;
	private HashSet<LogInvocation> logInvsNotLoweredInIf;
	private HashSet<LogInvocation> logInvsNotLoweredWithKeywords;
	private HashSet<LogInvocation> logInvsNotRaisedWithoutKeywords;
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
					this.notLowerLogLevelWithKeyWords, this.notRaiseLogLevelWithoutKeywords, this.maxTransDistance);

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

			this.setLogInvsNotLoweredInCatch(analyzer.getLogInvsNotLoweredInCatch());
			this.setLogInvsNotTransformedInIf(analyzer.getLogInvsNotTransformedInIf());
			this.setLogInvsNotLoweredInIf(analyzer.getLogInvsNotLoweredInIfStatement());
			this.setLogInvsNotLoweredWithKeywords(analyzer.getLogInvsNotLoweredByKeywords());
			this.setLogInvsNotRaisedWithoutKeywords(analyzer.getLogInvsNotRaisedByKeywords());
			this.setMethodToDOI(analyzer.getMethodToDOI());
			this.setEnclosingMethods(analyzer.getEnclosingMethods());

			// Get boundary
			this.boundary = analyzer.getBoundary();

			this.addLogInvocationSet(analyzer.getLogInvocationSet());

			this.estimateCandidates();

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
	 * Remove candidate log invocation whose enclosing method was not analyzed.
	 * 
	 * @param methodDeclsForAnalyzedMethod: A set of method declarations is analyzed
	 *                                      before.
	 */
	private void estimateCandidates() {
		Set<IMethod> validMethods = this.methodToDOI.keySet();

		candidates.addAll(this.logInvocationSet);
		for (LogInvocation candidate : this.logInvocationSet) {
			// if its enclosing method is not analyzed in the git history
			if (!validMethods.contains(candidate.getEnclosingEclipseMethod()))
				this.candidates.remove(candidate);
		}
	}

	public Set<LogInvocation> getRoughCandidateSet() {
		return this.candidates;
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

	public ArrayList<Float> getBoundary() {
		return this.boundary;
	}

	/**
	 * Process the inconsistent log level transformations in the overriding and
	 * overridden methods.
	 */
	private void inheritanceChecking(Set<LogInvocation> logInvocationSet, IProgressMonitor monitor)
			throws JavaModelException {
		Set<IMethod> enclosingMethodsForLogs = this.getEnclosingMethodForLogs(logInvocationSet);
		for (LogInvocation log : logInvocationSet) {
			IMethod enclosingMethod = log.getEnclosingEclipseMethod();
			if (enclosingMethod == null)
				continue;
			IType declaringType = enclosingMethod.getDeclaringType();
			ITypeHierarchy typeHierarchy = declaringType.newTypeHierarchy(monitor);

			IType[] superTypes = typeHierarchy.getAllSupertypes(declaringType);

			// Get all enclosing class types for all logs
			Set<IType> enclosingTypes = this.getEnclosingClassTypes(logInvocationSet);

			// If we find an super-type with log invocation
			if (this.checkTypes(superTypes, enclosingTypes)) {
				this.processTypes(superTypes, enclosingTypes, typeHierarchy, enclosingMethodsForLogs, logInvocationSet,
						log);
			}

		}
	}

	/**
	 * Get a set of enclosing methods for transformed logs.
	 */
	private Set<IMethod> getEnclosingMethodForLogs(Set<LogInvocation> transformedLogs) {
		return this.logInvocationSet.parallelStream().map(log -> log.getEnclosingEclipseMethod())
				.filter(Objects::nonNull).collect(Collectors.toSet());
	}

	/**
	 * Process super types.
	 */
	private void processTypes(IType[] superTypes, Set<IType> enclosingTypes, ITypeHierarchy typeHierarchy,
			Set<IMethod> enclosingMethodsForLogs, Set<LogInvocation> logSet, LogInvocation logInvocation)
			throws JavaModelException {
		// Store all log invocations in RootDefs.
		HashSet<LogInvocation> logInvsInRootDefs = new HashSet<LogInvocation>();
		// Store all log invocations in Hierarchy.
		HashSet<LogInvocation> logInvsInHierarchy = new HashSet<LogInvocation>();
		logInvsInHierarchy.add(logInvocation);

		for (IType type : superTypes) {
			// We find a super-type with log transformation
			if (enclosingTypes.contains(type)) {

				IMethod[] methods = type.getMethods();
				for (IMethod method : methods) {
					// The method should be an overridden method and includes a log invocation.
					if (this.isOverriddenMethod(method, logInvocation.getEnclosingEclipseMethod())
							&& enclosingMethodsForLogs.contains(method)) {
						Set<LogInvocation> logs = this.getInvocationsByMethod(method, logSet);
						logInvsInHierarchy.addAll(logs);
						// Check whether it's a RootDef
						if (this.isRootDef(typeHierarchy, method, enclosingTypes, logInvocation))
							logInvsInRootDefs.addAll(logs);
					}
				}
			}
		}

		if (!logInvsInRootDefs.isEmpty()) {
			Level newLevel = this.majorityVote(logInvsInRootDefs);
			this.adjustTransformation(logInvsInHierarchy, newLevel);
		}

	}

	/**
	 * Check whether the current method is a RootDef.
	 */
	private boolean isRootDef(ITypeHierarchy typeHierarchy, IMethod enclosingMethod, Set<IType> enclosingTypes,
			LogInvocation logInvocation) throws JavaModelException {
		IType[] types = typeHierarchy.getAllSupertypes(enclosingMethod.getDeclaringType());
		for (IType type : types) {
			// We find a super-type with log transformation
			if (enclosingTypes.contains(type)) {
				IMethod[] methods = type.getMethods();
				for (IMethod method : methods) {
					// The method should be an overridden method and includes a log invocation.
					if (this.isOverriddenMethod(method, logInvocation.getEnclosingEclipseMethod())) {
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * Get a set of invocations, given an enclosing method.
	 */
	private Set<LogInvocation> getInvocationsByMethod(IMethod enclosingMethod, Set<LogInvocation> logSet) {
		Set<LogInvocation> logs = new HashSet<LogInvocation>();
		logSet.forEach(log -> {
			if (log.getEnclosingEclipseMethod() != null && log.getEnclosingEclipseMethod().equals(enclosingMethod))
				logs.add(log);
		});
		return logs;
	}

	/**
	 * Adjust all transformations in the overriding method or overridden method in
	 * same hierarchy. We should make all these transformations the same.
	 */
	private void adjustTransformation(Set<LogInvocation> logInvocations, Level newLevel) {
		logInvocations.forEach(logInv -> {
			Action action;
			if (newLevel == null)
				action = Action.valueOf("NONE");
			else
				action = Action.valueOf("CONVERT_TO_" + newLevel.getName());
			logInv.setAction(action, newLevel);
		});
	}

	/**
	 * Take a majority vote on transformations in the RootDefs
	 */
	private Level majorityVote(Set<LogInvocation> logInvs) {

		HashMap<Level, Integer> newLevelToCount = new HashMap<Level, Integer>();
		logInvs.parallelStream().forEach(logInv -> {
			Level newLogLevel = logInv.getNewLogLevel();
			if (newLevelToCount.containsKey(newLogLevel))
				newLevelToCount.put(newLogLevel, newLevelToCount.get(newLogLevel) + 1);
			else
				newLevelToCount.put(newLogLevel, 1);
		});

		Level newLevel = null;
		int maxCounting = 0;

		for (Map.Entry<Level, Integer> levelToCount : newLevelToCount.entrySet()) {
			if (levelToCount.getValue() > maxCounting) {
				maxCounting = levelToCount.getValue();
				newLevel = levelToCount.getKey();
			}
		}

		return newLevel;
	}

	/**
	 * Method 1 and method 2 should have the identical signatures.
	 */
	private boolean isOverriddenMethod(IMethod method1, IMethod method2) throws JavaModelException {
		// Check method name
		if (!method1.getElementName().equals(method2.getElementName()))
			return false;

		return Util.getMethodIdentifier(method1).equals(Util.getMethodIdentifier(method2));
	}

	/**
	 * Check whether there is a super-type with log transformations
	 */
	private boolean checkTypes(IType[] types, Set<IType> enclosingTypes) {
		for (IType type : types) {
			if (enclosingTypes.contains(type))
				return true;
		}
		return false;
	}

	/**
	 * Get a set of enclosing types for all transformed logs
	 */
	private Set<IType> getEnclosingClassTypes(Set<LogInvocation> logSet) {
		HashSet<IType> types = new HashSet<IType>();
		logSet.forEach(log -> {
			types.add(log.getEnclosingType());
		});
		return types;
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		try {
			final TextEditBasedChangeManager manager = new TextEditBasedChangeManager();

			if (this.consistentLevelInInheritance)
				this.inheritanceChecking(this.logInvocationSet, pm);

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
}
