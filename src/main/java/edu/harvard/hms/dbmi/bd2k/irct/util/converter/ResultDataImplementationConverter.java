/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.util.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import edu.harvard.hms.dbmi.bd2k.irct.dataconverter.ResultDataConverter;

/**
 * Converts a Resource Data Converter to a String representation of the class to
 * allow for storage inside a Relational Database
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@Converter
public class ResultDataImplementationConverter implements
		AttributeConverter<ResultDataConverter, String> {

	@Override
	public String convertToDatabaseColumn(
			ResultDataConverter resultDataConverter) {
		if (resultDataConverter != null) {
			return resultDataConverter.getClass().getName().split("\\$")[0];
		}
		return null;
	}

	@Override
	public ResultDataConverter convertToEntityAttribute(
			String className) {
		if (className != null) {
			try {
				Class<?> resultDataConverter = Class.forName(className);
				return (ResultDataConverter) resultDataConverter.newInstance();
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
			
		}
		return null;
	}

}
