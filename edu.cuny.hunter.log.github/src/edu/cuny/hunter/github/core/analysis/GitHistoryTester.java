package edu.cuny.hunter.github.core.analysis;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.jgit.api.errors.GitAPIException;

public class GitHistoryTester {

	public void helper(String sha, GitAnalysisExpectedResult... expectedResults) throws IOException, GitAPIException {
		GitHistoryAnalyzer.testMethods(sha);
		HashMap<String, LinkedList<TypesOfMethodOperations>> methodSignaturesToOps = GitHistoryAnalyzer
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

	}

	@Test
	public void test() throws IOException, GitAPIException {
		helper("2b28383602304c0c6e96fdb95b02d3580203c2c9",
				new GitAnalysisExpectedResult("n()", TypesOfMethodOperations.RENAME));
		GitHistoryAnalyzer.clear();
	}

}
