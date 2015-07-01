package edu.harvard.hms.dbmi.bd2k.irct.action;

import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultSet;

public class QueryExecutable implements Executable {
	private Resource resource;
	
	private ExecutableState state;

	public void setup(Action action) {
		// TODO Auto-generated method stub
		this.state = ExecutableState.CREATED;
	}

	public void run() {
		// TODO Auto-generated method stub
		this.state = ExecutableState.RUNNING;

		this.state = ExecutableState.COMPLETED;
	}

	public ResultSet getResults() {
		// TODO Auto-generated method stub
		return null;
	}

	public ExecutableState getState() {
		return this.state;
	}
	
	public void setResource(Resource resource) {
		this.resource = resource;
	}
	public Resource getResource() {
		return this.resource;
	}
}
