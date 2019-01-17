package edu.cuny.hunter.mylyngit.core.analysis;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.merge.ThreeWayMerger;
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

import edu.cuny.hunter.mylyngit.core.utils.Commit;
import edu.cuny.hunter.mylyngit.core.utils.GitMethod;
import edu.cuny.hunter.mylyngit.core.utils.Graph;
import edu.cuny.hunter.mylyngit.core.utils.TimeCollector;
import edu.cuny.hunter.mylyngit.core.utils.Util;
import edu.cuny.hunter.mylyngit.core.utils.Vertex;

public class GitHistoryAnalyzer {

	// -----The variables below are used to store info only for one revision.----
	private HashSet<MethodDeclaration> methodDeclarationsForA = new HashSet<MethodDeclaration>();
	private HashSet<MethodDeclaration> methodDeclarationsForB = new HashSet<MethodDeclaration>();

	private HashMap<MethodDeclaration, Map<Integer, Integer>> methodPositionsForA = new HashMap<>();
	private HashMap<MethodDeclaration, Map<Integer, Integer>> methodPositionsForB = new HashMap<>();

	private HashMap<Integer, HashSet<MethodDeclaration>> editToMethodDeclarationForA = new HashMap<>();
	private HashMap<Integer, HashSet<MethodDeclaration>> editToMethodDeclarationForB = new HashMap<>();

	// A mapping from the method signature to the operations
	private HashMap<String, LinkedList<TypesOfMethodOperations>> methodSignaturesToOps = new HashMap<>();
	// ----------------------------------------------------------------------------

	private LinkedList<RevCommit> commitList = new LinkedList<>();

	private LinkedList<GitMethod> gitMethods = new LinkedList<>();

	private Graph renaming = new Graph();

	// ---- Which are needed for multiple projects (not for mylyngit test plugin)---
	private static HashMap<String, LinkedList<GitMethod>> repoToGitMethods = new HashMap<>();
	private static HashMap<String, Graph> repoToRenaming = new HashMap<>();
	// -----------------------------------------------------------------------------

	private LinkedList<Commit> commits = new LinkedList<Commit>();

	// the file index
	private int commitIndex = 0;

	private String repo;

	// -------------------------- data for one commit ------------------------------
	private int linesAdded = 0;
	private int linesRemoved = 0;
	private int methodFound = 0;
	private int interactionEvent = 0;
	// -----------------------------------------------------------------------------

	/**
	 * Given the repo path, compute all method operations (e.g., delete a method)
	 * for all commits.
	 * 
	 * @throws GitAPIException
	 * @throws IOException
	 * @throws NoHeadException
	 */
	public GitHistoryAnalyzer(File repoFile, int NToUseForCommits) throws GitAPIException, IOException {
		try (Git git = preProcessGitHistory(repoFile, NToUseForCommits)) {
			// Already evaluated before
			if (git == null)
				return;

			// from the earliest commit to the current commit
			for (RevCommit currentCommit : this.commitList) {

				Commit commit = new Commit(currentCommit.getName());
				// collect running time.
				TimeCollector commitTimeCollector = new TimeCollector();
				commitTimeCollector.start();

				if (currentCommit.getParentCount() == 1)
					// process the commit.
					processOneCommit(currentCommit, currentCommit.getParent(0), git);

				// special case for the initial commit, which has no parents.
				if (currentCommit.getParentCount() == 0)
					processOneCommit(currentCommit, null, git);

				// merge commits
				if (currentCommit.getParentCount() == 2) {
					processMergeCommit(currentCommit.getParent(0), currentCommit.getParent(1), git);
				}
				commitTimeCollector.stop();

				this.setCommit(commit, commitTimeCollector.getCollectedTime() / 1000);
				this.commits.add(commit);

				this.commitIndex++;
				this.resetDataForOneCommit();
			}
			this.storeMappingData();
		}
	}

	public LinkedList<Commit> getCommits() {
		return this.commits;
	}

	/**
	 * Set data of commits for each git commit.
	 */
	private void setCommit(Commit commit, float runTime) {
		commit.setLinesAdded(this.linesAdded);
		commit.setLinesRemoved(this.linesRemoved);
		commit.setMethodFound(this.methodFound);
		commit.setInteractionEvents(this.interactionEvent);
		commit.setRunTime(runTime);
	}

