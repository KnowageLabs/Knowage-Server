/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.api.v2.export;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.iterator.DataIterator;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;

public class ExcelExportJob extends AbstractExportJob {

	private static final String DATE_FORMAT = "dd/MM/yyyy";

	private static final Logger logger = Logger.getLogger(ExcelExportJob.class);

	private static final String TIMESTAMP_FORMAT = "dd/MM/yyyy HH:mm:ss.SSS";

	@Override
	protected void export(JobExecutionContext context) throws JobExecutionException {

		logger.debug("Start Excel export for dataSetId " + getDataSetId() + " with id " + getId() + " by user " + getUserProfile().getUserId());

		OutputStream exportFileOS = getDataOutputStream();
		try {

			IDataSet dataSet = getDataSet();

			SimpleDateFormat timeStampFormat = new SimpleDateFormat(TIMESTAMP_FORMAT, getLocale());
			SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, getLocale());

			// create WB
			try(Workbook wb = new SXSSFWorkbook()) {
				Sheet sheet = wb.createSheet("dataset");
				CreationHelper createHelper = wb.getCreationHelper();

				// STYLE CELL
				CellStyle borderStyleHeader = wb.createCellStyle();
				borderStyleHeader.setBorderBottom(BorderStyle.THIN);
				borderStyleHeader.setBorderLeft(BorderStyle.THIN);
				borderStyleHeader.setBorderRight(BorderStyle.THIN);
				borderStyleHeader.setBorderTop(BorderStyle.THIN);
				borderStyleHeader.setAlignment(HorizontalAlignment.CENTER);

				CellStyle borderStyleRow = wb.createCellStyle();
				borderStyleRow.setBorderBottom(BorderStyle.THIN);
				borderStyleRow.setBorderLeft(BorderStyle.THIN);
				borderStyleRow.setBorderRight(BorderStyle.THIN);
				borderStyleRow.setBorderTop(BorderStyle.THIN);
				borderStyleRow.setAlignment(HorizontalAlignment.RIGHT);

				CellStyle tsCellStyle = wb.createCellStyle();
				tsCellStyle.setDataFormat(createHelper.createDataFormat().getFormat(TIMESTAMP_FORMAT));
				tsCellStyle.setBorderBottom(BorderStyle.THIN);
				tsCellStyle.setBorderLeft(BorderStyle.THIN);
				tsCellStyle.setBorderRight(BorderStyle.THIN);
				tsCellStyle.setBorderTop(BorderStyle.THIN);
				tsCellStyle.setAlignment(HorizontalAlignment.RIGHT);

				CellStyle dateCellStyle = wb.createCellStyle();
				dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat(DATE_FORMAT));
				dateCellStyle.setBorderBottom(BorderStyle.THIN);
				dateCellStyle.setBorderLeft(BorderStyle.THIN);
				dateCellStyle.setBorderRight(BorderStyle.THIN);
				dateCellStyle.setBorderTop(BorderStyle.THIN);
				dateCellStyle.setAlignment(HorizontalAlignment.RIGHT);

				CellStyle intCellStyle = wb.createCellStyle();
				intCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("0"));
				intCellStyle.setBorderBottom(BorderStyle.THIN);
				intCellStyle.setBorderLeft(BorderStyle.THIN);
				intCellStyle.setBorderRight(BorderStyle.THIN);
				intCellStyle.setBorderTop(BorderStyle.THIN);
				intCellStyle.setAlignment(HorizontalAlignment.RIGHT);

