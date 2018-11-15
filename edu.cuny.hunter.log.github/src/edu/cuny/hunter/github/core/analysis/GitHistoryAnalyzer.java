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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
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
import org.eclipse.jgit.api.errors.NoHeadException;
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
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import edu.cuny.hunter.github.core.utils.Util;
import edu.cuny.hunter.github.core.utils.Edge;
import edu.cuny.hunter.github.core.utils.GitMethod;
import edu.cuny.hunter.github.core.utils.Graph;
import edu.cuny.hunter.github.core.utils.Vertex;

@SuppressWarnings("restriction")
public class GitHistoryAnalyzer {

	private static final Logger LOGGER = Logger.getLogger(Util.LOGGER_NAME);

	// Set of method declarations
	private static HashSet<MethodDeclaration> methodDeclarationsForA = new HashSet<MethodDeclaration>();
	private static HashSet<MethodDeclaration> methodDeclarationsForB = new HashSet<MethodDeclaration>();

	private static HashMap<MethodDeclaration, Map<Integer, Integer>> methodPositionsForA = new HashMap<>();
	private static HashMap<MethodDeclaration, Map<Integer, Integer>> methodPositionsForB = new HashMap<>();

	private static HashMap<Integer, HashSet<MethodDeclaration>> editToMethodDeclarationForA = new HashMap<>();
	private static HashMap<Integer, HashSet<MethodDeclaration>> editToMethodDeclarationForB = new HashMap<>();

	// A mapping from the method signature to the operations
	private static HashMap<String, LinkedList<TypesOfMethodOperations>> methodSignaturesToOps = new HashMap<>();

	private static LinkedList<GitMethod> gitMethods = new LinkedList<>();

	// The old method in the revision A and the new method in the revision B
	private static HashMap<String, String> methodToMethod = new HashMap<>();

	private static Graph renaming = new Graph();

	// the file index
	static int commitIndex = 0;

	static LinkedList<RevCommit> commitList = new LinkedList<>();

	/**
	 * Given the repo path, compute all method operations (e.g., delete a method)
	 * for all commits.
	 * 
	 * Example input:
	 * "C:\\Users\\tangy\\eclipse-workspace\\Java-8-Stream-Refactoring\\.git"
	 */
	public static void processGitHistory(File repoFile) {

		Git git;
		try {
			git = preProcessGitHistory(repoFile);
			RevCommit previousCommit = null;
			String filePath = null;

			// from the earliest commit to the current commit
			for (RevCommit currentCommit : commitList) {

				processOneCommit(currentCommit, previousCommit, git, filePath);
				previousCommit = currentCommit;

				commitIndex++;

				clearFiles(new File("").getAbsoluteFile());

				//// test
				if (commitIndex >= 20)
					break;
			}

			git.close();
		} catch (IOException | GitAPIException e) {
			LOGGER.warning("Cannot process git commits!");
		}

		renaming.printGraph();
	}

	/**
	 * Process one git commit
	 * 
	 * @throws IOException
	 * @throws GitAPIException
	 */
	public static void processOneCommit(RevCommit currentCommit, RevCommit previousCommit, Git git, String filePath)
			throws IOException, GitAPIException {
		AbstractTreeIterator oldTreeIterator;
		if (previousCommit != null) {
			oldTreeIterator = getCanonicalTreeParser(previousCommit, git.getRepository());
		} else {
			oldTreeIterator = new EmptyTreeIterator();
		}
		AbstractTreeIterator newTreeIterator = getCanonicalTreeParser(currentCommit, git.getRepository());

		// Each diff entry is corresponding to a file
		final List<DiffEntry> diffs = git.diff().setOldTree(oldTreeIterator).setNewTree(newTreeIterator).call();

		OutputStream outputStream = new ByteArrayOutputStream();
		try (DiffFormatter formatter = new DiffFormatter(outputStream)) {
			formatter.setRepository(git.getRepository());
			formatter.scan(oldTreeIterator, newTreeIterator);

			for (DiffEntry diffEntry : diffs) {

				switch (diffEntry.getChangeType().name()) {
				case "ADD":
					filePath = addFile(currentCommit, git.getRepository(), diffEntry);
					break;
				case "DELETE":
					filePath = deleteFile(previousCommit, git.getRepository(), diffEntry);
					break;
				case "MODIFY":
					filePath = modifyFile(currentCommit, previousCommit, git.getRepository(), diffEntry, formatter);
					break;
				case "RENAME":
				case "COPY":
					filePath = renameOrCopyFile(currentCommit, git.getRepository(), diffEntry);
					break;
				default:
					break;
				}

				storeAllMethodOps(currentCommit, filePath, diffEntry.getChangeType().name());
				clear();
			}
		}
	}

