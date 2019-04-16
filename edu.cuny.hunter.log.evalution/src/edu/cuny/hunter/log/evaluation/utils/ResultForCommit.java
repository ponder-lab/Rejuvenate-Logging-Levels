package edu.cuny.hunter.log.evaluation.utils;

public class ResultForCommit {

	private String headSha;
	private double averageJavaLinesAdded;
	private double averageJavaLinesRemoved;
	private int commitsProcessed;

	public int getCommitsProcessed() {
		return commitsProcessed;
	}

	public void setActualCommits(int commitsProcessed) {
		this.commitsProcessed = commitsProcessed;
	}

	public double getAverageJavaLinesAdded() {
		return this.averageJavaLinesAdded;
	}

	public double getAverageJavaLinesRemoved() {
		return this.averageJavaLinesRemoved;
	}

	public void setAverageJavaLinesAdded(double averageJavaLinesAdded) {
		this.averageJavaLinesAdded = averageJavaLinesAdded;
	}

	public void setAverageJavaLinesRemoved(double averageJavaLinesRemoved) {
		this.averageJavaLinesRemoved = averageJavaLinesRemoved;
	}

	public String getHeadSha() {
		return headSha;
	}

	public void setHeadSha(String headSha) {
		this.headSha = headSha;
	}
}
