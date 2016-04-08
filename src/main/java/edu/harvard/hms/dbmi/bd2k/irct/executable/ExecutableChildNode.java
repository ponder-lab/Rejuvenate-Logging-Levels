package edu.harvard.hms.dbmi.bd2k.irct.executable;

import java.util.HashMap;
import java.util.Map;

import edu.harvard.hms.dbmi.bd2k.irct.action.Action;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;

public class ExecutableChildNode implements Executable {
	
	private SecureSession session;
	private boolean blocking;
	private Action action;
	private Map<String, Executable> children;
	private Map<String, Result> childrenResults;
	private ExecutableStatus state;

	@Override
	public void setup(SecureSession secureSession) {
		this.session = secureSession;
		this.childrenResults = new HashMap<String, Result>();
		this.state = ExecutableStatus.CREATED;
	}

	@Override
	public void run() throws ResourceInterfaceException {
		if(isBlocking()) {
			runSequentially();
		} else {
			runConcurrently();
		}
		//TODO: FEED IN UPDATED PARAMETERS
		this.state = ExecutableStatus.RUNNING;
		this.action.run(this.session);
		this.state = ExecutableStatus.COMPLETED;
	}
	
	private void runSequentially() throws ResourceInterfaceException {
		for(String key : this.children.keySet()) {
			Executable executable = this.children.get(key);
			executable.setup(this.session);
			executable.run();
			childrenResults.put(key, executable.getResults());
		}
	}
	
	private void runConcurrently() throws ResourceInterfaceException {
		//TODO: CREATE THREAD PULL AND RUN CONCURRENTLY
		
	}

	@Override
	public ExecutableStatus getStatus() {
		return this.state;
	}

	@Override
	public Result getResults() throws ResourceInterfaceException {
		return this.action.getResults(this.session);
	}
	
	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public boolean isBlocking() {
		return blocking;
	}

	public void setBlocking(boolean blocking) {
		this.blocking = blocking;
	}
}
