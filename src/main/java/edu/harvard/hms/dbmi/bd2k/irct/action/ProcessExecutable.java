package edu.harvard.hms.dbmi.bd2k.irct.action;

import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultSet;

public class ProcessExecutable implements Executable {
	private Resource resource;
	public void setup(Action action) {
		// TODO Auto-generated method stub
		
	}
	
	public void run() {
		// TODO Auto-generated method stub

	}

	public ResultSet getResults() {
		// TODO Auto-generated method stub
		return null;
	}

	public Executable getLeft() {
		// TODO Auto-generated method stub
		return null;
	}

	public Executable getRight() {
		// TODO Auto-generated method stub
		return null;
	}

	public ExecutableState getState() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}
	public Resource getResource() {
		return this.resource;
	}

}
