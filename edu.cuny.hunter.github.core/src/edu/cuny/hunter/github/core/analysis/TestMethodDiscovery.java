package edu.cuny.hunter.github.core.analysis;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jgit.api.errors.GitAPIException;

public class TestMethodDiscovery {
	int index = 0;

	@Test
	public void testStreamAnalyzer() throws IOException, GitAPIException {
		TestGit.testMethods("70d20738eed3619baeba17eb709a9c3653cc1459");
		HashMap<Integer, MethodDeclaration> lineToMethodDeclarationForA = TestGit.getLineToMethodDeclarationForA();
		HashMap<Integer, MethodDeclaration> lineToMethodDeclarationForB = TestGit.getLineToMethodDeclarationForB();
		TestGit.clear();
		int[] linesForA = { 4 };
		String[] methodsForA = { "m()" };
		int[] linesForB = { 4, 6, 7 };
		String[] methodsForB = { "m()", "n()", "n()" };
		index = 0;
		lineToMethodDeclarationForA.forEach((line, methodDeclaration) -> {
			assertEquals(linesForA[index], line.intValue());
			assertEquals(methodsForA[index++], TestGit.getMethodSignature(methodDeclaration));
		});

		index = 0;
		lineToMethodDeclarationForB.forEach((line, methodDeclaration) -> {
			assertEquals(linesForB[index], line.intValue());
			assertEquals(methodsForB[index++], TestGit.getMethodSignature(methodDeclaration));
		});

	}

}
