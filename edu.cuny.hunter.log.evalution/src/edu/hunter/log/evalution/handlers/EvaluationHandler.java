package edu.hunter.log.evalution.handlers;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.jdt.internal.ui.util.SelectionUtil;
import org.eclipse.ui.handlers.HandlerUtil;
import edu.cuny.hunter.log.core.analysis.LogInvocation;
import edu.cuny.hunter.log.core.refactorings.LogRefactoringProcessor;
import edu.cuny.hunter.log.core.utils.LoggerNames;
import edu.hunter.log.evalution.utils.Util;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
@SuppressWarnings("restriction")
public class EvaluationHandler extends AbstractHandler {

	private static final Logger LOGGER = Logger.getLogger(LoggerNames.LOGGER_NAME);

	public static CSVPrinter createCSVPrinter(String fileName, String[] header) throws IOException {
		return new CSVPrinter(new FileWriter(fileName, true), CSVFormat.EXCEL.withHeader(header));
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Optional<IProgressMonitor> monitor = Optional.empty();
		ISelection currentSelection = HandlerUtil.getCurrentSelectionChecked(event);
		List<?> list = SelectionUtil.toList(currentSelection);

		List<IJavaProject> javaProjectList = new LinkedList<IJavaProject>();

		if (list != null) {

			for (Object obj : list)
				if (obj instanceof IJavaElement) {
					IJavaElement jElem = (IJavaElement) obj;
					switch (jElem.getElementType()) {
					case IJavaElement.JAVA_PROJECT:
						javaProjectList.add((IJavaProject) jElem);
						break;
					}

					CodeGenerationSettings settings = JavaPreferencesSettings
							.getCodeGenerationSettings(javaProjectList.get(0));

					try {
						CSVPrinter resultPrinter = createCSVPrinter("result.csv", new String[] { "subject raw",
								"log expression", "start pos", "logging level", "type FQN", "enclosing method" });

						// for each selected java project
						for (IJavaProject project : javaProjectList) {

							LogRefactoringProcessor logRefactoringProcessor = new LogRefactoringProcessor(
									new IJavaProject[] { project }, settings, monitor);

							new ProcessorBasedRefactoring(logRefactoringProcessor)
									.checkAllConditions(new NullProgressMonitor());

							Iterator<LogInvocation> logInvocationIterator = logRefactoringProcessor
									.getLogInvocationSet().iterator();

							// for each logInvocation
							while (logInvocationIterator.hasNext()) {
								LogInvocation logInvocation = logInvocationIterator.next();
								resultPrinter.printRecord(project.getElementName(), logInvocation.getExpression(),
										logInvocation.getStartPosition(), logInvocation.getLogLevel(),
										logInvocation.getEnclosingType().getFullyQualifiedName(),
										Util.getMethodIdentifier(logInvocation.getEnclosingEclipseMethod()));
							}
						}

						resultPrinter.close();
					} catch (IOException e) {
						LOGGER.severe("Cannot create printer.");
					} catch (OperationCanceledException | CoreException e) {
						LOGGER.severe("Cannot pass all conditions.");
					}
				}
		}
		return null;
	}
}
