package edu.harvard.hms.dbmi.bd2k.irct.action;

import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultSet;

public interface Executable {
	void setup(Action action);
	void run();
	ExecutableState getState();
	ResultSet getResults();
	Resource getResource();
	void setResource(Resource resource);
}
