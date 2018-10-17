package edu.cuny.hunter.github.core.analysis;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
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
public class Test {
	// the file index
	private static int index = 0;

	// Set of method declarations
	// Should we consider the ordering of the methods?
	private static HashSet<MethodDeclaration> methodDeclarationsForA = new HashSet<MethodDeclaration>();
	private static HashSet<MethodDeclaration> methodDeclarationsForB = new HashSet<MethodDeclaration>();

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
				System.out.println("------------------------------------------");

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
							System.out.println("MODIFY: " + diffEntry.getNewPath());
							List<? extends HunkHeader> hunks = fileHeader.getHunks();
							for (HunkHeader hunk : hunks) {
								EditList editList = hunk.toEditList();
								if (!editList.isEmpty()) {
									editList.forEach(edit -> {
										System.out.println("Revision A: start at " + edit.getBeginA() + ", end at "
												+ edit.getEndA());
										System.out.println("Revision B: start at " + edit.getBeginB() + ", end at "
												+ edit.getEndB());
									});
								}

							}
							// Get the file for revision A
							copyHistoricalFile(currentCommit.getParent(0), repo, diffEntry.getNewPath(), "tmp_A_");
							// Get the file for revision B
							copyHistoricalFile(currentCommit, repo, diffEntry.getOldPath(), "tmp_B_");

							// Should compare AST here.
							extractMethodChanges();
							// Should clear the sets of method declarations.
							clearSetOfMethodDeclarations();

							index++;
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
		}

		git.close();
	}

	private static void extractMethodChanges() {
		// System.out.println("**********************************");
		// methodDeclarationsForA.forEach(m -> {
		// System.out.println(m);
		// System.out.println("**********************************");
		// });
		// methodDeclarationsForB.forEach(m -> {
		// System.out.println(m);
		// System.out.println("**********************************");
		// });

		methodDeclarationsForA.forEach(m -> {
			SimpleName methodNameInA = m.getName();
			List<TypeParameter> parameterTypesInA = m.typeParameters();

			// Get the corresponding method declarations in B
			// Currently, we only consider the method changes in the method body. i.e., we
			// do not consider rename etc.
			for (MethodDeclaration methodDeclarationInB : methodDeclarationsForB) {
				if (isSameMethod(methodNameInA, parameterTypesInA, methodDeclarationInB)) {
					if (!(new ASTMatcher()).match(m, methodDeclarationInB)) {
						System.out.println(methodNameInA + "(" + m.parameters() + ") has changes!");
						// TODO: extract changes
					}
				}
			}
		});

		// TODO: match AST here
	}

	/**
	 * Check whether method A and method B are same method.
	 */
	private static boolean isSameMethod(SimpleName methodNameInA, List<TypeParameter> parameterTypesInA,
			MethodDeclaration methodDeclarationInB) {
		SimpleName methodNameInB = methodDeclarationInB.getName();
		List<TypeParameter> parameterTypesInB = methodDeclarationInB.typeParameters();

		// If they have the same method name.
		if (methodNameInA.getIdentifier().equals(methodNameInB.getIdentifier())) {

			// If they have the same number of parameters.
			if (parameterTypesInA.size() != parameterTypesInB.size())
				return false;

			// If they have the same ordered parameter types.
			int index = 0;
			for (TypeParameter parameter : parameterTypesInA) {
				if (!parameter.equals(parameterTypesInB.get(index)))
					return false;
			}
			return true;
		} else
			return false;

	}

	private static void clearSetOfMethodDeclarations() {
		methodDeclarationsForA.clear();
		methodDeclarationsForB.clear();
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
		System.out.println("New file path: " + file.getAbsolutePath());

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
		File file = new File(newDirectory + index + "/" + fileName);

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
	 * Parse a Java file, and let visitor to visit declaring methods.
	 */
	private static void parseJavaFile(File file, String fileContent, String newDirectory) throws IOException {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setResolveBindings(true);
		parser.setSource(fileContent.toCharArray());

		final CompilationUnit cu = (CompilationUnit) parser.createAST(new NullProgressMonitor());
		System.out.println("Parse a Java file! " + cu.getNodeType());

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

}
