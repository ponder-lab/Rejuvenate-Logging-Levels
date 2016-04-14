/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.util.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import edu.harvard.hms.dbmi.bd2k.irct.model.result.Data;

/**
 * Converts a ResultSet to a String representation of the class to allow for
 * storage inside a Relational Database
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@Converter
public class DataConverter implements
		AttributeConverter<Data, String> {

	@Override
	public String convertToDatabaseColumn(Data resultSet) {
		if (resultSet != null) {
			return resultSet.getClass().getName();
		}
		return null;
	}

	@Override
	public Data convertToEntityAttribute(String className) {
		if (className != null) {
			ClassLoader cl = DataConverter.class.getClassLoader();
			try {
				return (Data) cl.loadClass(className).newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		return null;
	}

}
