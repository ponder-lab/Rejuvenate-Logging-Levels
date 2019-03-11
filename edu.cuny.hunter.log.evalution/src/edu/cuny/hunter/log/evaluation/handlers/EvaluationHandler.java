package edu.cuny.hunter.log.evaluation.handlers;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
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
import edu.cuny.citytech.refactoring.common.core.RefactoringProcessor;
import edu.cuny.hunter.log.core.analysis.LogInvocation;
import edu.cuny.hunter.log.core.refactorings.LogRejuvenatingProcessor;
import edu.cuny.hunter.log.core.utils.LoggerNames;
import edu.cuny.hunter.log.evaluation.utils.ResultForCommit;
import edu.cuny.hunter.log.evaluation.utils.Util;
import edu.cuny.hunter.mylyngit.core.analysis.MylynGitPredictionProvider;
import edu.cuny.hunter.mylyngit.core.utils.Commit;

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
	private static final String EVALUATION_PROPERTIES_FILE_NAME = "eval.properties";
	private static final String USE_LOG_CATEGORY_KEY = "edu.cuny.hunter.log.evaluation.useLogCategory";
	private static final String USE_LOG_CATEGORY_CONFIG_KEY = "edu.cuny.hunter.log.evaluation.useLogCategoryWithConfig";
	private static final String USE_GIT_HISTORY_KEY = "edu.cuny.hunter.log.evaluation.useGitHistory";
	private static final String NOT_LOWER_LOG_LEVEL_CATCH_BLOCK_KEY = "edu.cuny.hunter.log.evaluation.notLowerLogLevelInCatchBlock";
	private static final String CHECK_IF_CONDITION_KEY = "edu.cuny.hunter.log. evaluation.checkIfCondition";
	private static final String N_TO_USE_FOR_COMMITS_KEY = "NToUseForCommits";
	private static final int N_TO_USE_FOR_COMMITS_DEFAULT = 100;
	private static final boolean USE_LOG_CATEGORY_DEFAULT = false;
	private static final boolean NOT_LOWER_LOG_LEVEL_CATCH_BLOCK_DEFAULT = false;
	private static final boolean USE_LOG_CATEGORY_CONFIG_DEFAULT = false;
	private static final boolean USE_GIT_HISTORY = false;
	private static final boolean CHECK_IF_CONDITION_DEFAULT = false;
	private boolean useLogCategory;
	private boolean useLogCategoryWithConfig;
	private boolean notLowerLogLevel;
	private boolean checkIfCondtion;

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
				}
			CodeGenerationSettings settings = JavaPreferencesSettings.getCodeGenerationSettings(javaProjectList.get(0));

			try {

				CSVPrinter resultPrinter = Util.createCSVPrinter("result.csv",
						new String[] { "sequence", "subject", "repo URL", "input logging statements",
								"passing logging statements", "failures", "transformed logging statements",
								"average Java lines added", "average Java lines removed",
								"use log category (SEVERE/WARNING/CONFIG)", "use log category (CONFIG)",
								"not lower log levels of logs inside of catch blocks", "time (s)" });
				CSVPrinter repoPrinter = Util.createCSVPrinter("repos.csv", new String[] { "sequence", "repo URL",
						"SHA-1 of head", "N for commits", "actual number of commits" });

				CSVPrinter actionPrinter = Util.createCSVPrinter("log_transformation_actions.csv",
						new String[] { "sequence", "subject", "log expression", "start pos", "log level", "type FQN",
								"enclosing method", "DOI value", "action", "new level" });
				CSVPrinter inputLogInvPrinter = Util.createCSVPrinter("input_log_invocations.csv",
						new String[] { "subject", "log expression", "start pos", "log level", "type FQN",
								"enclosing method", "DOI value" });
				CSVPrinter failurePrinter = Util.createCSVPrinter("failures.csv",
						new String[] { "sequence", "subject", "log expression", "start pos", "log level", "type FQN",
								"enclosing method", "code", "message" });
				CSVPrinter doiPrinter = Util.createCSVPrinter("DOI_boundaries.csv",
						new String[] { "sequence", "subject", "DOI boundary", "log level" });
				CSVPrinter gitCommitPrinter = Util.createCSVPrinter("git_commits.csv",
						new String[] { "subject", "SHA1", "Java lines added", "Java lines removed", "methods found",
								"interaction events", "run time (s)" });

				ResultForCommit resultCommit = new ResultForCommit();

				// for each selected java project
				for (int i = 0; i < 6; ++i) {
					long sequence = this.getRunId();

					for (IJavaProject project : javaProjectList) {

						this.loadSettings(i);

						// collect running time.
						edu.cuny.hunter.mylyngit.core.utils.TimeCollector resultsTimeCollector = new edu.cuny.hunter.mylyngit.core.utils.TimeCollector();
						resultsTimeCollector.start();

						int NToUseCommit = edu.cuny.hunter.mylyngit.core.utils.Util.getNToUseForCommits(project,
								N_TO_USE_FOR_COMMITS_KEY, N_TO_USE_FOR_COMMITS_DEFAULT,
								EVALUATION_PROPERTIES_FILE_NAME);
						LogRejuvenatingProcessor logRejuvenatingProcessor = new LogRejuvenatingProcessor(
								new IJavaProject[] { project }, this.isUseLogCategory(),
								this.isUseLogCategoryWithConfig(), this.getValueOfUseGitHistory(),
								this.isNotLowerLogLevel(), this.checkIfCondtion, NToUseCommit, settings, monitor, true);

						new ProcessorBasedRefactoring((RefactoringProcessor) logRejuvenatingProcessor)
								.checkAllConditions(new NullProgressMonitor());

						resultsTimeCollector.stop();

						Set<LogInvocation> logInvocationSet = logRejuvenatingProcessor.getLogInvocationSet();

						// Just print once.
						if (i == 0)
							// print input log invocations
							for (LogInvocation logInvocation : logInvocationSet) {
								// Print input log invocations
								inputLogInvPrinter.printRecord(project.getElementName(), logInvocation.getExpression(),
										logInvocation.getStartPosition(), logInvocation.getLogLevel(),
										logInvocation.getEnclosingType().getFullyQualifiedName(),
										Util.getMethodIdentifier(logInvocation.getEnclosingEclipseMethod()),
										logInvocation.getDegreeOfInterestValue());
							}

						// get the difference of log invocations and passing log invocations
						HashSet<LogInvocation> failures = new HashSet<LogInvocation>();
						failures.addAll(logInvocationSet);
						HashSet<LogInvocation> passingLogInvocationSet = logRejuvenatingProcessor
								.getPassingLogInvocation();
						failures.removeAll(passingLogInvocationSet);

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

								failurePrinter.printRecord(sequence, project.getElementName(),
										failedLogInvocation.getExpression(), failedLogInvocation.getStartPosition(),
										failedLogInvocation.getLogLevel(),
										failedLogInvocation.getEnclosingType().getFullyQualifiedName(),
										Util.getMethodIdentifier(failedLogInvocation.getEnclosingEclipseMethod()),
										entry.getCode(), entry.getMessage());
							}

						Set<LogInvocation> transformedLogInvocationSet = logRejuvenatingProcessor.getTransformedLog();

						for (LogInvocation logInvocation : transformedLogInvocationSet) {
							// print actions
							actionPrinter.printRecord(sequence, project.getElementName(), logInvocation.getExpression(),
									logInvocation.getStartPosition(), logInvocation.getLogLevel(),
									logInvocation.getEnclosingType().getFullyQualifiedName(),
									Util.getMethodIdentifier(logInvocation.getEnclosingEclipseMethod()),
									logInvocation.getDegreeOfInterestValue(), logInvocation.getAction(),
									logInvocation.getNewLogLevel());
						}

						LinkedList<Float> boundary = logRejuvenatingProcessor.getBoundary();
						if (boundary != null && boundary.size() > 0)
							if (this.isUseLogCategory()) {
								this.printBoundaryLogCategory(sequence, project.getElementName(), boundary, doiPrinter);
							} else if (this.isUseLogCategoryWithConfig()) { // Do not consider config
								this.printBoundaryWithConfig(sequence, project.getElementName(), boundary, doiPrinter);
							} else {// Treat log levels as traditional log levels
								this.printBoundaryDefault(sequence, project.getElementName(), boundary, doiPrinter);
							}

						resultCommit.clear();

						if (this.getValueOfUseGitHistory()) {
							LinkedList<Commit> commits = logRejuvenatingProcessor.getCommits();
							// We only need to print once.
							if (i == 0)
								commits.forEach(c -> {
									try {
										gitCommitPrinter.printRecord(project.getElementName(), c.getSHA1(),
												c.getJavaLinesAdded(), c.getJavaLinesRemoved(), c.getMethodFound(),
												c.getInteractionEvents(), c.getRunTime());
									} catch (IOException e) {
										LOGGER.warning("Cannot print commits correctly.");
									}
									resultCommit.computLines(c);
								});
							resultCommit.setActualCommits(commits.size());
							if (!commits.isEmpty())
								resultCommit.setHeadSha(commits.getLast().getSHA1());

						}

						resultPrinter.printRecord(sequence, project.getElementName(),
								logRejuvenatingProcessor.getRepoURL(), logInvocationSet.size(),
								passingLogInvocationSet.size(), errorEntries.size(), transformedLogInvocationSet.size(),
								resultCommit.getAverageJavaLinesAdded(), resultCommit.getAverageJavaLinesRemoved(),
								this.isUseLogCategory(), this.isUseLogCategoryWithConfig(), this.isNotLowerLogLevel(),
								resultsTimeCollector.getCollectedTime());
						repoPrinter.printRecord(sequence, logRejuvenatingProcessor.getRepoURL(),
								resultCommit.getHeadSha(), NToUseCommit, resultCommit.getActualCommits());
					}

					// Clear intermediate data for mylyn-git plug-in.
					MylynGitPredictionProvider.clearMappingData();
				}

				resultPrinter.close();
				repoPrinter.close();
				actionPrinter.close();
				inputLogInvPrinter.close();
				failurePrinter.close();
				doiPrinter.close();
				gitCommitPrinter.close();
			} catch (IOException e) {
				LOGGER.severe("Cannot create printer.");
			} catch (OperationCanceledException | CoreException e) {
				LOGGER.severe("Cannot pass all conditions.");
			}
		}

		return null;
	}

	/**
	 * We could convert i to binary. Each digit stores the boolean value of one
	 * option. We need triple-digit binary. This option is the second digit.
	 */
	private boolean computeLogCategoryWithConfig(int i) {
		if ((i / 2) % 2 == 1)
			return true;
		return false;
	}

	/**
	 * Third digit in the binary.
	 */
	private boolean computeLowerLogLevelInCatchBlock(int i) {
		if (i % 2 == 1)
			return true;
		return false;
	}

	/**
	 * First digit in the binary.
	 */
	private boolean computeLogCategory(int i) {
		if (i / 2 / 2 == 1)
			return true;
		return false;
	}

	/**
	 * Get id of run times.
	 */
	private long getRunId() {
		return System.currentTimeMillis();
	}

	private void printBoundaryLogCategory(long sequence, String subject, LinkedList<Float> boundary,
			CSVPrinter doiPrinter) throws IOException {
		doiPrinter.printRecord(sequence, subject, "[" + boundary.get(0) + ", " + boundary.get(1) + ")", Level.FINEST);
		doiPrinter.printRecord(sequence, subject, "[" + boundary.get(1) + ", " + boundary.get(2) + ")", Level.FINER);
		doiPrinter.printRecord(sequence, subject, "[" + boundary.get(2) + ", " + boundary.get(3) + ")", Level.FINE);
		doiPrinter.printRecord(sequence, subject, "[" + boundary.get(3) + ", " + boundary.get(4) + ")", Level.INFO);
	}

	private void printBoundaryWithConfig(long sequence, String subject, LinkedList<Float> boundary,
			CSVPrinter doiPrinter) throws IOException {
		this.printBoundaryLogCategory(sequence, subject, boundary, doiPrinter);
		doiPrinter.printRecord(sequence, subject, "[" + boundary.get(4) + ", " + boundary.get(5) + ")", Level.WARNING);
		doiPrinter.printRecord(sequence, subject, "[" + boundary.get(5) + ", " + boundary.get(6) + ")", Level.SEVERE);
	}

	private void printBoundaryDefault(long sequence, String subject, LinkedList<Float> boundary, CSVPrinter doiPrinter)
			throws IOException {
		doiPrinter.printRecord(sequence, subject, "[" + boundary.get(0) + ", " + boundary.get(1) + ")", Level.FINEST);
		doiPrinter.printRecord(sequence, subject, "[" + boundary.get(1) + ", " + boundary.get(2) + ")", Level.FINER);
		doiPrinter.printRecord(sequence, subject, "[" + boundary.get(2) + ", " + boundary.get(3) + ")", Level.FINE);
		doiPrinter.printRecord(sequence, subject, "[" + boundary.get(3) + ", " + boundary.get(4) + ")", Level.CONFIG);
		doiPrinter.printRecord(sequence, subject, "[" + boundary.get(4) + ", " + boundary.get(5) + ")", Level.INFO);
		doiPrinter.printRecord(sequence, subject, "[" + boundary.get(5) + ", " + boundary.get(6) + ")", Level.WARNING);
		doiPrinter.printRecord(sequence, subject, "[" + boundary.get(6) + ", " + boundary.get(7) + ")", Level.SEVERE);
	}

	private void loadSettings() {
		this.setUseLogCategory(this.getValueOfUseLogCategory());
		this.setUseLogCategoryWithConfig(this.getValueOfUseLogCategoryWithConfig());
		this.setNotLowerLogLevel(this.getValueOfNotLowerLogLevelInCatchBlock());
		this.setCheckIfCondition(this.getValueOfCheckIfCondition());
	}

	/**
	 * For automatically loading settings.
	 */
	private void loadSettings(int i) {
		this.setUseLogCategory(this.computeLogCategory(i));
		this.setUseLogCategoryWithConfig(this.computeLogCategoryWithConfig(i));
		this.setNotLowerLogLevel(this.computeLowerLogLevelInCatchBlock(i));
		this.setCheckIfCondition(this.getValueOfCheckIfCondition());
	}

	private boolean getValueOfUseLogCategory() {
		String useConfigLogLevels = System.getenv(USE_LOG_CATEGORY_KEY);

		if (useConfigLogLevels == null)
			return USE_LOG_CATEGORY_DEFAULT;
		else
			return Boolean.valueOf(useConfigLogLevels);
	}

	private boolean getValueOfUseLogCategoryWithConfig() {
		String useConfigLogLevels = System.getenv(USE_LOG_CATEGORY_CONFIG_KEY);

		if (useConfigLogLevels == null)
			return USE_LOG_CATEGORY_CONFIG_DEFAULT;
		else
			return Boolean.valueOf(useConfigLogLevels);
	}

	private boolean getValueOfUseGitHistory() {
		String useGitHistory = System.getenv(USE_GIT_HISTORY_KEY);

		if (useGitHistory == null)
			return USE_GIT_HISTORY;
		else
			return Boolean.valueOf(useGitHistory);
	}

	private boolean getValueOfNotLowerLogLevelInCatchBlock() {
		String notLowerLogLevelInCatchBlock = System.getenv(NOT_LOWER_LOG_LEVEL_CATCH_BLOCK_KEY);

		if (notLowerLogLevelInCatchBlock == null)
			return NOT_LOWER_LOG_LEVEL_CATCH_BLOCK_DEFAULT;
		else
			return Boolean.valueOf(notLowerLogLevelInCatchBlock);
	}

	private boolean getValueOfCheckIfCondition() {
		String notLowerLogLevelInCatchBlock = System.getenv(CHECK_IF_CONDITION_KEY);

		if (notLowerLogLevelInCatchBlock == null)
			return CHECK_IF_CONDITION_DEFAULT;
		else
			return Boolean.valueOf(notLowerLogLevelInCatchBlock);
	}

	public boolean isUseLogCategory() {
		return useLogCategory;
	}

	public void setUseLogCategory(boolean useLogCategory) {
		this.useLogCategory = useLogCategory;
	}

	public boolean isUseLogCategoryWithConfig() {
		return useLogCategoryWithConfig;
	}

	public void setUseLogCategoryWithConfig(boolean useLogCategoryWithConfig) {
		this.useLogCategoryWithConfig = useLogCategoryWithConfig;
	}

	public boolean isNotLowerLogLevel() {
		return notLowerLogLevel;
	}

	public void setNotLowerLogLevel(boolean notLowerLogLevel) {
		this.notLowerLogLevel = notLowerLogLevel;
	}

	public void setCheckIfCondition(boolean checkIfCondition) {
		this.checkIfCondtion = checkIfCondition;
	}
}
