package edu.cuny.hunter.log.core.descriptors;

import java.util.Map;

import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;

@SuppressWarnings("unchecked")
public class LogDescriptor extends JavaRefactoringDescriptor {

	public static final String REFACTORING_ID = "edu.cuny.hunter.logging.evolution"; //$NON-NLS-1$

	protected LogDescriptor() {
		super(REFACTORING_ID);
	}

	public LogDescriptor(String id, String project, String description, String comment,
			@SuppressWarnings("rawtypes") Map arguments, int flags) {
		super(id, project, description, comment, arguments, flags);
	}

	public LogDescriptor(String project, String description, String comment,
			@SuppressWarnings("rawtypes") Map arguments, int flags) {
		this(REFACTORING_ID, project, description, comment, arguments, flags);
	}

}