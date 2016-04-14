package edu.harvard.hms.dbmi.bd2k.irct.executable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import edu.harvard.hms.dbmi.bd2k.irct.action.Action;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultStatus;
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
		this.children = new HashMap<String, Executable>();
		this.childrenResults = new HashMap<String, Result>();
		this.state = ExecutableStatus.CREATED;
	}

	@Override
	public void run() throws ResourceInterfaceException {
		if (isBlocking() && !children.isEmpty()) {
			runSequentially();
		} else if (!children.isEmpty()){
			runConcurrently();
		}
		if(!childrenResults.isEmpty()) {
			action.updateActionParams(childrenResults);
		}
		
		this.state = ExecutableStatus.RUNNING;
		this.action.run(this.session);
		this.state = ExecutableStatus.COMPLETED;
	}

	private void runSequentially() throws ResourceInterfaceException {
		for (String key : this.children.keySet()) {
			Executable executable = this.children.get(key);
			executable.setup(this.session);
			executable.run();
			childrenResults.put(key, executable.getResults());
		}
	}

	private void runConcurrently() {
		List<ExecutorThread> myThreads = new ArrayList<ExecutorThread>();
		for (String key : this.children.keySet()) {
			myThreads.add(new ExecutorThread(key, this.children.get(key),
					this.session));
		}
		ExecutorService taskExecutor = Executors.newFixedThreadPool(10);
		for (ExecutorThread executorThread : myThreads) {
			taskExecutor.execute(executorThread);
		}
		taskExecutor.shutdown();
		try {
			taskExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		for (ExecutorThread executorThread : myThreads) {
			childrenResults.put(executorThread.getId(),
					executorThread.getResult());
		}
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

class ExecutorThread implements Runnable {

	private String id;
	private SecureSession session;
	private Executable executable;
	private Result result;

	public ExecutorThread(String id, Executable executable,
			SecureSession session) {
		this.session = session;
		this.executable = executable;
	}

	@Override
	public void run() {
		executable.setup(this.session);
		try {
			executable.run();
			this.result = executable.getResults();
		} catch (ResourceInterfaceException e) {
			result.setResultStatus(ResultStatus.ERROR);
		}
	}

	public Result getResult() {
		return this.result;
	}

	public String getId() {
		return this.id;
	}

}