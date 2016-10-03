/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.result;

import java.io.File;
import java.util.List;

import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.ResultSetException;

/**
 * An empty interface that is implemented by all data types 
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public interface Data {
	public List<File> getFileList();
	// Administrative
	/**
	 * Releases the connection to the datasource
	 * 
	 * @throws ResultSetException
	 *             If a ResultSetException occurs
	 */
	void close() throws ResultSetException;

	/**
	 * Returns the status of the connection to the datasource
	 * 
	 * @return true if closed
	 */
	boolean isClosed();

}
