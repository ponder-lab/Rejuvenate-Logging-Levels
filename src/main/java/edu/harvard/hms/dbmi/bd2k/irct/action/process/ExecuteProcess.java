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


import edu.harvard.hms.dbmi.bd2k.irct.model.process.IRCTProcess;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.ProcessResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultSet;

/**
 * Implements the ProcessAction interface to run a process
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class ExecuteProcess implements ProcessAction {
	private IRCTProcess process ;
	private Resource resource;
	private Long runId;
	
	@Override
	public void setup(Resource resource, IRCTProcess process) {
		this.resource = resource;
		this.process = process;
		
	}
	
	@Override
	public void run() {
		runId = ((ProcessResourceImplementationInterface)resource.getImplementingInterface()).run(process);
	}

	@Override
	public ResultSet getResults() {
		return ((ProcessResourceImplementationInterface)resource.getImplementingInterface()).getResults(runId);
	}

	@Override
	public IRCTProcess getProcess() {
		return this.process;
	}

	@Override
	public void setProcess(IRCTProcess process) {
		this.process = process;
	}
}
