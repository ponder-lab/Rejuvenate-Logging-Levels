package edu.cuny.hunter.log.ui.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.ui.tests.refactoring.Java18Setup;
import org.eclipse.jdt.ui.tests.refactoring.RefactoringTest;
import edu.cuny.hunter.log.core.analysis.LogAnalyzer;

@SuppressWarnings("restriction")
public class LoggingEvolutionTest extends RefactoringTest {
	
	
	private static final String RESOURCE_PATH = "resources";

	public LoggingEvolutionTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	public static Test suite() {
		return setUpTest(new TestSuite(LoggingEvolutionTest.class));
	}

	public static Test setUpTest(Test test) {
		return new Java18Setup(test);
	}

	private static boolean compiles(String source, Path path) throws IOException {
		return compiles(source, path.resolve("bin/p/A.java"));
	}

	@Override
	protected ICompilationUnit createCUfromTestFile(IPackageFragment pack, String className) throws Exception {

		System.out.println(System.getProperty("user.dir") + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

		ICompilationUnit unit = super.createCUfromTestFile(pack, className);

		if (!unit.isStructureKnown())
			throw new IllegalArgumentException(className + " has structural errors.");

		Path directory = Paths.get(unit.getParent().getParent().getResource().getLocation().toString());

		assertTrue("Should compile the testing cases:", compiles(unit.getSource(), directory));

		return unit;
	}

	public void testFindLocations() throws Exception {

		IPackageFragment p = getPackageP();

		// compute the actual results.
		ICompilationUnit cu = createCUfromTestFile(getPackageP(), "A");

		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setResolveBindings(true);
		parser.setSource(cu);

		ASTNode ast = parser.createAST(new NullProgressMonitor());

		LogAnalyzer loggingAnalyer = new LogAnalyzer();
		ast.accept(loggingAnalyer);

		loggingAnalyer.analyze();

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
