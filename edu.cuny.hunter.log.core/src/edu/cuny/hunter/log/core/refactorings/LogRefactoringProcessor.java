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
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.refactoring.changes.DynamicValidationRefactoringChange;
import org.eclipse.jdt.internal.corext.refactoring.util.TextEditBasedChangeManager;
import edu.cuny.citytech.refactoring.common.core.refactorings.CommonRefactoringProcessor;
import edu.cuny.hunter.log.core.analysis.LogAnalyzer;
import edu.cuny.hunter.log.core.analysis.LogInvocation;
import edu.cuny.hunter.log.core.descriptors.LogDescriptor;
import edu.cuny.hunter.log.core.messages.Messages;
import edu.cuny.hunter.log.core.utils.LoggerNames;

@SuppressWarnings({ "restriction", "deprecation" })
public class LogRefactoringProcessor extends CommonRefactoringProcessor {

	private Set<LogInvocation> logInvocationSet = new HashSet<>();

	private static final Logger LOGGER = Logger.getLogger(LoggerNames.LOGGER_NAME);

	public LogRefactoringProcessor(IJavaProject[] javaProjects, final CodeGenerationSettings settings,
			Optional<IProgressMonitor> monitor) {
		super(javaProjects, settings, monitor);
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
			LogAnalyzer analyzer = new LogAnalyzer();
			this.setLogInvocationSet(analyzer.getLogInvocationSet());

			for (IJavaProject jproj : super.getJavaProjects()) {
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

			// analyze and set entry points.
			analyzer.analyze();

			return status;
		} catch (Exception e) {
			LOGGER.info("Cannot accpet the visitors. ");
			throw e;
		} finally {
			monitor.done();
		}
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		try {
			pm.beginTask(Messages.CreatingChange, 1);

			final TextEditBasedChangeManager manager = new TextEditBasedChangeManager();

			// save the source changes.
			ICompilationUnit[] units = getCompilationUnitToCompilationUnitRewriteMap().keySet().stream()
					.filter(cu -> !manager.containsChangesIn(cu)).toArray(ICompilationUnit[]::new);

			for (ICompilationUnit cu : units) {
				CompilationUnit compilationUnit = getCompilationUnit(cu, pm);
				super.manageCompilationUnit(manager, super.getCompilationUnitRewrite(cu, compilationUnit),
						Optional.of(new SubProgressMonitor(pm, IProgressMonitor.UNKNOWN)));
			}

			final Map<String, String> arguments = new HashMap<>();
			int flags = RefactoringDescriptor.STRUCTURAL_CHANGE | RefactoringDescriptor.MULTI_CHANGE;

			// TODO: Fill in description.

			LogDescriptor descriptor = new LogDescriptor(null, "TODO", null, arguments, flags);

			return new DynamicValidationRefactoringChange(descriptor, this.getProcessorName(), manager.getAllChanges());
		} finally {
			pm.done();
			clearCaches();
		}
	}

	protected void setLogInvocationSet(Set<LogInvocation> logInvocationSet) {
		this.logInvocationSet = logInvocationSet;
	}

	public Set<LogInvocation> getLogInvocationSet() {
		return this.logInvocationSet;
	}

}