	/**
	 * Remove intermediate files and reset statistical data.
	 */
	private void resetDataForOneCommit() {
		this.clearFiles(new File("").getAbsoluteFile());
		this.linesAdded = 0;
		this.linesRemoved = 0;
		this.methodFound = 0;
		this.interactionEvent = 0;
	}

	private void storeMappingData() {
		repoToGitMethods.put(this.repo, this.gitMethods);
		repoToRenaming.put(this.repo, this.renaming);
	}

	/**
	 * Process merge commits. If it has conflicts, we need to process it. If not, we
	 * only need to ignore it.
	 * 
	 * @throws IOException
	 */
	private void processMergeCommit(RevCommit headCommit, RevCommit commitToMerge, Git git) throws IOException {
		ThreeWayMerger merger = MergeStrategy.RECURSIVE.newMerger(git.getRepository(), true);
		boolean canMerge = merger.merge(headCommit, commitToMerge);
		// no conflicts for merging
		if (canMerge)
			return;

		// TODO: process conflicts
		System.out.println("Conflicits here");
	}

	/**
	 * This method is used to JUnit test.
	 */
	public GitHistoryAnalyzer(String sha, File repoFile) throws IOException, GitAPIException {

		Git git = Git.init().setDirectory(repoFile).call();

		ObjectId currentCommitId = ObjectId.fromString(sha);
		RevWalk revWalk = new RevWalk(git.getRepository());
		RevCommit currentCommit = revWalk.parseCommit(currentCommitId);

		RevCommit previousCommit = currentCommit.getParent(0);
		previousCommit = revWalk.parseCommit(previousCommit.getId());
		revWalk.close();

		processOneCommit(currentCommit, previousCommit, git);

		this.clearFiles(new File("").getAbsoluteFile());

		git.close();
	}

	/**
	 * Get a mapping: the historical method to the current method if the historical
	 * method was renamed before.
	 */
	public HashMap<Vertex, Vertex> getHistoricalMethodToCurrentMethods() {
		return this.renaming.getHistoricalMethodToCurrentMethods();
	}

	/**
	 * Process one git commit
	 * 
	 * @throws IOException
	 * @throws GitAPIException
	 */
	private void processOneCommit(RevCommit currentCommit, RevCommit previousCommit, Git git)
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

				String filePath = null;

				switch (diffEntry.getChangeType()) {
				case ADD:
					filePath = this.addFile(currentCommit, git.getRepository(), diffEntry);
					break;
				case DELETE:
					filePath = this.deleteFile(previousCommit, git.getRepository(), diffEntry);
					break;
				case MODIFY:
					filePath = this.modifyFile(currentCommit, previousCommit, git.getRepository(), diffEntry,
							formatter);
					break;
				case RENAME:
				case COPY:
					filePath = this.renameOrCopyFile(currentCommit, git.getRepository(), diffEntry);
					break;
				default:
					break;
				}

