package edu.cuny.hunter.log.core.analysis;

import java.util.Collections;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.refactoring.structure.CompilationUnitRewrite;
import org.eclipse.jdt.internal.corext.refactoring.util.JavaStatusContext;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusContext;
import org.eclipse.mylyn.internal.tasks.core.TaskList;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.osgi.framework.FrameworkUtil;

import edu.cuny.hunter.log.core.utils.Util;

@SuppressWarnings("restriction")
public abstract class AbstractLogInvocation {

	protected boolean inCatchBlock;

	private float degreeOfInterestValue;

	private Name replacedName;

	private Name newTargetName;

	private MethodInvocation logExpression;

	private RefactoringStatus status = new RefactoringStatus();

	private static final String PLUGIN_ID = FrameworkUtil.getBundle(LogInvocation.class).getSymbolicName();

	public float getDegreeOfInterestValue() {
		return this.degreeOfInterestValue;
	}

	public void setInCatchBlock(boolean inCatchBlock) {
		this.inCatchBlock = inCatchBlock;
	}

	public void setLogExpression(MethodInvocation logExpression) {
		this.logExpression = logExpression;
	}

	public void setDegreeOfInterestValue(int degreeOfInterestValue) {
		this.degreeOfInterestValue = degreeOfInterestValue;
	}

	void addStatusEntry(Failure failure, String message) {
		MethodInvocation logExpression = this.getExpression();
		CompilationUnit compilationUnit = (CompilationUnit) ASTNodes.getParent(logExpression, ASTNode.COMPILATION_UNIT);
		ICompilationUnit compilationUnit2 = (ICompilationUnit) compilationUnit.getJavaElement();
		RefactoringStatusContext context = JavaStatusContext.create(compilationUnit2, logExpression);
		this.getStatus().addEntry(RefactoringStatus.ERROR, message, context, PLUGIN_ID, failure.getCode(), this);
	}

	public RefactoringStatus getStatus() {
		return status;
	}

	public TaskList getTaskList() {
		return TasksUiPlugin.getTaskList();
	}

	public MethodInvocation getExpression() {
		return this.logExpression;
	}

	public IJavaProject getExpressionJavaProject() {
		return this.getEnclosingEclipseMethod().getJavaProject();
	}

	public MethodDeclaration getEnclosingMethodDeclaration() {
		return (MethodDeclaration) ASTNodes.getParent(this.getExpression(), ASTNode.METHOD_DECLARATION);
	}

	/**
	 * Through the enclosing type, I can get type FQN
	 */
	public IType getEnclosingType() {
		AbstractTypeDeclaration enclosingType = (AbstractTypeDeclaration) ASTNodes.getParent(this.getExpression(),
				AbstractTypeDeclaration.class);
		return (IType) enclosingType.resolveBinding().getJavaElement();
	}

	public IMethod getEnclosingEclipseMethod() {
		MethodDeclaration enclosingMethodDeclaration = this.getEnclosingMethodDeclaration();

		if (enclosingMethodDeclaration == null)
			return null;

		IMethodBinding binding = enclosingMethodDeclaration.resolveBinding();
		return (IMethod) binding.getJavaElement();
	}

	public int getStartPosition() {
		return this.logExpression.getStartPosition();
	}

	public void logInfo() {
	}

	public CompilationUnit getEnclosingCompilationUnit() {
		return (CompilationUnit) ASTNodes.getParent(this.getExpression(), ASTNode.COMPILATION_UNIT);
	}

	/**
	 * Basic method to do transformation.
	 */
	public void convert(String target, String targetLogLevel, CompilationUnitRewrite rewrite) {
	}

	/**
	 * Set names.
	 * 
	 * @param oldName
	 * @param newLevelName
	 */
	public void setNames(Name oldName, Name newLevelName) {
		this.setReplacedName(oldName);
		this.setNewTargetName(newLevelName);
	}

	public boolean getInCatchBlock() {
		return this.inCatchBlock;
	}

	public void transform(CompilationUnitRewrite rewrite) {
	}

	/**
	 * Should update DOI values after evaluating git history.
	 */
	public void updateDOI() {
		this.degreeOfInterestValue = Util.getDOIValue(this.getEnclosingEclipseMethod(),
				Collections.singleton(this.getEnclosingEclipseMethod()));
	}

	public Name getReplacedName() {
		return this.replacedName;
	}

	public void setReplacedName(Name replacedName) {
		this.replacedName = replacedName;
	}

	public Name getNewTargetName() {
		return newTargetName;
	}

	public void setNewTargetName(Name newTargetName) {
		this.newTargetName = newTargetName;
	}

}
