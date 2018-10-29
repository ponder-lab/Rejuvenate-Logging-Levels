package edu.cuny.hunter.github.core.analysis;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.HunkHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

@SuppressWarnings("restriction")
public class TestGit {
	// the file index
	private static int commitIndex = 0;

	// Set of method declarations
	// Should we consider the ordering of the methods?
	private static HashSet<MethodDeclaration> methodDeclarationsForA = new HashSet<MethodDeclaration>();
	private static HashSet<MethodDeclaration> methodDeclarationsForB = new HashSet<MethodDeclaration>();

	private static HashMap<MethodDeclaration, Map<Integer, Integer>> methodPositionsForA = new HashMap<>();
	private static HashMap<MethodDeclaration, Map<Integer, Integer>> methodPositionsForB = new HashMap<>();

	private static HashMap<Integer, HashSet<MethodDeclaration>> editToMethodDeclarationForA = new HashMap<>();
	private static HashMap<Integer, HashSet<MethodDeclaration>> editToMethodDeclarationForB = new HashMap<>();

	// A mapping from the method signature to the operations
	private static HashMap<String, LinkedList<TypesOfMethodOperations>> methodSignaturesToOps = new HashMap<>();

	public static void main(String[] args) throws IOException, GitAPIException {

		Repository repo = new FileRepository("C:\\Users\\tangy\\eclipse-workspace\\Java-8-Stream-Refactoring\\.git");

		Git git = new Git(repo);

		Iterable<RevCommit> log = git.log().call();
		RevCommit currentCommit = null;

		int count = 0;
		// from the latest commit to the old commit
		for (RevCommit previousCommit : log) {
			count++;
			if (currentCommit != null) {

				System.out.println("Current commit: " + currentCommit);
				System.out.println("Current log messages: " + currentCommit.getFullMessage());
				System.out.println("#########################################");

				AbstractTreeIterator oldTreeIterator = getCanonicalTreeParser(previousCommit, repo);
				AbstractTreeIterator newTreeIterator = getCanonicalTreeParser(currentCommit, repo);

				// each diff entry is corresponding to a file
				final List<DiffEntry> diffs = git.diff().setOldTree(oldTreeIterator).setNewTree(newTreeIterator).call();

				OutputStream outputStream = new ByteArrayOutputStream();
				try (DiffFormatter formatter = new DiffFormatter(outputStream)) {
					formatter.setRepository(repo);
					formatter.scan(oldTreeIterator, newTreeIterator);

					for (DiffEntry diffEntry : diffs) {

						FileHeader fileHeader = formatter.toFileHeader(diffEntry);
						System.out.println("------------------------------------------");
						// delete a file
						if (diffEntry.getChangeType().name().equals("ADD")) {
							System.out.println("ADD: " + diffEntry.getNewPath());
							System.out.println();
						} else // add a file
						if (diffEntry.getChangeType().name().equals("DELETE")) {
							System.out.print("DELETE: ");
							System.out.println(diffEntry.getOldPath());
							System.out.println();

						} else // modify a file
						if (diffEntry.getChangeType().name().equals("MODIFY")) {

							// Get the file for revision A
							copyHistoricalFile(previousCommit, repo, diffEntry.getOldPath(), "tmp_A_");
							// Get the file for revision B
							copyHistoricalFile(currentCommit, repo, diffEntry.getNewPath(), "tmp_B_");

							// For deleting, get the differences
							computeMethodPositions(methodDeclarationsForA, methodPositionsForA);

							// For adding, get the differences
							computeMethodPositions(methodDeclarationsForB, methodPositionsForB);

							System.out.println("MODIFY: " + diffEntry.getNewPath());
							List<? extends HunkHeader> hunks = fileHeader.getHunks();
							int editId = 0;
							for (HunkHeader hunk : hunks) {
								EditList editList = hunk.toEditList();
								if (!editList.isEmpty()) {
									// For each pair of edit
									for (Edit edit : editList) {
										editId++;
										mapEditToMethod(editId, edit.getBeginA(), edit.getEndA(), methodPositionsForA,
												editToMethodDeclarationForA);
										mapEditToMethod(editId, edit.getBeginB(), edit.getEndB(), methodPositionsForB,
												editToMethodDeclarationForB);

									}
									;
								}

							}

							computeMethodChanges();

							clear();

							commitIndex++;
							System.out.println();

						} else if (diffEntry.getChangeType().name().equals("RENAME")) {
							System.out.println("RENAME: ");
							System.out.println("Oldpath: " + diffEntry.getOldPath());
							System.out.println("Newpath: " + diffEntry.getNewPath());
							System.out.println();
						} else if (diffEntry.getChangeType().name().equals("COPY")) {
							System.out.println("COPY: ");
							System.out.println("Oldpath: " + diffEntry.getOldPath());
							System.out.println("Newpath: " + diffEntry.getNewPath());
							System.out.println();
						}

					}
				}

			}
			System.out.println("#######################################");
			currentCommit = previousCommit;

			// For testing here!!!
			if (count == 10)
				break;

			clearFiles(new File("").getAbsoluteFile());
		}

		git.close();
	}

