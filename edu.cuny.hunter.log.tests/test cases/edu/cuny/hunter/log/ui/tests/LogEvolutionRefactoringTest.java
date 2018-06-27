package edu.cuny.hunter.log.ui.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.ui.tests.refactoring.Java18Setup;
import org.eclipse.ltk.core.refactoring.Refactoring;

import edu.cuny.citytech.refactoring.common.tests.RefactoringTest;
import edu.cuny.hunter.log.core.analysis.LogAnalyzer;
import edu.cuny.hunter.log.core.analysis.LogInvocation;
import edu.cuny.hunter.log.core.utils.LoggerNames;

@SuppressWarnings("restriction")
public class LogEvolutionRefactoringTest extends RefactoringTest {

	private static final String REFACTORING_PATH = "LogEvolution/";

	@Override
	protected Logger getLogger() {
		return Logger.getLogger(LoggerNames.LOGGER_NAME);
	}

	@Override
	public String getRefactoringPath() {
		return REFACTORING_PATH;
	}

	@Override
	protected Refactoring getRefactoring(IMethod... methods) throws JavaModelException {
		return null;
	}

	public LogEvolutionRefactoringTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	public static Test suite() {
		return setUpTest(new TestSuite(LogEvolutionRefactoringTest.class));
	}

	public static Test setUpTest(Test test) {
		return new Java18Setup(test);
	}

	@Override
	protected ICompilationUnit createCUfromTestFile(IPackageFragment pack, String cuName) throws Exception {

		ICompilationUnit unit = super.createCUfromTestFile(pack, cuName);

		Path directory = Paths.get(unit.getParent().getParent().getParent().getResource().getLocation().toString());

		assertTrue("Should compile the testing cases:", compiles(unit.getSource(), directory));

		return unit;
	}

	private void helper(LogInvocationExpectedResult... expectedResults) throws Exception {

		// compute the actual results.
		ICompilationUnit cu = createCUfromTestFile(getPackageP(), "A");

		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setResolveBindings(true);
		parser.setSource(cu);

		ASTNode ast = parser.createAST(new NullProgressMonitor());

		LogAnalyzer logAnalyzer = new LogAnalyzer();
		ast.accept(logAnalyzer);

		logAnalyzer.analyze();

		Set<LogInvocation> logInvocationSet = logAnalyzer.getLogInvocationSet();

		assertNotSame("The number of log invocations should not be 0:", 0, logInvocationSet.size());

		HashMap<String, LogInvocation> expressionToInvocation = new HashMap<String, LogInvocation>();
		logInvocationSet.forEach(logInvocation -> {
			expressionToInvocation.put(logInvocation.getExpression().toString(), logInvocation);
		});

		for (LogInvocationExpectedResult result : expectedResults) {
			LogInvocation logInvocation = expressionToInvocation.get(result.getLogExpression());

			assertNotNull("The log expression cannot be detected in the project!", logInvocation);

			assertEquals("Unexpected log level for " + result.getLogExpression(), result.getLogLevel(),
					logInvocation.getLogLevel());
		}

	}

	public void testFindLocations() throws Exception {
		helper(new LogInvocationExpectedResult("LOGGER.info(\"Logger Name: \" + LOGGER.getName())", Level.INFO),
				new LogInvocationExpectedResult("LOGGER.config(\"index is set to \" + index)", Level.CONFIG),
				new LogInvocationExpectedResult("LOGGER.log(Level.SEVERE,\"Exception occur\",ex)", Level.SEVERE),
				new LogInvocationExpectedResult("LOGGER.warning(\"Can cause ArrayIndexOutOfBoundsException\")",
						Level.WARNING));
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	@Override
	public void testPlainMethod() throws Exception {
	}

}
