package edu.cuny.hunter.log.core.analysis;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
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
import org.eclipse.jdt.internal.corext.refactoring.base.JavaStatusContext;
import org.eclipse.jdt.internal.corext.refactoring.structure.CompilationUnitRewrite;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusContext;
import org.osgi.framework.FrameworkUtil;

import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.core.IDegreeOfInterest;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.internal.context.core.ContextCorePlugin;
import org.eclipse.mylyn.internal.java.ui.JavaStructureBridge;
import org.eclipse.mylyn.internal.tasks.core.TaskList;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.monitor.core.InteractionEvent;

import edu.cuny.hunter.log.core.utils.LoggerNames;

@SuppressWarnings("restriction")
public class LogInvocation {

	private final MethodInvocation logExpression;
	private final Level logLevel;

	private RefactoringStatus status = new RefactoringStatus();

	private static final String PLUGIN_ID = FrameworkUtil.getBundle(LogInvocation.class).getSymbolicName();

	private IDegreeOfInterest degreeOfInterest;

	private float degreeOfInterestValue;

	private float degreeOfInterestValueForFile;

	private static final Logger LOGGER = Logger.getLogger(LoggerNames.LOGGER_NAME);

	private Action action = Action.NONE;

	public LogInvocation(MethodInvocation logExpression, Level loggingLevel) {
		this.logExpression = logExpression;
		this.logLevel = loggingLevel;

		if (loggingLevel == null) {
			this.addStatusEntry(PreconditionFailure.CURRENTLY_NOT_HANDLED, this.getExpression()
					+ " has argument LogRecord or log level variable which cannot be handled yet.");
		}

		degreeOfInterest = getDegreeOfInterest();

		if (degreeOfInterest != null) {
			degreeOfInterestValue = degreeOfInterest.getValue();
		}
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public float getDegreeOfInterestValue() {
		return degreeOfInterestValue;
	}

	void addStatusEntry(PreconditionFailure failure, String message) {
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

	/**
	 * Get DOI
	 */
	public IDegreeOfInterest getDegreeOfInterest() {
		IMethod enclosingMethod = this.getEnclosingEclipseMethod();
		IInteractionElement interactionElement = ContextCore.getContextManager()
				.getElement(enclosingMethod.getHandleIdentifier());

		if (interactionElement == null)
			return null;

		LOGGER.info(
				"DOI for enclosing method before manipulating file: " + interactionElement.getInterest().getValue());

		IJavaElement cu = this.getEnclosingEclipseMethod().getCompilationUnit();
		IInteractionElement interactionElementForFile = ContextCore.getContextManager()
				.getElement(cu.getHandleIdentifier());

		LOGGER.info("DOI for file before manipulating file: " + interactionElementForFile.getInterest().getValue());

		InteractionEvent event = new InteractionEvent(InteractionEvent.Kind.MANIPULATION,
				new JavaStructureBridge().getContentType(), interactionElementForFile.getHandleIdentifier(), "source");
		ContextCorePlugin.getContextManager().processInteractionEvent(event, true);

		LOGGER.info("DOI for file after manipulating file: " + interactionElementForFile.getInterest().getValue());
		LOGGER.info("DOI for enclosing method after manipulating file: " + interactionElement.getInterest().getValue());

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
				+ ". Degree of Interest " + (degreeOfInterest == null ? "N/A" : degreeOfInterest.getValue()) + ". ");
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
	@SuppressWarnings("unchecked")
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

					// there are no off() and all()
					if (target.equals("off") || target.equals("all")) {

						// change method name
						MethodInvocation newMethodInvocation = (MethodInvocation) ASTNode.copySubtree(ast, expression);
						newMethodInvocation.setName(ast.newSimpleName("log"));

						// change parameter name
						QualifiedName newParaName = ast.newQualifiedName(ast.newSimpleName("Level"),
								ast.newSimpleName(targetLogLevel));
						newMethodInvocation.arguments().add(0, newParaName);

						astRewrite.replace(expression, newMethodInvocation, null);

					} else {
						astRewrite.replace(expression.getName(), newMethodName, null);
					}

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
	 * Check whehter the logging method contains logging level
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
		case CONVERT_TO_ALL:
			this.convertToALL(rewrite);
			break;
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
		case CONVERT_TO_OFF:
			this.convertToOff(rewrite);
			break;
		}
	}

	private void convertToFinest(CompilationUnitRewrite rewrite) {
		convert("finest", "FINEST", rewrite);
	}

	private void convertToOff(CompilationUnitRewrite rewrite) {
		convert("off", "OFF", rewrite);

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

	private void convertToALL(CompilationUnitRewrite rewrite) {
		convert("all", "ALL", rewrite);

	}

}
