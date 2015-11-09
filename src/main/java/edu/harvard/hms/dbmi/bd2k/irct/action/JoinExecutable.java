/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.action;

import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultSet;

/**
 * The query executable implements the Executable interface for executing
 * local joins. It also supports binary tree executions through the
 * BinaryTreeExecutable interface.
 * 
 * NOTE: The current implementation has not been implemented
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
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
