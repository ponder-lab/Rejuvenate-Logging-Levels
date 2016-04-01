/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.controller;

import java.util.Map;

import javax.ejb.Stateful;
import javax.inject.Inject;

import edu.harvard.hms.dbmi.bd2k.irct.model.process.IRCTProcess;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Field;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.ProcessType;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.ResultSetException;

/**
 * This is a stateful controller that creates processes and ensures that they are put together correctly
 * 
 * NOTE: NOT ALL CHECKS ARE IMPLEMENTED
 * NOTE: NOT ALL METHODS HAVE BEEN FULLY IMPLEMENTED AND VETTED
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@Stateful
public class ProcessController {

	private Long lastId;
	private IRCTProcess process;
	
	@Inject
	private ResultController rc;
	/**
	 * Creates a process
	 * 
	 * @param processId Process Id
	 */
	public void createProcess(String processId) {
		this.setProcess(new IRCTProcess());
		this.getProcess().setId(Long.parseLong(processId));
		this.setLastId(0L);
	}
	
	/**
	 * Updates the process information
	 * 
	 * @param resource Resource to run on
	 * @param processName Process to run
	 * @param values Map of values
	 */
	public void updateProcess(Resource resource, String processName, Map<String, String> values) {
		ProcessType pt = findProcess(resource, processName);
		process.setResource(resource);
		try {
			for(Field field : pt.getFields()) {
				if(field.getDataTypes().contains(PrimitiveDataType.RESULTSET)) {
					process.getResultSets().put(field.getName(), rc.getResultSet(Long.parseLong(values.get(field.getName()), 10)));
				} else {
					process.getValues().put(field.getName(), values.get(field.getName()));
				}
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (ResultSetException e) {
			e.printStackTrace();
		} catch (PersistableException e) {
			e.printStackTrace();
		}
		
		
		
		
		process.setProcessType(pt);
	}
	
	/**
	 * Cancels the process
	 */
	public void cancelProcess() {
		this.setProcess(null);
		this.setLastId(0L);
	}
	
	private ProcessType findProcess(Resource resource, String processName) {
		for(ProcessType process : resource.getSupportedProcesses()) {
			if(process.getName().equals(processName)) {
				return process;
			}
		}
		
		return null;
	}

	/**
	 * Returns the last Id
	 * 
	 * @return the lastId
	 */
	public Long getLastId() {
		return lastId;
	}

	/**
	 * Sets the last Id
	 * 
	 * @param lastId the lastId to set
	 */
	public void setLastId(Long lastId) {
		this.lastId = lastId;
	}

	/**
	 * Returns the process
	 * 
	 * @return the process
	 */
	public IRCTProcess getProcess() {
		return process;
	}

	/**
	 * Sets the process
	 * 
	 * @param process the process to set
	 */
	public void setProcess(IRCTProcess process) {
		this.process = process;
	}
}
