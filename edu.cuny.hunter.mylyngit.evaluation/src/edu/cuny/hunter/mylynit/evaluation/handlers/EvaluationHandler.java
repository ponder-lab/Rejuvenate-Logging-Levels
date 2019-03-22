package edu.cuny.hunter.mylynit.evaluation.handlers;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.internal.ui.util.SelectionUtil;
import org.eclipse.ui.handlers.HandlerUtil;

import edu.cuny.hunter.mylyngit.core.analysis.MylynGitPredictionProvider;
import edu.cuny.hunter.mylyngit.core.utils.Util;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jgit.api.errors.GitAPIException;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
@SuppressWarnings("restriction")
public class EvaluationHandler extends AbstractHandler {

	private static final Logger LOGGER = Logger.getLogger(Util.LOGGER_NAME);

	private static final String EVALUATION_PROPERTIES_FILE_NAME = "eval.properties";

	private static final String N_TO_USE_FOR_COMMITS_KEY = "NToUseForCommits";
	private static final int N_TO_USE_FOR_COMMITS_DEFAULT = 100;

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
			CSVPrinter resultPrinter;
			try {
				resultPrinter = this.createCSVPrinter("DOI_Values.csv",
						new String[] { "subject", "N for commits", "TypeFQN", "methods", "DOI values" });

				for (IJavaProject javaProject : javaProjectList) {
					List<Integer> nsToUse = Util.getNToUseForCommits(javaProject, N_TO_USE_FOR_COMMITS_KEY,
							N_TO_USE_FOR_COMMITS_DEFAULT, EVALUATION_PROPERTIES_FILE_NAME);
					for (Integer NToUseForCommit : nsToUse){

					MylynGitPredictionProvider provider = new MylynGitPredictionProvider(NToUseForCommit);
					provider.processOneProject(javaProject);
					HashSet<MethodDeclaration> methods = provider.getMethods();
					for (MethodDeclaration m : methods) {
						IMethodBinding methodBinding = m.resolveBinding();
						if (methodBinding != null) {
							IMethod iMethod = (IMethod) methodBinding.getJavaElement();
							// Work around DOI values
							float doiValue = Util.getDOIValue(iMethod);
							if (!(Float.compare(0, doiValue) == 0)) {
								resultPrinter.printRecord(javaProject.getElementName(), NToUseForCommit,
										((IType) methodBinding.getDeclaringClass().getJavaElement())
												.getFullyQualifiedName(),
										Util.getMethodSignature(m), doiValue);
							}
						}
					}
					MylynGitPredictionProvider.clearTaskContext();
				}
				}
				MylynGitPredictionProvider.clearMappingData();
				resultPrinter.close();
			} catch (IOException e) {
				LOGGER.info("Cannot print info correctly or cannot process git commits.");
			} catch (GitAPIException e) {
				LOGGER.info("Cannot get valid git object or process commits.");
			} catch (JavaModelException e) {
				LOGGER.warning("Cannot find evaluation property file!");
			}

		}

		return null;
	}

	public CSVPrinter createCSVPrinter(String fileName, String[] header) throws IOException {
		return new CSVPrinter(new FileWriter(fileName, true), CSVFormat.EXCEL.withHeader(header));
	}
}
