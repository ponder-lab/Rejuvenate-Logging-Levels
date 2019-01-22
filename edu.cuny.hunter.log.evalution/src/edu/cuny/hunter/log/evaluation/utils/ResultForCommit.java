package edu.cuny.hunter.log.evaluation.utils;

import edu.cuny.hunter.mylyngit.core.utils.Commit;

public class ResultForCommit {
	private long linesAdded = 0;
	private long linesRemoved = 0;
	private long javaLinesAdded = 0;
	private long javaLinesRemoved = 0;
	private int actualCommits = 0;

	public void computLines(Commit commit) {
		this.linesAdded += commit.getLinesAdded();
		this.linesRemoved += commit.getLinesRemoved();
		this.javaLinesAdded += commit.getJavaLinesAdded();
		this.javaLinesRemoved += commit.getJavaLinesRemoved();
	}

	public int getActualCommits() {
		return actualCommits;
	}

	public void setActualCommits(int actualCommits) {
		this.actualCommits = actualCommits;
	}

	public long getLinesRemoved() {
		return linesRemoved;
	}

	public long getLinesAdded() {
		return linesAdded;
	}

	public float getAverageJavaLinesAdded() {
		return this.actualCommits == 0 ? 0 : this.javaLinesAdded / this.actualCommits;
	}

	public float getAverageJavaLinesRemoved() {
		return this.actualCommits == 0 ? 0 : this.javaLinesRemoved / this.actualCommits;
	}

	public float getAverageLinesRemoved() {
		return this.actualCommits == 0 ? 0 : this.linesRemoved / this.actualCommits;
	}

	public float getAverageLinesAdded() {
		return this.actualCommits == 0 ? 0 : this.linesAdded / this.actualCommits;
	}

	public void clear() {
		this.linesAdded = 0;
		this.linesRemoved = 0;
		this.actualCommits = 0;
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
}
