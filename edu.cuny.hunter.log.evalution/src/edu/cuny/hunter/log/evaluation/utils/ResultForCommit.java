package edu.cuny.hunter.log.evaluation.utils;

import edu.cuny.hunter.mylyngit.core.utils.Commit;

public class ResultForCommit {

	private String headSha = "";
	private long averageJavaLinesAdded = 0;
	private long averageJavaLinesRemoved = 0;
	private int commitsProcessed = 0;

	public void computLines(Commit commit) {
		this.averageJavaLinesAdded += commit.getJavaLinesAdded();
		this.averageJavaLinesRemoved += commit.getJavaLinesRemoved();
	}

	public int getCommitsProcessed() {
		return commitsProcessed;
	}

	public void setActualCommits(int commitsProcessed) {
		this.commitsProcessed = commitsProcessed;
	}

	public float getAverageJavaLinesAdded() {
		return this.averageJavaLinesAdded;
	}

	public float getAverageJavaLinesRemoved() {
		return this.averageJavaLinesRemoved;
	}

	public long getJavaLinesAdded() {
		return averageJavaLinesAdded;
	}

	public void setJavaLinesAdded(long javaLinesAdded) {
		this.averageJavaLinesAdded = javaLinesAdded;
	}

	public long getJavaLinesRemoved() {
		return averageJavaLinesRemoved;
	}

	public void setJavaLinesRemoved(long javaLinesRemoved) {
		this.averageJavaLinesRemoved = javaLinesRemoved;
	}

	public String getHeadSha() {
		return headSha;
	}

	public void setHeadSha(String headSha) {
		this.headSha = headSha;
	}
	
	public void clear() {
		this.headSha = "";
		this.averageJavaLinesAdded = 0;
		this.averageJavaLinesRemoved = 0;
		this.commitsProcessed = 0;
	}

}
