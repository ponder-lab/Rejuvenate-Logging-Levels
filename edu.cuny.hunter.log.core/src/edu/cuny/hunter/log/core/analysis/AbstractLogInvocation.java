package edu.cuny.hunter.log.core.analysis;

import java.util.Collections;
import java.util.logging.Level;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
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
	/**
	 * Transformation actions.
	 */
	private Action action;

	/**
	 * Current log level.
	 */
	private Level newLogLevel;

	protected boolean inCatchBlock;

	private float degreeOfInterestValue;

	private Name replacedName;

	private Name newTargetName;

	private MethodInvocation logExpression;

	private RefactoringStatus status = new RefactoringStatus();

	private static final String PLUGIN_ID = FrameworkUtil.getBundle(LogInvocation.class).getSymbolicName();

	public void setAction(Action action, Level newLogLevel) {
		this.action = action;
		this.newLogLevel = newLogLevel;
	}

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

	public Action getAction() {
		return this.action;
	}

	public CompilationUnit getEnclosingCompilationUnit() {
		return (CompilationUnit) ASTNodes.getParent(this.getExpression(), ASTNode.COMPILATION_UNIT);
	}

	/**
	 * Basic method to do transformation.
	 */
	private void convert(String target, String targetLogLevel, CompilationUnitRewrite rewrite) {

		MethodInvocation expression = this.getExpression();

		if (expression != null)
			if (expression.getNodeType() == ASTNode.METHOD_INVOCATION) {

				String identifier = expression.getName().getIdentifier();
				AST ast = expression.getAST();

				ASTRewrite astRewrite = rewrite.getASTRewrite();

				// The methods (e.g., warning() -> critical()).
				if (Util.isLoggingLevelMethod(identifier)) {

					SimpleName newMethodName = ast.newSimpleName(target);
					astRewrite.replace(expression.getName(), newMethodName, null);
					this.setNames(expression.getName(), newMethodName);
				} else // The parameters (e.g., log(Level.WARNING) ->
						// log(Level.CRITICAL).
				if (isLogMethod(identifier)) {
					Name firstArgument = (Name) expression.arguments().get(0);
					// log(WARNING, ...)
					if (firstArgument.isSimpleName()) {
						Name newLevelName = ast.newSimpleName(targetLogLevel);
						astRewrite.replace(firstArgument, newLevelName, null);
						this.setNames(firstArgument, newLevelName);
					} else {

						QualifiedName argument = (QualifiedName) firstArgument;
						Name qualifier = argument.getQualifier();

						QualifiedName newParaName = null;
						// log(java.util.logging.Level.warning, ...)
						if (qualifier.isQualifiedName())
							newParaName = ast
									.newQualifiedName(
											ast.newQualifiedName(
													ast.newQualifiedName(
															ast.newQualifiedName(ast.newSimpleName("java"),
																	ast.newSimpleName("util")),
															ast.newSimpleName("logging")),
													ast.newSimpleName("Level")),
											ast.newSimpleName(targetLogLevel));
						// log(Level.warning,...)
						if (qualifier.isSimpleName()) {
							newParaName = ast.newQualifiedName(ast.newSimpleName("Level"),
									ast.newSimpleName(targetLogLevel));
						}
						astRewrite.replace(argument, newParaName, null);
						this.setNames(argument, newParaName);
					}
				}

			}
	}

	/**
	 * Set names.
	 * 
	 * @param oldName
	 * @param newLevelName
	 */
	private void setNames(Name oldName, Name newLevelName) {
		this.setReplacedName(oldName);
		this.setNewTargetName(newLevelName);
	}

	/**
	 * Check whether the log method could have the parameter for logging level
	 */
	private static boolean isLogMethod(String methodName) {
		if (methodName.equals("log") || methodName.equals("logp") || methodName.equals("logrb"))
			return true;
		return false;
	}

	public boolean getInCatchBlock() {
		return this.inCatchBlock;
	}

	public Level getNewLogLevel() {
		return newLogLevel;
	}

	/**
	 * Do transformation!
	 * 
	 * @param rewrite
	 */
	public void transform(CompilationUnitRewrite rewrite) {
		switch (this.getAction()) {
		case CONVERT_TO_FINEST:
			this.convertToFinest(rewrite);
			break;
		case CONVERT_TO_FINER:
			this.convertToFiner(rewrite);
			break;
		case CONVERT_TO_FINE:
			this.convertToFine(rewrite);
			break;
		case CONVERT_TO_INFO:
			this.convertToInfo(rewrite);
			break;
		case CONVERT_TO_CONFIG:
			this.convertToConfig(rewrite);
			break;
		case CONVERT_TO_WARNING:
			this.convertToWarning(rewrite);
			break;
		case CONVERT_TO_SEVERE:
			this.convertToSevere(rewrite);
			break;
		default:
			break;
		}
	}

	private void convertToFinest(CompilationUnitRewrite rewrite) {
		convert("finest", "FINEST", rewrite);
	}

	private void convertToSevere(CompilationUnitRewrite rewrite) {
		convert("severe", "SEVERE", rewrite);
	}

	private void convertToWarning(CompilationUnitRewrite rewrite) {
		convert("warning", "WARNING", rewrite);
	}

	private void convertToConfig(CompilationUnitRewrite rewrite) {
		convert("config", "CONFIG", rewrite);
	}

	private void convertToInfo(CompilationUnitRewrite rewrite) {
		convert("info", "INFO", rewrite);
	}

	private void convertToFine(CompilationUnitRewrite rewrite) {
		convert("fine", "FINE", rewrite);
	}

	private void convertToFiner(CompilationUnitRewrite rewrite) {
		convert("finer", "FINER", rewrite);
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