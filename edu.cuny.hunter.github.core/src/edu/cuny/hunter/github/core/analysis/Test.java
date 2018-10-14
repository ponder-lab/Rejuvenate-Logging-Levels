package edu.cuny.hunter.github.core.analysis;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.HunkHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

@SuppressWarnings("restriction")
public class Test {

	public static void main(String[] args) throws IOException, GitAPIException {

		Repository repo = new FileRepository("C:\\Users\\tangy\\eclipse-workspace\\Java-8-Stream-Refactoring\\.git");

		Git git = new Git(repo);

		Iterable<RevCommit> log = git.log().call();
		RevCommit previousCommit = null;

		for (RevCommit commit : log) {

			if (previousCommit != null) {

				System.out.println("LogCommit: " + commit);
				String logMessage = commit.getFullMessage();
				System.out.println("LogMessage: " + logMessage);

				AbstractTreeIterator oldTreeIterator = getCanonicalTreeParser(previousCommit, git);
				AbstractTreeIterator newTreeIterator = getCanonicalTreeParser(commit, git);

				final List<DiffEntry> diffs = git.diff().setOldTree(oldTreeIterator).setNewTree(newTreeIterator).call();

				OutputStream outputStream = new ByteArrayOutputStream();
				try (DiffFormatter formatter = new DiffFormatter(outputStream)) {
					formatter.setRepository(repo);
					formatter.scan(oldTreeIterator, newTreeIterator);

					for (DiffEntry diffEntry : diffs) {
						FileHeader fileHeader = formatter.toFileHeader(diffEntry);;

						if (diffEntry.getChangeType().name().equals("ADD")) {
							System.out.println("ADD: " + diffEntry.getNewPath());
							System.out.println();
						} else if (diffEntry.getChangeType().name().equals("DELETE")) {
							System.out.println("DELETE: ");
							System.out.println(diffEntry.getOldPath());
							System.out.println();
						}
						if (diffEntry.getChangeType().name().equals("MODIFY")) {
							System.out.println("----------------------------------");
							System.out.println("MODIFY: " + diffEntry.getNewPath());
							List<? extends HunkHeader> hunks = fileHeader.getHunks();
							for (HunkHeader hunk : hunks) {
								System.out.println(
										"Start: " + hunk.getNewStartLine() + ", " + "#lines: " + hunk.getNewLineCount());
							}	
							System.out.println("----------------------------------");
							formatter.flush();
							formatter.format(diffEntry);
							System.out.println(outputStream);
							System.out.println("----------------------------------");
							System.out.println();
						}

					}
				}

			}
			System.out.println("#######################################");
			previousCommit = commit;
		}

		git.close();
	}
	
	private static void printFileContent(String path) throws IOException {
		 BufferedReader br = new BufferedReader(new FileReader(path));
		 String line = null;
		 while ((line = br.readLine()) != null) {
		   System.out.println(line);
		 }
	}

	private static AbstractTreeIterator getCanonicalTreeParser(ObjectId commitId, Git git) throws IOException {
		try (RevWalk walk = new RevWalk(git.getRepository())) {
			RevCommit commit = walk.parseCommit(commitId);
			ObjectId treeId = commit.getTree().getId();
			try (ObjectReader reader = git.getRepository().newObjectReader()) {
				return new CanonicalTreeParser(null, reader, treeId);
			}
		}
	}

}
