package edu.cuny.hunter.log.evaluation.utils;

import edu.cuny.hunter.mylyngit.core.utils.Commit;

public class ResultForCommit {
	private long linesAdded = 0;
	private long linesRemoved = 0;
	private int actualCommits = 0;

	public void computLines(Commit commit) {
		this.linesAdded += commit.getLinesAdded();
		this.linesRemoved += commit.getLinesRemoved();
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

	private void setLinesRemoved(long linesRemoved) {
		this.linesRemoved = linesRemoved;
	}

	public long getLinesAdded() {
		return linesAdded;
	}

	private void setLinesAdded(long linesAdded) {
		this.linesAdded = linesAdded;
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
}
