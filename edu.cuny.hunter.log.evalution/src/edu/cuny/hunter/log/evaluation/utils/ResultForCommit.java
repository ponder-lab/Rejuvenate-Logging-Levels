package edu.cuny.hunter.log.evaluation.utils;

import edu.cuny.hunter.mylyngit.core.utils.Commit;

public class ResultForCommit {

	private String headSha;
	private long javaLinesAdded;
	private long javaLinesRemoved;
	private int commitsProcessed;

	public void computLines(Commit commit) {
		this.javaLinesAdded += commit.getJavaLinesAdded();
		this.javaLinesRemoved += commit.getJavaLinesRemoved();
	}

	public int getCommitsProcessed() {
		return commitsProcessed;
	}

	public void setActualCommits(int commitsProcessed) {
		this.commitsProcessed = commitsProcessed;
	}

	public long getJavaLinesAdded() {
		return javaLinesAdded;
	}

	public void setJavaLinesAdded(long javaLinesAdded) {
		this.javaLinesAdded = javaLinesAdded;
	}

	public long getJavaLinesRemoved() {
		return javaLinesRemoved;
	}

	public void setJavaLinesRemoved(long javaLinesRemoved) {
		this.javaLinesRemoved = javaLinesRemoved;
	}

	public String getHeadSha() {
		return headSha;
	}

	public void setHeadSha(String headSha) {
		this.headSha = headSha;
	}

}
