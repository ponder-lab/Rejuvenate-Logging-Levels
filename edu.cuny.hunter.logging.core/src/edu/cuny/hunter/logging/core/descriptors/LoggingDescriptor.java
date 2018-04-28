package edu.cuny.hunter.logging.core.descriptors;

import java.util.Map;

import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;

public class LoggingDescriptor extends JavaRefactoringDescriptor {

	public static final String REFACTORING_ID = "edu.cuny.hunter.logging.research"; //$NON-NLS-1$

	protected LoggingDescriptor() {
		super(REFACTORING_ID);
	}

	public LoggingDescriptor(String id, String project, String description, String comment,
			@SuppressWarnings("rawtypes") Map arguments, int flags) {
		super(id, project, description, comment, arguments, flags);
	}

	public LoggingDescriptor(String project, String description, String comment,
			@SuppressWarnings("rawtypes") Map arguments, int flags) {
		this(REFACTORING_ID, project, description, comment, arguments, flags);
	}

}