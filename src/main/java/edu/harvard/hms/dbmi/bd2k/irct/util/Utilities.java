/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.harvard.hms.dbmi.bd2k.irct.controller.ResultController;
import edu.harvard.hms.dbmi.bd2k.irct.exception.FieldException;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.DataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Field;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.ResultSetException;

/**
 * A collection of static methods that provide shared functionality throughout
 * the IRCT-API
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class Utilities {

	public static boolean validateField(Field field, String value) {
		if (field.getDataTypes() == null || field.getDataTypes().isEmpty()) {
			return true;
		}

		for (DataType dataType : field.getDataTypes()) {
			if (dataType.validate(value)) {
				return true;
			}
		}

		return false;
	}
	
	public static Map<String, Object> createActionParametersFromStringMap(List<Field> fields, Map<String, String> parameters, ResultController resultController) throws FieldException {
		Map<String, Object> actionParameters = new HashMap<String, Object>();
		for(Field field : fields) {
			String value = parameters.get(field.getPath());
			
			if((value == null) && field.isRequired()) {
				throw new FieldException(field.getName() + " is not set");
			}
			
			if((field != null) && (Utilities.validateField(field, value))) {
				if(field.getDataTypes().contains(PrimitiveDataType.RESULTSET)) {
					try {
						actionParameters.put(field.getPath(), resultController.getResultSet(Long.parseLong(value, 10)));
					} catch (NumberFormatException | ResultSetException
							| PersistableException e) {
						throw new FieldException("Unable to set " + field.getName() + " with ResultSet " + value);
					}
				} else {
					actionParameters.put(field.getPath(), value);
				}
			}
		}
		return actionParameters;
	}

}
