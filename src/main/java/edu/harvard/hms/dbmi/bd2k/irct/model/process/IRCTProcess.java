/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.process;

import java.util.LinkedHashMap;
import java.util.Map;

import edu.harvard.hms.dbmi.bd2k.irct.model.resource.ProcessType;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultSet;

/**
 * Creates an executable process
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class IRCTProcess {
	private Long id;
	private ProcessType processType;
	private Resource resource;
	private Map<String, String> values;
	private Map<String, ResultSet> resultSets;
	
	public IRCTProcess() {
		this.setValues(new LinkedHashMap<String, String>());
		this.setResultSets(new LinkedHashMap<String, ResultSet>());
	}
	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}
	/**
	 * @return the processType
	 */
	public ProcessType getProcessType() {
		return processType;
	}
	/**
	 * @param processType the processType to set
	 */
	public void setProcessType(ProcessType processType) {
		this.processType = processType;
	}
	/**
	 * @return the resources
	 */
	public Resource getResource() {
		return resource;
	}
	/**
	 * @param resource the resource to set
	 */
	public void setResource(Resource resource) {
		this.resource = resource;
	}
	/**
	 * @return the values
	 */
	public Map<String, String> getValues() {
		return values;
	}
	/**
	 * @param values the values to set
	 */
	public void setValues(Map<String, String> values) {
		this.values = values;
	}
	/**
	 * @return the resultSets
	 */
	public Map<String, ResultSet> getResultSets() {
		return resultSets;
	}
	/**
	 * @param resultSets the resultSets to set
	 */
	public void setResultSets(Map<String, ResultSet> resultSets) {
		this.resultSets = resultSets;
	}
	
	
}
