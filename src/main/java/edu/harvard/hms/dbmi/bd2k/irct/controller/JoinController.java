/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateful;
import javax.inject.Inject;

import edu.harvard.hms.dbmi.bd2k.irct.IRCTApplication;
import edu.harvard.hms.dbmi.bd2k.irct.exception.JoinException;
import edu.harvard.hms.dbmi.bd2k.irct.model.join.IRCTJoin;
import edu.harvard.hms.dbmi.bd2k.irct.model.join.Join;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.DataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Field;

/**
 * A stateful controller for creating joins.
 * 
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@Stateful
public class JoinController {

	@Inject
	private IRCTApplication irctApp;
	
	private Join join;

	/**
	 * Creates a new join
	 * 
	 */
	public void createJoin() {
		this.setJoin(new Join());
	}

	/**
	 * Sets up an IRCT Join
	 * 
	 * @param irctJoin Join to perform
	 * @param fields Map of field values
	 * @param objectFields Map of object values
	 * @throws JoinException A Join Exception occurred
	 */
	public void setup(IRCTJoin irctJoin, Map<String, String> fields,
			Map<String, Object> objectFields) throws JoinException {
		validateFields(irctJoin.getFields(), fields, objectFields);

		getJoin().setJoinImplementation(irctJoin.getJoinImplementation());
		getJoin().setStringValues(fields);
		getJoin().setObjectValues(objectFields);
	}

	private void validateFields(List<Field> fields,
			Map<String, String> valueFields, Map<String, Object> objectFields)
			throws JoinException {

		for (Field predicateField : fields) {

			if (predicateField.isRequired()
					&& ((valueFields != null) && (valueFields
							.containsKey(predicateField.getPath())))) {
				String queryFieldValue = valueFields.get(predicateField
						.getPath());

				if (queryFieldValue != null) {
					// Is the predicate field data type allowed for this query
					// field
					if (!predicateField.getDataTypes().isEmpty()) {
						boolean validateFieldValue = false;

						for (DataType dt : predicateField.getDataTypes()) {
							if (dt.validate(queryFieldValue)) {
								validateFieldValue = true;
								break;
							}
						}

						if (!validateFieldValue) {
							throw new JoinException(
									"The field value set is not a supported type for this field");
						}
					}
					// Is the predicate field of allowedTypes
					if (!predicateField.getPermittedValues().isEmpty()
							&& (!predicateField.getPermittedValues().contains(
									queryFieldValue))) {
						throw new JoinException(
								"The field value is not of an allowed type");
					}
				}

			} else if (predicateField.isRequired()
					&& ((objectFields != null) && (objectFields
							.containsKey(predicateField.getPath())))) {

			} else if (predicateField.isRequired()) {
				throw new JoinException("Required field "
						+ predicateField.getName() + " is not set");
			}
		}
	}
	
	public IRCTJoin getIRCTJoin(String joinName) {
		return irctApp.getSupportedJoinTypes().get(joinName);
	}
	
	public List<IRCTJoin> getSupportedJoins() {
		return new ArrayList<IRCTJoin>(irctApp.getSupportedJoinTypes().values());
	}

	/**
	 * @return the join
	 */
	public Join getJoin() {
		return join;
	}

	/**
	 * @param join
	 *            the join to set
	 */
	public void setJoin(Join join) {
		this.join = join;
	}

	
}
