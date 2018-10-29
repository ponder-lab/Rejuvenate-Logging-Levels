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
	public void test() throws IOException, GitAPIException {
		TestGit.testMethods("f9efc371e5db9dedc73a63e455a6f2764a6232eb");
		HashMap<String, LinkedList<TypesOfMethodOperations>> methodSignaturesToOps = TestGit.getMethodSignaturesToOps();

		assertEquals("n()", methodSignaturesToOps.keySet().iterator().next());
		assertEquals(TypesOfMethodOperations.RENAME, methodSignaturesToOps.values().iterator().next().get(0));

		TestGit.clear();
	}

}
