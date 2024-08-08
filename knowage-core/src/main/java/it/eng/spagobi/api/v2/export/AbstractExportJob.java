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
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
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
import org.json.JSONException;
import org.json.JSONObject;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import it.eng.qbe.dataset.DerivedDataSet;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

abstract class AbstractExportJob implements Job {

	private static final Logger LOGGER = Logger.getLogger(AbstractExportJob.class);

	public static final String MAP_KEY_DATA_SET_ID = "dataSetId";

	public static final String MAP_KEY_DRIVERS = "drivers";

	public static final String MAP_KEY_ID = "id";

	public static final String MAP_KEY_LOCALE = "locale";

	public static final String MAP_KEY_PARAMETERS = "parameters";

	public static final String MAP_KEY_RESOURCE_PATH = "resourcePath";

	public static final String MAP_KEY_USER_PROFILE = "userProfile";

	private Path dataFile = null;
	private OutputStream dataOutputStream = null;
	private IDataSet dataSet = null;
	private Integer dataSetId = null;
	private Map<String, Object> drivers = null;
	private UUID id = null;
	private Locale locale = null;
	private Map<String, String> parameters = null;
	Path resourcePath = null;
	String resourcePathAsStr = null;
	UserProfile userProfile = null;

	/**
	 * Internal cleanup in case of error.
	 */
	protected void deleteJobDirectory() {
		try {
			FileUtils.deleteDirectory(resourcePath.toFile());
		} catch (IOException e) {
			// Yes, it's mute!
		}
	}

	@Override
	public final void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap mergedJobDataMap = context.getMergedJobDataMap();

		dataSetId = getDataSetId(mergedJobDataMap);
		drivers = getDriversData(mergedJobDataMap);
		id = getJobId(mergedJobDataMap);
		locale = getLocale(mergedJobDataMap);
		parameters = getParametersData(mergedJobDataMap);
		resourcePathAsStr = getResourcePathString(mergedJobDataMap);
		userProfile = getUserProfile(mergedJobDataMap);

		initializeTenant();

		dataSet = getDataSet(dataSetId, drivers, parameters, userProfile);

		resourcePath = ExportPathBuilder.getInstance().getPerJobExportPath(resourcePathAsStr, userProfile, id);

		try {
			Files.createDirectories(resourcePath);
		} catch (IOException e) {
			String msg = String.format("Error creating directory \"%s\"!", resourcePath);
			LOGGER.error(msg, e);
			throw new JobExecutionException(e);
		}

		dataFile = ExportPathBuilder.getInstance().getPerJobIdDataFile(resourcePathAsStr, userProfile, id);

		try {
			dataOutputStream = Files.newOutputStream(dataFile);
		} catch (IOException e) {

			deleteJobDirectory();

			String msg = String.format("Error creating file \"%s\"!", dataFile);
			LOGGER.error(msg, e);
			throw new JobExecutionException(e);
		}

		export(context);
		Path metadataFile = ExportPathBuilder.getInstance().getPerJobIdMetadataFile(resourcePathAsStr, userProfile, id);

