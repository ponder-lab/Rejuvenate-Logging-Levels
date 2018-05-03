package edu.cuny.hunter.logging.core.analysis;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.refactoring.base.JavaStatusContext;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusContext;
import org.osgi.framework.FrameworkUtil;
import edu.cuny.hunter.logging.core.untils.LoggerNames;

@SuppressWarnings("restriction")
public class Logging {

	private static final String BASE_STREAM_TYPE_NAME = "logging";

	private final MethodInvocation creation;

	private final IMethod method;

	private final MethodDeclaration enclosingMethodDeclaration;

	private final TypeDeclaration enclosingTypeDeclaration;

	private static final Logger LOGGER = Logger.getLogger(LoggerNames.LOGGER_NAME);

	private static final String PLUGIN_ID = FrameworkUtil.getBundle(Logging.class).getSymbolicName();

	private RefactoringStatus status = new RefactoringStatus();

	private final Level loggingLevel;

	public Logging(MethodInvocation loggingCreation) {
		this.creation = loggingCreation;
		this.enclosingTypeDeclaration = (TypeDeclaration) ASTNodes.getParent(this.getCreation(),
				ASTNode.TYPE_DECLARATION);
		this.enclosingMethodDeclaration = (MethodDeclaration) ASTNodes.getParent(this.getCreation(),
				ASTNode.METHOD_DECLARATION);

		IMethodBinding methodBinding = this.getCreation().resolveMethodBinding();
		this.method = (IMethod) methodBinding.getJavaElement();
		loggingLevel = discoverLoggingLevel(methodBinding.getName());
	}

	/**
	 * We only focus on the logging level, which is set by the developer. Hence, we
	 * do not record the logging level which is embedded by the logging package.
	 * e.g. each time we call method entering, a logging record which has "FINER"
	 * level is created.
	 * 
	 * @param methodName
	 *            the name of method
	 * @return logging level
	 */
	public Level discoverLoggingLevel(String methodName) {
		// TODO: method could be used to find more logging level
		if (methodName.equals("config"))
			return Level.CONFIG;
		if (methodName.equals("fine"))
			return Level.FINE;
		if (methodName.equals("finer"))
			return Level.FINER;
		if (methodName.equals("finest"))
			return Level.FINEST;
		if (methodName.equals("info"))
			return Level.INFO;
		if (methodName.equals("severe"))
			return Level.SEVERE;
		if (methodName.equals("warning"))
			return Level.WARNING;

		// TODO: may need wala?
		if (methodName.equals("log"))
			return null;
		if (methodName.equals("logp"))
			return null;
		if (methodName.equals("logrb"))
			return null;
		if (methodName.equals("setLevel"))
			return null;

		// TODO: the handler can contain logging level
		if (methodName.equals("addHandler"))
			return null;

		return null;
	}

	public MethodInvocation getCreation() {
		return this.creation;
	}

	void addStatusEntry(PreconditionFailure failure, String message) {
		MethodInvocation creation = this.getCreation();
		CompilationUnit compilationUnit = (CompilationUnit) ASTNodes.getParent(creation, ASTNode.COMPILATION_UNIT);
		ICompilationUnit compilationUnit2 = (ICompilationUnit) compilationUnit.getJavaElement();
		RefactoringStatusContext context = JavaStatusContext.create(compilationUnit2, creation);
		this.getStatus().addEntry(RefactoringStatus.ERROR, message, context, PLUGIN_ID, failure.getCode(), this);
	}

	public RefactoringStatus getStatus() {
		return this.status;
	}

	public IJavaProject getCreationJavaProject() {
		return this.getMethod().getJavaProject();
	}

	public IMethod getMethod() {
		return this.method;
	}

	public IMethod getEnclosingEclipseMethod() {
		MethodDeclaration enclosingMethodDeclaration = this.getEnclosingMethodDeclaration();

		if (enclosingMethodDeclaration == null)
			return null;

		IMethodBinding binding = enclosingMethodDeclaration.resolveBinding();
		return (IMethod) binding.getJavaElement();
	}

	public MethodDeclaration getEnclosingMethodDeclaration() {
		return this.enclosingMethodDeclaration;
	}

	public void logInfo() {

		LOGGER.info("Find a logging statement. The AST location: " + this.creation.getStartPosition()
				+ ". The method name: " + this.method.getElementName() + ".");

	}

	@Override
	public String toString() {
		return "The AST Location: " + this.creation.getStartPosition() + "\n" + "The enclosing method: "
				+ this.enclosingMethodDeclaration + "\n";
	}

}
