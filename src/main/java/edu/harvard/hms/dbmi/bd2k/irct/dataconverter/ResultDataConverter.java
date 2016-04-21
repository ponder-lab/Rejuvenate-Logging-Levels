/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.dataconverter;

import javax.ws.rs.core.StreamingOutput;

import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultDataType;

public interface ResultDataConverter {
	
	public ResultDataType getResultDataType();
	public String getName();
	public String getMediaType();
	
	public StreamingOutput createStream(final Result result);
	public String getFileExtension();
}