	/**
	 * Check whether a directory is a temporary directory.
	 */
	private static boolean isTemporaryDirectory(File directory) {
		if (directory.getName().length() >= 7 && directory.getName().substring(0, 4).equals("tmp_"))
			return true;
		else
			return false;
	}

	/**
	 * Remove all temporary files
	 */
	private static boolean clearFiles(File directory) {
		if (directory.exists()) {
			File[] files = directory.listFiles();
			if (null != files) {
				for (int i = 0; i < files.length; i++) {
					if (files[i].isDirectory()) {
						// Need to clear the content of the temporary directory first before remove it
						if (isTemporaryDirectory(files[i]))
							clearFiles(files[i]);
					} else if (isTemporaryDirectory(directory)) {
						files[i].delete();
					}
				}
			}
		}
		return (directory.delete());
	}

	public static void testMethods(String sha) throws IOException, GitAPIException {

		Repository repo = new FileRepository("C:\\Users\\tangy\\logging-workspace\\Log-Git-Test\\.git");

		Git git = new Git(repo);

		ObjectId currentCommitId = ObjectId.fromString(sha);
		RevWalk revWalk = new RevWalk(repo);
		RevCommit currentCommit = revWalk.parseCommit(currentCommitId);
		RevTree tree = currentCommit.getTree();

		RevCommit previousCommit = currentCommit.getParent(0);
		previousCommit = revWalk.parseCommit(previousCommit.getId());
		RevTree previousTree = previousCommit.getTree();
		revWalk.close();

		System.out.println("Current commit: " + currentCommit);
		System.out.println("Current log messages: " + currentCommit.getFullMessage());
		System.out.println("------------------------------------------");

		AbstractTreeIterator oldTreeIterator = getCanonicalTreeParser(previousCommit, repo);
		AbstractTreeIterator newTreeIterator = getCanonicalTreeParser(currentCommit, repo);

		// each diff entry is corresponding to a file
		final List<DiffEntry> diffs = git.diff().setOldTree(oldTreeIterator).setNewTree(newTreeIterator).call();

		OutputStream outputStream = new ByteArrayOutputStream();
		try (DiffFormatter formatter = new DiffFormatter(outputStream)) {
			formatter.setRepository(repo);
			formatter.scan(oldTreeIterator, newTreeIterator);

			// only get the first file.
			DiffEntry diffEntry = diffs.get(0);

			FileHeader fileHeader = formatter.toFileHeader(diffEntry);

			// Get the file for revision A
			copyHistoricalFile(repo, diffEntry.getOldPath(), "tmp_A_", previousTree);
			// Get the file for revision B
			copyHistoricalFile(repo, diffEntry.getNewPath(), "tmp_B_", tree);

			// For deleting, get the differences
			computeMethodPositions(methodDeclarationsForA, methodPositionsForA);

			// For adding, get the differences
			computeMethodPositions(methodDeclarationsForB, methodPositionsForB);

			System.out.println("MODIFY: " + diffEntry.getNewPath());
			List<? extends HunkHeader> hunks = fileHeader.getHunks();
			int editId = 0;
			for (HunkHeader hunk : hunks) {
				EditList editList = hunk.toEditList();
				if (!editList.isEmpty()) {
					// For each pair of edit
					for (Edit edit : editList) {
						editId++;
						mapEditToMethod(editId, edit.getBeginA(), edit.getEndA(), methodPositionsForA,
								editToMethodDeclarationForA);
						mapEditToMethod(editId, edit.getBeginB(), edit.getEndB(), methodPositionsForB,
								editToMethodDeclarationForB);

					}
					;
				}

			}

			computeMethodChanges();

			commitIndex++;
			System.out.println();

		}

		git.close();
	}

