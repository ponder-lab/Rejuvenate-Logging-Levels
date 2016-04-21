/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package edu.harvard.hms.dbmi.bd2k.irct.dataconverter;

import javax.ws.rs.core.StreamingOutput;

public class ResultDataStream {
	private String mediaType;
	private StreamingOutput result;
	private String message;
	private String fileExtension;
	
	/**
	 * @return the mediaType
	 */
	public String getMediaType() {
		return mediaType;
	}
	/**
	 * @param mediaType the mediaType to set
	 */
	public void setMediaType(String mediaType) {
		this.mediaType = mediaType;
	}
	/**
	 * @return the result
	 */
	public StreamingOutput getResult() {
		return result;
	}
	/**
	 * @param result the result to set
	 */
	public void setResult(StreamingOutput result) {
		this.result = result;
	}
	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	
	/**
	 * @param fileExtension the fileExtension to set
	 */
	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}
	
	
	public String getFileExtension() {
		return this.fileExtension;
	}
	
	
	
}
