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
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @return the resultDataType
	 */
	public ResultDataType getResultDataType() {
		return resultDataType;
	}

	/**
	 * @param resultDataType the resultDataType to set
	 */
	public void setResultDataType(ResultDataType resultDataType) {
		this.resultDataType = resultDataType;
	}

	/**
	 * @return the returnDataType
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * @param returnDataType the returnDataType to set
	 */
	public void setFormat(String format) {
		this.format = format;
	}

	/**
	 * @return the dataConverter
	 */
	public ResultDataConverter getDataConverter() {
		return dataConverter;
	}

	/**
	 * @param dataConverter the dataConverter to set
	 */
	public void setDataConverter(ResultDataConverter dataConverter) {
		this.dataConverter = dataConverter;
	}
	
	
	

}
