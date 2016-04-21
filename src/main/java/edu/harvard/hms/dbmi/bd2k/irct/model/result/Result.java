/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.result;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.SequenceGenerator;

import edu.harvard.hms.dbmi.bd2k.irct.executable.Executable;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.User;
import edu.harvard.hms.dbmi.bd2k.irct.util.converter.DataConverter;

/**
 * The result class is created for each execution that is run on the IRCT
 * (Query, Process, etc...). It provides a way of the end user to rerun
 * processes, and retrieve the results.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@Entity
public class Result {
	@Id
	@GeneratedValue(generator = "resultSequencer")
	@SequenceGenerator(name = "resultSequencer", sequenceName = "resSeq")
	private Long id;

	// TODO: REMOVE TRANSIENT
	@Transient
	private Executable executable;

	@ManyToOne
	private User user;

	@Temporal(TemporalType.TIMESTAMP)
	private Date startTime;

	@Temporal(TemporalType.TIMESTAMP)
	private Date endTime;

	@Enumerated(EnumType.STRING)
	private ResultStatus resultStatus;

	@Enumerated(EnumType.STRING)
	private ResultDataType dataType;

	private String resourceActionId;

	@Convert(converter = DataConverter.class)
	private Data data;
	private String resultSetLocation;

	private String message;

	/**
	 * Returns a JSONObject representation of the object. This returns only the
	 * attributes associated with this object and not their representation.
	 * 
	 * This is equivalent of toJson(1);
	 * 
	 * @return JSON Representation
	 */
	public JsonObject toJson() {
		return toJson(1);
	}

	/**
	 * Returns a JSONObject representation of the object. This returns only the
	 * attributes associated with this object and not their representation.
	 * 
	 * 
	 * @param depth
	 *            Depth to travel
	 * @return JSON Representation
	 */
	public JsonObject toJson(int depth) {
		depth--;
		JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
		jsonBuilder.add("id", this.id);
		jsonBuilder.add("status", this.resultStatus.toString());
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		jsonBuilder.add("runTime", formatter.format(new Date()));

		return jsonBuilder.build();
	}

	// -------------------------------------------------------------------------
	// SETTERS AND GETTERS
	// -------------------------------------------------------------------------

	/**
	 * Returns the id of the result
	 * 
	 * @return Id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Sets the id of the result
	 * 
	 * @param id
	 *            Id
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Returns the executable used for the result
	 * 
	 * @return Executable
	 */
	public Executable getExecutable() {
		return executable;
	}

	/**
	 * Sets the executable used for the result
	 * 
	 * @param executable
	 *            Executable
	 */
	public void setExecutable(Executable executable) {
		this.executable = executable;
	}

	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	/**
	 * @param user
	 *            the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * @return the startTime
	 */
	public Date getStartTime() {
		return startTime;
	}

	/**
	 * @param startTime
	 *            the startTime to set
	 */
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	/**
	 * @return the endTime
	 */
	public Date getEndTime() {
		return endTime;
	}

	/**
	 * @param endTime
	 *            the endTime to set
	 */
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	/**
	 * Gets the result status
	 * 
	 * @return Result status
	 */
	public ResultStatus getResultStatus() {
		return resultStatus;
	}

	/**
	 * Sets the result status
	 * 
	 * @param resultStatus
	 *            Result status
	 */
	public void setResultStatus(ResultStatus resultStatus) {
		this.resultStatus = resultStatus;
	}

	public ResultDataType getDataType() {
		return dataType;
	}

	public void setDataType(ResultDataType dataType) {
		this.dataType = dataType;
	}

	public String getResourceActionId() {
		return resourceActionId;
	}

	public void setResourceActionId(String resourceActionId) {
		this.resourceActionId = resourceActionId;
	}

	/**
	 * Returns an instantiation of a class that implements the result status
	 * 
	 * @return Data Object
	 */
	public Data getData() {
		return data;
	}

	/**
	 * Sets the class that is used to implement the result status
	 * 
	 * @param implementingResultSet
	 *            Implementing Result Set
	 */
	public void setData(Data data) {
		this.data = data;
	}

	/**
	 * Returns the location of the result set
	 * 
	 * @return Result Set
	 */
	public String getResultSetLocation() {
		return resultSetLocation;
	}

	/**
	 * Sets the result location of the result set
	 * 
	 * @param resultSetLocation
	 *            Result Set
	 */
	public void setResultSetLocation(String resultSetLocation) {
		this.resultSetLocation = resultSetLocation;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
