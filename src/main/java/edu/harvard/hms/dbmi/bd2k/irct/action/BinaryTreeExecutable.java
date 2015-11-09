/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.action;

/**
 * The binary tree executable interface provides an implementation for branches
 * that can have two different child executables.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public interface BinaryTreeExecutable {
	/**
	 * Returns the left executable
	 * @return Left Executable
	 */
	Executable getLeft();

	/**
	 * Returns the right executable
	 * @return Right Executable
	 */
	Executable getRight();
}
