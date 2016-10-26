/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation;

import java.io.Serializable;
import java.util.Map;

import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;

/**
 * Provides a base interface that is used by a resource implementation 
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public interface ResourceImplementationInterface extends Serializable {
	/**
	 * A set of parameters that can used to setup the Resource Implementation
	 * 
	 * @param parameters
	 *            Setup parameters
	 * @throws ResourceInterfaceException Throws a resource interface exception 
	 */
	void setup(Map<String, String> parameters) throws ResourceInterfaceException;

	/**
	 * A string representation of the type of resource implementation this is
	 * 
	 * @return Type Type of Resource
	 */
	String getType();
}
