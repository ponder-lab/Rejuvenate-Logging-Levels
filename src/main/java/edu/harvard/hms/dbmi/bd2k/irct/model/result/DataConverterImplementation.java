/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.result;

import java.io.Serializable;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import edu.harvard.hms.dbmi.bd2k.irct.dataconverter.ResultDataConverter;
import edu.harvard.hms.dbmi.bd2k.irct.util.converter.ResultDataImplementationConverter;

/**
 * A representation of a data converter between the IRCT Result Set of a given
 * type and type a user would like to receive the format in.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@Entity
public class DataConverterImplementation implements Serializable {

	private static final long serialVersionUID = -8700481468758389000L;

	@Id
	@GeneratedValue
	private long id;

	@Enumerated(EnumType.STRING)
	private ResultDataType resultDataType;

	private String format;

	@Convert(converter = ResultDataImplementationConverter.class)
	private ResultDataConverter dataConverter;

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
	 * Returns which data type this Data Converter Implementation supports
	 * 
	 * @return the resultDataType
	 */
	public ResultDataType getResultDataType() {
		return resultDataType;
	}

	/**
	 * Sets the data type this Data Converter Implementation supports
	 * 
	 * @param resultDataType
	 *            the resultDataType to set
	 */
	public void setResultDataType(ResultDataType resultDataType) {
		this.resultDataType = resultDataType;
	}

	/**
	 * Returns the format that this Data Converter Implementation will return
	 * 
	 * @return the format
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * Sets the format that this Data Converter Implementation will return
	 * 
	 * @param format
	 *            The format to set
	 * 
	 */
	public void setFormat(String format) {
		this.format = format;
	}

	/**
	 * Returns the class that will do the conversion into the desired format
	 * 
	 * @return the dataConverter
	 */
	public ResultDataConverter getDataConverter() {
		return dataConverter;
	}

	/**
	 * Sets the class that will do the conversion into the desired format
	 * 
	 * @param dataConverter
	 *            the dataConverter to set
	 */
	public void setDataConverter(ResultDataConverter dataConverter) {
		this.dataConverter = dataConverter;
	}

}