	/**
	 * Clear all sets and maps.
	 */
	public static void clear() {
		methodDeclarationsForA.clear();
		methodDeclarationsForB.clear();
		methodPositionsForA.clear();
		methodPositionsForB.clear();
		editToMethodDeclarationForA.clear();
		editToMethodDeclarationForB.clear();
		methodSignaturesToOps.clear();
	}

	/**
	 * Compute the start position and end position of a method.
	 */
	private static void computeMethodPositions(HashSet<MethodDeclaration> methodDeclarations,
			HashMap<MethodDeclaration, Map<Integer, Integer>> methodPositions) {

		for (MethodDeclaration methodDeclaration : methodDeclarations) {
			int start = getStartingLineNumber(methodDeclaration);
			int end = getEndingLineNumber(methodDeclaration);
			methodPositions.put(methodDeclaration, Collections.singletonMap(start, end));
		}
	}

	/**
	 * Map line number to method
	 */
	private static void mapEditToMethod(int editId, int editStart, int editEnd,
			HashMap<MethodDeclaration, Map<Integer, Integer>> methodPositions,
			HashMap<Integer, HashSet<MethodDeclaration>> editToMethodDeclaration) {
		for (int line = editStart + 1; line <= editEnd; ++line) {
			addCorrespondingMethod(editId, methodPositions, editToMethodDeclaration, line);
		}
		if (editStart == editEnd)
			addCorrespondingMethod(editId, methodPositions, editToMethodDeclaration, editEnd + 1);

	}

	// Given a line, add its corresponding method into editToMethodDeclaration
	private static void addCorrespondingMethod(int editId,
			HashMap<MethodDeclaration, Map<Integer, Integer>> methodPositions,
			HashMap<Integer, HashSet<MethodDeclaration>> editToMethodDeclaration, int line) {
		methodPositions.forEach((methodDeclaration, positions) -> {
			int start = positions.keySet().iterator().next();
			int end = positions.values().iterator().next();

			if (line >= start && line <= end) {
				HashSet<MethodDeclaration> methodDeclarations = new HashSet<>();
				if (!editToMethodDeclaration.containsKey(editId)) {
					methodDeclarations.add(methodDeclaration);
					editToMethodDeclaration.put(editId, methodDeclarations);
				} else {
					methodDeclarations = editToMethodDeclaration.get(editId);
					if (!methodDeclarations.contains(methodDeclaration)) {
						methodDeclarations.add(methodDeclaration);
						editToMethodDeclaration.put(editId, methodDeclarations);
					}
				}
			}
		});
	}

	// Return a method signature
	public static String getMethodSignature(MethodDeclaration methodDeclaration) {
		String signature = "";
		signature += methodDeclaration.getName() + "(";

		Iterator<SingleVariableDeclaration> parameterIterator = methodDeclaration.parameters().iterator();
		if (parameterIterator.hasNext())
			signature += parameterIterator.next().getType();
		while (parameterIterator.hasNext()) {
			signature += ", " + parameterIterator.next().getType();
		}
		signature += ")";
		return signature;
	}

	public static int getStartingLineNumber(MethodDeclaration methodDeclaration) {
		return (((CompilationUnit) methodDeclaration.getRoot()).getLineNumber(methodDeclaration.getStartPosition()));
	}

	public static int getEndingLineNumber(MethodDeclaration methodDeclaration) {
		return (((CompilationUnit) methodDeclaration.getRoot())
				.getLineNumber(methodDeclaration.getStartPosition() + methodDeclaration.getLength()));
	}

