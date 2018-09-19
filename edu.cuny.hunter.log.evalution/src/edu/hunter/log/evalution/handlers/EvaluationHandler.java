package edu.hunter.log.evalution.handlers;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
import org.osgi.framework.FrameworkUtil;

import edu.cuny.citytech.refactoring.common.core.RefactoringProcessor;
import edu.cuny.hunter.log.core.analysis.LogInvocation;
import edu.cuny.hunter.log.core.analysis.PreconditionFailure;
import edu.cuny.hunter.log.core.refactorings.LogRejuvenatingProcessor;
import edu.cuny.hunter.log.core.utils.LoggerNames;
import edu.hunter.log.evalution.utils.Util;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;
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
	private static final String USE_LOG_CATEGORY_KEY = "edu.hunter.log.evalution.useLogCategory";
	private static final boolean USE_LOG_CATEGORY_DEFAULT = false;

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
								"log invocations before", "candidate log invocations", "failed preconditions" });
						CSVPrinter actionPrinter = createCSVPrinter("log_actions.csv",
								new String[] { "subject raw", "log expression", "start pos", "logging level",
										"type FQN", "enclosing method", "action" });
						CSVPrinter candidatePrinter = createCSVPrinter("candidate_log_invocations.csv",
								new String[] { "subject raw", "log expression", "start pos", "logging level",
										"type FQN", "enclosing method", "DOI" });
						CSVPrinter optimizablePrinter = createCSVPrinter("optimizable_log_invocations.csv",
								new String[] { "subject raw", "log expression", "start pos", "logging level",
										"type FQN", "enclosing method", "DOI" });
						CSVPrinter failedPreConsPrinter = createCSVPrinter("failed_preconditions.csv",
								new String[] { "subject raw", "log expression", "start pos", "logging level",
										"type FQN", "enclosing method", "code", "name", "message" });

						// for each selected java project
						for (IJavaProject project : javaProjectList) {

							LogRejuvenatingProcessor logRefactoringProcessor = new LogRejuvenatingProcessor(
									new IJavaProject[] { project }, this.useLogCategory(), settings, monitor);

							new ProcessorBasedRefactoring((RefactoringProcessor) logRefactoringProcessor)
									.checkAllConditions(new NullProgressMonitor());

							Set<LogInvocation> logInvocationSet = logRefactoringProcessor.getLogInvocationSet();

							// get candidate log invocations
							Set<LogInvocation> candidates = logInvocationSet == null ? Collections.emptySet()
									: logInvocationSet.parallelStream().filter(logInvocation -> {
										String pluginId = FrameworkUtil.getBundle(LogInvocation.class)
												.getSymbolicName();

										// check all precondition failures
										RefactoringStatusEntry missingJavaElementError = logInvocation.getStatus()
												.getEntryMatchingCode(pluginId,
														PreconditionFailure.MISSING_JAVA_ELEMENT.getCode());
										RefactoringStatusEntry binaryElementError = logInvocation.getStatus()
												.getEntryMatchingCode(pluginId,
														PreconditionFailure.BINARY_ELEMENT.getCode());
										RefactoringStatusEntry readOnlyElementError = logInvocation.getStatus()
												.getEntryMatchingCode(pluginId,
														PreconditionFailure.READ_ONLY_ELEMENT.getCode());
										RefactoringStatusEntry generatedElementError = logInvocation.getStatus()
												.getEntryMatchingCode(pluginId,
														PreconditionFailure.GENERATED_ELEMENT.getCode());
										return missingJavaElementError == null && binaryElementError == null
												&& readOnlyElementError == null && generatedElementError == null;
									}).collect(Collectors.toSet());

							// print candidate log invocations
							for (LogInvocation logInvocation : candidates) {
								// print candidates
								candidatePrinter.printRecord(project.getElementName(), logInvocation.getExpression(),
										logInvocation.getStartPosition(), logInvocation.getLogLevel(),
										logInvocation.getEnclosingType().getFullyQualifiedName(),
										Util.getMethodIdentifier(logInvocation.getEnclosingEclipseMethod()),
										logInvocation.getDegreeOfInterestValue());
							}

							Set<LogInvocation> optimizableLogInvocationSet = logRefactoringProcessor
									.getPossibleRejuvenatedLog();
							// get the difference of candidates and optimizable log invocations
							Set<LogInvocation> failures = new HashSet<LogInvocation>();
							failures.addAll(candidates);
							failures.removeAll(optimizableLogInvocationSet);

							// failed preconditions.
							Collection<RefactoringStatusEntry> errorEntries = failures.parallelStream()
									.map(LogInvocation::getStatus).flatMap(s -> Arrays.stream(s.getEntries()))
									.filter(RefactoringStatusEntry::isError).collect(Collectors.toSet());

							// print failed preconditions
							for (RefactoringStatusEntry entry : errorEntries)
								if (!entry.isFatalError()) {
									Object correspondingElement = entry.getData();

									if (!(correspondingElement instanceof LogInvocation))
										throw new IllegalStateException("The element: " + correspondingElement
												+ " corresponding to a failed precondition is not a Stream. Instead, it is a: "
												+ correspondingElement.getClass());

									LogInvocation failedLogInvocation = (LogInvocation) correspondingElement;

									failedPreConsPrinter.printRecord(project.getElementName(),
											failedLogInvocation.getExpression(), failedLogInvocation.getStartPosition(),
											failedLogInvocation.getLogLevel(),
											failedLogInvocation.getEnclosingType().getFullyQualifiedName(),
											Util.getMethodIdentifier(failedLogInvocation.getEnclosingEclipseMethod()),
											entry.getCode(), entry, entry.getMessage());
								}

							for (LogInvocation logInvocation : optimizableLogInvocationSet) {
								// print candidates
								optimizablePrinter.printRecord(project.getElementName(), logInvocation.getExpression(),
										logInvocation.getStartPosition(), logInvocation.getLogLevel(),
										logInvocation.getEnclosingType().getFullyQualifiedName(),
										Util.getMethodIdentifier(logInvocation.getEnclosingEclipseMethod()),
										logInvocation.getDegreeOfInterestValue());
							}

							for (LogInvocation logInvocation : logInvocationSet) {
								// print actions
								actionPrinter.printRecord(project.getElementName(), logInvocation.getExpression(),
										logInvocation.getStartPosition(), logInvocation.getLogLevel(),
										logInvocation.getEnclosingType().getFullyQualifiedName(),
										Util.getMethodIdentifier(logInvocation.getEnclosingEclipseMethod()),
										logInvocation.getAction());
							}

							resultPrinter.printRecord(project.getElementName(), logInvocationSet.size(),
									candidates.size(), errorEntries.size());

						}

						resultPrinter.close();
						actionPrinter.close();
						candidatePrinter.close();
						failedPreConsPrinter.close();
						optimizablePrinter.close();
					} catch (IOException e) {
						LOGGER.severe("Cannot create printer.");
					} catch (OperationCanceledException | CoreException e) {
						LOGGER.severe("Cannot pass all conditions.");
					}
				}
		}
		return null;
	}

	private boolean useLogCategory() {
		String useConfigLogLevels = System.getenv(USE_LOG_CATEGORY_KEY);

		if (useConfigLogLevels == null)
			return USE_LOG_CATEGORY_DEFAULT;
		else
			return Boolean.valueOf(useConfigLogLevels);
	}

}
