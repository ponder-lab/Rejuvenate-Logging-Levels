package edu.cuny.hunter.log.core.analysis;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.refactoring.base.JavaStatusContext;
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

	private final MethodInvocation expression;
	private final Level logLevel;

	private RefactoringStatus status = new RefactoringStatus();

	private static final String PLUGIN_ID = FrameworkUtil.getBundle(LogInvocation.class).getSymbolicName();

	private static IDegreeOfInterest degreeOfInterest;

	private static final Logger LOGGER = Logger.getLogger(LoggerNames.LOGGER_NAME);

	public LogInvocation(MethodInvocation logExpression, Level loggingLevel) {
		this.expression = logExpression;
		this.logLevel = loggingLevel;

		if (loggingLevel == null) {
			this.addStatusEntry(PreconditionFailure.CURRENTLY_NOT_HANDLED,
					this.getExpression() + "has argument LogRecord which cannot be handled yet.");
		}

		this.degreeOfInterest = getDegreeOfInterest();

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
		return interactionElement.getInterest();
	}

	public MethodInvocation getExpression() {
		return this.expression;
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
		return this.expression.getStartPosition();
	}

	public Level getLogLevel() {
		return this.logLevel;
	}

	public void logInfo() {
		LOGGER.info("Find a log expression." + this.getExpression().toString() + " The logging level: " + getLogLevel()
				+ ". ");
	}

}