	private static AbstractTreeIterator getCanonicalTreeParser(ObjectId commitId, Repository repo) throws IOException {
		try (RevWalk walk = new RevWalk(repo)) {
			RevCommit commit = walk.parseCommit(commitId);
			ObjectId treeId = commit.getTree().getId();
			try (ObjectReader reader = repo.newObjectReader()) {
				return new CanonicalTreeParser(null, reader, treeId);
			}
		}
	}

	/**
	 * Given the commit, repository and the path of the file, get the file, and copy
	 * it into a new file.
	 */
	@SuppressWarnings("resource")
	private static void copyHistoricalFile(RevCommit commit, Repository repo, String path, String newDirectory)
			throws MissingObjectException, IncorrectObjectTypeException, CorruptObjectException, IOException {
		RevTree tree = commit.getTree();
		TreeWalk treeWalk = new TreeWalk(repo);
		treeWalk.addTree(tree);
		treeWalk.setRecursive(true);
		treeWalk.setFilter(PathFilter.create(path));
		if (!treeWalk.next()) {
			return;
		}
		ObjectId objectId = treeWalk.getObjectId(0);
		ObjectLoader loader = repo.open(objectId);
		copyToFile(loader, path, newDirectory);
	}

	/**
	 * Given the commit, repository and the path of the file, get the file, and copy
	 * it into a new file.
	 */
	@SuppressWarnings("resource")
	private static void copyHistoricalFile(Repository repo, String path, String newDirectory, RevTree tree)
			throws MissingObjectException, IncorrectObjectTypeException, CorruptObjectException, IOException {
		TreeWalk treeWalk = new TreeWalk(repo);
		treeWalk.addTree(tree);
		treeWalk.setRecursive(true);
		treeWalk.setFilter(PathFilter.create(path));
		if (!treeWalk.next()) {
			return;
		}
		ObjectId objectId = treeWalk.getObjectId(0);
		ObjectLoader loader = repo.open(objectId);
		copyToFile(loader, path, newDirectory);
	}

	private static void copyToFile(ObjectLoader loader, String path, String newDirectory) throws IOException {
		// Get the empty or existing file in the new directory.
		File file = getFile(path, newDirectory);
		if (file == null)
			return;
		// Copy the file content into the new file.
		FileOutputStream fileOutputStream = new FileOutputStream(file.getAbsolutePath(), false);
		loader.copyTo(fileOutputStream);
		fileOutputStream.close();

		// Parse the java file.
		String fileContent = new BufferedReader(new InputStreamReader(loader.openStream())).lines()
				.collect(Collectors.joining("\n"));
		if (!fileContent.isEmpty())
			parseJavaFile(file, fileContent, newDirectory);
	}

	/**
	 * Get the file in the new directory.
	 */
	private static File getFile(String path, String newDirectory) throws IOException {
		String fileName = getJavaFileName(path);
		if (fileName == null)
			return null;
		// ALL files are moved into a new directory
		File file = new File(newDirectory + commitIndex + "/" + fileName);

		if (!file.exists()) {
			if (!file.getParentFile().exists())
				file.getParentFile().mkdir();

			file.createNewFile();
		}
		return file;
	}

	/**
	 * Get a java file name. If it returns null, the file is not a Java file.
	 */
	private static String getJavaFileName(String path) {
		// Only need to consider java files here.
		if (!path.contains(".java"))
			return null;

		// I would like to move the files using the same file name
		int index = path.lastIndexOf("/");
		String fileName;
		if (index != -1)
			fileName = path.substring(index + 1);
		else
			fileName = path;

		return fileName;
	}

	/**
	 * Check whether two methods have the same method name.
	 */
	private static boolean isSameMethodName(MethodDeclaration methodA, MethodDeclaration methodB) {
		if (methodA.getName().toString().equals(methodB.getName().toString()))
			return true;
		else
			return false;
	}

