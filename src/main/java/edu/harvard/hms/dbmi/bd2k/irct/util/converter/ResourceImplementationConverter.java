/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.util.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import edu.harvard.hms.dbmi.bd2k.irct.model.resource.ResourceImplementationInterface;

/**
 * Converts a Resource Implementation to a String representation of the class to
 * allow for storage inside a Relational Database
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@Converter
public class ResourceImplementationConverter implements
		AttributeConverter<ResourceImplementationInterface, String> {

	@Override
	public String convertToDatabaseColumn(
			ResourceImplementationInterface joinAction) {
		if (joinAction != null) {
			return joinAction.getClass().getName();
		}
		return null;
	}

	@Override
	public ResourceImplementationInterface convertToEntityAttribute(
			String className) {
		if (className != null) {
			ClassLoader cl = ResourceImplementationConverter.class.getClassLoader();
			try {
				return (ResourceImplementationInterface) cl
						.loadClass(className).newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		return null;
	}

}
