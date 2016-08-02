/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.event;

import java.io.Serializable;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;

import edu.harvard.hms.dbmi.bd2k.irct.util.converter.IRCTEventImplementationConverter;

/**
 * A representation of a IRCT Event Listener
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@Entity
public class EventConverterImplementation implements Serializable {

	private static final long serialVersionUID = -8700481468758389000L;

	@Id
	@GeneratedValue
	private long id;

	private String name;
	
	@ElementCollection(fetch=FetchType.EAGER)
	@MapKeyColumn(name = "name")
	@Column(name = "value")
	@CollectionTable(name = "event_parameters", joinColumns = @JoinColumn(name = "id"))
	private Map<String, String> parameters;

	@Convert(converter = IRCTEventImplementationConverter.class)
	private IRCTEvent eventListener;

	/**
	 * Returns the Id of the Data Converter Implementation
	 * 
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * Sets the Id of the Data Converter Implementation
	 * 
	 * @param id
	 *            the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Returns the IRCT Event Listener Implementation
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the IRCT Event Listener Implementation 
	 * 
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the parameters
	 */
	public Map<String, String> getParameters() {
		return parameters;
	}

	/**
	 * @param parameters the parameters to set
	 */
	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	/**
	 * Returns the IRCT Event Converter Implementation
	 * 
	 * @return the eventListener
	 */
	public IRCTEvent getEventListener() {
		return eventListener;
	}

	/**
	 * Sets the IRCT Event Converter Implementation
	 * 
	 * @param eventListener
	 *            the eventListener to set
	 */
	public void setEventListener(IRCTEvent eventListener) {
		this.eventListener = eventListener;
	}

}
