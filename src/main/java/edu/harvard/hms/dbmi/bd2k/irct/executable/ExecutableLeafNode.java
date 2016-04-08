package edu.harvard.hms.dbmi.bd2k.irct.executable;

import edu.harvard.hms.dbmi.bd2k.irct.action.Action;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;

public class ExecutableLeafNode implements Executable {

	private SecureSession session;
	private Action action;
	private ExecutableStatus state;
	
	@Override
	public void setup(SecureSession secureSession) {
		this.session = secureSession;
		this.state = ExecutableStatus.CREATED;
	}

	@Override
	public void run() throws ResourceInterfaceException {
		this.state = ExecutableStatus.RUNNING;
		this.action.run(this.session);
		this.state = ExecutableStatus.COMPLETED;
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
	
	
}