				CellStyle decimalCellStyle = wb.createCellStyle();
				decimalCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("#,##0.00"));
				decimalCellStyle.setBorderBottom(BorderStyle.THIN);
				decimalCellStyle.setBorderLeft(BorderStyle.THIN);
				decimalCellStyle.setBorderRight(BorderStyle.THIN);
				decimalCellStyle.setBorderTop(BorderStyle.THIN);
				decimalCellStyle.setAlignment(HorizontalAlignment.RIGHT);

				IMetaData dataSetMetadata = dataSet.getMetadata();

				// CREATE HEADER SHEET
				Row header;
				header = sheet.createRow((short) 0); // first row
				if (dataSetMetadata != null && dataSetMetadata.getFieldCount() > 0) {
					for (int i = 0; i <= dataSetMetadata.getFieldCount() - 1; i++) {
						Cell cell = header.createCell(i);
						cell.setCellValue(dataSetMetadata.getFieldAlias(i));
						cell.setCellStyle(borderStyleHeader);
					}
				}

				// FILL CELL RECORD
				try (DataIterator iterator = dataSet.iterator()) {

					int i = 0;
					final int recordLimit = SpreadsheetVersion.EXCEL2007.getLastRowIndex();
					while (iterator.hasNext() && i < recordLimit) {

						try {
							IRecord dataSetRecord = iterator.next();

							Row row = sheet.createRow(i + 1); // starting from 2nd row

							for (int k = 0; k <= dataSetRecord.getFields().size() - 1; k++) {
								Class<?> clazz = dataSetMetadata.getFieldType(k);
								Object value = dataSetRecord.getFieldAt(k).getValue();
								Cell cell = row.createCell(k);

								try {
									if (value != null) {

										if (Timestamp.class.isAssignableFrom(clazz)) {
											String formatedTimestamp = timeStampFormat.format(value);
											Date ts = timeStampFormat.parse(formatedTimestamp);
											cell.setCellValue(ts);
											cell.setCellStyle(tsCellStyle);
										} else if (Date.class.isAssignableFrom(clazz)) {
											String formatedDate = dateFormat.format(value);
											Date date = dateFormat.parse(formatedDate);
											cell.setCellValue(date);
											cell.setCellStyle(dateCellStyle);
										} else if (Integer.class.isAssignableFrom(clazz) || Long.class.isAssignableFrom(clazz) || Double.class.isAssignableFrom(clazz)
												|| Float.class.isAssignableFrom(clazz) || BigDecimal.class.isAssignableFrom(clazz)) {
											// Format Numbers
											if (Integer.class.isAssignableFrom(clazz) || Long.class.isAssignableFrom(clazz)) {
												cell.setCellValue(Double.parseDouble(value.toString()));
												cell.setCellStyle(intCellStyle);
											} else {
												cell.setCellValue(Double.parseDouble(value.toString()));
												cell.setCellStyle(decimalCellStyle);
											}

										} else {
											cell.setCellValue(value.toString());
											cell.setCellStyle(borderStyleRow);
										}

									} else {
										cell.setCellStyle(borderStyleRow);
									}
								} catch (ParseException e) {
									String msg = "Error parsing values";
									logger.error(msg, e);
									throw new IllegalStateException(msg, e);
								}

							}

						} catch (Exception e) {
							String msg = "Error generating Excel file";
							logger.error(msg, e);
							throw new IllegalStateException(msg, e);
						}

						i++;
					}
				}

				wb.write(exportFileOS);
				exportFileOS.flush();
				exportFileOS.close();
			}
		} catch (Exception e) {
			String msg = String.format("Error writing data file \"%s\"!", getDataFile());
			logger.error(msg, e);
			throw new JobExecutionException(msg, e);
		} finally {
			if (exportFileOS != null) {
				try {
					exportFileOS.close();
				} catch (IOException e) {
					// Yes, it's mute!
				}
			}
		}

		LogMF.info(logger, "XLSX export completed for user {0}. DataSet is {1}. Final file: dimension (in bytes): {2,number}, path: [{3}], ",
				this.getUserProfile().getUserId(), this.getDataSet().getLabel(), getDataFile().toFile().length(), getDataFile().toString());

	}

	@Override
	protected String extension() {
		return "xlsx";
	}

	@Override
	protected String mime() {
		return "application/vnd.ms-excel";
	}

}
