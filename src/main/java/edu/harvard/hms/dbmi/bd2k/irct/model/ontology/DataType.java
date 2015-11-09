/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.ontology;

public interface DataType {
	/**
	 * Returns the byte array representation of the give object
	 * 
	 * @param value Object to convert
	 * @return Byte array
	 */
	public abstract byte[] toBytes(Object value);

	/**
	 * Returns the object representation of the given byte array
	 * 
	 * @param bytes Byte array
	 * @return Value
	 */
	public abstract Object fromBytes(byte[] bytes);
}
