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
package edu.harvard.hms.dbmi.bd2k.irct.controller;

import java.util.concurrent.Future;
import java.util.logging.Logger;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;

import edu.harvard.hms.dbmi.bd2k.irct.action.query.ExecuteQuery;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultSet;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.ResultSetException;

@Stateless
public class ExecutionController {
	
	@Inject
	Logger log;
	
	
	@Asynchronous
	public Future<String> runQuery(Query query) {
		log.info("Start: " + query.getId());
		String resultSize = "";
		try {
			ExecuteQuery eq = new ExecuteQuery();
			eq.setQuery(query);
			eq.run();
			ResultSet rs = eq.getResults();
			try {
				resultSize = resultSize + rs.getSize();
			} catch (ResultSetException e) {
				e.printStackTrace();
			}
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		log.info("End: " + query.getId());
		return new AsyncResult<String>("Complete: " + resultSize);
	}
}
