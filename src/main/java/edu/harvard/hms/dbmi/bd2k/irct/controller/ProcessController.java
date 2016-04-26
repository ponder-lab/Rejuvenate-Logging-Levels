package edu.harvard.hms.dbmi.bd2k.irct.controller;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.ejb.Stateful;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import edu.harvard.hms.dbmi.bd2k.irct.exception.ProcessException;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.DataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.process.IRCTProcess;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Field;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.ProcessType;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;

@Stateful
public class ProcessController {
	@PersistenceContext
	EntityManager entityManager;

	@Inject
	Logger log;

	private IRCTProcess process;

	public void createProcess() {
		this.process = new IRCTProcess();
	}

	public void saveProcess() throws ProcessException {
		if (this.process == null) {
			throw new ProcessException("No process to save.");
		}
		if (this.process.getId() == null) {
			entityManager.persist(this.process);
		} else {
			entityManager.merge(this.process);
		}
		log.info("Query " + this.process.getId() + " saved");

	}

	public void loadProcess(Long processId) throws ProcessException {
		if (processId == null) {
			throw new ProcessException("No process id.");
		}

		this.process = entityManager.find(IRCTProcess.class, processId);
		if (this.process == null) {
			throw new ProcessException("No process to load.");
		}
		log.info("Query " + this.process.getId() + " loaded");
	}

	public void updateProcess(Resource resource, ProcessType processType,
			Map<String, String> processFields) throws ProcessException {
		//Is the resource part of the process
		if(this.process.getResources().isEmpty()) {
			this.process.getResources().add(resource);
		} else if (!this.process.getResources().contains(resource)) {
			throw new ProcessException("Processes only support one resource");
		}
		//Does the resource support the processtype
		if((processType != null) && (!resource.getSupportedProcesses().contains(processType))) {
			throw new ProcessException("Logical operator is not supported by the resource");
		}
		
		//Are the fields valid?
		validateFields(processType.getFields(), processFields);
		
		//Update process
		this.process.setProcessType(processType);
		this.process.setStringValues(processFields);
	}
	
	private void validateFields(List<Field> fields, Map<String, String> valueFields) throws ProcessException {
		for(Field predicateField : fields) {
			//Is the predicate field required and is in the query fields
			if(predicateField.isRequired() && (!valueFields.containsKey(predicateField.getPath()))) {
				throw new ProcessException("Required field is not set");
			}
			String queryFieldValue = valueFields.get(predicateField.getPath());
			
			if(queryFieldValue != null) {
				//Is the predicate field data type allowed for this query field
				if(!predicateField.getDataTypes().isEmpty()) {
					boolean validateFieldValue = false;
					
					for(DataType dt : predicateField.getDataTypes()) {
						if(dt.validate(queryFieldValue)) {
							validateFieldValue = true;
							break;
						}
					}
					
					if(!validateFieldValue) {
						throw new ProcessException("The field value set is not a supported type for this field");
					}
				}
				//Is the predicate field of allowedTypes
				if(!predicateField.getPermittedValues().isEmpty() && (!predicateField.getPermittedValues().contains(queryFieldValue))) {
					throw new ProcessException("The field value set is not a required field");
				}
			}
		}
	}

	public IRCTProcess getProcess() {
		return this.process;
	}

}
