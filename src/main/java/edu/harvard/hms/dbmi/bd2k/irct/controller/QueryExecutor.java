package edu.harvard.hms.dbmi.bd2k.irct.controller;

import java.util.concurrent.Future;
import java.util.logging.Logger;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;

import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;

@Stateless
public class QueryExecutor {
	
	@Inject
	Logger log;
	
	
	@Asynchronous
	public Future<String> runQuery(Query query) {
		log.info("Start: " + query.getId());
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		log.info("End: " + query.getId());
		return new AsyncResult<String>("Complete");
	}
}
