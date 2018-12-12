package edu.cuny.hunter.mylyngit.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Test;

import edu.cuny.hunter.mylyngit.core.analysis.GitHistoryAnalyzer;
import edu.cuny.hunter.mylyngit.core.analysis.TypesOfMethodOperations;

public class GitHistoryTest {

	public void helper(String sha, String repoPath, GitAnalysisExpectedResult... expectedResults) throws IOException, GitAPIException {

		GitHistoryAnalyzer gitHistoryAnalyzer = new GitHistoryAnalyzer(sha, new File(repoPath));

		HashMap<String, LinkedList<TypesOfMethodOperations>> methodSignaturesToOps = gitHistoryAnalyzer
				.getMethodSignaturesToOps();

		for (GitAnalysisExpectedResult expectedResult : expectedResults) {
			assertTrue(methodSignaturesToOps.keySet().contains(expectedResult.methodSig));
			LinkedList<TypesOfMethodOperations> methodOps = methodSignaturesToOps.get(expectedResult.methodSig);
			int sizeOfOps = expectedResult.getMethodOperations().size();
			assertSame(sizeOfOps, methodOps.size());

			for (int i = 0; i < sizeOfOps; ++i) {
				assertEquals(expectedResult.getMethodOperations().get(i), methodOps.get(i));
			}

		}

		gitHistoryAnalyzer.clear();

	}

	@Test
	public void testRename() throws IOException, GitAPIException {
		helper("6e9bc600a2ccbd5d5b94e7f768e6ff1b6a33e508", "C:\\Users\\tangy\\log-test-workspace\\htm\\",
				new GitAnalysisExpectedResult("newMethod()", TypesOfMethodOperations.RENAME));

	}
}
