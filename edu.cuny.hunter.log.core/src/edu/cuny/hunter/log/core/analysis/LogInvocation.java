package edu.cuny.hunter.log.core.analysis;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.refactoring.structure.CompilationUnitRewrite;
import org.eclipse.jdt.internal.corext.refactoring.util.JavaStatusContext;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusContext;
import org.osgi.framework.FrameworkUtil;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.core.IDegreeOfInterest;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.internal.tasks.core.TaskList;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import edu.cuny.hunter.log.core.utils.LoggerNames;

@SuppressWarnings("restriction")
public class LogInvocation {

	private final Level logLevel;

	private Action action = Action.NONE;

	private float degreeOfInterestValue;

	private IDegreeOfInterest degreeOfInterest;

	private final MethodInvocation logExpression;

	private RefactoringStatus status = new RefactoringStatus();

	private static final Logger LOGGER = Logger.getLogger(LoggerNames.LOGGER_NAME);

	private static final String PLUGIN_ID = FrameworkUtil.getBundle(LogInvocation.class).getSymbolicName();

	public LogInvocation(MethodInvocation logExpression, Level loggingLevel) {
		this.logExpression = logExpression;
		this.logLevel = loggingLevel;

		if (loggingLevel == null) {
			this.addStatusEntry(Failure.CURRENTLY_NOT_HANDLED, this.getExpression()
					+ " has argument LogRecord or log level variable which cannot be handled yet.");
		}

		degreeOfInterest = this.getDegreeOfInterest();

		if (degreeOfInterest != null) {
			degreeOfInterestValue = degreeOfInterest.getValue();
		}
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public float getDegreeOfInterestValue() {
		return this.degreeOfInterestValue;
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

	// The element in Mylyn
	private IInteractionElement getInteractionElement() {
		IMethod enclosingMethod = this.getEnclosingEclipseMethod();
		return ContextCore.getContextManager().getElement(enclosingMethod.getHandleIdentifier());
	}

	/**
	 * Get DOI
	 */
	public IDegreeOfInterest getDegreeOfInterest() {
		IInteractionElement interactionElement = getInteractionElement();
		if (interactionElement == null || interactionElement.getContext() == null) // workaround bug ...
			return null;
		return interactionElement.getInterest();
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
	 * Through the enclosing type, I can type FQN
	 */
	public IType getEnclosingType() {
		MethodDeclaration enclosingMethodDeclaration = getEnclosingMethodDeclaration();

		if (enclosingMethodDeclaration == null)
			return null;

		IMethodBinding binding = enclosingMethodDeclaration.resolveBinding();
		return (IType) binding.getDeclaringClass().getJavaElement();
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

	public Level getLogLevel() {
		return this.logLevel;
	}

	public void logInfo() {
		IDegreeOfInterest degreeOfInterest = this.getDegreeOfInterest();
		LOGGER.info("Find a log expression." + this.getExpression().toString() + " The logging level: " + getLogLevel()
				+ ". Degree of Interest " + (degreeOfInterest == null ? "0" : degreeOfInterest.getValue()) + ". ");
	}

	public Action getAction() {
		return this.action;
	}

	public CompilationUnit getEnclosingCompilationUnit() {
		return (CompilationUnit) ASTNodes.getParent(this.getEnclosingTypeDeclaration(), ASTNode.COMPILATION_UNIT);
	}

	private ASTNode getEnclosingTypeDeclaration() {
		return (TypeDeclaration) ASTNodes.getParent(this.getExpression(), ASTNode.TYPE_DECLARATION);
	}

	/**
	 * Basic method to do transformation.
	 */
	private void convert(String target, String targetLogLevel, CompilationUnitRewrite rewrite) {

		MethodInvocation expression = this.getExpression();

		if (expression != null)
			if (expression.getNodeType() == ASTNode.METHOD_INVOCATION) {

				String identifier = expression.getName().getIdentifier();
				AST ast = logExpression.getAST();

				ASTRewrite astRewrite = rewrite.getASTRewrite();

				// The methods (e.g., warning() -> critical()).
				if (isLoggingLevelMethod(identifier)) {

					SimpleName newMethodName = ast.newSimpleName(target);
					astRewrite.replace(expression.getName(), newMethodName, null);

				} else // The parameters (e.g., log(Level.WARNING) -> log(Level.CRITICAL).
				if (isLogMethod(identifier)) {
					ASTNode argument = (ASTNode) expression.arguments().get(0);
					QualifiedName newParaName = ast.newQualifiedName(ast.newSimpleName("Level"),
							ast.newSimpleName(targetLogLevel));
					astRewrite.replace(argument, newParaName, null);
				}
			}
	}

	/**
	 * Check whether the log method could have the parameter for logging level
	 */
	private static boolean isLogMethod(String methodName) {
		if (methodName.equals("log") || methodName.equals("logp") || methodName.equals("logrb"))
			return true;
		return false;
	}

	/**
	 * Check whether the logging method contains logging level
	 */
	private static boolean isLoggingLevelMethod(String methodName) {
		if (methodName.equals("config") || methodName.equals("fine") || methodName.equals("finest")
				|| methodName.equals("info") || methodName.equals("severe") || methodName.equals("warning"))
			return true;
		return false;
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
		this.degreeOfInterest = this.getDegreeOfInterest();
		if (this.degreeOfInterest != null)
			this.degreeOfInterestValue = this.degreeOfInterest.getValue();
		else this.degreeOfInterestValue = 0;
	}

}
