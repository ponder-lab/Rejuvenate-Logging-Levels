/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.action.process;

import edu.harvard.hms.dbmi.bd2k.irct.action.Action;
import edu.harvard.hms.dbmi.bd2k.irct.model.process.IRCTProcess;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;

/**
 * This interface extends the action interface for running processes
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public interface ProcessAction extends Action {
	/**
	 * Sets up a process to be run on a specific resource
	 * 
	 * @param resource Resource to run on
	 * @param process Process to run
	 */
	void setup(Resource resource, IRCTProcess process);

	/**
	 * Returns the process to be run
	 * 
	 * @return Process to be run
	 */
	IRCTProcess getProcess();

	/**
	 * Sets the process to be run
	 * 
	 * @param process Process to be run
	 */
	void setProcess(IRCTProcess process);
}
