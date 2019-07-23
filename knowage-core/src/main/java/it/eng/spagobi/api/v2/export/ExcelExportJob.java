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
import java.nio.file.Files;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.messages.MessageBuilder;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;

public class ExcelExportJob extends AbstractExportJob {

	private static final Logger logger = Logger.getLogger(ExcelExportJob.class);

	private static final String TIMESTAMP_FORMAT = "dd/MM/yyyy HH:mm:ss.SSS";

	private static final String DATE_FORMAT = "dd/MM/yyyy";

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap mergedJobDataMap = context.getMergedJobDataMap();

		Integer dataSetId = getDataSetId(mergedJobDataMap);
		Map<String, Object> drivers = getDriversData(mergedJobDataMap);
		UUID id = getJobId(mergedJobDataMap);
		Locale locale = getLocale(mergedJobDataMap);
		Map<String, String> parameters = getParametersData(mergedJobDataMap);
		String resourcePathAsStr = getResourcePathString(mergedJobDataMap);
		UserProfile userProfile = getUserProfile(mergedJobDataMap);

		logger.debug("Start Excel export for dataSetId " + dataSetId + " with id " + id + " by user " + userProfile.getUserId());

		java.nio.file.Path resourcePath = ExportPathBuilder.getInstance().getPerJobExportPath(resourcePathAsStr, userProfile, id);

		OutputStream exportFileOS = null;
		try {

			Files.createDirectories(resourcePath);

			IDataSet dataSet = getDataSet(dataSetId, drivers, parameters, userProfile);
			IDataStore dataStore = dataSet.getDataStore();

			SimpleDateFormat timeStampFormat = new SimpleDateFormat(TIMESTAMP_FORMAT, locale);
			SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, locale);

			// create WB
			XSSFWorkbook wb = new XSSFWorkbook();
			XSSFSheet sheet = wb.createSheet("datastore");
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

			if (dataStore != null) {
				// CREATE HEADER SHEET
				XSSFRow header = sheet.createRow((short) 0); // first row
				if (dataStore.getMetaData() != null && dataStore.getMetaData().getFieldCount() > 0) {
					for (int i = 0; i <= dataStore.getMetaData().getFieldCount() - 1; i++) {
						XSSFCell cell = header.createCell(i);
						cell.setCellValue(dataStore.getMetaData().getFieldAlias(i));
						cell.setCellStyle(borderStyleHeader);
					}
				}
				// FILL CELL RECORD
				if (dataStore.getRecordsCount() > 0) {
					for (int i = 0; i <= dataStore.getRecordsCount() - 1; i++) {
						XSSFRow row = sheet.createRow(i + 1); // starting from 2nd row
						if (dataStore.getRecordAt(i) != null && dataStore.getRecordAt(i).getFields() != null
								&& dataStore.getRecordAt(i).getFields().size() > 0) {
							for (int k = 0; k <= dataStore.getRecordAt(i).getFields().size() - 1; k++) {
								Class<?> clazz = dataStore.getMetaData().getFieldType(k);
								Object value = dataStore.getRecordAt(i).getFieldAt(k).getValue();
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
										} else if (Integer.class.isAssignableFrom(clazz) || Long.class.isAssignableFrom(clazz)
												|| Double.class.isAssignableFrom(clazz) || Float.class.isAssignableFrom(clazz)
												|| BigDecimal.class.isAssignableFrom(clazz)) {
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
									logger.error("write output stream error " + e.getMessage());
									throw new IllegalStateException("Error generating Excel export file");
									// TODO : new SpagoBIServiceException(this.getActionName(), "Impossible to parse Date/DateTime value", e);
								}

							}
						}
					}
				}
			} else {
				MessageBuilder msgBuild = new MessageBuilder();

				XSSFRow header = sheet.createRow((short) 0); // first row
				XSSFCell cell = header.createCell(1);
				cell.setCellValue(msgBuild.getMessage("exporter.dataset.excel", locale));
				cell.setCellStyle(borderStyleHeader);
			}

			java.nio.file.Path exportFile = resourcePath.resolve(dataSet.getName() + ".xlsx");

			exportFileOS = Files.newOutputStream(exportFile);
			wb.write(exportFileOS);
		} catch (IOException e) {
			String msg = String.format("Error during create of directory \"%s\"!", resourcePath);
			logger.error(msg, e);
			throw new JobExecutionException(e);
		} finally {
			if (exportFileOS != null) {
				try {
					exportFileOS.close();
				} catch (IOException e) {
				}
			}
		}

		logger.debug("End CSV export for dataSetId " + dataSetId + " with id " + id + " by user " + userProfile.getUserId());

	}

}
