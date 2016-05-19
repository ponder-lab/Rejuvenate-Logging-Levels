/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.dataconverter;

import javax.ws.rs.core.StreamingOutput;

import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultDataType;

/**
 * A base interface that is implemented to create a set of data converters for output from the IRCT format
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public interface ResultDataConverter {
	
	/**
	 * Returns the data type the data converter supports
	 * 
	 * @return Result Data Type
	 */
	public ResultDataType getResultDataType();
	
	/**
	 * Returns the name of the converter
	 * 
	 * @return Name
	 */
	public String getName();
	
	/**
	 * Returns the media type the converter supports
	 * 
	 * @return Media type
	 */
	public String getMediaType();
	
	/**
	 * Returns an output stream 
	 * 
	 * @param result Result to stream out
	 * @return Output Stream
	 */
	public StreamingOutput createStream(final Result result);
	
	/**
	 * Returns the file extension that is typically associated with a file of that type
	 * 
	 * @return File Extension
	 */
	public String getFileExtension();
}