	/**
	 * Return a set of A-B.
	 */
	private static HashSet<String> getAdditionalMethods(HashSet<String> methodSignaturesSetA,
			HashSet<String> methodSignaturesSetB) {
		HashSet<String> additionalMethods = new HashSet<>();
		additionalMethods.addAll(methodSignaturesSetA);
		additionalMethods.removeAll(methodSignaturesSetB);
		return additionalMethods;
	}

	/**
	 * Given a set of method signatures, get its corresponding set of method
	 * declarations.
	 */
	private static HashSet<MethodDeclaration> getSetOfMethodDeclaration(HashSet<MethodDeclaration> methodsDecs,
			HashSet<String> methodSigs) {
		HashSet<MethodDeclaration> methodDeclarations = new HashSet<>();
		methodsDecs.forEach(method -> {
			String methodSig = getMethodSignature(method);
			if (methodSigs.contains(methodSig))
				methodDeclarations.add(method);
		});
		return methodDeclarations;
	}

	/**
	 * The core method to return a list of methods and their operation types for one
	 * file.
	 */
	private static void computeMethodChanges() {
		HashSet<String> methodSignaturesForEditsA = getMethodSignatures(editToMethodDeclarationForA.values());
		HashSet<String> methodSignaturesForEditsB = getMethodSignatures(editToMethodDeclarationForB.values());

		HashSet<String> additionalMethodInB = getAdditionalMethods(methodSignaturesForEditsB,
				methodSignaturesForEditsA);

		HashSet<MethodDeclaration> additionalMethodDecInB = getSetOfMethodDeclaration(methodDeclarationsForB,
				additionalMethodInB);

		// Iterate over edits. Each edit should be counted as an event
		editToMethodDeclarationForA.forEach((editIdForA, methodsInOneEditA) -> {

			for (MethodDeclaration methodForA : methodsInOneEditA) {
				String methodSig = getMethodSignature(methodForA);

				// Method body is modified or rename parameters
				if (methodSignaturesForEditsB.contains(methodSig)) {
					putIntoMethodToOps(methodSignaturesToOps, methodSig, TypesOfMethodOperations.CHANGE);
				} else {

					// Keep the method name same, but add/delete the parameter or change the type of
					// the parameter
					MethodDeclaration targetMethodDec = getMethodWithParameterChanged(methodForA,
							additionalMethodDecInB);
					if (targetMethodDec != null) {
						process(targetMethodDec, additionalMethodDecInB, additionalMethodInB,
								TypesOfMethodOperations.CHANGEPARAMETER);
						break;
					}

					// If the method is renamed
					targetMethodDec = getMethodWithMethodNameChanged(methodForA, additionalMethodDecInB);
					if (targetMethodDec != null) {
						process(targetMethodDec, additionalMethodDecInB, additionalMethodInB,
								TypesOfMethodOperations.RENAME);
						break;
					}

					putIntoMethodToOps(methodSignaturesToOps, methodSig, TypesOfMethodOperations.DELETE);
				}

			}
			// edits
		});

		additionalMethodDecInB.forEach(methodDec -> {
			putIntoMethodToOps(methodSignaturesToOps, getMethodSignature(methodDec), TypesOfMethodOperations.ADD);
		});

		methodSignaturesToOps.forEach((methodSig, ops) -> {
			System.out.println("------------------------------------------");
			System.out.println(methodSig);
			System.out.println(ops);
		});
		System.out.println("------------------------------------------");

	}

	/**
	 * Store target method declaration and remove it in the difference set.
	 */
	private static void process(MethodDeclaration targetMethodDec, HashSet<MethodDeclaration> additionalMethodDecInB,
			HashSet<String> additionalMethodInB, TypesOfMethodOperations op) {
		putIntoMethodToOps(methodSignaturesToOps, getMethodSignature(targetMethodDec), op);
		String tagetMethodSig = getMethodSignature(targetMethodDec);
		additionalMethodDecInB.remove(targetMethodDec);
		additionalMethodInB.remove(tagetMethodSig);
	}

