package edu.harvard.hms.dbmi.bd2k.irct.action.query;

import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.QueryResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultSet;

public class ExecuteQuery implements QueryAction {
	private ResultSet results;
	private Query query;
	private Resource resource;

	public void setup(Resource resource, Query query) {
		setQuery(query);
		this.resource = resource;
	}

	public void run() {
		((QueryResourceImplementationInterface)resource.getImplementingInterface()).run(query);
		this.results = ((QueryResourceImplementationInterface)resource.getImplementingInterface()).getResults();
	}

	public ResultSet getResults() {

		return this.results;
	}

	public Query getQuery() {
		return this.query;

	}

	public void setQuery(Query query) {
		this.query = query;
	}

}
