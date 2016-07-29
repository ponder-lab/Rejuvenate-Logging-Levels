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
import javax.persistence.Transient;

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
	
	@Transient
	private Map<String, Object> objectValues; 

	/**
	 * Creates an IRCT process
	 * 
	 */
	public IRCTProcess() {
		this.setResources(new ArrayList<Resource>());
		this.stringValues = new HashMap<String, String>();
	}
	
	public String toString() {
		
		String processFields = "";
		for (String processField : this.stringValues.keySet()) {
			if (!processFields.equals("")) {
				processFields += ", ";
			}
			processFields += processField + "="
					+ this.stringValues.get(processField);
		}
		return processType.getDisplayName() + " " + processFields;
	}

	/**
	 * Returns the process id
	 * 
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Sets the process id
	 * 
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Returns the type of process this is
	 * 
	 * @return the processType
	 */
	public ProcessType getProcessType() {
		return processType;
	}

	/**
	 * Sets the type of process this is
	 * 
	 * @param processType
	 *            the processType to set
	 */
	public void setProcessType(ProcessType processType) {
		this.processType = processType;
	}

	/**
	 * Returns a list of resources the process is to run on
	 * 
	 * @return the resources
	 */
	public List<Resource> getResources() {
		return resources;
	}

	/**
	 * Sets the list of resources the process is to run on
	 * 
	 * @param resources
	 *            the resources to set
	 */
	public void setResources(List<Resource> resources) {
		this.resources = resources;
	}

	/**
	 * Returns a map of string representation of the values of the fields for the process
	 * @return the stringValues
	 */
	public Map<String, String> getStringValues() {
		return stringValues;
	}

	/**
	 * Sets a map of string representation of the values of the fields for the process
	 * 
	 * @param stringValues
	 *            the stringValues to set
	 */
	public void setStringValues(Map<String, String> stringValues) {
		this.stringValues = stringValues;
	}

	/**
	 * Returns a map of the values of the fields for the process
	 * 
	 * @return the objectValues
	 */
	public Map<String, Object> getObjectValues() {
		return objectValues;
	}

	/**
	 * Sets a map of the values of the fields for the process
	 * 
	 * @param objectValues the objectValues to set
	 */
	public void setObjectValues(Map<String, Object> objectValues) {
		this.objectValues = objectValues;
	}

}
