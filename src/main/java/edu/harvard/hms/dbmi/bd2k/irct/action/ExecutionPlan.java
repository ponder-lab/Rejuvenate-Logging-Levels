package edu.harvard.hms.dbmi.bd2k.irct.action;

import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultSet;

public class ExecutionPlan {
	private ExecutableState state;
	private Executable executable;
	private ResultSet results;
	
	public void setup(Executable executable) {
		this.executable = executable;
		this.state = ExecutableState.CREATED;
		this.results = null;
	}
	
	public void run() {
		this.state = ExecutableState.RUNNING;
		this.executable.run();
		this.results = this.executable.getResults();
		this.state = ExecutableState.COMPLETED;
	}

	public ResultSet getResults() {
		return this.results;
	}

	public ExecutableState getState() {
		return this.state;
	}
	

}
