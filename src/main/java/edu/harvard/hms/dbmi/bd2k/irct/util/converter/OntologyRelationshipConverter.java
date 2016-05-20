package edu.harvard.hms.dbmi.bd2k.irct.util.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyRelationship;

/**
 * Converts a OntologyRelationship to a String representation of the class to allow for
 * storage inside a Relational Database
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@Converter
public class OntologyRelationshipConverter implements AttributeConverter<OntologyRelationship, String> {

	@Override
	public String convertToDatabaseColumn(OntologyRelationship ontologyRelationship) {
		if(ontologyRelationship != null) {
			return ontologyRelationship.getClass().getName().split("\\$")[0] + ":" + ontologyRelationship.toString();
		}
		return null;
	}

	@Override
	public OntologyRelationship convertToEntityAttribute(String ontologyRelationshipString) {
		if(ontologyRelationshipString != null) {
			String[] split = ontologyRelationshipString.split(":");
			try {
				Class enumClass = Class.forName(split[0]);
				if(enumClass.isEnum()) {
					return (OntologyRelationship) Enum.valueOf(enumClass, split[1]);
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
