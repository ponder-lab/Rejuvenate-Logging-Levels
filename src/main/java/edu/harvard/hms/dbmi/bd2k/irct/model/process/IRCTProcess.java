/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.process;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;

import edu.harvard.hms.dbmi.bd2k.irct.model.resource.ProcessType;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;

/**
 * Creates an executable process
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@Entity
public class IRCTProcess implements Serializable {
	private static final long serialVersionUID = -1805899138692864630L;

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne
	private ProcessType processType;

	@OneToMany
	private List<Resource> resources;

	@ElementCollection
	@CollectionTable(name = "process_values", joinColumns = @JoinColumn(name = "PROCESS_VALUE"))
	@MapKeyColumn(name = "process_id")
	@Column(name = "process_value")
	private Map<String, String> stringValues;

	public IRCTProcess() {
		this.setResources(new ArrayList<Resource>());
		this.stringValues = new HashMap<String, String>();
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
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
	 * @param processType
	 *            the processType to set
	 */
	public void setProcessType(ProcessType processType) {
		this.processType = processType;
	}

	/**
	 * @return the resources
	 */
	public List<Resource> getResources() {
		return resources;
	}

	/**
	 * @param resources
	 *            the resources to set
	 */
	public void setResources(List<Resource> resources) {
		this.resources = resources;
	}

	/**
	 * @return the stringValues
	 */
	public Map<String, String> getStringValues() {
		return stringValues;
	}

	/**
	 * @param stringValues
	 *            the stringValues to set
	 */
	public void setStringValues(Map<String, String> stringValues) {
		this.stringValues = stringValues;
	}

}
