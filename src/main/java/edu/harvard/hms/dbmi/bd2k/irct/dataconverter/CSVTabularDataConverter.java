/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.dataconverter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.ResultSetException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular.ResultSet;

public class CSVTabularDataConverter implements ResultDataConverter {

	@Override
	public ResultDataType getResultDataType() {
		return ResultDataType.TABULAR;
	}
	
	@Override
	public String getFileExtension() {
		return ".csv";
	}

	@Override
	public String getName() {
		return "CSV";
	}

	@Override
	public String getMediaType() {
		return "text/csv";
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
					
					CSVPrinter printer = new CSVPrinter(new OutputStreamWriter(
							outputStream), CSVFormat.DEFAULT);

					String[] columnHeaders = new String[rs.getColumnSize()];
					for (int i = 0; i < rs.getColumnSize(); i++) {
						columnHeaders[i] = rs.getColumn(i).getName();
					}
					printer.printRecord((Object[]) columnHeaders);

					rs.beforeFirst();
					while (rs.next()) {
						String[] row = new String[rs.getColumnSize()];
						for (int i = 0; i < rs.getColumnSize(); i++) {
							row[i] = rs.getString(i);
						}
						printer.printRecord((Object[]) row);
					}

					printer.flush();
					printer.close();
					
				} catch (ResultSetException | PersistableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				outputStream.close();
			}
		};
		return stream;
	}
}
