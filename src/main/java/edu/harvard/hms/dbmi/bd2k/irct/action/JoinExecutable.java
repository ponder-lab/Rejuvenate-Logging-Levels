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
package edu.harvard.hms.dbmi.bd2k.irct.action;

import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultSet;

public class JoinExecutable implements Executable, BinaryTreeExecutable {
	private Resource resource;
	
	public void setup(Action action) {
		// TODO Auto-generated method stub
		
	}

	public void run() {
		// TODO Auto-generated method stub
		
	}

	public ExecutableState getState() {
		// TODO Auto-generated method stub
		return null;
	}

	public ResultSet getResults() {
		// TODO Auto-generated method stub
		return null;
	}

	public Executable getLeft() {
		// TODO Auto-generated method stub
		return null;
	}

	public Executable getRight() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void setResource(Resource resource) {
		this.resource = resource;
	}
	public Resource getResource() {
		return this.resource;
	}
	
}