	public static LinkedList<GitMethod> getGitMethods() {
		return gitMethods;
	}

	/**
	 * Rename or copy a file in a commit.
	 */
	public static String renameOrCopyFile(RevCommit currentCommit, Repository repo, DiffEntry diffEntry)
			throws MissingObjectException, IncorrectObjectTypeException, CorruptObjectException, IOException {
		copyHistoricalFile(currentCommit, repo, diffEntry.getNewPath(), "tmp_B_");
		methodDeclarationsForB.forEach(methodDec -> {
			// add vertex
			Vertex vertex1 = new Vertex(Util.getMethodSignature(methodDec), diffEntry.getOldPath());
			renaming.addVertex(vertex1);
			// add vertex
			Vertex vertex2 = new Vertex(Util.getMethodSignature(methodDec), diffEntry.getNewPath());
			renaming.addVertex(vertex2);
			// add edge
			renaming.addEdge(new Edge(vertex1, vertex2));
		});
		return diffEntry.getNewPath();
	}

	public static Graph getRenaming() {
		return renaming;
	}

	/**
	 * Get git and git commits.
	 */
	private static Git preProcessGitHistory(File repoFile) throws IOException, NoHeadException, GitAPIException {

		Git git = Git.init().setDirectory(repoFile).call();

		Iterable<RevCommit> log = git.log().call();

		for (RevCommit commit : log) {
			commitList.addFirst(commit);
		}
		
		return git;
	}

	private static String modifyFile(RevCommit currentCommit, RevCommit previousCommit, Repository repo,
			DiffEntry diffEntry, DiffFormatter formatter)
			throws MissingObjectException, IncorrectObjectTypeException, CorruptObjectException, IOException {

		FileHeader fileHeader = formatter.toFileHeader(diffEntry);

		// Get the file for revision A
		copyHistoricalFile(previousCommit, repo, diffEntry.getOldPath(), "tmp_A_");
		// Get the file for revision B
		copyHistoricalFile(currentCommit, repo, diffEntry.getNewPath(), "tmp_B_");

		// For deleting, get the differences
		computeMethodPositions(methodDeclarationsForA, methodPositionsForA);

		// For adding, get the differences
		computeMethodPositions(methodDeclarationsForB, methodPositionsForB);

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

		computeMethodChanges(diffEntry.getNewPath());

		return diffEntry.getNewPath();
	}

	/**
	 * Add a file in a commit.
	 */
	private static String addFile(RevCommit currentCommit, Repository repo, DiffEntry diffEntry)
			throws MissingObjectException, IncorrectObjectTypeException, CorruptObjectException, IOException {
		// Get the file for revision B
		copyHistoricalFile(currentCommit, repo, diffEntry.getNewPath(), "tmp_B_");
		methodDeclarationsForB.forEach(methodDec -> {
			putIntoMethodToOps(methodSignaturesToOps, Util.getMethodSignature(methodDec), TypesOfMethodOperations.ADD);
		});
		return diffEntry.getNewPath();
	}

	/**
	 * Delete a file in a commit;
	 */
	private static String deleteFile(RevCommit previousCommit, Repository repo, DiffEntry diffEntry)
			throws MissingObjectException, IncorrectObjectTypeException, CorruptObjectException, IOException {
		// Get the file for revision A
		copyHistoricalFile(previousCommit, repo, diffEntry.getOldPath(), "tmp_A_");
		methodDeclarationsForA.forEach(methodDec -> {
			putIntoMethodToOps(methodSignaturesToOps, Util.getMethodSignature(methodDec),
					TypesOfMethodOperations.DELETE);

			Set<Vertex> exitVertices = renaming.getExitVertices();
			Vertex entry = null;
			for (Vertex v : exitVertices) {
				if (v.getFile().equals(diffEntry.getOldPath())
						&& v.getMethod().equals(Util.getMethodSignature(methodDec))) {
					entry = v.getHead();
					break;
				}
			}
			while (entry != null) {
				Vertex tmp = entry;
				entry = entry.getNextVertex();
				renaming.removeVertex(tmp);
			}
		});
		return diffEntry.getOldPath();
	}

