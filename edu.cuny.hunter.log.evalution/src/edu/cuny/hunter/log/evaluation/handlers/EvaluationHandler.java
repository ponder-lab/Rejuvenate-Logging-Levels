package edu.cuny.hunter.log.evaluation.handlers;

import static edu.cuny.hunter.mylyngit.core.utils.Util.getNToUseForCommits;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVPrinter;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.osgi.framework.FrameworkUtil;

import edu.cuny.citytech.refactoring.common.core.RefactoringProcessor;
import edu.cuny.hunter.log.core.analysis.LogInvocation;
import edu.cuny.hunter.log.core.analysis.LogInvocationSlf4j;
import edu.cuny.hunter.log.core.analysis.LoggingFramework;
import edu.cuny.hunter.log.core.refactorings.LogRejuvenatingProcessor;
import edu.cuny.hunter.log.core.utils.LoggerNames;
import edu.cuny.hunter.log.core.utils.Util;
import edu.cuny.hunter.log.evaluation.utils.EvaluationUtil;
import edu.cuny.hunter.log.evaluation.utils.ResultForCommit;
import edu.cuny.hunter.mylyngit.core.analysis.MylynGitPredictionProvider;
import edu.cuny.hunter.mylyngit.core.utils.Commit;
import edu.cuny.hunter.mylyngit.core.utils.TimeCollector;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
@SuppressWarnings("restriction")
public class EvaluationHandler extends AbstractHandler {

	private static final Logger LOGGER = Logger.getLogger(LoggerNames.LOGGER_NAME);

	private Map<String, Double> repoToLinesAdded = new HashMap<String, Double>();
	private Map<String, Double> repoToLinesRemoved = new HashMap<String, Double>();

	private static final String EVALUATION_PROPERTIES_FILE_NAME = "eval.properties";
	private static final String NOT_LOWER_LOG_LEVEL_IF_STATEMENT_KEY = "edu.cuny.hunter.log.evaluation.notLowerLogLevelInIfStatement";
	private static final String NOT_LOWER_LOG_LEVEL_CATCH_BLOCK_KEY = "edu.cuny.hunter.log.evaluation.notLowerLogLevelInCatchBlock";
	private static final String NOT_LOWER_LOG_LEVEL_KEYWORDS_KEY = "edu.cuny.hunter.log.evaluation.notLowerLogLevelWithKeywords";
	private static final String NOT_RAISE_LOG_LEVEL_KEYWORKS_KEY = "edu.cuny.hunter.log.evaluation.notRaiseLogLevelWithoutKeywords";
	private static final String CONSISTENT_LEVEL_IN_INHERITANCE_KEY = "edu.cuny.hunter.log.evaluation.consistentLevelInInheritance";
	private static final String USE_LOG_CATEGORY_CONFIG_KEY = "edu.cuny.hunter.log.evaluation.useLogCategoryWithConfig";
	private static final String MAX_TRANSFORMATION_DISTANCE_KEY = "edu.cuny.hunter.log.evaluation.maxTransDistance";
	private static final String CHECK_IF_CONDITION_KEY = "edu.cuny.hunter.log.evaluation.checkIfCondition";
	private static final String USE_LOG_CATEGORY_KEY = "edu.cuny.hunter.log.evaluation.useLogCategory";
	private static final String USE_GIT_HISTORY_KEY = "edu.cuny.hunter.log.evaluation.useGitHistory";
	private static final String N_TO_USE_FOR_COMMITS_KEY = "NToUseForCommits";
	private static final int N_TO_USE_FOR_COMMITS_DEFAULT = 100;
	private static final int MAX_TRANSFORMATION_DISTANCE__DEFUALT = Integer.MAX_VALUE;
	private static final boolean NOT_LOWER_LOG_LEVEL_IF_STATEMENT_DEFAULT = false;
	private static final boolean CONSISTENT_LEVEL_IN_INHERITANCE_DEFAULT = false;
	private static final boolean NOT_LOWER_LOG_LEVEL_KEYWORDS_DEFAULT = false;
	private static final boolean NOT_RAISE_LOG_LEVEL_KEYWORDS_DEFAULT = false;
	private static final boolean NOT_LOWER_LOG_LEVEL_CATCH_BLOCK_DEFAULT = false;
	private static final boolean USE_LOG_CATEGORY_CONFIG_DEFAULT = false;
	private static final boolean CHECK_IF_CONDITION_DEFAULT = false;
	private static final boolean USE_LOG_CATEGORY_DEFAULT = false;
	private static final boolean USE_GIT_HISTORY = false;

	/**
	 * Treat CONFIG/WARNING/SEVERE log levels as category and not traditional
	 * levels.
	 */
	private boolean useLogCategory;

	/**
	 * Treat CONFIG log level as a category and not a traditional level.
	 */
	private boolean useLogCategoryWithConfig;

	/**
	 * Never lower the logging level of logging statements within catch blocks.
	 */
	private boolean notLowerLogLevelInCatchBlock;

	/**
	 * Never lower the logging level of logging statements within immediate if
	 * statements.
	 */
	private boolean notLowerLogLevelInIfStatement;

	/**
	 * Never lower logs with particular keywords in their log messages.
	 */
	private boolean notLowerLogLevelWithKeywords;

	/**
	 * Never raise logs without particular keywords in their log messages.
	 */
	private boolean notRaiseLogLevelWithoutKeywords;

	/**
	 * Log level transformations between overriding methods should be consistent.
	 */
	private boolean consistentLevelInInheritance;

	/**
	 * Do not change a log level in a logging statement if there exists an immediate
	 * if statement whose condition contains a log level.
	 */
	private boolean checkIfCondtion;

