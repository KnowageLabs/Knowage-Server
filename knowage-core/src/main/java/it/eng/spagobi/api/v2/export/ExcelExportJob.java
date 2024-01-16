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

import java.io.File;
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
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import it.eng.knowage.commons.multitenant.OrganizationImageManager;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.iterator.DataIterator;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class ExcelExportJob extends AbstractExportJob {

	private static final Logger LOGGER = Logger.getLogger(ExcelExportJob.class);
	private static final String DATE_FORMAT = "dd/MM/yyyy";
	private static final String TIMESTAMP_FORMAT = "dd/MM/yyyy HH:mm:ss.SSS";
	
	private String documentName = "";

	@Override
	protected void export(JobExecutionContext context) throws JobExecutionException {

		LOGGER.debug("Start Excel export for dataSetId " + getDataSetId() + " with id " + getId() + " by user " + getUserProfile().getUserId());

		OutputStream exportFileOS = getDataOutputStream();
		try {

			IDataSet dataSet = getDataSet();

			SimpleDateFormat timeStampFormat = new SimpleDateFormat(TIMESTAMP_FORMAT, getLocale());
			SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, getLocale());

			// create WB
			try (Workbook wb = new SXSSFWorkbook()) {
				Sheet sheet = wb.createSheet("dataset");
				CreationHelper createHelper = wb.getCreationHelper();

				// STYLE CELL
				CellStyle borderStyleHeader = wb.createCellStyle();
//				borderStyleHeader.setBorderBottom(BorderStyle.THIN);
//				borderStyleHeader.setBorderLeft(BorderStyle.THIN);
//				borderStyleHeader.setBorderRight(BorderStyle.THIN);
//				borderStyleHeader.setBorderTop(BorderStyle.THIN);
				borderStyleHeader.setAlignment(HorizontalAlignment.CENTER);

				CellStyle borderStyleRow = wb.createCellStyle();
//				borderStyleRow.setBorderBottom(BorderStyle.THIN);
//				borderStyleRow.setBorderLeft(BorderStyle.THIN);
//				borderStyleRow.setBorderRight(BorderStyle.THIN);
//				borderStyleRow.setBorderTop(BorderStyle.THIN);
				borderStyleRow.setAlignment(HorizontalAlignment.RIGHT);

				CellStyle tsCellStyle = wb.createCellStyle();
				tsCellStyle.setDataFormat(createHelper.createDataFormat().getFormat(TIMESTAMP_FORMAT));
//				tsCellStyle.setBorderBottom(BorderStyle.THIN);
//				tsCellStyle.setBorderLeft(BorderStyle.THIN);
//				tsCellStyle.setBorderRight(BorderStyle.THIN);
//				tsCellStyle.setBorderTop(BorderStyle.THIN);
				tsCellStyle.setAlignment(HorizontalAlignment.RIGHT);

				CellStyle dateCellStyle = wb.createCellStyle();
				dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat(DATE_FORMAT));
//				dateCellStyle.setBorderBottom(BorderStyle.THIN);
//				dateCellStyle.setBorderLeft(BorderStyle.THIN);
//				dateCellStyle.setBorderRight(BorderStyle.THIN);
//				dateCellStyle.setBorderTop(BorderStyle.THIN);
				dateCellStyle.setAlignment(HorizontalAlignment.RIGHT);

				CellStyle intCellStyle = wb.createCellStyle();
				intCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("0"));
//				intCellStyle.setBorderBottom(BorderStyle.THIN);
//				intCellStyle.setBorderLeft(BorderStyle.THIN);
//				intCellStyle.setBorderRight(BorderStyle.THIN);
//				intCellStyle.setBorderTop(BorderStyle.THIN);
				intCellStyle.setAlignment(HorizontalAlignment.RIGHT);

				CellStyle decimalCellStyle = wb.createCellStyle();
				decimalCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("#,##0.00"));
