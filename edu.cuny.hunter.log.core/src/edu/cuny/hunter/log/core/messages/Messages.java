package edu.cuny.hunter.log.core.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "edu.cuny.hunter.logging.core.messages.messages"; //$NON-NLS-1$
	public static final String NoOptimizableLog = null;
	public static final String Name = "Optimize Logging Level!";
	public static String CreatingChange;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		super();
	}
}