		try {
			String dataSetName = dataSet.getName();

			ExportMetadata exportMetadata = new ExportMetadata();
			exportMetadata.setId(id);
			exportMetadata.setDataSetName(dataSetName);
			exportMetadata.setFileName(dataSetName + "." + extension());
			exportMetadata.setMimeType(mime());
			exportMetadata.setStartDate(Calendar.getInstance(getLocale()).getTime());

			ExportMetadata.writeToJsonFile(exportMetadata, metadataFile);

		} catch (Exception e) {

			deleteJobDirectory();

			String msg = String.format("Error creating file \"%s\"!", metadataFile);
			LOGGER.error(msg, e);
			throw new JobExecutionException(e);
		}

	}

	/**
	 * @return The MIME type of generated file.
	 */
	protected abstract String extension();

	public Path getDataFile() {
		return dataFile;
	}

	protected OutputStream getDataOutputStream() {
		return dataOutputStream;
	}

	protected final IDataSet getDataSet() {
		return dataSet;
	}

	private final IDataSet getDataSet(Integer dataSetId, Map<String, Object> drivers, Map<String, String> parameters, UserProfile userProfile)
			throws JobExecutionException {
		IDataSetDAO dsDAO = DAOFactory.getDataSetDAO();
		dsDAO.setUserProfile(userProfile);
		IDataSet currDataSet = dsDAO.loadDataSetById(dataSetId);
		if (currDataSet instanceof VersionedDataSet) {
			VersionedDataSet vds = (VersionedDataSet) currDataSet;
			if (vds.getWrappedDataset() instanceof DerivedDataSet) {
				IDataSet sourcedataSet = this.getDerivedSourceDataset(currDataSet);
				String jsonQuery = this.getJSonQueryDataset(currDataSet);
				if (sourcedataSet != null) {
					DerivedDataSet dataSetDer = (DerivedDataSet) vds.getWrappedDataset();
					dataSetDer.setSourceDataset(sourcedataSet);
					dataSetDer.setJsonQuery(jsonQuery);
					dataSetDer
							.setDataSource(dataSetDer.getDataSourceForReading() != null ? dataSetDer.getDataSourceForReading() : sourcedataSet.getDataSource());
					dataSetDer.setDataSourceForReading(
							dataSetDer.getDataSourceForReading() != null ? dataSetDer.getDataSourceForReading() : sourcedataSet.getDataSource());
					currDataSet = dataSetDer;
				}
			}
		}

		LOGGER.debug("Dump drivers:");
		for (Entry<String, Object> entry : drivers.entrySet()) {
			String msg = String.format("\t%s: %s", entry.getKey(), entry.getValue());
			LOGGER.debug(msg);
		}

		LOGGER.debug("Dump parameters:");
		for (java.util.Map.Entry<String, String> entry : parameters.entrySet()) {
			String msg = String.format("\t%s: %s", entry.getKey(), entry.getValue());
			LOGGER.debug(msg);
		}

		currDataSet.setDrivers(drivers);
		try {
			currDataSet.setParametersMap(parameters);
		} catch (JSONException e) {
			throw new JobExecutionException("An error occurred when applying parameters into dataset", e);
		}
		currDataSet.resolveParameters();

		currDataSet.setUserProfileAttributes(userProfile.getUserAttributes());
		return currDataSet;
	}

	protected Integer getDataSetId() {
		return dataSetId;
	}

	protected final Integer getDataSetId(JobDataMap mergedJobDataMap) {
		return (Integer) mergedJobDataMap.get(MAP_KEY_DATA_SET_ID);
	}

	protected Map<String, Object> getDrivers() {
		return drivers;
	}

	protected final Map<String, Object> getDriversData(JobDataMap mergedJobDataMap) {
		return (Map<String, Object>) mergedJobDataMap.get(MAP_KEY_DRIVERS);
	}

	protected UUID getId() {
		return id;
	}

	protected final UUID getJobId(JobDataMap mergedJobDataMap) {
		return (UUID) mergedJobDataMap.get(MAP_KEY_ID);
	}

	protected Locale getLocale() {
		return locale;
	}

	protected final Locale getLocale(JobDataMap mergedJobDataMap) {
		return (Locale) mergedJobDataMap.get(MAP_KEY_LOCALE);
	}

	protected Map<String, String> getParameters() {
		return parameters;
	}

	protected final Map<String, String> getParametersData(JobDataMap mergedJobDataMap) {
		return (Map<String, String>) mergedJobDataMap.get(MAP_KEY_PARAMETERS);
	}

	protected String getResourcePathAsStr() {
		return resourcePathAsStr;
	}

	protected final String getResourcePathString(JobDataMap mergedJobDataMap) {
		return (String) mergedJobDataMap.get(MAP_KEY_RESOURCE_PATH);
	}

	protected UserProfile getUserProfile() {
		return userProfile;
	}

	protected final UserProfile getUserProfile(JobDataMap mergedJobDataMap) {
		return (UserProfile) mergedJobDataMap.get(MAP_KEY_USER_PROFILE);
	}

	/**
	 * Set tenant in the job thread.
	 */
	private void initializeTenant() {
		String organization = userProfile.getOrganization();
		Tenant tenant = new Tenant(organization);
		TenantManager.setTenant(tenant);
	}

	/**
	 * Call the real export.
	 *
	 * @param context
	 * @throws JobExecutionException
	 */
	protected abstract void export(JobExecutionContext context) throws JobExecutionException;

	/**
	 * @return The MIME type of generated file.
	 */
	protected abstract String mime();

	public IDataSet getDerivedSourceDataset(IDataSet dataset) {
		if (dataset instanceof VersionedDataSet) {
			VersionedDataSet vds = (VersionedDataSet) dataset;
			if (vds.getWrappedDataset() instanceof DerivedDataSet) {
				JSONObject sourceJsonConfig;
				try {
					sourceJsonConfig = new JSONObject(vds.getWrappedDataset().getConfiguration());
					String sourceDatasetLabel = sourceJsonConfig.getString("sourceDatasetLabel");
					return DAOFactory.getDataSetDAO().loadDataSetByLabel(sourceDatasetLabel);
				} catch (JSONException e) {
					throw new SpagoBIRuntimeException("sourceJsonConfig no longer exists for " + vds.getWrappedDataset().getLabel() + " Dataset");
				}

			}
		}
		return dataset;
	}

	public String getJSonQueryDataset(IDataSet dataset) {
		String jsonQuery = null;
		if (dataset instanceof VersionedDataSet) {
			VersionedDataSet vds = (VersionedDataSet) dataset;
			if (vds.getWrappedDataset() instanceof DerivedDataSet) {
				JSONObject sourceJsonConfig;
				try {
					sourceJsonConfig = new JSONObject(vds.getWrappedDataset().getConfiguration());
					jsonQuery = sourceJsonConfig.getString("qbeJSONQuery");
				} catch (JSONException e) {
					throw new SpagoBIRuntimeException("sourceJsonConfig no longer exists for " + vds.getWrappedDataset().getLabel() + " Dataset");
				}

			}
		}
		return jsonQuery;
	}
	
	public void adjustColumnWidth(Sheet sheet, String imageB64) {
		try {		    			
			((SXSSFSheet) sheet).trackAllColumnsForAutoSizing();
			Row row = sheet.getRow(sheet.getLastRowNum());
			if(row != null) {
				for (int i = 0; i < row.getLastCellNum(); i++) {
					sheet.autoSizeColumn(i);
					if(StringUtils.isNotEmpty(imageB64) && (i == 0 || i == 1)) {
						// first or second column
						int colWidth = 25;
						if (sheet.getColumnWidthInPixels(i) < (colWidth * 256))
							sheet.setColumnWidth(i, colWidth * 256);
					}
				}	
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Cannot write data to Excel file", e);
		}
	}
	
	public int createBrandedHeaderSheet(Sheet sheet, String imageB64, 
			int startRow, float rowHeight, int rowspan, int startCol, int colWidth, int colspan, int namespan, int dataspan, 
			String documentName, String widgetName) {				
		if (StringUtils.isNotEmpty(imageB64)) {			
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
			DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			Date date = new Date();
			sheet.getRow(startRow+1).createCell(startCol+colspan).setCellValue("Data di generazione: " + dateFormat.format(date));
			sheet.addMergedRegion(new CellRangeAddress(startRow+1, startRow+1, startCol+colspan, dataspan));
			// set cell style
			CellStyle dateCellStyle = buildCellStyle(sheet, false, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, (short) 8);
			sheet.getRow(startRow+1).getCell(startCol+colspan).setCellStyle(dateCellStyle);
		}
		
		int headerIndex = (StringUtils.isNotEmpty(imageB64)) ? (startRow+rowspan) : 0;
		Row widgetNameRow = sheet.createRow((short) headerIndex);
		Cell widgetNameCell = widgetNameRow.createCell(0);
		widgetNameCell.setCellValue(widgetName);
		// set cell style
		CellStyle widgetNameStyle = buildCellStyle(sheet, true, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, (short) 14);
		widgetNameCell.setCellStyle(widgetNameStyle);
		
		return headerIndex;
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
				float tmpscale = rowsHeightPx / pictHeightPx;
				if (tmpscale < scale)
					scale = tmpscale;
			}
			if (pictWidthPx > colsWidthPx) {
				float tmpscale = colsWidthPx / pictWidthPx;
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
}