	/**
	 * Print all method operations into CSV file
	 */
	private static void storeAllMethodOps(RevCommit commit, String path, String fileOp) {
		methodSignaturesToOps.forEach((methodSig, ops) -> {
			for (TypesOfMethodOperations op : ops)
				gitMethods.add(new GitMethod(methodSig, op, path, fileOp, commitIndex, commit.name()));
		});
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

		RevCommit previousCommit = currentCommit.getParent(0);
		previousCommit = revWalk.parseCommit(previousCommit.getId());
		revWalk.close();

		String filePath = null;

		processOneCommit(currentCommit, previousCommit, git, filePath);

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
			String methodSig = Util.getMethodSignature(method);
			if (methodSigs.contains(methodSig))
				methodDeclarations.add(method);
		});
		return methodDeclarations;
	}

	/**
	 * The core method to return a list of methods and their operation types for one
	 * file.
	 */
	private static void computeMethodChanges(String file) {
		HashSet<String> methodSignaturesForEditsA = getMethodSignatures(editToMethodDeclarationForA.values());
		HashSet<String> methodSignaturesForEditsB = getMethodSignatures(editToMethodDeclarationForB.values());

		HashSet<String> additionalMethodInB = getAdditionalMethods(methodSignaturesForEditsB,
				methodSignaturesForEditsA);

		HashSet<MethodDeclaration> additionalMethodDecInB = getSetOfMethodDeclaration(methodDeclarationsForB,
				additionalMethodInB);

		// Iterate over edits. Each edit should be counted as an event
		editToMethodDeclarationForA.forEach((editIdForA, methodsInOneEditA) -> {

			for (MethodDeclaration methodForA : methodsInOneEditA) {
				String methodSig = Util.getMethodSignature(methodForA);

				// Modify method body, or rename parameters
				if (methodSignaturesForEditsB.contains(methodSig)) {
					putIntoMethodToOps(methodSignaturesToOps, methodSig, TypesOfMethodOperations.CHANGE);
				} else if (methodToMethod.keySet().contains(methodSig)) {
					putIntoMethodToOps(methodSignaturesToOps, methodToMethod.get(methodSig),
							TypesOfMethodOperations.CHANGE);
				} else {

					// Keep the method name same, but add/delete the parameter or change the type of
					// the parameter
					MethodDeclaration targetMethodDec = getMethodWithParameterChanged(methodForA,
							additionalMethodDecInB);
					if (targetMethodDec != null) {
						process(targetMethodDec, additionalMethodDecInB, additionalMethodInB,
								TypesOfMethodOperations.CHANGEPARAMETER);
						methodToMethod.put(methodSig, Util.getMethodSignature(targetMethodDec));
						break;
					}

					// If the method is renamed
					targetMethodDec = getMethodWithMethodNameChanged(methodForA, additionalMethodDecInB);
					if (targetMethodDec != null) {
						process(targetMethodDec, additionalMethodDecInB, additionalMethodInB,
								TypesOfMethodOperations.RENAME);
						addVertexIntoGraph(Util.getMethodSignature(targetMethodDec), methodSig, file);
						methodToMethod.put(methodSig, Util.getMethodSignature(targetMethodDec));
						break;
					}

					putIntoMethodToOps(methodSignaturesToOps, methodSig, TypesOfMethodOperations.DELETE);
				}

			}
			// edits
		});

		additionalMethodDecInB.forEach(methodDec -> {
			putIntoMethodToOps(methodSignaturesToOps, Util.getMethodSignature(methodDec), TypesOfMethodOperations.ADD);
		});

		methodSignaturesToOps.forEach((methodSig, ops) -> {
			System.out.println(methodSig + ": " + ops);
		});

	}

	/**
	 * Add a vertex into the graph
	 */
	private static void addVertexIntoGraph(String targetMethodSig, String oldMethodSig, String file) {
		// add vertex
		Vertex vertex1 = new Vertex(targetMethodSig, file);
		renaming.addVertex(vertex1);
		// add vertex
		Vertex vertex2 = new Vertex(oldMethodSig, file);
		renaming.addVertex(vertex2);
		// add edge
		renaming.addEdge(new Edge(vertex1, vertex2));
	}

	/**
	 * Store target method declaration and remove it in the difference set.
	 */
	private static void process(MethodDeclaration targetMethodDec, HashSet<MethodDeclaration> additionalMethodDecInB,
			HashSet<String> additionalMethodInB, TypesOfMethodOperations op) {
		putIntoMethodToOps(methodSignaturesToOps, Util.getMethodSignature(targetMethodDec), op);
		String tagetMethodSig = Util.getMethodSignature(targetMethodDec);
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
		if (parametersA.size() != parametersB.size())
			return false;
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
				methodSignatures.add(Util.getMethodSignature(methodDec));
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