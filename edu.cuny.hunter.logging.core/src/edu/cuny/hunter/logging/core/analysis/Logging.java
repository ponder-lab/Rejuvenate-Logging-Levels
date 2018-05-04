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

	private final MethodInvocation creation;

	private final IMethod method;

	private final MethodDeclaration enclosingMethodDeclaration;

	private final TypeDeclaration enclosingTypeDeclaration;
	
	private static final String BASE_STREAM_TYPE_NAME = "logging";
	private static final Logger LOGGER = Logger.getLogger(LoggerNames.LOGGER_NAME);
	private final Level loggingLevel;

	public Logging(MethodInvocation loggingCreation, Level loggingLevel) {
		this.creation = loggingCreation;
		this.enclosingTypeDeclaration = (TypeDeclaration) ASTNodes.getParent(this.getCreation(),
				ASTNode.TYPE_DECLARATION);
		this.enclosingMethodDeclaration = (MethodDeclaration) ASTNodes.getParent(this.getCreation(),
				ASTNode.METHOD_DECLARATION);
		this.loggingLevel = loggingLevel;

		IMethodBinding methodBinding = this.getCreation().resolveMethodBinding();
		this.method = (IMethod) methodBinding.getJavaElement();
	}

	public MethodInvocation getCreation() {
		return this.creation;
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
				+ ". The method name: " + this.method.getElementName() + ". The logging level: " + this.loggingLevel
				+ ". ");

	}

}
