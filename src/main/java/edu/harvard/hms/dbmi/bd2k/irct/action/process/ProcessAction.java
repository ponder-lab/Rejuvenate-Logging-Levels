package edu.harvard.hms.dbmi.bd2k.irct.action.process;

import edu.harvard.hms.dbmi.bd2k.irct.action.Action;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultSet;

public interface ProcessAction extends Action {
	void setup(Resource resource, ResultSet... resultSets);
}