	/**
	 * Adjust transformations if their transformation distance is over the allowed
	 * transformation distance.
	 */
	private int maxTransDistance;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Job.create("Evaluating log level rejuvenation ...", monitor -> {
			CSVPrinter resultPrinter = null;
			CSVPrinter repoPrinter = null;
			CSVPrinter actionPrinter = null;
			CSVPrinter inputLogInvPrinter = null;
			CSVPrinter nonenclosingMethodPrinter = null;
			CSVPrinter failurePrinter = null;
			CSVPrinter doiPrinter = null;
			CSVPrinter gitCommitPrinter = null;
			CSVPrinter candidatePrinter = null;
			CSVPrinter notLowerLevelsInCatchBlockPrinter = null;
			CSVPrinter notLowerLevelsInIfStatementPrinter = null;
			CSVPrinter notLowerLevelsDueToKeywordsPrinter = null;
			CSVPrinter notRaiseLevelDueToKeywordsPrinter = null;
			CSVPrinter considerIfConditionPrinter = null;
			CSVPrinter adjustLogLevelByInheritancePrinter = null;
			CSVPrinter adjustLogLevelByDistancePritner = null;

			try {
				IJavaProject[] javaProjects = Util.getSelectedJavaProjectsFromEvent(event);

				if (javaProjects.length == 0)
					return new Status(IStatus.ERROR, FrameworkUtil.getBundle(this.getClass()).getSymbolicName(),
							"No Java projects selected");

				CodeGenerationSettings settings = JavaPreferencesSettings.getCodeGenerationSettings(javaProjects[0]);

				resultPrinter = EvaluationUtil.createCSVPrinter("result.csv", new String[] { "sequence", "subject",
						"repo URL", "decay factor", "input logging statements", "candidate logging statements",
						"passing logging statements", "failures", "transformed logging statements",
						"log level not lowered in catch blocks", "log level not lowered in if statements",
						"log level not transformed due to if condition", "log level not lowered due to keywords",
						"log level not raised without keywords", "log level adjusted by max transformation distance",
						"log level adjusted by inheritance", "use log category (SEVERE/WARNING/CONFIG)",
						"use log category (CONFIG)", "not lower log levels of logs inside of catch blocks",
						"not lower log levels of logs inside of if statements",
						"not lower log levels in their messages with keywords",
						"not raise log levels in their message without keywords",
						"consider if condition having log level", "consistent log level in inheritance",
						"maximum transformation distance", "time (s)" });

				repoPrinter = EvaluationUtil.createCSVPrinter("repos.csv",
						new String[] { "sequence", "repo URL", "SHA-1 of head", "N for commits",
								"number of commits processed", "actual number of commits", "average Java lines added",
								"average Java lines removed" });
				actionPrinter = EvaluationUtil.createCSVPrinter("log_transformation_actions.csv",
						new String[] { "sequence", "subject", "log expression", "start pos", "log level", "type FQN",
								"enclosing method", "DOI value", "action", "new level", "logging framework" });
				inputLogInvPrinter = EvaluationUtil.createCSVPrinter("input_log_invocations.csv",
						new String[] { "subject", "log expression", "start pos", "log level", "type FQN",
								"enclosing method", "logging framework", "DOI value" });

				nonenclosingMethodPrinter = EvaluationUtil.createCSVPrinter("nonenclosing_methods.csv",
						new String[] { "subject", "type FQN", "method", "DOI" });

				failurePrinter = EvaluationUtil.createCSVPrinter("failures.csv",
						new String[] { "sequence", "subject", "log expression", "start pos", "log level", "type FQN",
								"enclosing method", "code", "message", "logging framework" });
				doiPrinter = EvaluationUtil.createCSVPrinter("DOI_boundaries.csv", new String[] { "sequence", "subject",
						"low boundary inclusive", "high boundary exclusive", "log level", "logging framework" });
				gitCommitPrinter = EvaluationUtil.createCSVPrinter("git_commits.csv",
						new String[] { "subject", "SHA1", "Java lines added", "Java lines removed", "methods found",
								"interaction events", "run time (s)" });
				candidatePrinter = EvaluationUtil.createCSVPrinter("candidate_log_invocations.csv",
						new String[] { "sequence", "subject", "log expression", "start pos", "log level", "type FQN",
								"enclosing method", "logging framework", "DOI value" });
				notLowerLevelsInCatchBlockPrinter = EvaluationUtil.createCSVPrinter(
						"not_lower_levels_in_catch_blocks.csv", new String[] { "sequence", "subject", "log expression",
								"start pos", "log level", "type FQN", "enclosing method", "logging framework" });
				notLowerLevelsInIfStatementPrinter = EvaluationUtil.createCSVPrinter(
						"not_lower_levels_in_if_statements.csv", new String[] { "sequence", "subject", "log expression",
								"start pos", "log level", "type FQN", "enclosing method", "logging framework" });
				notLowerLevelsDueToKeywordsPrinter = EvaluationUtil.createCSVPrinter(
						"not_lower_levels_due_to_keywords.csv", new String[] { "sequence", "subject", "log expression",
								"start pos", "log level", "type FQN", "enclosing method", "logging framework" });
				notRaiseLevelDueToKeywordsPrinter = EvaluationUtil.createCSVPrinter(
						"not_raise_levels_due_to_keywords.csv", new String[] { "sequence", "subject", "log expression",
								"start pos", "log level", "type FQN", "enclosing method", "logging framework" });
				considerIfConditionPrinter = EvaluationUtil.createCSVPrinter(
						"not_transform_levels_due_to_if_condition.csv",
						new String[] { "sequence", "subject", "log expression", "start pos", "log level", "type FQN",
								"enclosing method", "logging framework" });
				adjustLogLevelByInheritancePrinter = EvaluationUtil.createCSVPrinter("adjust_level_by_inheritance.csv",
						new String[] { "sequence", "subject", "log expression", "start pos", "log level", "type FQN",
								"enclosing method", "logging framework" });
				adjustLogLevelByDistancePritner = EvaluationUtil.createCSVPrinter("adjust_level_by_distance.csv",
						new String[] { "sequence", "subject", "log expression", "start pos", "log level", "type FQN",
								"enclosing method", "logging framework" });

				// we are using 6 settings
				for (int i = 0; i < 6; ++i) {

					long sequence = this.getRunId();

					for (IJavaProject project : javaProjects) {
						List<Integer> nsToUse = getNToUseForCommits(project, N_TO_USE_FOR_COMMITS_KEY,
								N_TO_USE_FOR_COMMITS_DEFAULT, EVALUATION_PROPERTIES_FILE_NAME);

						if (nsToUse.size() > 1)
							throw new IllegalStateException(
									"Using more than one N value is currently unsupported: " + nsToUse.size() + ".");

						for (int NToUseCommit : nsToUse) {

							this.loadSettings(i);

							// collect running time.
							TimeCollector resultsTimeCollector = new TimeCollector();
							resultsTimeCollector.start();

							LogRejuvenatingProcessor logRejuvenatingProcessor = new LogRejuvenatingProcessor(
									new IJavaProject[] { project }, this.isUseLogCategory(),
									this.isUseLogCategoryWithConfig(), this.getValueOfUseGitHistory(),
									this.isNotLowerLogLevelInCatchBlock(), this.isNotLowerLogLevelInIfStatement(),
									this.isNotLowerLogLevelWithKeywords(), this.isNotRaiseLogLevelWithoutKeywords(),
									this.isCheckIfCondition(), this.isConsistentLevelInInheritance(),
									this.getMaxTransDistance(), NToUseCommit, settings, Optional.ofNullable(monitor),
									true);

							RefactoringStatus status = new ProcessorBasedRefactoring(
									(RefactoringProcessor) logRejuvenatingProcessor)
											.checkAllConditions(new NullProgressMonitor());

							if (status.hasFatalError())
								return new Status(IStatus.ERROR,
										FrameworkUtil.getBundle(this.getClass()).getSymbolicName(),
										"Fatal error encountered during evaluation: "
												+ status.getMessageMatchingSeverity(RefactoringStatus.FATAL));

							resultsTimeCollector.stop();

							Set<LogInvocation> logInvocationSet = logRejuvenatingProcessor.getLogInvocationSet();
							Set<LogInvocationSlf4j> logInvocationSlf4js = logRejuvenatingProcessor
									.getLogInvocationSlf4j();

							// Just print once.
							// Using 1 here because both settings are enabled
							// for this index.
							if (i == 1) {
								// print input log invocations
								for (LogInvocation logInvocation : logInvocationSet) {
									// Print input log invocations
									inputLogInvPrinter.printRecord(project.getElementName(),
											logInvocation.getExpression(), logInvocation.getStartPosition(),
											logInvocation.getLogLevel(),
											logInvocation.getEnclosingType().getFullyQualifiedName(),
											Util.getMethodIdentifier(logInvocation.getEnclosingEclipseMethod()),
											LoggingFramework.JAVA_UTIL_LOGGING,
											logInvocation.getDegreeOfInterestValue());
								}

								// print input log invocations for slf4j.
								for (LogInvocationSlf4j logInvocationSlf4j : logInvocationSlf4js) {
									inputLogInvPrinter.printRecord(project.getElementName(),
											logInvocationSlf4j.getExpression(), logInvocationSlf4j.getStartPosition(),
											logInvocationSlf4j.getLogLevel(),
											logInvocationSlf4j.getEnclosingType().getFullyQualifiedName(),
											Util.getMethodIdentifier(logInvocationSlf4j.getEnclosingEclipseMethod()),
											LoggingFramework.SLF4J, logInvocationSlf4j.getDegreeOfInterestValue());
								}

							}

							Map<IMethod, Float> nonenclosingMethodToDOI = logRejuvenatingProcessor
									.getNonenclosingMethodToDOI();

							for (IMethod method : nonenclosingMethodToDOI.keySet()) {
								nonenclosingMethodPrinter.printRecord(project.getElementName(),
										method.getDeclaringType().getFullyQualifiedName(),
										Util.getMethodIdentifier(method), nonenclosingMethodToDOI.get(method));
							}

							// Print candidates.
							Set<LogInvocation> candidates = this
									.computeCandidateLogs(logRejuvenatingProcessor.getRoughCandidateSet());
							Set<LogInvocationSlf4j> candidateSlf4js = this
									.computeCandidateLogsSlf4j(logRejuvenatingProcessor.getRoughCandidateSetSlf4j());

							for (LogInvocation logInvocation : candidates)
								candidatePrinter.printRecord(sequence, project.getElementName(),
										logInvocation.getExpression(), logInvocation.getStartPosition(),
										logInvocation.getLogLevel(),
										logInvocation.getEnclosingType().getFullyQualifiedName(),
										Util.getMethodIdentifier(logInvocation.getEnclosingEclipseMethod()),
										LoggingFramework.JAVA_UTIL_LOGGING, logInvocation.getDegreeOfInterestValue());

							for (LogInvocationSlf4j logInvocationSlf4j : candidateSlf4js) {
								candidatePrinter.printRecord(sequence, project.getElementName(),
										logInvocationSlf4j.getExpression(), logInvocationSlf4j.getStartPosition(),
										logInvocationSlf4j.getLogLevel(),
										logInvocationSlf4j.getEnclosingType().getFullyQualifiedName(),
										Util.getMethodIdentifier(logInvocationSlf4j.getEnclosingEclipseMethod()),
										LoggingFramework.SLF4J, logInvocationSlf4j.getDegreeOfInterestValue());
							}

							// get the difference of log invocations and passing
							// log invocations
							HashSet<LogInvocation> failures = new HashSet<LogInvocation>();
							failures.addAll(candidates);
							HashSet<LogInvocation> passingLogInvocationSet = this.getPassingLogInvocation(candidates);
							failures.removeAll(passingLogInvocationSet);

							// get the difference of log invocations and passing
							// log invocations for slf4j
							HashSet<LogInvocationSlf4j> failuresSlf4j = new HashSet<LogInvocationSlf4j>();
							failuresSlf4j.addAll(candidateSlf4js);
							HashSet<LogInvocationSlf4j> passingLogInvocationSlf4js = this
									.getPassingLogInvocationSlf4j(candidateSlf4js);
							failuresSlf4j.removeAll(passingLogInvocationSlf4js);

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
											entry.getCode(), entry.getMessage(), LoggingFramework.JAVA_UTIL_LOGGING);
								}

							// failures.
							Collection<RefactoringStatusEntry> errorEntriesSlf4j = failuresSlf4j.parallelStream()
									.map(LogInvocationSlf4j::getStatus).flatMap(s -> Arrays.stream(s.getEntries()))
									.filter(RefactoringStatusEntry::isError).collect(Collectors.toSet());

							// print failures.
							for (RefactoringStatusEntry entry : errorEntriesSlf4j)
								if (!entry.isFatalError()) {
									Object correspondingElement = entry.getData();

									if (!(correspondingElement instanceof LogInvocation))
										throw new IllegalStateException("The element: " + correspondingElement
												+ " corresponding to a failure is not a log inovation."
												+ correspondingElement.getClass());

									LogInvocationSlf4j failedLogInvocation = (LogInvocationSlf4j) correspondingElement;

									failurePrinter.printRecord(sequence, project.getElementName(),
											failedLogInvocation.getExpression(), failedLogInvocation.getStartPosition(),
											failedLogInvocation.getLogLevel(),
											failedLogInvocation.getEnclosingType().getFullyQualifiedName(),
											Util.getMethodIdentifier(failedLogInvocation.getEnclosingEclipseMethod()),
											entry.getCode(), entry.getMessage(), LoggingFramework.SLF4J);
								}

							Set<LogInvocation> transformedLogInvocationSet = logRejuvenatingProcessor
									.getTransformedLog();
							Set<LogInvocationSlf4j> transformedInvocationSlf4js = logRejuvenatingProcessor
									.getSlf4jTransformedLog();

							for (LogInvocation logInvocation : transformedLogInvocationSet)
								// print actions
								actionPrinter.printRecord(sequence, project.getElementName(),
										logInvocation.getExpression(), logInvocation.getStartPosition(),
										logInvocation.getLogLevel(),
										logInvocation.getEnclosingType().getFullyQualifiedName(),
										Util.getMethodIdentifier(logInvocation.getEnclosingEclipseMethod()),
										logInvocation.getDegreeOfInterestValue(), logInvocation.getAction(),
										logInvocation.getNewLogLevel(), LoggingFramework.JAVA_UTIL_LOGGING);

							for (LogInvocationSlf4j logInvocationSlf4j : transformedInvocationSlf4js) {
								// print actions for slf4j
								actionPrinter.printRecord(sequence, project.getElementName(),
										logInvocationSlf4j.getExpression(), logInvocationSlf4j.getStartPosition(),
										logInvocationSlf4j.getLogLevel(),
										logInvocationSlf4j.getEnclosingType().getFullyQualifiedName(),
										Util.getMethodIdentifier(logInvocationSlf4j.getEnclosingEclipseMethod()),
										logInvocationSlf4j.getDegreeOfInterestValue(), logInvocationSlf4j.getAction(),
										logInvocationSlf4j.getNewLogLevel(), LoggingFramework.SLF4J);
							}

							ArrayList<Float> boundary = logRejuvenatingProcessor.getBoundary();
							ArrayList<Float> boundarySlf4j = logRejuvenatingProcessor.getBoundarySlf4j();
							if (boundary != null && boundary.size() > 0)
								if (this.isUseLogCategory()) {
									this.printBoundaryLogCategory(sequence, project.getElementName(), boundary,
											doiPrinter);
									this.printBoundaryLogCategorySlf4j(sequence, project.getElementName(),
											boundarySlf4j, doiPrinter);
								} else {

									this.printBoundarySlf4j(sequence, project.getElementName(), boundarySlf4j,
											doiPrinter);
									if (this.isUseLogCategoryWithConfig()) { // Do
																				// not
																				// consider
																				// config
										this.printBoundaryWithConfig(sequence, project.getElementName(), boundary,
												doiPrinter);
									} else {// Treat log levels as traditional log
											// levels
										this.printBoundaryDefault(sequence, project.getElementName(), boundary,
												doiPrinter);
									}
								}

							ResultForCommit resultCommit = new ResultForCommit();
							String repoURL = logRejuvenatingProcessor.getRepoURL();
							if (this.getValueOfUseGitHistory()) {
								LinkedList<Commit> commits = logRejuvenatingProcessor.getCommits();
								// We only need to print once.
								if (i == 0) {
									// If memoization happens, the list of commits is empty.
									for (Commit c : commits) {
										gitCommitPrinter.printRecord(project.getElementName(), c.getSHA1(),
												c.getJavaLinesAdded(), c.getJavaLinesRemoved(), c.getMethodFound(),
												c.getInteractionEvents(), c.getRunTime());
										resultCommit.computLines(c);
									}
									if (!this.repoToLinesAdded.containsKey(repoURL))
										this.repoToLinesAdded.put(repoURL, commits.size() == 0 ? 0
												: ((double) resultCommit.getJavaLinesAdded()) / commits.size());
									if (!this.repoToLinesRemoved.containsKey(repoURL))
										this.repoToLinesRemoved.put(repoURL, commits.size() == 0 ? 0
												: ((double) resultCommit.getJavaLinesRemoved()) / commits.size());
								}
								resultCommit.setActualCommits(commits.size());
								if (!commits.isEmpty())
									resultCommit.setHeadSha(commits.getLast().getSHA1());

								// Duplicate rows.
								// For memoization checking.
								if (resultCommit.getHeadSha() != null)
									repoPrinter.printRecord(sequence, repoURL, resultCommit.getHeadSha(), NToUseCommit,
											resultCommit.getCommitsProcessed(),
											logRejuvenatingProcessor.getActualNumberOfCommits(),
											this.repoToLinesAdded.get(repoURL), this.repoToLinesRemoved.get(repoURL));

							}

							resultPrinter.printRecord(sequence, project.getElementName(),
									logRejuvenatingProcessor.getRepoURL(), logRejuvenatingProcessor.getDecayFactor(),
									logInvocationSet.size() + logInvocationSlf4js.size(),
									candidates.size() + candidateSlf4js.size(),
									passingLogInvocationSet.size() + passingLogInvocationSlf4js.size(),
									errorEntries.size() + errorEntriesSlf4j.size(),
									transformedLogInvocationSet.size() + transformedInvocationSlf4js.size(),
									// log level not lowered in catch blocks
									logRejuvenatingProcessor.getLogInvsNotLoweredInCatch().size()
											+ logRejuvenatingProcessor.getLogInvsNotLoweredInCatchSlf4j().size(),

									// log level not lowered in if statements
									logRejuvenatingProcessor.getLogInvsNotLoweredInIf().size()
											+ logRejuvenatingProcessor.getLogInvsNotLoweredInIfStatementSlf4j().size(),

									// log level not transformed due to if condition
									logRejuvenatingProcessor.getLogInvsNotTransformedInIf().size()
											+ logRejuvenatingProcessor.getLogInvsNotTransformedInIfSlf4j().size(),

									// log level not lowered due to keywords
									logRejuvenatingProcessor.getLogInvsNotLoweredWithKeywords().size()
											+ logRejuvenatingProcessor.getLogInvsNotLoweredByKeywordsSlf4j().size(),

									// log level not raised without keywords
									logRejuvenatingProcessor.getLogInvsNotRaisedWithoutKeywords().size()
											+ logRejuvenatingProcessor.getLogInvsNotRaisedWithoutKeywordsSlf4j().size(),

									// log level adjusted by max transformation distance
									logRejuvenatingProcessor.getLogInvsAdjustedByDis().size()
											+ logRejuvenatingProcessor.getLogInvsAdjustedByDistanceSlf4j().size(),

									// log level adjusted by inheritance
									logRejuvenatingProcessor.getLogInvsAdjustedByInheritance().size()
											+ logRejuvenatingProcessor.getLogInvsAdjustedByInheritanceSlf4j().size(),
									this.isUseLogCategory(), this.isUseLogCategoryWithConfig(),
									this.isNotLowerLogLevelInCatchBlock(), this.isNotLowerLogLevelInIfStatement(),
									this.isNotLowerLogLevelWithKeywords(), this.isNotRaiseLogLevelWithoutKeywords(),
									this.isCheckIfCondition(), this.isConsistentLevelInInheritance(),
									this.getMaxTransDistance(), resultsTimeCollector.getCollectedTime());

							/**
							 * Print log invocations (JUL) whose levels are not lowered in catch blocks.
							 */
							for (LogInvocation logInvocation : logRejuvenatingProcessor.getLogInvsNotLoweredInCatch())
								notLowerLevelsInCatchBlockPrinter.printRecord(sequence, project.getElementName(),
										logInvocation.getExpression(), logInvocation.getStartPosition(),
										logInvocation.getLogLevel(),
										logInvocation.getEnclosingType().getFullyQualifiedName(),
										Util.getMethodIdentifier(logInvocation.getEnclosingEclipseMethod()),
										LoggingFramework.JAVA_UTIL_LOGGING);

							/**
							 * Print log invocations (Slf4j) whose levels are not lowered in catch blocks.
							 */
							for (LogInvocationSlf4j logInvocationSlf4j : logRejuvenatingProcessor
									.getLogInvsNotLoweredInCatchSlf4j())
								notLowerLevelsInCatchBlockPrinter.printRecord(sequence, project.getElementName(),
										logInvocationSlf4j.getExpression(), logInvocationSlf4j.getStartPosition(),
										logInvocationSlf4j.getLogLevel(),
										logInvocationSlf4j.getEnclosingType().getFullyQualifiedName(),
										Util.getMethodIdentifier(logInvocationSlf4j.getEnclosingEclipseMethod()),
										LoggingFramework.SLF4J);

							/**
							 * Print log invocations (JUL) whose levels are not lowered in if statement.
							 */
							for (LogInvocation logInvocation : logRejuvenatingProcessor.getLogInvsNotLoweredInIf())
								notLowerLevelsInIfStatementPrinter.printRecord(sequence, project.getElementName(),
										logInvocation.getExpression(), logInvocation.getStartPosition(),
										logInvocation.getLogLevel(),
										logInvocation.getEnclosingType().getFullyQualifiedName(),
										Util.getMethodIdentifier(logInvocation.getEnclosingEclipseMethod()),
										LoggingFramework.JAVA_UTIL_LOGGING);

							/**
							 * Print log invocations (Slf4j) whose levels are not lowered in if statement.
							 */
							for (LogInvocationSlf4j logInvocation : logRejuvenatingProcessor
									.getLogInvsNotLoweredInIfStatementSlf4j())
								notLowerLevelsInIfStatementPrinter.printRecord(sequence, project.getElementName(),
										logInvocation.getExpression(), logInvocation.getStartPosition(),
										logInvocation.getLogLevel(),
										logInvocation.getEnclosingType().getFullyQualifiedName(),
										Util.getMethodIdentifier(logInvocation.getEnclosingEclipseMethod()),
										LoggingFramework.SLF4J);

							/**
							 * Print log invocations (JUL) whose levels are not lowered due to keywords.
							 */
							for (LogInvocation logInvocation : logRejuvenatingProcessor
									.getLogInvsNotLoweredWithKeywords())
								notLowerLevelsDueToKeywordsPrinter.printRecord(sequence, project.getElementName(),
										logInvocation.getExpression(), logInvocation.getStartPosition(),
										logInvocation.getLogLevel(),
										logInvocation.getEnclosingType().getFullyQualifiedName(),
										Util.getMethodIdentifier(logInvocation.getEnclosingEclipseMethod()),
										LoggingFramework.JAVA_UTIL_LOGGING);

							/**
							 * Print log invocations (Slf4j) whose levels are not lowered due to keywords.
							 */
							for (LogInvocationSlf4j logInvocation : logRejuvenatingProcessor
									.getLogInvsNotLoweredByKeywordsSlf4j())
								notLowerLevelsDueToKeywordsPrinter.printRecord(sequence, project.getElementName(),
										logInvocation.getExpression(), logInvocation.getStartPosition(),
										logInvocation.getLogLevel(),
										logInvocation.getEnclosingType().getFullyQualifiedName(),
										Util.getMethodIdentifier(logInvocation.getEnclosingEclipseMethod()),
										LoggingFramework.SLF4J);

							/**
							 * Print log invocations (JUL) whose levels are not raised due to keywords.
							 */
							for (LogInvocation logInvocation : logRejuvenatingProcessor
									.getLogInvsNotRaisedWithoutKeywords())
								notRaiseLevelDueToKeywordsPrinter.printRecord(sequence, project.getElementName(),
										logInvocation.getExpression(), logInvocation.getStartPosition(),
										logInvocation.getLogLevel(),
										logInvocation.getEnclosingType().getFullyQualifiedName(),
										Util.getMethodIdentifier(logInvocation.getEnclosingEclipseMethod()),
										LoggingFramework.JAVA_UTIL_LOGGING);

							/**
							 * Print log invocations (Slf4j) whose levels are not raised due to keywords.
							 */
							for (LogInvocationSlf4j logInvocation : logRejuvenatingProcessor
									.getLogInvsNotRaisedWithoutKeywordsSlf4j())
								notRaiseLevelDueToKeywordsPrinter.printRecord(sequence, project.getElementName(),
										logInvocation.getExpression(), logInvocation.getStartPosition(),
										logInvocation.getLogLevel(),
										logInvocation.getEnclosingType().getFullyQualifiedName(),
										Util.getMethodIdentifier(logInvocation.getEnclosingEclipseMethod()),
										LoggingFramework.SLF4J);

							/**
							 * Print log invocations (JUL) whose levels are not transformed due to if
							 * condition.
							 */
							for (LogInvocation logInvocation : logRejuvenatingProcessor.getLogInvsNotTransformedInIf())
								considerIfConditionPrinter.printRecord(sequence, project.getElementName(),
										logInvocation.getExpression(), logInvocation.getStartPosition(),
										logInvocation.getLogLevel(),
										logInvocation.getEnclosingType().getFullyQualifiedName(),
										Util.getMethodIdentifier(logInvocation.getEnclosingEclipseMethod()),
										LoggingFramework.JAVA_UTIL_LOGGING);

							/**
							 * Print log invocations (Slf4j) whose levels are not transformed due to if
							 * condition.
							 */
							for (LogInvocationSlf4j logInvocation : logRejuvenatingProcessor
									.getLogInvsNotTransformedInIfSlf4j())
								considerIfConditionPrinter.printRecord(sequence, project.getElementName(),
										logInvocation.getExpression(), logInvocation.getStartPosition(),
										logInvocation.getLogLevel(),
										logInvocation.getEnclosingType().getFullyQualifiedName(),
										Util.getMethodIdentifier(logInvocation.getEnclosingEclipseMethod()),
										LoggingFramework.SLF4J);

							/**
							 * Print log invocations (JUL) whose levels are adjusted due to inheritance.
							 */
							for (LogInvocation logInvocation : logRejuvenatingProcessor
									.getLogInvsAdjustedByInheritance())
								adjustLogLevelByInheritancePrinter.printRecord(sequence, project.getElementName(),
										logInvocation.getExpression(), logInvocation.getStartPosition(),
										logInvocation.getLogLevel(),
										logInvocation.getEnclosingType().getFullyQualifiedName(),
										Util.getMethodIdentifier(logInvocation.getEnclosingEclipseMethod()),
										LoggingFramework.JAVA_UTIL_LOGGING);

							/**
							 * Print log invocations (Slf4j) whose levels are adjusted due to inheritance.
							 */
							for (LogInvocationSlf4j logInvocation : logRejuvenatingProcessor
									.getLogInvsAdjustedByInheritanceSlf4j())
								adjustLogLevelByInheritancePrinter.printRecord(sequence, project.getElementName(),
										logInvocation.getExpression(), logInvocation.getStartPosition(),
										logInvocation.getLogLevel(),
										logInvocation.getEnclosingType().getFullyQualifiedName(),
										Util.getMethodIdentifier(logInvocation.getEnclosingEclipseMethod()),
										LoggingFramework.SLF4J);

							/**
							 * Print log invocations (JUL) whose levels are adjusted due to maximum
							 * distance.
							 */
							for (LogInvocation logInvocation : logRejuvenatingProcessor.getLogInvsAdjustedByDis())
								adjustLogLevelByDistancePritner.printRecord(sequence, project.getElementName(),
										logInvocation.getExpression(), logInvocation.getStartPosition(),
										logInvocation.getLogLevel(),
										logInvocation.getEnclosingType().getFullyQualifiedName(),
										Util.getMethodIdentifier(logInvocation.getEnclosingEclipseMethod()),
										LoggingFramework.JAVA_UTIL_LOGGING);

							/**
							 * Print log invocations (Slf4j) whose levels are adjusted due to maximum
							 * distance.
							 */
							for (LogInvocationSlf4j logInvocation : logRejuvenatingProcessor
									.getLogInvsAdjustedByDistanceSlf4j())
								adjustLogLevelByDistancePritner.printRecord(sequence, project.getElementName(),
										logInvocation.getExpression(), logInvocation.getStartPosition(),
										logInvocation.getLogLevel(),
										logInvocation.getEnclosingType().getFullyQualifiedName(),
										Util.getMethodIdentifier(logInvocation.getEnclosingEclipseMethod()),
										LoggingFramework.SLF4J);

						}
					}
					// Clear intermediate data for mylyn-git plug-in.
					MylynGitPredictionProvider.clearMappingData();
				}
			} catch (Exception e) {
				return new Status(IStatus.ERROR, FrameworkUtil.getBundle(this.getClass()).getSymbolicName(),
						"Encountered exception during evaluation", e);
			} finally {
				try {
					if (resultPrinter != null)
						resultPrinter.close();
					if (repoPrinter != null)
						repoPrinter.close();
					if (actionPrinter != null)
						actionPrinter.close();
					if (inputLogInvPrinter != null)
						inputLogInvPrinter.close();
					if (failurePrinter != null)
						failurePrinter.close();
					if (doiPrinter != null)
						doiPrinter.close();
					if (gitCommitPrinter != null)
						gitCommitPrinter.close();
					if (candidatePrinter != null)
						candidatePrinter.close();
					if (notLowerLevelsInCatchBlockPrinter != null)
						notLowerLevelsInCatchBlockPrinter.close();
					if (notLowerLevelsInIfStatementPrinter != null)
						notLowerLevelsInIfStatementPrinter.close();
					if (notLowerLevelsDueToKeywordsPrinter != null)
						notLowerLevelsDueToKeywordsPrinter.close();
					if (notRaiseLevelDueToKeywordsPrinter != null)
						notRaiseLevelDueToKeywordsPrinter.close();
					if (considerIfConditionPrinter != null)
						considerIfConditionPrinter.close();
					if (nonenclosingMethodPrinter != null)
						nonenclosingMethodPrinter.close();
					if (adjustLogLevelByInheritancePrinter != null)
						adjustLogLevelByInheritancePrinter.close();
					if (adjustLogLevelByDistancePritner != null)
						adjustLogLevelByDistancePritner.close();
				} catch (IOException e) {
					return new Status(IStatus.ERROR, FrameworkUtil.getBundle(this.getClass()).getSymbolicName(),
							"Encountered exception during file closing", e);
				}
			}
			LOGGER.info("Evaluation complete.");
			return new Status(IStatus.OK, FrameworkUtil.getBundle(this.getClass()).getSymbolicName(),
					"Evaluation successful.");
		}).schedule();

		return null;
	}

	/**
	 * Get passing log invocations.
	 */
	public HashSet<LogInvocation> getPassingLogInvocation(Set<LogInvocation> candidates) {
		HashSet<LogInvocation> passingLogInvocations = new HashSet<>();
		candidates.forEach(inv -> {
			if (inv.getAction() != null)
				passingLogInvocations.add(inv);
		});
		return passingLogInvocations;
	}

	/**
	 * Get passing log invocations for slf4j.
	 */
	public HashSet<LogInvocationSlf4j> getPassingLogInvocationSlf4j(Set<LogInvocationSlf4j> candidates) {
		HashSet<LogInvocationSlf4j> passingLogInvocations = new HashSet<>();
		candidates.forEach(inv -> {
			if (inv.getAction() != null)
				passingLogInvocations.add(inv);
		});
		return passingLogInvocations;
	}

	/**
	 * @return Candidate logging statements.
	 */
	private Set<LogInvocation> computeCandidateLogs(Set<LogInvocation> logInvocationSet) {
		// Set of candidate log invocations.
		Set<LogInvocation> candidates = new HashSet<LogInvocation>();
		if (!this.isUseLogCategory() && !this.isUseLogCategoryWithConfig())
			candidates.addAll(logInvocationSet);

		if (this.isUseLogCategoryWithConfig()) {
			for (LogInvocation inv : logInvocationSet)
				if (inv.getLogLevel() == null || !inv.getLogLevel().equals(Level.CONFIG))
					candidates.add(inv);
		}

		if (this.isUseLogCategory()) {
			for (LogInvocation inv : logInvocationSet)
				if (inv.getLogLevel() == null || !(inv.getLogLevel().equals(Level.CONFIG)
						|| inv.getLogLevel().equals(Level.WARNING) || inv.getLogLevel().equals(Level.SEVERE)))
					candidates.add(inv);
		}
		return candidates;
	}

	/**
	 * @return Candidate logging statements for Slf4j.
	 */
	private Set<LogInvocationSlf4j> computeCandidateLogsSlf4j(Set<LogInvocationSlf4j> logInvocationSet) {
		// Set of candidate log invocations.
		Set<LogInvocationSlf4j> candidates = new HashSet<LogInvocationSlf4j>();
		logInvocationSet.forEach(log -> {
			if (log.getLogLevel() != null)
				candidates.add(log);
		});

		if (this.isUseLogCategory()) {
			for (LogInvocationSlf4j inv : logInvocationSet)
				if (inv.getLogLevel() != null && !(inv.getLogLevel().equals(org.slf4j.event.Level.WARN)
						|| inv.getLogLevel().equals(org.slf4j.event.Level.ERROR)))
					candidates.add(inv);
		}

		return candidates;
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

	/**
	 * Print DOI boundary Set for Slf4j.
	 */
	private void printBoundarySlf4j(long sequence, String subject, ArrayList<Float> boundarySlf4j,
			CSVPrinter doiPrinter) throws IOException {
		doiPrinter.printRecord(sequence, subject, boundarySlf4j.get(0), boundarySlf4j.get(1),
				org.slf4j.event.Level.TRACE, LoggingFramework.SLF4J);
		doiPrinter.printRecord(sequence, subject, boundarySlf4j.get(1), boundarySlf4j.get(2),
				org.slf4j.event.Level.DEBUG, LoggingFramework.SLF4J);
		doiPrinter.printRecord(sequence, subject, boundarySlf4j.get(2), boundarySlf4j.get(3),
				org.slf4j.event.Level.INFO, LoggingFramework.SLF4J);
		doiPrinter.printRecord(sequence, subject, boundarySlf4j.get(3), boundarySlf4j.get(4),
				org.slf4j.event.Level.WARN, LoggingFramework.SLF4J);
		doiPrinter.printRecord(sequence, subject, boundarySlf4j.get(4), boundarySlf4j.get(5),
				org.slf4j.event.Level.ERROR, LoggingFramework.SLF4J);
	}

	/**
	 * Print DOI boundary set for Slf4j, if the setting "don't consider ERROR/WARN"
	 * is checked.
	 */
	private void printBoundaryLogCategorySlf4j(long sequence, String subject, ArrayList<Float> boundarySlf4j,
			CSVPrinter doiPrinter) throws IOException {
		doiPrinter.printRecord(sequence, subject, boundarySlf4j.get(0), boundarySlf4j.get(1),
				org.slf4j.event.Level.TRACE, LoggingFramework.SLF4J);
		doiPrinter.printRecord(sequence, subject, boundarySlf4j.get(1), boundarySlf4j.get(2),
				org.slf4j.event.Level.DEBUG, LoggingFramework.SLF4J);
		doiPrinter.printRecord(sequence, subject, boundarySlf4j.get(2), boundarySlf4j.get(3),
				org.slf4j.event.Level.INFO, LoggingFramework.SLF4J);
	}

	private void printBoundaryLogCategory(long sequence, String subject, ArrayList<Float> boundary,
			CSVPrinter doiPrinter) throws IOException {
		doiPrinter.printRecord(sequence, subject, boundary.get(0), boundary.get(1), Level.FINEST,
				LoggingFramework.JAVA_UTIL_LOGGING);
		doiPrinter.printRecord(sequence, subject, boundary.get(1), boundary.get(2), Level.FINER,
				LoggingFramework.JAVA_UTIL_LOGGING);
		doiPrinter.printRecord(sequence, subject, boundary.get(2), boundary.get(3), Level.FINE,
				LoggingFramework.JAVA_UTIL_LOGGING);
		doiPrinter.printRecord(sequence, subject, boundary.get(3), boundary.get(4), Level.INFO,
				LoggingFramework.JAVA_UTIL_LOGGING);
	}

	private void printBoundaryWithConfig(long sequence, String subject, ArrayList<Float> boundary,
			CSVPrinter doiPrinter) throws IOException {
		this.printBoundaryLogCategory(sequence, subject, boundary, doiPrinter);
		doiPrinter.printRecord(sequence, subject, boundary.get(4), boundary.get(5), Level.WARNING,
				LoggingFramework.JAVA_UTIL_LOGGING);
		doiPrinter.printRecord(sequence, subject, boundary.get(5), boundary.get(6), Level.SEVERE,
				LoggingFramework.JAVA_UTIL_LOGGING);
	}

	private void printBoundaryDefault(long sequence, String subject, ArrayList<Float> boundary, CSVPrinter doiPrinter)
			throws IOException {
		doiPrinter.printRecord(sequence, subject, boundary.get(0), boundary.get(1), Level.FINEST,
				LoggingFramework.JAVA_UTIL_LOGGING);
		doiPrinter.printRecord(sequence, subject, boundary.get(1), boundary.get(2), Level.FINER,
				LoggingFramework.JAVA_UTIL_LOGGING);
		doiPrinter.printRecord(sequence, subject, boundary.get(2), boundary.get(3), Level.FINE,
				LoggingFramework.JAVA_UTIL_LOGGING);
		doiPrinter.printRecord(sequence, subject, boundary.get(3), boundary.get(4), Level.CONFIG,
				LoggingFramework.JAVA_UTIL_LOGGING);
		doiPrinter.printRecord(sequence, subject, boundary.get(4), boundary.get(5), Level.INFO,
				LoggingFramework.JAVA_UTIL_LOGGING);
		doiPrinter.printRecord(sequence, subject, boundary.get(5), boundary.get(6), Level.WARNING,
				LoggingFramework.JAVA_UTIL_LOGGING);
		doiPrinter.printRecord(sequence, subject, boundary.get(6), boundary.get(7), Level.SEVERE,
				LoggingFramework.JAVA_UTIL_LOGGING);
	}

	@SuppressWarnings("unused")
	private void loadSettings() {
		this.setUseLogCategory(this.getValueOfUseLogCategory());
		this.setUseLogCategoryWithConfig(this.getValueOfUseLogCategoryWithConfig());
		this.setNotLowerLogLevelInCatchBlock(this.getValueOfNotLowerLogLevelInCatchBlock());
		this.setNotLowerLogLevelInIfStatement(this.getValueOfNotLowerLogLevelInIfStatement());
		this.setCheckIfCondition(this.getValueOfCheckIfCondition());
		this.setMaxTransDistance(this.getValueOfMaxTransDistance());
	}

	/**
	 * For automatically loading settings.
	 */
	private void loadSettings(int i) {
		this.setUseLogCategory(this.computeLogCategory(i));
		this.setUseLogCategoryWithConfig(this.computeLogCategoryWithConfig(i));
		this.setNotLowerLogLevelInCatchBlock(this.computeLowerLogLevelInCatchBlock(i));

		this.setNotLowerLogLevelInIfStatement(this.getValueOfNotLowerLogLevelInIfStatement());
		this.setNotLowerLogLevelWithKeywords(this.getValueOfNotLowerLogLevelWithKeywords());
		this.setConsistentLevelInInheritance(this.getValueOfConsistentLogLevel());
		this.setNotRaiseLogLevelWithoutKeywords(this.getValueOfNotRaiseLogLevelWithoutKeywords());
		this.setCheckIfCondition(this.getValueOfCheckIfCondition());
		this.setMaxTransDistance(this.getValueOfMaxTransDistance());

		if (this.isUseLogCategory() && this.isUseLogCategoryWithConfig())
			throw new IllegalStateException("You cannot choose two log categories in the same time");
	}

	private boolean getValueOfConsistentLogLevel() {
		String consistentLogLevel = System.getenv(CONSISTENT_LEVEL_IN_INHERITANCE_KEY);

		if (consistentLogLevel == null)
			return CONSISTENT_LEVEL_IN_INHERITANCE_DEFAULT;
		else
			return Boolean.valueOf(consistentLogLevel);
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

	private boolean getValueOfNotLowerLogLevelInIfStatement() {
		String notLowerLogLevelInIfStatement = System.getenv(NOT_LOWER_LOG_LEVEL_IF_STATEMENT_KEY);

		if (notLowerLogLevelInIfStatement == null)
			return NOT_LOWER_LOG_LEVEL_IF_STATEMENT_DEFAULT;
		else
			return Boolean.valueOf(notLowerLogLevelInIfStatement);
	}

	private boolean getValueOfNotLowerLogLevelWithKeywords() {
		String notLowerLogLevelWithKeywords = System.getenv(NOT_LOWER_LOG_LEVEL_KEYWORDS_KEY);

		if (notLowerLogLevelWithKeywords == null)
			return NOT_LOWER_LOG_LEVEL_KEYWORDS_DEFAULT;
		else
			return Boolean.valueOf(notLowerLogLevelWithKeywords);
	}

	private boolean getValueOfNotRaiseLogLevelWithoutKeywords() {
		String notRaiseLogLevelWithoutKeywords = System.getenv(NOT_RAISE_LOG_LEVEL_KEYWORKS_KEY);

		if (notRaiseLogLevelWithoutKeywords == null)
			return NOT_RAISE_LOG_LEVEL_KEYWORDS_DEFAULT;
		else
			return Boolean.valueOf(notRaiseLogLevelWithoutKeywords);
	}

	private boolean getValueOfCheckIfCondition() {
		String considerIfCondition = System.getenv(CHECK_IF_CONDITION_KEY);

		if (considerIfCondition == null)
			return CHECK_IF_CONDITION_DEFAULT;
		else
			return Boolean.valueOf(considerIfCondition);
	}

	private int getValueOfMaxTransDistance() {
		String maxTransDistance = System.getenv(MAX_TRANSFORMATION_DISTANCE_KEY);

		if (maxTransDistance == null)
			return MAX_TRANSFORMATION_DISTANCE__DEFUALT;
		else
			return Integer.valueOf(maxTransDistance);
	}

	public boolean isUseLogCategory() {
		return useLogCategory;
	}

	private void setUseLogCategory(boolean useLogCategory) {
		this.useLogCategory = useLogCategory;
	}

	public boolean isUseLogCategoryWithConfig() {
		return useLogCategoryWithConfig;
	}

	private void setUseLogCategoryWithConfig(boolean useLogCategoryWithConfig) {
		this.useLogCategoryWithConfig = useLogCategoryWithConfig;
	}

	public boolean isNotLowerLogLevelInCatchBlock() {
		return this.notLowerLogLevelInCatchBlock;
	}

	private void setNotLowerLogLevelInCatchBlock(boolean notLowerLogLevelInCatchBlock) {
		this.notLowerLogLevelInCatchBlock = notLowerLogLevelInCatchBlock;
	}

	private void setCheckIfCondition(boolean checkIfCondition) {
		this.checkIfCondtion = checkIfCondition;
	}

	private void setMaxTransDistance(int maxTransDistance) {
		this.maxTransDistance = maxTransDistance;
	}

	private int getMaxTransDistance() {
		return this.maxTransDistance;
	}

	public boolean isCheckIfCondition() {
		return this.checkIfCondtion;
	}

	private void setNotLowerLogLevelInIfStatement(boolean notLowerLogLevelInIfStatement) {
		this.notLowerLogLevelInIfStatement = notLowerLogLevelInIfStatement;
	}

	public boolean isNotLowerLogLevelInIfStatement() {
		return this.notLowerLogLevelInIfStatement;
	}

	private void setNotLowerLogLevelWithKeywords(boolean notLowerLogLevelWithKeywords) {
		this.notLowerLogLevelWithKeywords = notLowerLogLevelWithKeywords;
	}

	private void setNotRaiseLogLevelWithoutKeywords(boolean notRaiseLogLevelWithoutKeywords) {
		this.notRaiseLogLevelWithoutKeywords = notRaiseLogLevelWithoutKeywords;

	}

	public boolean isNotLowerLogLevelWithKeywords() {
		return this.notLowerLogLevelWithKeywords;
	}

	public boolean isNotRaiseLogLevelWithoutKeywords() {
		return this.notRaiseLogLevelWithoutKeywords;
	}

	public boolean isConsistentLevelInInheritance() {
		return this.consistentLevelInInheritance;
	}

	public void setConsistentLevelInInheritance(boolean consistentLevelInInheritance) {
		this.consistentLevelInInheritance = consistentLevelInInheritance;
	}

}
