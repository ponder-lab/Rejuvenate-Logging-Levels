/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.dataconverter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.ResultSetException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular.Column;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular.ResultSet;

/**
 * A data converter that returns a JSON stream
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class JSONTabularDataConverter implements ResultDataConverter {
	
	@Inject
	Logger log;

	@Override
	public ResultDataType getResultDataType() {
		return ResultDataType.TABULAR;
	}
	
	@Override
	public String getFileExtension() {
		return ".json";
	}

	@Override
	public String getName() {
		return "JSON";
	}

	@Override
	public String getMediaType() {
		return MediaType.APPLICATION_JSON;
	}

	@Override
	public StreamingOutput createStream(final Result result) {
		StreamingOutput stream = new StreamingOutput() {
			@Override
			public void write(OutputStream outputStream) throws IOException,
					WebApplicationException {
				try {
					ResultSet rs = (ResultSet) result.getData();
					rs.load(result.getResultSetLocation());
					Map<String, Object> properties = new HashMap<String, Object>(
							1);
					JsonGeneratorFactory jgf = Json.createGeneratorFactory(properties);
					JsonGenerator jg = jgf.createGenerator(outputStream);

					jg.writeStartObject(); //Start Object
					jg.writeStartArray("columns");

					// Get columns
					for (Column column : rs.getColumns()) {
						jg.write(column.toJson());
					}
					jg.writeEnd(); //End columns
					jg.writeStartArray("data");
					
					rs.beforeFirst();
					while (rs.next()) {
						jg.writeStartArray(); //Begin Row Array
						for (int columnIndex = 0; columnIndex < rs.getColumnSize(); columnIndex++) {
							String value = rs.getString(columnIndex);
							if(value != null) {
								jg.writeStartObject();
								jg.write(rs.getColumn(columnIndex).getName(), rs.getString(columnIndex));
								jg.writeEnd();
							}
							
						}
						jg.writeEnd(); //End Row Array
					}
					
					
					jg.writeEnd(); //End data
					jg.writeEnd(); //End Full Object
					jg.close();
				} catch (ResultSetException | PersistableException e) {
					log.info("Error creating JSON Stream: " + e.getMessage());
				}
				outputStream.close();
			}
		};
		return stream;
	}
}
