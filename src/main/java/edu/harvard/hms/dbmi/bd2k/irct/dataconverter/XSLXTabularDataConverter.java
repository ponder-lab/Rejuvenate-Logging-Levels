/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.dataconverter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.ResultSetException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular.ResultSet;

/**
 * A data converter that returns a XSLX Stream
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class XSLXTabularDataConverter implements ResultDataConverter {

	@Inject
	Logger log;
	
	@Override
	public ResultDataType getResultDataType() {
		return ResultDataType.TABULAR;
	}
	
	@Override
	public String getFileExtension() {
		return ".xlsx";
	}

	@Override
	public String getName() {
		return "XLSX";
	}

	@Override
	public String getMediaType() {
		return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
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
					
					SXSSFWorkbook wb = new SXSSFWorkbook(100);
					// Create Sheet
					Sheet sh = wb.createSheet("Results");

					// Create Header
					CellStyle headerStyle = wb.createCellStyle();
					Font font = wb.createFont();
					font.setBoldweight(Font.BOLDWEIGHT_BOLD);
					headerStyle.setFont(font);

					Row headerRow = sh.createRow(0);
					for (int i = 0; i < rs.getColumnSize(); i++) {
						Cell cell = headerRow.createCell(i);
						cell.setCellStyle(headerStyle);
						cell.setCellValue(rs.getColumn(i).getName());
					}

					// Add data
					rs.beforeFirst();
					int rowNum = 1;
					while (rs.next()) {
						Row row = sh.createRow(rowNum);
						for (int i = 0; i < rs.getColumnSize(); i++) {
							String value = rs.getString(i);
							Cell cell = row.createCell(i);
							if(value != null) {
								cell.setCellValue(rs.getString(i));
							}
						}
						rowNum++;
					}
					wb.write(outputStream);
					wb.close();
					
				} catch (ResultSetException | PersistableException e) {
					log.info("Error creating XSLX Stream: " + e.getMessage());
				}
				outputStream.close();
			}
		};
		return stream;
	}
}
