/*
 *  This file is part of Inter-Resource Communication Tool (IRCT).
 *
 *  IRCT is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  IRCT is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with IRCT.  If not, see <http://www.gnu.org/licenses/>.
 */
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