//				decimalCellStyle.setBorderBottom(BorderStyle.THIN);
//				decimalCellStyle.setBorderLeft(BorderStyle.THIN);
//				decimalCellStyle.setBorderRight(BorderStyle.THIN);
//				decimalCellStyle.setBorderTop(BorderStyle.THIN);
				decimalCellStyle.setAlignment(HorizontalAlignment.RIGHT);

				IMetaData dataSetMetadata = dataSet.getMetadata();

				// CREATE BRANDED HEADER SHEET
				String imageB64 = OrganizationImageManager.getOrganizationB64Image(TenantManager.getTenant().getName());
				int startRow = 0;
				float rowHeight = 35; // in points	
				int rowspan = 2;
				int startCol = 0;
				int colWidth = 25;
				int colspan = 2;
				int namespan = 10;
				int dataspan = 10;
				
				if (imageB64 != null) {
					
					for (int r = startRow; r < startRow+rowspan; r++) {
						   sheet.createRow(r).setHeightInPoints(rowHeight);
						   for (int c = startCol; c < startCol+colspan; c++) {
							   sheet.getRow(r).createCell(c);
							   sheet.setColumnWidth(c, colWidth * 256);
						}
					}
					
					// set brandend header image
					sheet.addMergedRegion(new CellRangeAddress(startRow, startRow+rowspan-1, startCol, startCol+colspan-1));
					drawBrandendHeaderImage(sheet, imageB64, Workbook.PICTURE_TYPE_PNG, startCol, startRow, colspan, rowspan);							
					
					// set document name
					sheet.getRow(startRow).createCell(startCol+colspan).setCellValue(documentName);
					sheet.addMergedRegion(new CellRangeAddress(startRow, startRow, startCol+colspan, namespan));
					// set cell style
					CellStyle documentNameCellStyle = buildCellStyle(sheet, true, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, (short) 16);
					sheet.getRow(startRow).getCell(startCol+colspan).setCellStyle(documentNameCellStyle);
					
					// set date 
					Date date = new Date();
					sheet.getRow(startRow+1).createCell(startCol+colspan).setCellValue("Data di generazione: " + timeStampFormat.format(date));
					sheet.addMergedRegion(new CellRangeAddress(startRow+1, startRow+1, startCol+colspan, dataspan));
					// set cell style
					CellStyle headerDateCellStyle = buildCellStyle(sheet, false, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, (short) 8);
					sheet.getRow(startRow+1).getCell(startCol+colspan).setCellStyle(headerDateCellStyle);
				}
				
				int headerIndex = (imageB64 != null) ? (startRow+rowspan) : 0;
				Row widgetNameRow = sheet.createRow((short) headerIndex);
				Cell widgetNameCell = widgetNameRow.createCell(0);
				widgetNameCell.setCellValue(sheet.getSheetName());
				// set cell style
				CellStyle widgetNameStyle = buildCellStyle(sheet, true, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, (short) 14);
				widgetNameCell.setCellStyle(widgetNameStyle);
				
				Row header;
				header = sheet.createRow((short) headerIndex+1); // first row
				if (dataSetMetadata != null && dataSetMetadata.getFieldCount() > 0) {
					for (int i = 0; i <= dataSetMetadata.getFieldCount() - 1; i++) {
						Cell cell = header.createCell(i);
						cell.setCellValue(dataSetMetadata.getFieldAlias(i));
						cell.setCellStyle(borderStyleHeader);
						// set cell style
						CellStyle headerCellStyle = buildCellStyle(sheet, true, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, (short) 11);
						cell.setCellStyle(headerCellStyle);
					}
				}
				
				// adjusts the column width to fit the contents
				adjustColumnWidth(sheet);

				// FILL CELL RECORD
				try (DataIterator iterator = dataSet.iterator()) {

					int i = headerIndex+1;
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
									String msg = "Error parsing values";
									LOGGER.error(msg, e);
									throw new IllegalStateException(msg, e);
								}

							}

						} catch (Exception e) {
							String msg = "Error generating Excel file";
							LOGGER.error(msg, e);
							throw new IllegalStateException(msg, e);
						}

						i++;
					}
				}
				
				// adjusts the column width to fit the contents
				adjustColumnWidth(sheet);

				wb.write(exportFileOS);
				exportFileOS.flush();
				exportFileOS.close();
			}
		} catch (Exception e) {
			String msg = String.format("Error writing data file \"%s\"!", getDataFile());
			LOGGER.error(msg, e);
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

		LogMF.info(LOGGER, "XLSX export completed for user {0}. DataSet is {1}. Final file: dimension (in bytes): {2,number}, path: [{3}], ",
				this.getUserProfile().getUserId(), this.getDataSet().getLabel(), getDataFile().toFile().length(), getDataFile().toString());

	}

	@Override
	protected String extension() {
		return "xlsx";
	}

	@Override
	protected String mime() {
		return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	}
	
	public CellStyle buildCellStyle(Sheet sheet, boolean bold, HorizontalAlignment alignment, VerticalAlignment verticalAlignment, short headerFontSizeShort) {
		
		// CELL
		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		
		// alignment 
		cellStyle.setAlignment(alignment);
		cellStyle.setVerticalAlignment(verticalAlignment);

		// foreground color
//		String headerBGColor = (String) this.getProperty(PROPERTY_HEADER_BACKGROUND_COLOR);
//		short backgroundColorIndex = headerBGColor != null ? IndexedColors.valueOf(headerBGColor).getIndex()
//				: IndexedColors.valueOf(DEFAULT_HEADER_BACKGROUND_COLOR).getIndex();
//		cellStyle.setFillForegroundColor(backgroundColorIndex);

		// pattern
//		cellStyle.setFillPattern(fp);

		// borders
//		cellStyle.setBorderBottom(borderBottom);
//		cellStyle.setBorderLeft(borderLeft);
//		cellStyle.setBorderRight(borderRight);
//		cellStyle.setBorderTop(borderTop);

//		String bordeBorderColor = (String) this.getProperty(PROPERTY_HEADER_BORDER_COLOR);
//		short borderColorIndex = bordeBorderColor != null ? IndexedColors.valueOf(bordeBorderColor).getIndex()
//				: IndexedColors.valueOf(DEFAULT_HEADER_BORDER_COLOR).getIndex();
//		cellStyle.setLeftBorderColor(borderColorIndex);
//		cellStyle.setRightBorderColor(borderColorIndex);
//		cellStyle.setBottomBorderColor(borderColorIndex);
//		cellStyle.setTopBorderColor(borderColorIndex);

		// FONT
		Font font = sheet.getWorkbook().createFont();

		// size
//		Short headerFontSize = (Short) this.getProperty(PROPERTY_HEADER_FONT_SIZE);
//		short headerFontSizeShort = headerFontSize != null ? headerFontSize.shortValue() : DEFAULT_HEADER_FONT_SIZE;
		font.setFontHeightInPoints(headerFontSizeShort);

		// name
//		String fontName = (String) this.getProperty(PROPERTY_FONT_NAME);
//		fontName = fontName != null ? fontName : DEFAULT_FONT_NAME;
//		font.setFontName(fontName);

		// color
//		String headerColor = (String) this.getProperty(PROPERTY_HEADER_COLOR);
//		short headerColorIndex = headerColor != null ? IndexedColors.valueOf(headerColor).getIndex()
//				: IndexedColors.valueOf(DEFAULT_HEADER_COLOR).getIndex();
//		font.setColor(headerColorIndex);

		// bold		
		font.setBold(bold);
		
		cellStyle.setFont(font);
		return cellStyle;
	}

	public void adjustColumnWidth(Sheet sheet) {
		try {		    			
			boolean enabled = true;
			((SXSSFSheet) sheet).trackAllColumnsForAutoSizing();
			Row row = sheet.getRow(sheet.getLastRowNum());
			if(row != null) {
				for (int i = 0; i < row.getLastCellNum(); i++) {
					sheet.autoSizeColumn(i);
					if(enabled && (i == 0 || i == 1)) {
						// first or second column
						int colWidth = 25;
						if (sheet.getColumnWidthInPixels(i) < (colWidth * 256))
							sheet.setColumnWidth(i, colWidth * 256);
					}
				}	
			}
		} catch (Exception e) {
			// to do
		}
	}
	
	public void drawBrandendHeaderImage(Sheet sheet, String imageB64, int pictureType, int startCol, int startRow,
			int colspan, int rowspan) {
		try {
			Workbook wb = sheet.getWorkbook();
			
			// load the picture
		    String encodingPrefix = "base64,";
		    int contentStartIndex = imageB64.indexOf(encodingPrefix) + encodingPrefix.length();
		    byte[] bytes = org.apache.commons.codec.binary.Base64.decodeBase64(imageB64.substring(contentStartIndex));			
			int pictureIdx = wb.addPicture(bytes, pictureType);

			// create an anchor with upper left cell startCol/startRow
			CreationHelper helper = wb.getCreationHelper();
			ClientAnchor anchor = helper.createClientAnchor();
			anchor.setCol1(startCol);
			anchor.setRow1(startRow);

			Drawing drawing = sheet.createDrawingPatriarch();
			Picture pict = drawing.createPicture(anchor, pictureIdx);

			int pictWidthPx = pict.getImageDimension().width;
			int pictHeightPx = pict.getImageDimension().height;
			
			// get the heights of all merged rows in px
			float[] rowHeightsPx = new float[startRow+rowspan];
			float rowsHeightPx = 0f;
			for (int r = startRow; r < startRow+rowspan; r++) {
				Row row = sheet.getRow(r);
				float rowHeightPt = row.getHeightInPoints();
				rowHeightsPx[r-startRow] = rowHeightPt * Units.PIXEL_DPI / Units.POINT_DPI;
				rowsHeightPx += rowHeightsPx[r-startRow];
			}

			// get the widths of all merged cols in px
			float[] colWidthsPx = new float[startCol + colspan];
			float colsWidthPx = 0f;
			for (int c = startCol; c < startCol + colspan; c++) {
				colWidthsPx[c - startCol] = sheet.getColumnWidthInPixels(c);
				colsWidthPx += colWidthsPx[c - startCol];
			}

			// calculate scale
			float scale = 1;
			if (pictHeightPx > rowsHeightPx) {
				float tmpscale = rowsHeightPx / (float) pictHeightPx;
				if (tmpscale < scale)
					scale = tmpscale;
			}
			if (pictWidthPx > colsWidthPx) {
				float tmpscale = colsWidthPx / (float) pictWidthPx;
				if (tmpscale < scale)
					scale = tmpscale;
			}

			// calculate the horizontal center position
			int horCenterPosPx = Math.round(colsWidthPx / 2f - pictWidthPx * scale / 2f);
			Integer col1 = null;
			colsWidthPx = 0f;
			for (int c = 0; c < colWidthsPx.length; c++) {
				float colWidthPx = colWidthsPx[c];
				if (colsWidthPx + colWidthPx > horCenterPosPx) {
					col1 = c + startCol;
					break;
				}
				colsWidthPx += colWidthPx;
			}
			
			// set the horizontal center position as Col1 plus Dx1 of anchor
			if (col1 != null) {
				anchor.setCol1(col1);
				anchor.setDx1(Math.round(horCenterPosPx - colsWidthPx) * Units.EMU_PER_PIXEL);
			}

			// calculate the vertical center position
			int vertCenterPosPx = Math.round(rowsHeightPx / 2f - pictHeightPx * scale / 2f);
			Integer row1 = null;
			rowsHeightPx = 0f;
			for (int r = 0; r < rowHeightsPx.length; r++) {
				float rowHeightPx = rowHeightsPx[r];
				if (rowsHeightPx + rowHeightPx > vertCenterPosPx) {
					row1 = r + startRow;
				    break;
				}
				rowsHeightPx += rowHeightPx;
			}
			  
			if (row1 != null) {
				anchor.setRow1(row1);
				anchor.setDy1(Math.round(vertCenterPosPx - rowsHeightPx) * Units.EMU_PER_PIXEL); //in unit EMU for XSSF
			}
			 
			anchor.setCol2(startCol+colspan);
			anchor.setDx2(Math.round(colsWidthPx - Math.round(horCenterPosPx - colsWidthPx)) * Units.EMU_PER_PIXEL);
			anchor.setRow2(startRow+rowspan);
			anchor.setDy2(Math.round(rowsHeightPx - Math.round(vertCenterPosPx - rowsHeightPx)) * Units.EMU_PER_PIXEL);
			
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Cannot write data to Excel file", e);
		}
	}	
}
