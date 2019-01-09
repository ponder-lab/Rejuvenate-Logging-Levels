package edu.cuny.hunter.log.core.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "edu.cuny.hunter.log.core.messages.Messages"; //$NON-NLS-1$
	public static String Name;
	public static String NoPossibleTransformedLog;
	public static String ReadOnlyElement;
	public static String BinaryElement;
	public static String GeneratedElement;
	public static String MissingJavaElement;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		super();
	}
}