				this.storeAllMethodOps(currentCommit, filePath, diffEntry.getChangeType().name());
				this.clear();
			}
		}
	}

	/**
	 * Returns a sequence of methods in the git history. Each instance stores method
	 * signature, method ops, file path, file ops, commit index and commit name.
	 */
	public LinkedList<GitMethod> getGitMethods() {
		return this.gitMethods;
	}

	/**
	 * Rename or copy a file in a commit.
	 */
	private String renameOrCopyFile(RevCommit currentCommit, Repository repo, DiffEntry diffEntry)
			throws MissingObjectException, IncorrectObjectTypeException, CorruptObjectException, IOException {
		long lines = this.copyHistoricalFile(currentCommit, repo, diffEntry.getNewPath(), "tmp_B_");
		if (lines != -1) {
			this.linesAdded += lines;
			this.linesRemoved += lines;
		}

		this.methodDeclarationsForB.forEach(methodDec -> {
			// add vertex
			Vertex vertex1 = new Vertex(Util.getMethodSignature(methodDec), diffEntry.getOldPath(), this.commitIndex);
			this.renaming.addVertex(vertex1);
			// add vertex
			Vertex vertex2 = new Vertex(Util.getMethodSignature(methodDec), diffEntry.getNewPath(), this.commitIndex);
			this.renaming.addVertex(vertex2);
			// add edge
			this.renaming.addEdge(vertex1, vertex2);
		});
		return diffEntry.getNewPath();
	}

	public Graph getRenaming() {
		return this.renaming;
	}

	/**
	 * Try to get git.
	 * 
	 * @throws NoHeadException
	 * 
	 * @throws GitAPIException
	 */
	private Git tryPreProcessGitHistory(File repoFile, int NToUseForCommits) throws NoHeadException, GitAPIException {
		Git git = Git.init().setDirectory(repoFile).call();

		Iterable<RevCommit> log = git.log().call();

		// Limit number of commits.
		int maxCommitNumber = 0;
		for (RevCommit commit : log) {
			if (maxCommitNumber < NToUseForCommits)
				this.commitList.addFirst(commit);
			maxCommitNumber++;
		}

		return git;
	}

	/**
	 * Search up all files to find repo file.
	 * 
	 * @param repoFile
	 * @return
	 */
	private Git preProcessGitHistory(File repoFile, int NToUseForCommits) {
		Git git = null;
		while (repoFile != null) {
			try {
				git = tryPreProcessGitHistory(repoFile, NToUseForCommits);
				break;
			} catch (GitAPIException e) {
				repoFile = repoFile.getParentFile();
			}
		}

		if (repoFile != null) {
			String repoPath = repoFile.getAbsolutePath();
			if (repoToGitMethods.containsKey(repoPath) && repoToRenaming.containsKey(repoPath)) {
				this.gitMethods = repoToGitMethods.get(repoPath);
				this.renaming = repoToRenaming.get(repoPath);
				return null;
			}
			this.repo = repoPath;
		}
		return git;
	}

	private String modifyFile(RevCommit currentCommit, RevCommit previousCommit, Repository repo, DiffEntry diffEntry,
			DiffFormatter formatter)
			throws MissingObjectException, IncorrectObjectTypeException, CorruptObjectException, IOException {

		FileHeader fileHeader = formatter.toFileHeader(diffEntry);

		// Get the file for revision A
		this.copyHistoricalFile(previousCommit, repo, diffEntry.getOldPath(), "tmp_A_");
		// Get the file for revision B
		this.copyHistoricalFile(currentCommit, repo, diffEntry.getNewPath(), "tmp_B_");

		// For revision A, get the differences
		this.computeMethodPositions(this.methodDeclarationsForA, this.methodPositionsForA);

		// For revision B, get the differences
		this.computeMethodPositions(this.methodDeclarationsForB, this.methodPositionsForB);

		List<? extends HunkHeader> hunks = fileHeader.getHunks();
		int editId = 0;
		for (HunkHeader hunk : hunks) {
			EditList editList = hunk.toEditList();
			if (!editList.isEmpty()) {
				// For each pair of edit
				for (Edit edit : editList) {
					editId++;
					this.linesRemoved += this.mapEditToMethod(editId, edit.getBeginA(), edit.getEndA(),
							this.methodPositionsForA, this.editToMethodDeclarationForA);
					this.linesAdded += this.mapEditToMethod(editId, edit.getBeginB(), edit.getEndB(),
							this.methodPositionsForB, this.editToMethodDeclarationForB);

				}
			}
		}

		this.computeMethodChanges(diffEntry.getNewPath());

		return diffEntry.getNewPath();
	}

	/**
	 * Add a file in a commit.
	 */
	private String addFile(RevCommit currentCommit, Repository repo, DiffEntry diffEntry)
			throws MissingObjectException, IncorrectObjectTypeException, CorruptObjectException, IOException {
		// Get the file for revision B
		long linesAddedForFile = this.copyHistoricalFile(currentCommit, repo, diffEntry.getNewPath(), "tmp_B_");
		if (linesAddedForFile != -1)
			this.linesAdded += linesAddedForFile;
		this.methodDeclarationsForB.forEach(methodDec -> {
			this.putIntoMethodToOps(this.methodSignaturesToOps, Util.getMethodSignature(methodDec),
					TypesOfMethodOperations.ADD);
		});
		return diffEntry.getNewPath();
	}

	/**
	 * Delete a file in a commit;
	 */
	private String deleteFile(RevCommit previousCommit, Repository repo, DiffEntry diffEntry)
			throws MissingObjectException, IncorrectObjectTypeException, CorruptObjectException, IOException {
		// Get the file for revision A
		long linesRemovedForFile = this.copyHistoricalFile(previousCommit, repo, diffEntry.getOldPath(), "tmp_A_");
		if (linesRemovedForFile != -1)
			this.linesRemoved += linesRemovedForFile;
		this.methodDeclarationsForA.forEach(methodDec -> {
			this.putIntoMethodToOps(this.methodSignaturesToOps, Util.getMethodSignature(methodDec),
					TypesOfMethodOperations.DELETE);

			Set<Vertex> tailVertices = new HashSet<>();
			tailVertices.addAll(this.renaming.getTailVertices());
			for (Vertex v : tailVertices) {
				if (v.getFile().equals(diffEntry.getOldPath())
						&& v.getMethod().equals(Util.getMethodSignature(methodDec))) {
					this.renaming.pruneGraphByTail(v);
				}
			}

		});
		return diffEntry.getOldPath();
	}

	/**
	 * Print all method operations into CSV file
	 */
	private void storeAllMethodOps(RevCommit commit, String path, String fileOp) {
		this.methodSignaturesToOps.forEach((methodSig, ops) -> {
			this.methodFound++;
			for (TypesOfMethodOperations op : ops) {
				this.interactionEvent++;
				this.gitMethods.add(new GitMethod(methodSig, op, path, fileOp, commitIndex, commit.name()));
			}
		});
	}

	/**
	 * Check whether a directory is a temporary directory.
	 */
	private boolean isTemporaryDirectory(File directory) {
		if (directory.getName().length() >= 7 && directory.getName().substring(0, 4).equals("tmp_"))
			return true;
		else
			return false;
	}

	/**
	 * Remove all temporary files
	 */
	private boolean clearFiles(File directory) {
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

	/**
	 * Clear list of commits.
	 */
	public void clearCommits() {
		this.commits.clear();
	}

	/**
	 * Clear all sets and maps.
	 */
	public void clear() {
		this.methodDeclarationsForA.clear();
		this.methodDeclarationsForB.clear();
		this.methodPositionsForA.clear();
		this.methodPositionsForB.clear();
		this.editToMethodDeclarationForA.clear();
		this.editToMethodDeclarationForB.clear();
		this.methodSignaturesToOps.clear();
	}

	/**
	 * Compute the start position and end position of a method.
	 */
	private void computeMethodPositions(HashSet<MethodDeclaration> methodDeclarations,
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
	private int mapEditToMethod(int editId, int editStart, int editEnd,
			HashMap<MethodDeclaration, Map<Integer, Integer>> methodPositions,
			HashMap<Integer, HashSet<MethodDeclaration>> editToMethodDeclaration) {
		for (int line = editStart + 1; line <= editEnd; ++line) {
			addCorrespondingMethod(editId, methodPositions, editToMethodDeclaration, line);
		}
		if (editStart == editEnd)
			addCorrespondingMethod(editId, methodPositions, editToMethodDeclaration, editEnd + 1);
		return editEnd - editStart;
	}

	// Given a line, add its corresponding method into editToMethodDeclaration
	private void addCorrespondingMethod(int editId, HashMap<MethodDeclaration, Map<Integer, Integer>> methodPositions,
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

	private static int getStartingLineNumber(MethodDeclaration methodDeclaration) {
		return (((CompilationUnit) methodDeclaration.getRoot()).getLineNumber(methodDeclaration.getStartPosition()));
	}

	private static int getEndingLineNumber(MethodDeclaration methodDeclaration) {
		return (((CompilationUnit) methodDeclaration.getRoot())
				.getLineNumber(methodDeclaration.getStartPosition() + methodDeclaration.getLength()));
	}

	private AbstractTreeIterator getCanonicalTreeParser(ObjectId commitId, Repository repo) throws IOException {
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
	private long copyHistoricalFile(RevCommit commit, Repository repo, String path, String newDirectory)
			throws MissingObjectException, IncorrectObjectTypeException, CorruptObjectException, IOException {
		RevTree tree = commit.getTree();
		TreeWalk treeWalk = new TreeWalk(repo);
		treeWalk.addTree(tree);
		treeWalk.setRecursive(true);
		treeWalk.setFilter(PathFilter.create(path));
		if (!treeWalk.next()) {
			return -1;
		}
		ObjectId objectId = treeWalk.getObjectId(0);
		ObjectLoader loader = repo.open(objectId);
		return this.copyToFile(loader, path, newDirectory);
	}

	private long copyToFile(ObjectLoader loader, String path, String newDirectory) throws IOException {
		// Get the empty or existing file in the new directory.
		File file = this.getFile(path, newDirectory);
		if (file == null)
			return -1;
		// Copy the file content into the new file.
		FileOutputStream fileOutputStream = new FileOutputStream(file.getAbsolutePath(), false);
		loader.copyTo(fileOutputStream);
		fileOutputStream.close();

		// Parse the java file.
		String fileContent = new BufferedReader(new InputStreamReader(loader.openStream())).lines()
				.collect(Collectors.joining("\n"));
		if (!fileContent.isEmpty())
			this.parseJavaFile(fileContent, newDirectory);
		try (Stream<String> stream = Files.lines(Paths.get(file.getAbsolutePath()))) {
			return stream.count();
		}
	}

	/**
	 * Get the file in the new directory.
	 */
	private File getFile(String path, String newDirectory) throws IOException {
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
	private String getJavaFileName(String path) {
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
	 * Return a set of B-A.
	 */
	private Collection<String> getAdditionalMethods(Collection<String> methodSignaturesA,
			Collection<String> methodSignaturesB) {
		Collection<String> additionalMethods = new LinkedList<String>();
		additionalMethods.addAll(methodSignaturesB);
		additionalMethods.removeAll(methodSignaturesA);
		return additionalMethods;
	}

	/**
	 * Given a set of method signatures, get its corresponding set of method
	 * declarations.
	 */
	private Collection<MethodDeclaration> getSetOfMethodDeclaration(HashSet<MethodDeclaration> methodsDecs,
			Collection<String> methodSigs) {
		Collection<MethodDeclaration> methodDeclarations = new LinkedList<MethodDeclaration>();
		HashMap<String, MethodDeclaration> methodSigToMethodDec = new HashMap<>();
		methodsDecs.forEach(method -> {
			methodSigToMethodDec.put(Util.getMethodSignature(method), method);
		});
		methodSigs.forEach(methodSig -> {
			methodDeclarations.add(methodSigToMethodDec.get(methodSig));
		});
		return methodDeclarations;
	}

	/**
	 * The core method to return a list of methods and their operation types for one
	 * file.
	 */
	private void computeMethodChanges(String file) {
		Collection<String> methodSignaturesForEditsA = getMethodSignatures(this.editToMethodDeclarationForA.values());
		Collection<String> methodSignaturesForEditsB = getMethodSignatures(this.editToMethodDeclarationForB.values());

		Collection<String> additionalMethodInB = getAdditionalMethods(methodSignaturesForEditsA,
				methodSignaturesForEditsB);

		Collection<MethodDeclaration> additionalMethodDecInB = getSetOfMethodDeclaration(this.methodDeclarationsForB,
				additionalMethodInB);

		// Iterate over edits. Each edit should be counted as an event
		this.editToMethodDeclarationForA.forEach((editIdForA, methodsInOneEditA) -> {

			for (MethodDeclaration methodForA : methodsInOneEditA) {
				String methodSig = Util.getMethodSignature(methodForA);

				// Modify method body, or rename parameters
				if (methodSignaturesForEditsB.contains(methodSig)) {
					this.putIntoMethodToOps(this.methodSignaturesToOps, methodSig, TypesOfMethodOperations.CHANGE);
				} else {

					// Keep the method name same, but add/delete the parameter or change the type of
					// the parameter
					MethodDeclaration targetMethodDec = getMethodWithParameterChanged(methodForA,
							additionalMethodDecInB);
					if (!this.processGitMethodOperations(additionalMethodDecInB, additionalMethodInB, file, methodSig,
							TypesOfMethodOperations.CHANGEPARAMETER, targetMethodDec)) {

						// If the method is renamed
						targetMethodDec = getMethodWithMethodNameChanged(methodForA, additionalMethodDecInB);

						if (!this.processGitMethodOperations(additionalMethodDecInB, additionalMethodInB, file,
								methodSig, TypesOfMethodOperations.RENAME, targetMethodDec))
							this.process(methodForA, additionalMethodDecInB, additionalMethodInB,
									TypesOfMethodOperations.DELETE);
					}
				}
			}
		});

		additionalMethodDecInB.forEach(methodDec -> {
			this.putIntoMethodToOps(this.methodSignaturesToOps, Util.getMethodSignature(methodDec),
					TypesOfMethodOperations.ADD);
		});

//		this.methodSignaturesToOps.forEach((methodSig, ops) -> {
//			System.out.println(methodSig + ": " + ops);
//		});

	}

	/**
	 * process when the git method is renamed or its parameters are changed.
	 */
	private boolean processGitMethodOperations(Collection<MethodDeclaration> additionalMethodDecInB,
			Collection<String> additionalMethodInB, String file, String methodSig, TypesOfMethodOperations methodOp,
			MethodDeclaration targetMethodDec) {
		if (targetMethodDec != null) {
			this.process(targetMethodDec, additionalMethodDecInB, additionalMethodInB, methodOp);
			this.addVertexIntoGraph(Util.getMethodSignature(targetMethodDec), methodSig, file);
			return true;
		} else
			return false;
	}

	/**
	 * Add a vertex into the graph
	 */
	private void addVertexIntoGraph(String targetMethodSig, String oldMethodSig, String file) {
		// add vertex
		Vertex vertex1 = new Vertex(targetMethodSig, file, this.commitIndex);
		this.renaming.addVertex(vertex1);
		// add vertex
		Vertex vertex2 = new Vertex(oldMethodSig, file, this.commitIndex);
		this.renaming.addVertex(vertex2);
		// add edge
		this.renaming.addEdge(vertex1, vertex2);
	}

	/**
	 * Store target method declaration and remove it in the difference set.
	 */
	private void process(MethodDeclaration targetMethodDec, Collection<MethodDeclaration> additionalMethodDecInB,
			Collection<String> additionalMethodInB, TypesOfMethodOperations op) {
		this.putIntoMethodToOps(this.methodSignaturesToOps, Util.getMethodSignature(targetMethodDec), op);
		this.removeMethodInRevisionB(targetMethodDec, additionalMethodDecInB, additionalMethodInB);
	}

	/**
	 * Remove method in revision B.
	 */
	private void removeMethodInRevisionB(MethodDeclaration targetMethodDec,
			Collection<MethodDeclaration> additionalMethodDecInB, Collection<String> additionalMethodInB) {
		String tagetMethodSig = Util.getMethodSignature(targetMethodDec);
		additionalMethodDecInB.remove(targetMethodDec);
		additionalMethodInB.remove(tagetMethodSig);
	}

	/**
	 * Check whether the two methods have the same parameter types.
	 */
	private boolean isSameParameterType(MethodDeclaration methodA, MethodDeclaration methodB) {
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
	private MethodDeclaration getMethodWithParameterChanged(MethodDeclaration methodForA,
			Collection<MethodDeclaration> additionalMethodDecInB) {
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
	private MethodDeclaration getMethodWithMethodNameChanged(MethodDeclaration methodForA,
			Collection<MethodDeclaration> additionalMethodDecInB) {
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
	private void putIntoMethodToOps(HashMap<String, LinkedList<TypesOfMethodOperations>> map, String key,
			TypesOfMethodOperations element) {
		LinkedList<TypesOfMethodOperations> list;
		if (map.containsKey(key)) {
			list = map.get(key);
		} else {
			list = new LinkedList<>();
		}
		list.add(element);
		map.put(key, list);
	}

	/**
	 * Returns all method signatures for all edits in one file.
	 */
	private Collection<String> getMethodSignatures(Collection<HashSet<MethodDeclaration>> methodDeclarations) {
		Collection<String> methodSignatures = new LinkedList<String>();
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
	private void parseJavaFile(String fileContent, String newDirectory) throws IOException {
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

	public HashMap<String, LinkedList<TypesOfMethodOperations>> getMethodSignaturesToOps() {
		return this.methodSignaturesToOps;
	}

	/**
	 * Clear intermediate data for each tool running.
	 */
	public static void clearMappingData() {
		repoToGitMethods.clear();
		repoToRenaming.clear();
	}

}