	/**
	 * Check whether the two methods have the same parameter types.
	 */
	private static boolean isSameParameterType(MethodDeclaration methodA, MethodDeclaration methodB) {
		List<SingleVariableDeclaration> parametersA = methodA.parameters();
		List<SingleVariableDeclaration> parametersB = methodB.parameters();
		int index = 0;
		for (SingleVariableDeclaration parameterA : parametersA) {
			SingleVariableDeclaration parameterB = parametersB.get(index++);
			if (!parameterA.getType().toString().equals(parameterB.getType().toString()))
				return false;
		}
		return true;
	}

	/**
	 * Get the method when the number of parameter is changed or parameters' type is
	 * changed.
	 */
	private static MethodDeclaration getMethodWithParameterChanged(MethodDeclaration methodForA,
			HashSet<MethodDeclaration> additionalMethodDecInB) {
		// Parameters are modified
		MethodDeclaration targetMethodDec = null;
		float similarity = -1;
		for (MethodDeclaration methodForB : additionalMethodDecInB) {
			if (isSameMethodName(methodForA, methodForB)) {
				float currentSimilarity = computeSimilarity(methodForA, methodForB);
				if (currentSimilarity > similarity) {
					similarity = currentSimilarity;
					targetMethodDec = methodForB;
				}
			}
		}
		return targetMethodDec;
	}

	/**
	 * Get the method when the method is renamed.
	 */
	private static MethodDeclaration getMethodWithMethodNameChanged(MethodDeclaration methodForA,
			HashSet<MethodDeclaration> additionalMethodDecInB) {
		// Parameters are modified
		MethodDeclaration targetMethodDec = null;
		float similarity = -1;
		for (MethodDeclaration methodForB : additionalMethodDecInB) {
			if (isSameParameterType(methodForA, methodForB)) {
				float currentSimilarity = computeSimilarity(methodForA, methodForB);
				if (currentSimilarity > similarity) {
					similarity = currentSimilarity;
					targetMethodDec = methodForB;
				}
			}
		}
		return targetMethodDec;
	}

	/**
	 * Compute the degree that two methods are similar.
	 */
	private static float computeSimilarity(MethodDeclaration methodA, MethodDeclaration methodB) {
		int methodLengthA = getEndingLineNumber(methodA) - getStartingLineNumber(methodA);
		int methodLengthB = getEndingLineNumber(methodB) - getStartingLineNumber(methodB);

		if (methodLengthA == 0 || methodLengthB == 0)
			return -1;

		if (methodLengthA >= methodLengthB)
			return methodLengthB / methodLengthA;
		else
			return methodLengthA / methodLengthB;
	}

	/**
	 * Add an element into the map methodSignaturesToOps
	 */
	private static void putIntoMethodToOps(HashMap<String, LinkedList<TypesOfMethodOperations>> map, String key,
			TypesOfMethodOperations element) {
		LinkedList<TypesOfMethodOperations> list = new LinkedList<>();
		if (map.containsKey(key)) {
			list = map.get(key);
		}
		list.add(element);
		map.put(key, list);
	}

	/**
	 * Returns all method signatures for all edits in one file.
	 */
	private static HashSet<String> getMethodSignatures(Collection<HashSet<MethodDeclaration>> methodDeclarations) {
		HashSet<String> methodSignatures = new HashSet<String>();
		methodDeclarations.forEach(methodDecSet -> {
			methodDecSet.forEach(methodDec -> {
				methodSignatures.add(getMethodSignature(methodDec));
			});
		});
		return methodSignatures;
	}

	/**
	 * Parse a Java file, and let visitor to visit declaring methods.
	 */
	private static void parseJavaFile(File file, String fileContent, String newDirectory) throws IOException {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setResolveBindings(true);
		parser.setSource(fileContent.toCharArray());

		final CompilationUnit cu = (CompilationUnit) parser.createAST(new NullProgressMonitor());

		cu.accept(new ASTVisitor() {
			@Override
			public boolean visit(MethodDeclaration methodDeclaration) {
				if (newDirectory.equals("tmp_A_"))
					methodDeclarationsForA.add(methodDeclaration);
				else
					methodDeclarationsForB.add(methodDeclaration);
				return true;
			}
		});
	}

	public static HashMap<String, LinkedList<TypesOfMethodOperations>> getMethodSignaturesToOps() {
		return methodSignaturesToOps;
	}

}
