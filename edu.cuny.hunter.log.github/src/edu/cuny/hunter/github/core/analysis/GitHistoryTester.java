package edu.cuny.hunter.github.core.analysis;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.jgit.api.errors.GitAPIException;

public class GitHistoryTester {
	int index = 0;

	@Test
	public void test() throws IOException, GitAPIException {
		GitHistoryAnalyzer.testMethods("2b28383602304c0c6e96fdb95b02d3580203c2c9");
		HashMap<String, LinkedList<TypesOfMethodOperations>> methodSignaturesToOps = GitHistoryAnalyzer.getMethodSignaturesToOps();

		assertEquals("n()", methodSignaturesToOps.keySet().iterator().next());
		assertEquals(TypesOfMethodOperations.RENAME, methodSignaturesToOps.values().iterator().next().get(0));

		GitHistoryAnalyzer.clear();
	}

}
