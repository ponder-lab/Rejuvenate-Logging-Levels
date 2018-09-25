package edu.cuny.hunter.github.ui.preferences;

import java.util.logging.Logger;

import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;

public class PreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	private static Logger logger = Logger.getLogger(PreferencePage.class
			.getName());

	public PreferencePage() {
		super(GRID);
		// setPreferenceStore(FraglightUiPlugin.getDefault().getPreferenceStore());
		setDescription("Set desired properties of the Fraglight pointcut change predictor here.");
	}

	/**
	 * add fields here
	 */
	public void createFieldEditors() {

		// addField(new StringFieldEditor(PreferenceConstants.P_HIGH_THRESHOLD,
		//			"&High change confidence threshold:", getFieldEditorParent()));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

}