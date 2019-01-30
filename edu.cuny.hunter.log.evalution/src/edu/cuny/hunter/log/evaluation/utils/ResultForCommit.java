package edu.cuny.hunter.log.evaluation.utils;

import edu.cuny.hunter.mylyngit.core.utils.Commit;

public class ResultForCommit {

	private String headSha = "";
	private long javaLinesAdded = 0;
	private long javaLinesRemoved = 0;
	private int actualCommits = 0;
	private boolean isSameRepo;

	public void computLines(Commit commit) {
		this.javaLinesAdded += commit.getJavaLinesAdded();
		this.javaLinesRemoved += commit.getJavaLinesRemoved();
	}

	public int getActualCommits() {
		return actualCommits;
	}

	public void setActualCommits(int actualCommits) {
		this.actualCommits = actualCommits;
	}

	public float getAverageJavaLinesAdded() {
		return this.actualCommits == 0 ? 0 : this.javaLinesAdded / this.actualCommits;
	}

	public float getAverageJavaLinesRemoved() {
		return this.actualCommits == 0 ? 0 : this.javaLinesRemoved / this.actualCommits;
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
	
	public void clear() {
		this.headSha = "";
		this.javaLinesAdded = 0;
		this.javaLinesRemoved = 0;
		this.actualCommits = 0;
		this.isSameRepo = false;
	}

	public boolean isSameRepo() {
		return isSameRepo;
	}

	public void setSameRepo(boolean isSameRepo) {
		this.isSameRepo = isSameRepo;
	}
}
