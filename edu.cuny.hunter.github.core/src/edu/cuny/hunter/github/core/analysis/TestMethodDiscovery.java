package edu.cuny.hunter.github.core.analysis;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.jgit.api.errors.GitAPIException;

public class TestMethodDiscovery {
	int index = 0;

	@Test
	public void testStreamAnalyzer() throws IOException, GitAPIException {
		TestGit.testMethods("f9efc371e5db9dedc73a63e455a6f2764a6232eb");
		HashMap<String, LinkedList<TypesOfMethodOperations>> methodSignaturesToOps = TestGit.getMethodSignaturesToOps();
		TestGit.clear();

		methodSignaturesToOps.forEach((methodSig, ops) -> {
			assertEquals("n()", methodSig);
			assertEquals(1, ops.size());
			assertEquals(TypesOfMethodOperations.CHANGE, ops.get(0));
		});

	}

}
