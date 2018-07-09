package edu.cuny.hunter.log.ui.wizards;

import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.refactoring.RefactoringMessages;
import org.eclipse.jdt.internal.ui.refactoring.actions.RefactoringStarter;
import org.eclipse.jdt.ui.refactoring.RefactoringSaveHelper;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import edu.cuny.hunter.log.core.messages.Messages;
import edu.cuny.hunter.log.core.refactorings.LogRefactoringProcessor;
import edu.cuny.hunter.log.core.utils.Util;

@SuppressWarnings("restriction")
public class LogWizard extends RefactoringWizard {

	private static class LogInputPage extends UserInputWizardPage {

		private static final String DESCRIPTION = Messages.Name;

		private static final String DIALOG_SETTING_SECTION = "OptimizeLoggingLevel"; //$NON-NLS-1$

		public static final String PAGE_NAME = "LogInputPage"; //$NON-NLS-1$

		private static final String PARTICULAR_CONFIG_LOG_LEVEL = "useConfigLogLevel";

		private LogRefactoringProcessor processor;

		IDialogSettings settings;

		public LogInputPage() {
			super(PAGE_NAME);
			this.setDescription(DESCRIPTION);
		}

		private void addBooleanButton(String text, String key, Consumer<Boolean> valueConsumer, Composite result) {
			Button button = new Button(result, SWT.CHECK);
			button.setText(text);
			boolean value = this.settings.getBoolean(key);
			valueConsumer.accept(value);
			button.setSelection(value);
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					Button btn = (Button) event.getSource();
					boolean selection = btn.getSelection();
					LogInputPage.this.settings.put(key, selection);
					System.out.println("config" + selection + "!!!!!!!!!!!!!!!!!!");
					valueConsumer.accept(selection);
				}
			});
		}

		@Override
		public void createControl(Composite parent) {
			ProcessorBasedRefactoring processorBasedRefactoring = (ProcessorBasedRefactoring) this.getRefactoring();
			org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor refactoringProcessor = processorBasedRefactoring
					.getProcessor();
			this.setProcessor((LogRefactoringProcessor) refactoringProcessor);
			this.loadSettings();

			Composite result = new Composite(parent, SWT.NONE);
			this.setControl(result);
			GridLayout layout = new GridLayout();
			layout.numColumns = 1;
			result.setLayout(layout);

			Label doit = new Label(result, SWT.WRAP);
			doit.setText("Optimize Logging Level.");
			doit.setLayoutData(new GridData());

			Label separator = new Label(result, SWT.SEPARATOR | SWT.HORIZONTAL);
			separator.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

			// set up buttons.
			this.addBooleanButton("Treat CONFIG logging level as particular logging level.",
					PARTICULAR_CONFIG_LOG_LEVEL, this.getProcessor()::setParticularConfigLogLevel, result);

			this.updateStatus();
			Dialog.applyDialogFont(result);
			PlatformUI.getWorkbench().getHelpSystem().setHelp(this.getControl(),
					"optimize_logging_level_wizard_page_context");
		}

		private LogRefactoringProcessor getProcessor() {
			return this.processor;
		}

		private void loadSettings() {
			this.settings = this.getDialogSettings().getSection(DIALOG_SETTING_SECTION);
			if (this.settings == null) {
				this.settings = this.getDialogSettings().addNewSection(DIALOG_SETTING_SECTION);
				this.settings.put(PARTICULAR_CONFIG_LOG_LEVEL, this.getProcessor().getParticularConfigLogLevel());
			}
			this.processor.setParticularConfigLogLevel(this.settings.getBoolean(PARTICULAR_CONFIG_LOG_LEVEL));
		}

		private void setProcessor(LogRefactoringProcessor processor) {
			this.processor = processor;
		}

		private void updateStatus() {
			this.setPageComplete(true);
		}
	}

	public static void startRefactoring(IJavaProject[] javaProjects, Shell shell, Optional<IProgressMonitor> monitor)
			throws JavaModelException {
		Refactoring refactoring = Util.createRefactoring(javaProjects, monitor);
		RefactoringWizard wizard = new LogWizard(refactoring);

		new RefactoringStarter().activate(wizard, shell, RefactoringMessages.OpenRefactoringWizardAction_refactoring,
				RefactoringSaveHelper.SAVE_REFACTORING);
	}

	public LogWizard(Refactoring refactoring) {
		super(refactoring,
				RefactoringWizard.DIALOG_BASED_USER_INTERFACE & RefactoringWizard.CHECK_INITIAL_CONDITIONS_ON_OPEN);
		this.setWindowTitle(Messages.Name);
	}

	@Override
	protected void addUserInputPages() {
		this.addPage(new LogInputPage());
	}

}
