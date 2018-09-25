package edu.cuny.hunter.github.ui.views;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import edu.cuny.hunter.github.ui.ExtractChangeViewProvider;

/**
 * The activator class controls the plug-in life cycle
 */
public class ExtractChangeUiPlugin extends AbstractUIPlugin {

	private enum Property {EXTRACT_CHANGE_PROVIDER};

	/**
	 * The plug-in ID.
	 */
	public static final String PLUGIN_ID = "edu.cuny.hunter.github.ui";

	/**
	 * The shared instance.
	 */
	private static ExtractChangeUiPlugin plugin;

	private ExtractChangeViewProvider extractChangeViewProvider;

	private ExtractChangeView extractChangeView;
	
	private final PropertyChangeSupport changes = new PropertyChangeSupport(this);
	
	public void addPropertyChangeListener(final PropertyChangeListener l) {
		this.changes.addPropertyChangeListener(l);
	}
	
	public ExtractChangeUiPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;	
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static ExtractChangeUiPlugin getDefault() {
		return plugin;
	}

	public ExtractChangeViewProvider getExtractChangeViewProvider() {
		return this.extractChangeViewProvider;
	}

	public void setExtractChangeViewProvider(
			ExtractChangeViewProvider extractChangeViewProvider) {
		ExtractChangeViewProvider oldValue = this.extractChangeViewProvider;
		this.extractChangeViewProvider = extractChangeViewProvider;
		this.changes.firePropertyChange(Property.EXTRACT_CHANGE_PROVIDER.toString(), oldValue, this.extractChangeViewProvider);
	}

	public ExtractChangeView getExtractChangeView() {
		return this.extractChangeView;
	}

	public void setExtractChangeView(
			ExtractChangeView extractChangeView) {
		this.extractChangeView = extractChangeView;
	}

}
