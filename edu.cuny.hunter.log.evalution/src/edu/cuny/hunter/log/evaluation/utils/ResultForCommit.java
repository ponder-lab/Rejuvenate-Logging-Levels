package edu.cuny.hunter.log.evaluation.utils;

import edu.cuny.hunter.mylyngit.core.utils.Commit;

public class ResultForCommit {

	private String headSha = "";
	private long javaLinesAdded = 0;
	private long javaLinesRemoved = 0;
	private int commitsProcessed = 0;

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

	public float getAverageJavaLinesAdded() {
		return this.commitsProcessed == 0 ? 0 : this.javaLinesAdded / this.commitsProcessed;
	}

	public float getAverageJavaLinesRemoved() {
		return this.commitsProcessed == 0 ? 0 : this.javaLinesRemoved / this.commitsProcessed;
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
