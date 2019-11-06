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

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
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
			XSSFWorkbook wb = new XSSFWorkbook();
			XSSFSheet sheet = wb.createSheet("dataset");
			CreationHelper createHelper = wb.getCreationHelper();

			// STYLE CELL
			CellStyle borderStyleHeader = wb.createCellStyle();
			borderStyleHeader.setBorderBottom(CellStyle.BORDER_THIN);
			borderStyleHeader.setBorderLeft(CellStyle.BORDER_THIN);
			borderStyleHeader.setBorderRight(CellStyle.BORDER_THIN);
			borderStyleHeader.setBorderTop(CellStyle.BORDER_THIN);
			borderStyleHeader.setAlignment(CellStyle.ALIGN_CENTER);

			CellStyle borderStyleRow = wb.createCellStyle();
			borderStyleRow.setBorderBottom(CellStyle.BORDER_THIN);
			borderStyleRow.setBorderLeft(CellStyle.BORDER_THIN);
			borderStyleRow.setBorderRight(CellStyle.BORDER_THIN);
			borderStyleRow.setBorderTop(CellStyle.BORDER_THIN);
			borderStyleRow.setAlignment(CellStyle.ALIGN_RIGHT);

			CellStyle tsCellStyle = wb.createCellStyle();
			tsCellStyle.setDataFormat(createHelper.createDataFormat().getFormat(TIMESTAMP_FORMAT));
			tsCellStyle.setBorderBottom(CellStyle.BORDER_THIN);
			tsCellStyle.setBorderLeft(CellStyle.BORDER_THIN);
			tsCellStyle.setBorderRight(CellStyle.BORDER_THIN);
			tsCellStyle.setBorderTop(CellStyle.BORDER_THIN);
			tsCellStyle.setAlignment(CellStyle.ALIGN_RIGHT);

			CellStyle dateCellStyle = wb.createCellStyle();
			dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat(DATE_FORMAT));
			dateCellStyle.setBorderBottom(CellStyle.BORDER_THIN);
			dateCellStyle.setBorderLeft(CellStyle.BORDER_THIN);
			dateCellStyle.setBorderRight(CellStyle.BORDER_THIN);
			dateCellStyle.setBorderTop(CellStyle.BORDER_THIN);
			dateCellStyle.setAlignment(CellStyle.ALIGN_RIGHT);

			CellStyle intCellStyle = wb.createCellStyle();
			intCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("0"));
			intCellStyle.setBorderBottom(CellStyle.BORDER_THIN);
			intCellStyle.setBorderLeft(CellStyle.BORDER_THIN);
			intCellStyle.setBorderRight(CellStyle.BORDER_THIN);
			intCellStyle.setBorderTop(CellStyle.BORDER_THIN);
			intCellStyle.setAlignment(CellStyle.ALIGN_RIGHT);

			CellStyle decimalCellStyle = wb.createCellStyle();
			decimalCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("#,##0.00"));
			decimalCellStyle.setBorderBottom(CellStyle.BORDER_THIN);
			decimalCellStyle.setBorderLeft(CellStyle.BORDER_THIN);
			decimalCellStyle.setBorderRight(CellStyle.BORDER_THIN);
			decimalCellStyle.setBorderTop(CellStyle.BORDER_THIN);
			decimalCellStyle.setAlignment(CellStyle.ALIGN_RIGHT);

			// CREATE HEADER SHEET
			IMetaData dataSetMetadata = dataSet.getMetadata();
			XSSFRow header = sheet.createRow((short) 0); // first row
			if (dataSetMetadata != null && dataSetMetadata.getFieldCount() > 0) {
				for (int i = 0; i <= dataSetMetadata.getFieldCount() - 1; i++) {
					XSSFCell cell = header.createCell(i);
					cell.setCellValue(dataSetMetadata.getFieldAlias(i));
					cell.setCellStyle(borderStyleHeader);
				}
			}
			// FILL CELL RECORD
			dataSet.loadData();
			IDataStore dataStore = dataSet.getDataStore();
			for (int i = 0; i < Integer.MAX_VALUE && i < dataStore.getRecordsCount(); i++) {
				try {
					IRecord dataSetRecord = dataStore.getRecordAt(i);

					XSSFRow row = sheet.createRow(i + 1); // starting from 2nd row

					for (int k = 0; k <= dataSetRecord.getFields().size() - 1; k++) {
						Class<?> clazz = dataSetMetadata.getFieldType(k);
						Object value = dataSetRecord.getFieldAt(k).getValue();
						XSSFCell cell = row.createCell(k);

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
			}

			wb.write(exportFileOS);
			exportFileOS.flush();
			exportFileOS.close();
		} catch (Exception e) {
			String msg = String.format("Error writing data file \"%s\"!", getDataFile());
			logger.error(msg, e);
			throw new JobExecutionException(msg, e);
		} finally {
//			if (iterator != null) {
//				iterator.close();
//			}
			if (exportFileOS != null) {
				try {
					exportFileOS.close();
				} catch (IOException e) {
					// Yes, it's mute!
				}
			}
		}

		logger.debug("End Excel export for dataSetId " + getDataSetId() + " with id " + getId() + " by user " + getUserProfile().getUserId());

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
