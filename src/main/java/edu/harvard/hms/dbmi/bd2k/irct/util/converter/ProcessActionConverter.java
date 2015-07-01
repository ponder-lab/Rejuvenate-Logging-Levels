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
package edu.harvard.hms.dbmi.bd2k.irct.util.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import edu.harvard.hms.dbmi.bd2k.irct.action.process.ProcessAction;

/**
 * Converts a ProcessAction to a String representation of the class to allow for
 * storage inside a Relational Database
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@Converter
public class ProcessActionConverter implements
		AttributeConverter<ProcessAction, String> {

	public String convertToDatabaseColumn(ProcessAction joinAction) {
		if (joinAction != null) {
			return joinAction.getClass().getName();
		}
		return null;
	}

	public ProcessAction convertToEntityAttribute(String className) {
		if (className != null) {
			ClassLoader cl = ProcessActionConverter.class.getClassLoader();
			try {
				return (ProcessAction) cl.loadClass(className).newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		return null;
	}

}
