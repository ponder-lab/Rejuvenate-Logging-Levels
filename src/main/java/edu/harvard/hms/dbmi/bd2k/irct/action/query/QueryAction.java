package edu.harvard.hms.dbmi.bd2k.irct.action.query;

import edu.harvard.hms.dbmi.bd2k.irct.action.Action;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;

public interface QueryAction extends Action {
	void setup(Resource resource, Query query);
	Query getQuery();
	void setQuery(Query query);
}
