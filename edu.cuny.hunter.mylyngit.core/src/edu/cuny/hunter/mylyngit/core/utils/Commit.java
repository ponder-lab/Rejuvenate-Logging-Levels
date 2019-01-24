package edu.cuny.hunter.mylyngit.core.utils;

public class Commit {
	/**
	 * SHA1 of the commit
	 */
	private String SHA1;
	
	/**
	 * Number of java lines added in the commit
	 */
	private int javaLinesAdded;
	
	/**
	 * Number of java lines removed in the commit
	 */
	private int javaLinesRemoved;

	/**
	 * Number of methods found and processed
	 */
	private int methodFound;

	/**
	 * Number of interaction events created
	 */
	private int interactionEvents;

	/**
	 * Time needed to process the commit in seconds
	 */
	private double runTime;

	public Commit(String SHA1) {
		this.SHA1 = SHA1;
	}
	
	public String getSHA1() {
		return this.SHA1;
	}

	public void setSHA1(String SHA1) {
		this.SHA1 = SHA1;
	}

	public int getMethodFound() {
		return this.methodFound;
	}

	public void setMethodFound(int methodFound) {
		this.methodFound = methodFound;
	}

	public int getInteractionEvents() {
		return this.interactionEvents;
	}

	public void setInteractionEvents(int interactionEvents) {
		this.interactionEvents = interactionEvents;
	}

	public double getRunTime() {
		return this.runTime;
	}

	public void setRunTime(double runTime) {
		this.runTime = runTime;
	}

	public int getJavaLinesAdded() {
		return javaLinesAdded;
	}

	public void setJavaLinesAdded(int javaLinesAdded) {
		this.javaLinesAdded = javaLinesAdded;
	}

	public int getJavaLinesRemoved() {
		return javaLinesRemoved;
	}

	public void setJavaLinesRemoved(int javaLinesRemoved) {
		this.javaLinesRemoved = javaLinesRemoved;
	}
}
