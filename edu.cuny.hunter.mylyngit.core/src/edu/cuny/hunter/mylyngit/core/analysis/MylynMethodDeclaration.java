package edu.cuny.hunter.mylyngit.core.analysis;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.mylyn.context.core.AbstractContextStructureBridge;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.core.IDegreeOfInterest;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.monitor.core.InteractionEvent;

/**
 * Each instance represents an method declaration in the current source code.
 * @author tangy
 *
 */
@SuppressWarnings("restriction")
public class MylynMethodDeclaration {
	
	private MethodDeclaration methodDeclaration;
	private IDegreeOfInterest degreeOfInterest;
	private float degreeOfInterestValue;
	private String methodSignature;
	
	
	public void setDegreeOfInterestValue(float degreeOfInterest) {
		this.degreeOfInterestValue = degreeOfInterest;
	}
	
	public float getDegreeOfInterestValue() {
		return this.degreeOfInterestValue;
	}
	
	public void setMethodSignature(String methodSignature) {
		this.methodSignature = methodSignature;
	}
	
	public String getMethodSignature() {
		return this.methodSignature;
	}
	

	public MethodDeclaration getMethodDeclaration() {
		return this.methodDeclaration;
	}
	
	public IMethod getEclipseMethod() {
		if (this.methodDeclaration == null)
			return null;

		IMethodBinding binding = this.methodDeclaration.resolveBinding();
		return (IMethod) binding.getJavaElement();
	}
	
	// The element in Mylyn
	private IInteractionElement getInteractionElement() {
		IMethod enclosingMethod = this.getEclipseMethod();
		return ContextCore.getContextManager().getElement(enclosingMethod.getHandleIdentifier());
	}
	
	/**
	 * Get DOI
	 */
	public IDegreeOfInterest getDegreeOfInterest() {
		IInteractionElement interactionElement = getInteractionElement();
		if (interactionElement == null)
			return null;
		return interactionElement.getInterest();
	}
	
	/**
	 * Bump DOI when there is a method change.
	 */
	public void bumpDOI() {
		AbstractContextStructureBridge adapter = ContextCore.getStructureBridge(getInteractionElement());
		InteractionEvent event = new InteractionEvent(InteractionEvent.Kind.MANIPULATION, 
				adapter.getContentType(), this.getEclipseMethod().getHandleIdentifier(), "");
		ContextCore.getContextManager().processInteractionEvent(event);
		// Update doi value
		this.degreeOfInterestValue = this.degreeOfInterest == null ? 0 : this.degreeOfInterest.getValue();
	}

}
