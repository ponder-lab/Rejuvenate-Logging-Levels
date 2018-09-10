package edu.cuny.hunter.log.core.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "edu.cuny.hunter.logging.core.messages.messages"; //$NON-NLS-1$
	public static final String NoOptimizableLog = "No optimizable logging level!";
	public static final String Name = "Optimize Logging Level!";
	public static final String ReadOnlyElement = "We've hit a jar or other Model Element where we can't make relevant changes.";
	public static final String BinaryElement = "Element is in a .class file.";
	public static final String GeneratedElement = "We can't refactoring anything because of generated code.";
	public static final String MissingJavaElement = "we are resolving this element from the JavaModel using JDT internal API, and it's missing.";
	public static String CreatingChange;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		super();
	}
}
