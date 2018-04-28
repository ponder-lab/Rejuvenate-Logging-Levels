package edu.cuny.hunter.logging.core.refactorings;

import java.util.Optional;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.refactoring.changes.DynamicValidationRefactoringChange;
import org.eclipse.jdt.internal.corext.refactoring.structure.CompilationUnitRewrite;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;
import org.eclipse.jdt.internal.corext.refactoring.util.TextEditBasedChangeManager;

import edu.cuny.hunter.logging.core.analysis.Logging;
import edu.cuny.hunter.logging.core.analysis.LoggingAnalyzer;
import edu.cuny.hunter.logging.core.descriptors.LoggingDescriptor;
import edu.cuny.hunter.logging.core.messages.Messages;

@SuppressWarnings({ "restriction", "deprecation" })
public class LoggingRefactoringProcessor extends RefactoringProcessor {

	private IJavaProject[] javaProjects;
	private CodeGenerationSettings settings;
	private Map<ITypeRoot, CompilationUnit> typeRootToCompilationUnitMap = new HashMap<>();

	private Set<Logging> loggingSet;
	private Map<ICompilationUnit, CompilationUnitRewrite> compilationUnitToCompilationUnitRewriteMap = new HashMap<>();

	public LoggingRefactoringProcessor(IJavaProject[] javaProjects, final CodeGenerationSettings settings,
			Optional<IProgressMonitor> monitor) {
		try {
			this.javaProjects = javaProjects;
			this.settings = settings;
		} finally {
			monitor.ifPresent(IProgressMonitor::done);
		}
	}

	@Override
	public Object[] getElements() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getIdentifier() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProcessorName() {
		return Messages.Name;
	}

	@Override
	public boolean isApplicable() throws CoreException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		// TODO Auto-generated method stub
		return null;
	}

	protected IJavaProject[] getJavaProjects() {
		return this.javaProjects;
	}

	@Override
	public RefactoringStatus checkFinalConditions(final IProgressMonitor monitor, final CheckConditionsContext context)
			throws CoreException, OperationCanceledException {
		try {
			SubMonitor subMonitor = SubMonitor.convert(monitor, Messages.CheckingPreconditions,
					this.getJavaProjects().length * 1000);
			final RefactoringStatus status = new RefactoringStatus();
			LoggingAnalyzer analyzer = new LoggingAnalyzer();
			this.setLoggingSet(analyzer.getLoggingSet());

			for (IJavaProject jproj : this.getJavaProjects()) {
				IPackageFragmentRoot[] roots = jproj.getPackageFragmentRoots();
				for (IPackageFragmentRoot root : roots) {
					IJavaElement[] children = root.getChildren();
					for (IJavaElement child : children)
						if (child.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
							IPackageFragment fragment = (IPackageFragment) child;
							ICompilationUnit[] units = fragment.getCompilationUnits();
							for (ICompilationUnit unit : units) {
								CompilationUnit compilationUnit = this.getCompilationUnit(unit, subMonitor.split(1));
								compilationUnit.accept(analyzer);
							}
						}
				}
			}

			// analyze and set entry points.
			analyzer.analyze();

			return status;
		} catch (Exception e) {
			// JavaPlugin.log(e);
			throw e;
		} finally {
			monitor.done();
		}
	}

	private CompilationUnit getCompilationUnit(ITypeRoot root, IProgressMonitor pm) {
		CompilationUnit compilationUnit = this.getTypeRootToCompilationUnitMap().get(root);
		if (compilationUnit == null) {
			compilationUnit = RefactoringASTParser.parseWithASTProvider(root, true, pm);
			this.getTypeRootToCompilationUnitMap().put(root, compilationUnit);
		}
		return compilationUnit;
	}

	protected Map<ITypeRoot, CompilationUnit> getTypeRootToCompilationUnitMap() {
		return this.typeRootToCompilationUnitMap;
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		try {
			pm.beginTask(Messages.CreatingChange, 1);

			final TextEditBasedChangeManager manager = new TextEditBasedChangeManager();

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

			LoggingDescriptor descriptor = new LoggingDescriptor(
					null, "TODO", null, arguments, flags);

			return new DynamicValidationRefactoringChange(descriptor, this.getProcessorName(), manager.getAllChanges());
		} finally {
			pm.done();
			this.clearCaches();
		}
	}
	
	private void manageCompilationUnit(final TextEditBasedChangeManager manager, CompilationUnitRewrite rewrite,
			Optional<IProgressMonitor> monitor) throws CoreException {
		monitor.ifPresent(m -> m.beginTask("Creating change ...", IProgressMonitor.UNKNOWN));
		CompilationUnitChange change = rewrite.createChange(false, monitor.orElseGet(NullProgressMonitor::new));

		if (change != null)
			change.setTextType("java");

		manager.manage(rewrite.getCu(), change);
	}
	
	private CompilationUnitRewrite getCompilationUnitRewrite(ICompilationUnit unit, CompilationUnit root) {
		CompilationUnitRewrite rewrite = this.getCompilationUnitToCompilationUnitRewriteMap().get(unit);
		if (rewrite == null) {
			rewrite = new CompilationUnitRewrite(unit, root);
			this.getCompilationUnitToCompilationUnitRewriteMap().put(unit, rewrite);
		}
		return rewrite;
	}


	public void clearCaches() {
		this.getTypeRootToCompilationUnitMap().clear();
	}

	protected Map<ICompilationUnit, CompilationUnitRewrite> getCompilationUnitToCompilationUnitRewriteMap() {
		return this.compilationUnitToCompilationUnitRewriteMap;
	}

	@Override
	public RefactoringParticipant[] loadParticipants(RefactoringStatus status, SharableParticipants sharedParticipants)
			throws CoreException {
		return new RefactoringParticipant[0];
	}

	protected void setLoggingSet(Set<Logging> loggingSet) {
		this.loggingSet = loggingSet;
	}

}
