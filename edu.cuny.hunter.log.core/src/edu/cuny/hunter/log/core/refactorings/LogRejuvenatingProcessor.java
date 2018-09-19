package edu.cuny.hunter.log.core.refactorings;

import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.HashMap;
import java.util.HashSet;
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
import edu.cuny.citytech.refactoring.common.core.RefactoringProcessor;
import edu.cuny.hunter.log.core.analysis.Action;
import edu.cuny.hunter.log.core.analysis.LogAnalyzer;
import edu.cuny.hunter.log.core.analysis.LogInvocation;
import edu.cuny.hunter.log.core.descriptors.LogDescriptor;
import edu.cuny.hunter.log.core.messages.Messages;
import edu.cuny.hunter.log.core.utils.LoggerNames;

@SuppressWarnings({ "restriction", "deprecation" })
public class LogRejuvenatingProcessor extends RefactoringProcessor {

	private IJavaProject[] javaProjects;

	private Set<LogInvocation> logInvocationSet = new HashSet<>();

	private static final Logger LOGGER = Logger.getLogger(LoggerNames.LOGGER_NAME);

	private boolean useLogCategory = false;

	public LogRejuvenatingProcessor(final CodeGenerationSettings settings) {
		super(settings);
	}
	
	public void setParticularConfigLogLevel(boolean configLogLevel) {
		this.useLogCategory = configLogLevel;
	}
	
	public boolean getParticularConfigLogLevel() {
		return this.useLogCategory;
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

	public LogRejuvenatingProcessor(IJavaProject[] javaProjects, boolean useConfigLogLevel,
			final CodeGenerationSettings settings, Optional<IProgressMonitor> monitor) {
		super(settings);
		try {
			this.javaProjects = javaProjects;
			this.useLogCategory = useConfigLogLevel;
		} finally {
			monitor.ifPresent(IProgressMonitor::done);
		}
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
			throws CoreException, OperationCanceledException {
		try {
			final RefactoringStatus status = new RefactoringStatus();
			LogAnalyzer analyzer = new LogAnalyzer(this.useLogCategory);

			for (IJavaProject jproj : this.getJavaProjects()) {
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
			}
			
			// analyze.
			analyzer.analyze();

			this.setLogInvocationSet(analyzer.getLogInvocationSet());

			// get the status of each log invocation.
			RefactoringStatus collectedStatus = this.getLogInvocationSet().stream().map(LogInvocation::getStatus)
					.collect(() -> new RefactoringStatus(), (a, b) -> a.merge(b), (a, b) -> a.merge(b));
			status.merge(collectedStatus);

			if (!status.hasFatalError()) {
				// those log invocations whose logging level can be refactored
				Set<LogInvocation> possibleRejuvenatedLogSet = this.getPossibleRejuvenatedLog();
				if (possibleRejuvenatedLogSet.isEmpty()) {
					status.addFatalError(Messages.NoPossibleRejuvenatedLog);
				}
			}
			return status;
		} catch (Exception e) {
			LOGGER.info("Cannot accpet the visitors. ");
			throw e;
		} finally {
			monitor.done();
		}
	}

	/**
	 * get a set of optimizable log set
	 */
	public Set<LogInvocation> getPossibleRejuvenatedLog() {
		HashSet<LogInvocation> optimizableSet = new HashSet<>();
		for (LogInvocation logInvocation : this.logInvocationSet) {
			if (!logInvocation.getAction().equals(Action.NONE))
				optimizableSet.add(logInvocation);
		}

		return optimizableSet;
	}

	private IJavaProject[] getJavaProjects() {
		return this.javaProjects;
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		try {
			final TextEditBasedChangeManager manager = new TextEditBasedChangeManager();
			Set<LogInvocation> optimizableLogs = this.getLogInvocationSet();

			if (optimizableLogs.isEmpty())
				return new NullChange(Messages.NoPossibleRejuvenatedLog);

			pm.beginTask("Transforming logging levels ...", optimizableLogs.size());
			for (LogInvocation logInvocation : optimizableLogs) {
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

	protected void setLogInvocationSet(Set<LogInvocation> logInvocationSet) {
		this.logInvocationSet = logInvocationSet;
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

}
