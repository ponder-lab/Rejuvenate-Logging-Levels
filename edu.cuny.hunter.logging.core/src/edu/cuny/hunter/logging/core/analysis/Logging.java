package edu.cuny.hunter.logging.core.analysis;

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

	private final MethodDeclaration enclosingMethodDeclaration;

	private final TypeDeclaration enclosingTypeDeclaration;

	private static final Logger LOGGER = Logger.getLogger(LoggerNames.LOGGER_NAME);

	private static final String PLUGIN_ID = FrameworkUtil.getBundle(Logging.class).getSymbolicName();

	private RefactoringStatus status = new RefactoringStatus();

	// TODO: find the logging level
	private String loggingLevel;

	public Logging(MethodInvocation loggingCreation) {
		this.creation = loggingCreation;
		this.enclosingTypeDeclaration = (TypeDeclaration) ASTNodes.getParent(this.getCreation(),
				ASTNode.TYPE_DECLARATION);
		this.enclosingMethodDeclaration = (MethodDeclaration) ASTNodes.getParent(this.getCreation(),
				ASTNode.METHOD_DECLARATION);

		if (this.enclosingMethodDeclaration == null) {
			LOGGER.warning("Logging: " + this.creation + " not handled.");
			this.addStatusEntry(PreconditionFailure.CURRENTLY_NOT_HANDLED, "Logging: " + this.creation
					+ " is most likely used in a context that is currently not handled by this plug-in.");
		}
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
		return this.getEnclosingEclipseMethod().getJavaProject();
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
	
	@Override
	public String toString() {
		return "The AST Location: " + this.creation.getStartPosition() + "\n" + "The enclosing method: "
				+ this.enclosingMethodDeclaration + "\n";
	}

}
