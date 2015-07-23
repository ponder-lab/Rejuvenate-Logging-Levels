/*
 *  This file is part of Inter-Resource Communication Tool (IRCT).
 *
 *  IRCT is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  IRCT is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with IRCT.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.harvard.hms.dbmi.bd2k.irct.action.query;

import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.QueryResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultSet;

public class ExecuteQuery implements QueryAction {
	private Long runId;
	private Query query;
	private Resource resource;

	public void setup(Resource resource, Query query) {
		setQuery(query);
		this.resource = resource;
	}

	public void run() {
		runId = ((QueryResourceImplementationInterface)resource.getImplementingInterface()).run(query);
	}

	public ResultSet getResults() {
		return ((QueryResourceImplementationInterface)resource.getImplementingInterface()).getResults(runId);
	}

	public Query getQuery() {
		return this.query;

	}

	public void setQuery(Query query) {
		this.query = query;
	}

}
