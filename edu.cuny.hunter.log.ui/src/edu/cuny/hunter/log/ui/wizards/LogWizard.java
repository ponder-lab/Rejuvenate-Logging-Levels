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
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import edu.cuny.hunter.log.core.messages.Messages;
import edu.cuny.hunter.log.core.refactorings.LogRejuvenatingProcessor;
import edu.cuny.hunter.log.core.utils.Util;

@SuppressWarnings("restriction")
public class LogWizard extends RefactoringWizard {

	private static class LogInputPage extends UserInputWizardPage {

		private static final String DESCRIPTION = Messages.Name;

		private static final String DIALOG_SETTING_SECTION = "RejuvenateLogLevel"; //$NON-NLS-1$

		public static final String PAGE_NAME = "LogInputPage"; //$NON-NLS-1$

		private static final String USE_LOG_CATEGORY = "useLogCategory";

		private static final String USE_LOG_CATEGORY_CONFIG = "useLogCategoryWithConfig";

		private static final String USE_GIT_HISTORY = "useGitHistory";

		private static final String N_TO_USE_FOR_COMMITS = "NToUseForCommits";

		private static final String NOT_LOWER_LOG_LEVEL_CATCH_BLOCK = "notLowerLogLevelInCatchBlock";

		private LogRejuvenatingProcessor processor;

		private IDialogSettings settings;

		public LogInputPage() {
			super(PAGE_NAME);
			this.setDescription(DESCRIPTION);
		}

		private Button addBooleanButton(String text, String key, Consumer<Boolean> valueConsumer, Composite result,
				int buttonStyle) {
			Button button = new Button(result, buttonStyle);
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
					valueConsumer.accept(selection);
				}
			});
			return button;
		}

		@Override
		public void createControl(Composite parent) {
			ProcessorBasedRefactoring processorBasedRefactoring = (ProcessorBasedRefactoring) this.getRefactoring();
			org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor refactoringProcessor = processorBasedRefactoring
					.getProcessor();
			this.setProcessor((LogRejuvenatingProcessor) refactoringProcessor);
			this.loadSettings();

			Composite result = new Composite(parent, SWT.NONE);
			this.setControl(result);
			GridLayout layout = new GridLayout();
			layout.numColumns = 1;
			result.setLayout(layout);

			Label logLable = new Label(result, SWT.NONE);
			logLable.setText("Choose the log level style:");

			Button button = new Button(result, SWT.RADIO);
			button.setText("Defalut： traditional levels.");
			button.setSelection(true);

			// set up buttons.
			this.addBooleanButton("Treat CONFIG log level as a category and not a traditional level.",
					USE_LOG_CATEGORY_CONFIG, this.getProcessor()::setParticularConfigLogLevel, result, SWT.RADIO);

			// set up buttons.
			this.addBooleanButton("Treat CONFIG/WARNING/SEVERE log levels as category and not traditional levels.",
					USE_LOG_CATEGORY, this.getProcessor()::setParticularLogLevel, result, SWT.RADIO);

			Label separator = new Label(result, SWT.SEPARATOR | SWT.SHADOW_OUT | SWT.HORIZONTAL);
			separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			Label gitLabel = new Label(result, SWT.NONE);
			gitLabel.setText(
					"Check the option below if you would like to use git history to " + "rejuvenate log levels.");

			// set up buttons.
			Button checkButton = this.addBooleanButton("Traverse git history to rejuvenate log levels.",
					USE_GIT_HISTORY, this.getProcessor()::setUseGitHistory, result, SWT.CHECK);
			checkButton.setSelection(true);

			Label separator2 = new Label(result, SWT.SEPARATOR | SWT.SHADOW_OUT | SWT.HORIZONTAL);
			separator2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			// set up buttons.
			Button checkButton2 = this.addBooleanButton(
					"Never lower the logging level of logging statements within catch blocks.", USE_GIT_HISTORY,
					this.getProcessor()::setNotLowerLogLevelInCatchBlock, result, SWT.CHECK);
			checkButton2.setSelection(true);

			Label separator3 = new Label(result, SWT.SEPARATOR | SWT.SHADOW_OUT | SWT.HORIZONTAL);
			separator3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			this.addIntegerButton("N values used to limit number of commits: ", N_TO_USE_FOR_COMMITS,
					this.getProcessor()::setNToUseForCommits, this.addIntegerButton(result));

			this.updateStatus();
			Dialog.applyDialogFont(result);
			PlatformUI.getWorkbench().getHelpSystem().setHelp(this.getControl(),
					"rejuvenate_logging_level_wizard_page_context");
		}

		private Composite addIntegerButton(Composite result) {
			Composite compositeForIntegerButton = new Composite(result, SWT.NONE);
			GridLayout layoutForIntegerButton = new GridLayout(2, true);

			compositeForIntegerButton.setLayout(layoutForIntegerButton);
			return compositeForIntegerButton;
		}

		private void addIntegerButton(String text, String key, Consumer<Integer> valueConsumer, Composite result) {
			Label label = new Label(result, SWT.NULL);
			label.setText(text);

			Text textBox = new Text(result, SWT.SINGLE);
			textBox.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
			int value = this.settings.getInt(key);
			valueConsumer.accept(value);
			textBox.setText(String.valueOf(value));
			textBox.addModifyListener(event -> {
				int selection;
				try {
					selection = Integer.parseInt(((Text) event.widget).getText());
				} catch (NumberFormatException e) {
					return;
				}
				LogInputPage.this.settings.put(key, selection);
				valueConsumer.accept(selection);
			});
		}

		private LogRejuvenatingProcessor getProcessor() {
			return this.processor;
		}

		private void loadSettings() {
			this.settings = this.getDialogSettings().getSection(DIALOG_SETTING_SECTION);
			if (this.settings == null) {
				this.settings = this.getDialogSettings().addNewSection(DIALOG_SETTING_SECTION);
				this.settings.put(USE_LOG_CATEGORY_CONFIG, this.getProcessor().getParticularConfigLogLevel());
				this.settings.put(USE_LOG_CATEGORY, this.getProcessor().getParticularLogLevel());
				this.settings.put(USE_GIT_HISTORY, this.getProcessor().getGitHistory());
				this.settings.put(N_TO_USE_FOR_COMMITS, this.getProcessor().getNToUseForCommits());
				this.settings.put(NOT_LOWER_LOG_LEVEL_CATCH_BLOCK, this.getProcessor().getNotLowerLogLevelInCatchBlock());
			}
			this.processor.setParticularConfigLogLevel(this.settings.getBoolean(USE_LOG_CATEGORY_CONFIG));
			this.processor.setParticularLogLevel(this.settings.getBoolean(USE_LOG_CATEGORY));
			this.processor.setUseGitHistory(this.settings.getBoolean(USE_GIT_HISTORY));
			this.processor.setNToUseForCommits(this.settings.getInt(N_TO_USE_FOR_COMMITS));
			this.processor.setNotLowerLogLevelInCatchBlock(this.settings.getBoolean(NOT_LOWER_LOG_LEVEL_CATCH_BLOCK));
		}

		private void setProcessor(LogRejuvenatingProcessor processor) {
			this.processor = processor;
		}

		private void updateStatus() {
			this.setPageComplete(true);
		}
		
	}

	public static void startRefactoring(IJavaProject[] javaProjects, Shell shell, Optional<IProgressMonitor> monitor)
			throws JavaModelException {
		Refactoring refactoring = Util.createRejuvenating(javaProjects, monitor);
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
