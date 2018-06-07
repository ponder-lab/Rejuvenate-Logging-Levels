package edu.cuny.hunter.log.ui.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;
import org.eclipse.jdt.ui.tests.refactoring.Java18Setup;
import org.eclipse.jdt.ui.tests.refactoring.RefactoringTest;
import org.eclipse.jdt.ui.tests.refactoring.RefactoringTestSetup;

import edu.cuny.hunter.log.core.analysis.LogAnalyzer;
import edu.cuny.hunter.log.core.analysis.LogInvocation;

@SuppressWarnings("restriction")
public class LogEvolutionRefactoringTest extends RefactoringTest {

	private static final String REFACTORING_PATH = "LogEvolution/";

	private static final String RESOURCE_PATH = "resources";

	@Override
	public String getRefactoringPath() {
		return REFACTORING_PATH;
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

	/**
	 * Compile the test case
	 */
	private static boolean compiles(String source, Path path) throws IOException {
		// Save source in .java file.
		File sourceFile = new File(path.toFile(), "bin/p/A.java");
		sourceFile.getParentFile().mkdirs();
		Files.write(sourceFile.toPath(), source.getBytes());

		// Compile source file.
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		boolean compileSuccess = compiler.run(null, null, null, sourceFile.getPath()) == 0;

		sourceFile.delete();
		return compileSuccess;
	}

	@Override
	protected ICompilationUnit createCUfromTestFile(IPackageFragment pack, String cuName) throws Exception {

		ICompilationUnit unit = super.createCUfromTestFile(pack, cuName);

		if (!unit.isStructureKnown())
			throw new IllegalArgumentException(cuName + " has structural errors.");

		Path directory = Paths.get(unit.getParent().getParent().getParent().getResource().getLocation().toString());

		assertTrue("Should compile the testing cases:", compiles(unit.getSource(), directory));

		return unit;
	}
	
	private void helper(LogInvocationExpectedResult... expectedResults) throws Exception {

		// compute the actual results.
		ICompilationUnit cu = createCUfromTestFile(getPackageP(), "A");

		CompilationUnit unit = RefactoringASTParser.parseWithASTProvider(cu, true, new NullProgressMonitor());

		// ASTNode ast = parser.createAST(new NullProgressMonitor());

		LogAnalyzer logAnalyzer = new LogAnalyzer();
		logAnalyzer.setTest(false);

		unit.accept(logAnalyzer);

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

	/*
	 * This method could fix the issue that the bundle has no entry.
	 */
	@Override
	public String getFileContents(String fileName) throws IOException {
		Path absolutePath = getAbsolutePath(fileName);
		byte[] encoded = Files.readAllBytes(absolutePath);
		return new String(encoded, Charset.defaultCharset());
	}

	private static Path getAbsolutePath(String fileName) {
		Path path = Paths.get(RESOURCE_PATH, fileName);
		Path absolutePath = path.toAbsolutePath();
		return absolutePath;
	}

}
