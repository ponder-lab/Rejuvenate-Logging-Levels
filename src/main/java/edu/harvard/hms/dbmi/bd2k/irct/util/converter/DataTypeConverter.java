package edu.harvard.hms.dbmi.bd2k.irct.util.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.DataType;

@Converter(autoApply = true)
public class DataTypeConverter implements AttributeConverter<DataType, String> {

	@Override
	public String convertToDatabaseColumn(DataType dataType) {
		if(dataType != null) {
			return dataType.getClass().getName().split("\\$")[0] + ":" + dataType.toString();
		}
		return null;
	}

	@Override
	public DataType convertToEntityAttribute(String dataTypeString) {
		if(dataTypeString != null) {
			String[] split = dataTypeString.split(":");
			try {
				Class enumClass = Class.forName(split[0]);
				
				if(enumClass.isEnum()) {
					return Enum.valueOf(enumClass, split[1]);
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
