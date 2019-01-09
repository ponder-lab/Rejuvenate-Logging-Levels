package edu.cuny.hunter.log.evaluation.handlers;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
import edu.cuny.hunter.log.core.analysis.Failure;
import edu.cuny.hunter.log.core.refactorings.LogRejuvenatingProcessor;
import edu.cuny.hunter.log.core.utils.LoggerNames;
import edu.cuny.hunter.log.evaluation.utils.Util;

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
	private static final String USE_LOG_CATEGORY_KEY = "edu.cuny.hunter.log.evaluation.useLogCategory";
	private static final String USE_LOG_CATEGORY_CONFIG_KEY = "edu.cuny.hunter.log.evaluation.useLogCategoryWithConfig";
	private static final String USE_GIT_HISTORY_KEY = "edu.cuny.hunter.log.evaluation.useGitHistory";
	private static final boolean USE_LOG_CATEGORY_DEFAULT = false;
	private static final boolean USE_LOG_CATEGORY_CONFIG_DEFAULT = false;
	private static final boolean USE_GIT_HISTORY = false;

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

						CSVPrinter resultPrinter = Util.createCSVPrinter("result.csv", new String[] { "subject raw",
								"log invocations before", "candidate log invocations", "failures" });
						CSVPrinter actionPrinter = Util.createCSVPrinter("log_actions.csv",
								new String[] { "subject raw", "log expression", "start pos", "logging level",
										"type FQN", "enclosing method", "action" });
						CSVPrinter candidatePrinter = Util.createCSVPrinter("candidate_log_invocations.csv",
								new String[] { "subject raw", "log expression", "start pos", "logging level",
										"type FQN", "enclosing method", "DOI" });
						CSVPrinter transformedLogInvPrinter = Util.createCSVPrinter("transformed_log_invocations.csv",
								new String[] { "subject raw", "log expression", "start pos", "logging level",
										"type FQN", "enclosing method", "DOI" });
						CSVPrinter failurePrinter = Util.createCSVPrinter("failures.csv",
								new String[] { "subject raw", "log expression", "start pos", "logging level",
										"type FQN", "enclosing method", "code", "name", "message" });
						CSVPrinter doiPrinter = Util.createCSVPrinter("DOI_boundaries.csv",
								new String[] { "DOI boundary", "Log level" });

						// for each selected java project
						for (IJavaProject project : javaProjectList) {

							LogRejuvenatingProcessor logRejuvenatingProcessor = new LogRejuvenatingProcessor(
									new IJavaProject[] { project }, this.useLogCategory(),
									this.useLogCategoryWithConfig(), this.useGitHistory(), settings, monitor);

							new ProcessorBasedRefactoring((RefactoringProcessor) logRejuvenatingProcessor)
									.checkAllConditions(new NullProgressMonitor());

							Set<LogInvocation> logInvocationSet = logRejuvenatingProcessor.getLogInvocationSet();

							// get candidate log invocations
							Set<LogInvocation> candidates = logInvocationSet == null ? Collections.emptySet()
									: logInvocationSet.parallelStream().filter(logInvocation -> {
										String pluginId = FrameworkUtil.getBundle(LogInvocation.class)
												.getSymbolicName();

										// check all failures
										RefactoringStatusEntry missingJavaElementError = logInvocation.getStatus()
												.getEntryMatchingCode(pluginId,
														Failure.MISSING_JAVA_ELEMENT.getCode());
										RefactoringStatusEntry binaryElementError = logInvocation.getStatus()
												.getEntryMatchingCode(pluginId,
														Failure.BINARY_ELEMENT.getCode());
										RefactoringStatusEntry readOnlyElementError = logInvocation.getStatus()
												.getEntryMatchingCode(pluginId,
														Failure.READ_ONLY_ELEMENT.getCode());
										RefactoringStatusEntry generatedElementError = logInvocation.getStatus()
												.getEntryMatchingCode(pluginId,
														Failure.GENERATED_ELEMENT.getCode());
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

							Set<LogInvocation> transformedLogInvocationSet = logRejuvenatingProcessor
									.getPossibleTransformedLog();
							// get the difference of candidates and transformed log invocations
							Set<LogInvocation> failures = new HashSet<LogInvocation>();
							failures.addAll(candidates);
							failures.removeAll(transformedLogInvocationSet);

							// failures.
							Collection<RefactoringStatusEntry> errorEntries = failures.parallelStream()
									.map(LogInvocation::getStatus).flatMap(s -> Arrays.stream(s.getEntries()))
									.filter(RefactoringStatusEntry::isError).collect(Collectors.toSet());

							// print failures.
							for (RefactoringStatusEntry entry : errorEntries)
								if (!entry.isFatalError()) {
									Object correspondingElement = entry.getData();

									if (!(correspondingElement instanceof LogInvocation))
										throw new IllegalStateException("The element: " + correspondingElement
												+ " corresponding to a failure is not a log inovation."
												+ correspondingElement.getClass());

									LogInvocation failedLogInvocation = (LogInvocation) correspondingElement;

									failurePrinter.printRecord(project.getElementName(),
											failedLogInvocation.getExpression(), failedLogInvocation.getStartPosition(),
											failedLogInvocation.getLogLevel(),
											failedLogInvocation.getEnclosingType().getFullyQualifiedName(),
											Util.getMethodIdentifier(failedLogInvocation.getEnclosingEclipseMethod()),
											entry.getCode(), entry, entry.getMessage());
								}

							for (LogInvocation logInvocation : transformedLogInvocationSet) {
								// print candidates
								transformedLogInvPrinter.printRecord(project.getElementName(), logInvocation.getExpression(),
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

							LinkedList<Float> boundary = logRejuvenatingProcessor.getBoundary();
							if (boundary != null && boundary.size() > 0)
								if (USE_LOG_CATEGORY_DEFAULT) {
									this.printBoundaryLogCategory(boundary, doiPrinter);
								} else if (USE_LOG_CATEGORY_CONFIG_DEFAULT) { // Do not consider config
									this.printBoundaryWithConfig(boundary, doiPrinter);
								} else {// Treat log levels as traditional log levels
									this.printBoundaryDefault(boundary, doiPrinter);
								}

						}

						resultPrinter.close();
						actionPrinter.close();
						candidatePrinter.close();
						failurePrinter.close();
						transformedLogInvPrinter.close();
						doiPrinter.close();
					} catch (IOException e) {
						LOGGER.severe("Cannot create printer.");
					} catch (OperationCanceledException | CoreException e) {
						LOGGER.severe("Cannot pass all conditions.");
					}
				}
		}
		return null;
	}

	private void printBoundaryLogCategory(LinkedList<Float> boundary, CSVPrinter doiPrinter) throws IOException {
		doiPrinter.printRecord("[" + boundary.get(0) + ", " + boundary.get(1) + ")", Level.FINEST);
		doiPrinter.printRecord("[" + boundary.get(1) + ", " + boundary.get(2) + ")", Level.FINER);
		doiPrinter.printRecord("[" + boundary.get(2) + ", " + boundary.get(3) + ")", Level.FINE);
		doiPrinter.printRecord("[" + boundary.get(3) + ", " + boundary.get(4) + ")", Level.INFO);
	}

	private void printBoundaryWithConfig(LinkedList<Float> boundary, CSVPrinter doiPrinter) throws IOException {
		this.printBoundaryLogCategory(boundary, doiPrinter);
		doiPrinter.printRecord("[" + boundary.get(4) + ", " + boundary.get(5) + ")", Level.WARNING);
		doiPrinter.printRecord("[" + boundary.get(5) + ", " + boundary.get(6) + ")", Level.SEVERE);
	}

	private void printBoundaryDefault(LinkedList<Float> boundary, CSVPrinter doiPrinter) throws IOException {
		doiPrinter.printRecord("[" + boundary.get(0) + ", " + boundary.get(1) + ")", Level.FINEST);
		doiPrinter.printRecord("[" + boundary.get(1) + ", " + boundary.get(2) + ")", Level.FINER);
		doiPrinter.printRecord("[" + boundary.get(2) + ", " + boundary.get(3) + ")", Level.FINE);
		doiPrinter.printRecord("[" + boundary.get(3) + ", " + boundary.get(4) + ")", Level.CONFIG);
		doiPrinter.printRecord("[" + boundary.get(4) + ", " + boundary.get(5) + ")", Level.INFO);
		doiPrinter.printRecord("[" + boundary.get(5) + ", " + boundary.get(6) + ")", Level.WARNING);
		doiPrinter.printRecord("[" + boundary.get(6) + ", " + boundary.get(7) + ")", Level.SEVERE);
	}

	private boolean useLogCategory() {
		String useConfigLogLevels = System.getenv(USE_LOG_CATEGORY_KEY);

		if (useConfigLogLevels == null)
			return USE_LOG_CATEGORY_DEFAULT;
		else
			return Boolean.valueOf(useConfigLogLevels);
	}

	private boolean useLogCategoryWithConfig() {
		String useConfigLogLevels = System.getenv(USE_LOG_CATEGORY_CONFIG_KEY);

		if (useConfigLogLevels == null)
			return USE_LOG_CATEGORY_CONFIG_DEFAULT;
		else
			return Boolean.valueOf(useConfigLogLevels);
	}

	private boolean useGitHistory() {
		String useGitHistory = System.getenv(USE_GIT_HISTORY_KEY);

		if (useGitHistory == null)
			return USE_GIT_HISTORY;
		else
			return Boolean.valueOf(useGitHistory);
	}

}
