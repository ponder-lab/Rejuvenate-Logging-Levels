/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.ontology;

import java.util.regex.Pattern;

import javax.json.JsonObject;

/**
 * An interface that is implemented to create a set of data types that will be supported by a resource
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public interface DataType {
	/**
	 * Returns the byte array representation of the give object
	 * 
	 * @param value
	 *            Object to convert
	 * @return Byte array
	 */
	public abstract byte[] toBytes(Object value);

	/**
	 * Returns the object representation of the given byte array
	 * 
	 * @param bytes
	 *            Byte array
	 * @return Value
	 */
	public abstract Object fromBytes(byte[] bytes);

	/**
	 * Returns the name of the data type
	 * 
	 * @return Name
	 */
	public abstract String getName();

	/**
	 * Returns the pattern to validate the data type
	 * 
	 * @return Pattern
	 */
	public abstract Pattern getPattern();

	/**
	 * Returns a description of the data type
	 * 
	 * @return Description
	 */
	public abstract String getDescription();

	/**
	 * Returns the type of data type it is.
	 * 
	 * @return Type
	 */
	public abstract DataType typeOf();

	/**
	 * Turns the data type into byte array from a String. If it does not match
	 * the pattern it will return null
	 * 
	 * @param value
	 *            String representation
	 * @return Byte representation
	 */
	public abstract byte[] fromString(String value);

	/**
	 * Converts a byte array to a String representation of the data type
	 * 
	 * @param bytes
	 *            Byte representation
	 * @return String representation
	 */
	public abstract String toString(byte[] bytes);

	/**
	 * Validates the String to ensure it is in the correct format
	 * 
	 * @param value
	 *            String representation
	 * @return True if valid, False if not valid
	 */
	public abstract boolean validate(String value);

	/**
	 * Returns a JSONObject representation of the object. This returns only the
	 * attributes associated with this object and not their representation.
	 * 
	 * This is equivalent of toJson(1);
	 * 
	 * @return JSON Representation
	 */
	public abstract JsonObject toJson();
}
