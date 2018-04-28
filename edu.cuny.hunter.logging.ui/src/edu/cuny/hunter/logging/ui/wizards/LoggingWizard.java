package edu.cuny.hunter.logging.ui.wizards;

import java.util.Optional;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.refactoring.RefactoringMessages;
import org.eclipse.jdt.internal.ui.refactoring.actions.RefactoringStarter;
import org.eclipse.jdt.ui.refactoring.RefactoringSaveHelper;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import edu.cuny.hunter.logging.core.messages.Messages;
import edu.cuny.hunter.logging.core.untils.Util;;

public class LoggingWizard extends RefactoringWizard {

	// TODO: GUI implementations
	// private static class LoggingInputPage extends UserInputWizardPage {
	//
	// private static final String PAGE_NAME = "LoggingInputPage";;
	// private static final String DESCRIPTION = Messages.Name;
	//
	// public LoggingInputPage() {
	// super(PAGE_NAME);
	// setDescription(DESCRIPTION);
	// }
	//
	// @Override
	// public void createControl(Composite parent) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// }

	@SuppressWarnings("restriction")
	public static void startRefactoring(IJavaProject[] javaProjects, Shell shell, Optional<IProgressMonitor> monitor)
			throws JavaModelException {
		Refactoring refactoring = Util.createRefactoring(javaProjects, monitor);
		RefactoringWizard wizard = new LoggingWizard(refactoring);

		new RefactoringStarter().activate(wizard, shell, RefactoringMessages.OpenRefactoringWizardAction_refactoring,
				RefactoringSaveHelper.SAVE_REFACTORING);
	}

	public LoggingWizard(Refactoring refactoring) {
		super(refactoring,
				RefactoringWizard.DIALOG_BASED_USER_INTERFACE & RefactoringWizard.CHECK_INITIAL_CONDITIONS_ON_OPEN);
		this.setWindowTitle(Messages.Name);
	}

	@Override
	protected void addUserInputPages() {
		// this.addPage(new LoggingInputPage());
	}

}
