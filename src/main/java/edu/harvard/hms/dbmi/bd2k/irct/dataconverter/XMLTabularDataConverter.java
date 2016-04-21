/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.dataconverter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.ResultSetException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular.ResultSet;

public class XMLTabularDataConverter implements ResultDataConverter {

	@Override
	public ResultDataType getResultDataType() {
		return ResultDataType.TABULAR;
	}
	
	@Override
	public String getFileExtension() {
		return ".xml";
	}

	@Override
	public String getName() {
		return "XML";
	}

	@Override
	public String getMediaType() {
		return MediaType.APPLICATION_XML;
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
					XMLOutputFactory xof = XMLOutputFactory.newInstance();
					XMLStreamWriter xtw = xof.createXMLStreamWriter(new OutputStreamWriter(outputStream));
					xtw.writeStartDocument("utf-8", "1.0");
					xtw.writeStartElement("results");
					rs.beforeFirst();
					while (rs.next()) {
						xtw.writeStartElement("result");
						for (int i = 0; i < rs.getColumnSize(); i++) {
							xtw.writeStartElement(rs.getColumn(i).getName().replace(" ", "_"));
							xtw.writeCharacters(rs.getString(i));
							xtw.writeEndElement();
						}
						xtw.writeEndElement();
					}
					xtw.writeEndElement();
					xtw.writeEndDocument();

					xtw.flush();
					xtw.close();
				} catch (ResultSetException | PersistableException | XMLStreamException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				outputStream.close();
			}
		};
		return stream;
	}
}
