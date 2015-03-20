/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.worksheet.exporter;

import it.eng.spagobi.commons.QbeEngineStaticVariables;
import it.eng.spagobi.engines.qbe.QbeEngineConfig;
import it.eng.spagobi.engines.qbe.exporter.QbeXLSExporter;
import it.eng.spagobi.engines.worksheet.services.export.ExportWorksheetAction;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Exports the crosstab data (formatted as a JSON object in input) into a XLS
 * file. The JSON object should have this structure (a node is {node_key:"Text",
 * node_childs:[...]}): columns: {...} contains tree node structure of the
 * columns' headers rows: {...} contains tree node structure of the rows'
 * headers data: [[...], [...], ...] 2-dimensional matrix containing crosstab
 * data
 * 
 * @author Chiara Chiarelli
 */
public class WorkSheetXLSExporter {

	/** Logger component. */
	public static transient Logger logger = Logger
			.getLogger(WorkSheetXLSExporter.class);

	public static final String CROSSTAB_JSON_DESCENDANTS_NUMBER = "descendants_no";
	public static final String SHEETS_NUM = "SHEETS_NUM";
	public static final String EXPORTED_SHEETS = "EXPORTED_SHEETS";

	public static String OUTPUT_FORMAT_JPEG = "image/jpeg";

	public static final String HEADER = "HEADER";
	public static final String FOOTER = "FOOTER";
	public static final String CONTENT = "CONTENT";

	public static final String SHEET_TYPE = "SHEET_TYPE";
	public static final String CHART = "CHART";
	public static final String CROSSTAB = "CROSSTAB";
	public static final String STATIC_CROSSTAB = "STATIC_CROSSTAB";
	public static final String TABLE = "TABLE";

	public static final String SVG = "SVG";

	public static final String POSITION = "position";
	public static final String TITLE = "title";
	public static final String IMG = "img";
	
	public static final String CENTER = "center";
	public static final String RIGHT = "right";
	public static final String LEFT = "left";
	
	public static final short METADATA_TITLE_FONT_SIZE = 9;
	public static final short METADATA_NAME_FONT_SIZE = 8;
	public static final short METADATA_VALUE_FONT_SIZE = 8;
	public static final short FILTERS_TITLE_FONT_SIZE = 9;
	public static final short FILTERS_VALUES_FONT_SIZE = 8;
	public static final short TABLE_HEADER_FONT_SIZE = 8;
	public static final short TABLE_CELL_CONTENT_FONT_SIZE = 8;
	public static final short HEADER_FONT_SIZE = 16;
	
	public static final String FONT_NAME = "Verdana";

	Map<Integer, String> decimalFormats = new HashMap<Integer, String>();
	
	public JSONObject getOptionalUserFilters(JSONObject paramsJSON) throws JSONException{
		JSONObject optionalUserFiltersJSON = null;
		if(paramsJSON.has(QbeEngineStaticVariables.FILTERS)){
			String optionalUserFilters = paramsJSON.getString(QbeEngineStaticVariables.FILTERS);
			optionalUserFiltersJSON = new JSONObject(optionalUserFilters);	
		} 
		return optionalUserFiltersJSON;
	}
	
	public Workbook createNewWorkbook() {
		HSSFWorkbook workbook = new HSSFWorkbook();
		return workbook;
	}
	
	public List<String> getJsonVisibleSelectFields(JSONObject paramsJSON) throws JSONException{
		JSONArray jsonVisibleSelectFields = null;
		if(paramsJSON.has(QbeEngineStaticVariables.OPTIONAL_VISIBLE_COLUMNS)){
			String jsonVisibleSelectFieldsS = paramsJSON.getString(QbeEngineStaticVariables.OPTIONAL_VISIBLE_COLUMNS);
			jsonVisibleSelectFields = new JSONArray(jsonVisibleSelectFieldsS);	 
		}
		
		List<String> visibleSelectFields = new ArrayList<String>();
		try {
			if (jsonVisibleSelectFields != null) {
				for (int j = 0; j < jsonVisibleSelectFields.length(); j++) {
					JSONObject jsonVisibleSelectField = jsonVisibleSelectFields.getJSONObject(j);
					visibleSelectFields.add(jsonVisibleSelectField.getString("alias"));
				}	
			}
		} catch (Exception e) {
			logger.debug("The optional attribute visible select fields is not valued. No visible select field selected.. All fields will be taken..");
		}
		return visibleSelectFields;
	}
	
	public void designTableInWorksheet(Sheet sheet, Workbook wb, CreationHelper createHelper, 
			  IDataStore dataStore, int startRow, Locale locale) throws JSONException{
		
		QbeXLSExporter qbeXLSExporter = new QbeXLSExporter(dataStore, locale);
		qbeXLSExporter.setProperty(QbeXLSExporter.PROPERTY_HEADER_FONT_SIZE, TABLE_HEADER_FONT_SIZE);
		qbeXLSExporter.setProperty(QbeXLSExporter.PROPERTY_CELL_FONT_SIZE, TABLE_CELL_CONTENT_FONT_SIZE);
		qbeXLSExporter.setProperty(QbeXLSExporter.PROPERTY_FONT_NAME, FONT_NAME);
		qbeXLSExporter.fillSheet(sheet, wb, createHelper, startRow);
	}

	public int setHeader(Sheet sheet, JSONObject header,
			CreationHelper createHelper, Workbook wb, Drawing patriarch, int sheetRow) throws JSONException, IOException {
		String title = header.getString(TITLE);
		String imgName = header.optString(IMG);
		String imagePosition = header.getString(POSITION);
		CellStyle cellStyle = this.buildHeaderTitleCellStyle(sheet);
		
		if(title!=null && !title.equals("")){			
			Row row = sheet.createRow(sheetRow);
			sheetRow++;
			Cell cell = row.createCell(6);
			cell.setCellValue(createHelper.createRichTextString(title));
			cell.setCellType(this.getCellTypeString());
			cell.setCellStyle(cellStyle);
		}
		
		if(imgName!=null && !imgName.equals("") && !imgName.equals("null")){
			File img = getImage(imgName);
			String imgNameUpperCase = imgName.toUpperCase();
			int impgType = getImageType(imgNameUpperCase);
			
			int c = 2;
			int colend = 3;

			if(imagePosition!=null && !imagePosition.equals("")){
				if(imagePosition.equals(LEFT)){
					c = 0;
					colend = 1;
				}else if(imagePosition.equals(RIGHT)){
					c = 4;
					colend = 5;
				}
			}
			if(impgType!=0){
				for(int i=0; i<4; i++){
					sheet.createRow(sheetRow+i);
				}
				setImageIntoWorkSheet(wb, patriarch, img, c, colend, sheetRow, 4,impgType);

				sheetRow = sheetRow+4;
			}
		}
		
		return sheetRow;
		
	}

	public int setFooter(Sheet sheet, JSONObject footer,
			CreationHelper createHelper, Workbook wb, Drawing patriarch, int sheetRow) throws JSONException, IOException {
		String title = footer.getString(TITLE);
		String imgName = footer.optString(IMG);
		String imagePosition = footer.getString(POSITION);
		CellStyle cellStyle = buildHeaderTitleCellStyle(sheet);
		
		if(title!=null && !title.equals("")){		
			Row row = sheet.createRow(sheetRow);
			sheetRow++;
			Cell cell = row.createCell(6);
			cell.setCellValue(createHelper.createRichTextString(title));
			cell.setCellType(this.getCellTypeString());
			cell.setCellStyle(cellStyle);
		}
		
		if(imgName!=null && !imgName.equals("") && !imgName.equals("null")){
			File img = getImage(imgName);
			String imgNameUpperCase = imgName.toUpperCase();
			int impgType = getImageType(imgNameUpperCase);
			

			int c = 2;
			int colend = 3;
			
			if(imagePosition!=null && !imagePosition.equals("")){
				if(imagePosition.equals(LEFT)){
					c = 0;
					colend = 1;			
				}else if(imagePosition.equals(RIGHT)){
					c = 4;
					colend = 5;
				}
			}
			if(impgType!=0){
				setImageIntoWorkSheet(wb, patriarch, img, c, colend, sheetRow, 4,impgType);
				sheetRow = sheetRow+4;
			}
		}
		
		return sheetRow;
	}
	
	public int getImageType(String imgNameUpperCase) {
		int impgType = 0;
		if (imgNameUpperCase.contains(".PNG")) {
			impgType = HSSFWorkbook.PICTURE_TYPE_PNG;
		} else if (imgNameUpperCase.contains(".JPG")
				|| imgNameUpperCase.contains(".JPEG")) {
			impgType = HSSFWorkbook.PICTURE_TYPE_JPEG;
		} else if (imgNameUpperCase.contains(".DIB")
				|| imgNameUpperCase.contains(".BMP")) {
			impgType = HSSFWorkbook.PICTURE_TYPE_DIB;
		} else if (imgNameUpperCase.contains(".EMF")) {
			impgType = HSSFWorkbook.PICTURE_TYPE_EMF;
		} else if (imgNameUpperCase.contains(".PICT")
				|| imgNameUpperCase.contains(".PCT")
				|| imgNameUpperCase.contains(".PIC")) {
			impgType = HSSFWorkbook.PICTURE_TYPE_PICT;
		} else if (imgNameUpperCase.contains(".WMF")
				|| imgNameUpperCase.contains(".WMZ")) {
			impgType = HSSFWorkbook.PICTURE_TYPE_WMF;
		}
		return impgType;
	}
	
	public CellStyle buildHeaderTitleCellStyle(Sheet sheet) {
		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
        cellStyle.setAlignment(CellStyle.ALIGN_LEFT);
        cellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER); 
        Font font = sheet.getWorkbook().createFont();
        font.setFontHeightInPoints(HEADER_FONT_SIZE);
        font.setFontName(FONT_NAME);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        cellStyle.setFont(font);
        return cellStyle;
	}
	
	public CellStyle buildMetadataTitleCellStyle(Sheet sheet){
		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
        cellStyle.setAlignment(CellStyle.ALIGN_LEFT);
        cellStyle.setVerticalAlignment(CellStyle.VERTICAL_TOP); 
        cellStyle.setWrapText(true);
        Font font = sheet.getWorkbook().createFont();
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        font.setFontHeightInPoints(METADATA_TITLE_FONT_SIZE);
        font.setFontName(FONT_NAME);
        cellStyle.setFont(font);
        return cellStyle;
	}
	
	public CellStyle buildMetadataNameCellStyle(Sheet sheet){
		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
        cellStyle.setAlignment(CellStyle.ALIGN_LEFT);
        cellStyle.setVerticalAlignment(CellStyle.VERTICAL_TOP); 
        cellStyle.setWrapText(true);
        Font font = sheet.getWorkbook().createFont();
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        font.setFontHeightInPoints(METADATA_NAME_FONT_SIZE);
        font.setFontName(FONT_NAME);
        cellStyle.setFont(font);
        return cellStyle;
	}
	
	public CellStyle buildMetadataValueCellStyle(Sheet sheet){
		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
        cellStyle.setAlignment(CellStyle.ALIGN_LEFT);
        cellStyle.setVerticalAlignment(CellStyle.VERTICAL_TOP); 
        cellStyle.setWrapText(true);
        Font font = sheet.getWorkbook().createFont();
        font.setFontHeightInPoints(METADATA_VALUE_FONT_SIZE);
        font.setFontName(FONT_NAME);
        cellStyle.setFont(font);
        return cellStyle;
	}
	
	public CellStyle buildFiltersTitleCellStyle(Sheet sheet){
		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
        cellStyle.setAlignment(CellStyle.ALIGN_LEFT);
        cellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER); 
        cellStyle.setWrapText(false);
        Font font = sheet.getWorkbook().createFont();
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        font.setFontHeightInPoints(FILTERS_TITLE_FONT_SIZE);
        font.setFontName(FONT_NAME);
        cellStyle.setFont(font);
        return cellStyle;
	}
	
	public CellStyle buildFiltersValuesCellStyle(Sheet sheet){
		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
        cellStyle.setAlignment(CellStyle.ALIGN_LEFT);
        cellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER); 
        cellStyle.setWrapText(false);
        Font font = sheet.getWorkbook().createFont();
        font.setFontHeightInPoints(FILTERS_VALUES_FONT_SIZE);
        font.setFontName(FONT_NAME);
        cellStyle.setFont(font);
        return cellStyle;
	}
	
	private File getImage(String fileName) {
		logger.debug("IN");
		File toReturn = null;
		File imagesDir = QbeEngineConfig.getInstance().getWorksheetImagesDir();
		toReturn = new File(imagesDir, fileName);
		logger.debug("OUT");
		return toReturn;
	}

	public void setImageIntoWorkSheet(Workbook wb, Drawing drawing ,
			File f, int col, int colend, int sheetRow, int height,int imgType) throws IOException {
		FileInputStream fis = new FileInputStream(f);

		ByteArrayOutputStream imgBytes = new ByteArrayOutputStream();
		int b;
		while ((b = fis.read()) != -1) {
			imgBytes.write(b);
		}	
		int dx1 = 0;
        int dy1 = 0;
        int dx2 = 0;
        int dy2 = 0;
		
		int index = wb.addPicture(imgBytes.toByteArray(),imgType);
		imgBytes.close();
		fis.close();
		
		ClientAnchor anchor = getClientAnchor(col, colend, sheetRow,
				height, dx1, dy1, dx2, dy2);
		drawing.createPicture(anchor, index);

	}

	public static List<File> getImage(JSONObject content){
		String chartType = content.optString("CHART_TYPE"); //check If the chart to export is ext
		if(chartType!=null && chartType.equals("ext3")){
			return createPNGImage(content); 
		}
		return createJPGImage(content);
	}
	
	public static List<File> createPNGImage(JSONObject content) {
		List<File> exportFiles = new ArrayList<File>();
		File exportFile = null;
		try {
			
			InputStream inputStream = null;
			JSONArray images = content.optJSONArray("CHARTS_ARRAY");
			if(images==null || images.length()==0){
				return null;
			}
			for(int i=0; i<images.length(); i++){
				inputStream = new ByteArrayInputStream(ExportWorksheetAction.decodeToByteArray(images.getString(i)));
				String ext = ".png";
				BufferedImage image = ImageIO.read(inputStream);
				exportFile = File.createTempFile("chart", ext);
				ImageIO.write(image, "png", exportFile);
				exportFiles.add(exportFile);
			}

		} catch (IOException e) {
			logger.error(e);
		} catch (JSONException e) {
			logger.error(e);
		}
		return exportFiles;
	}
	
	public static List<File> createJPGImage(JSONObject content) {
		List<File> exportFiles = new ArrayList<File>();
		File exportFile = null;
		try {
			InputStream inputStream = null;
			OutputStream outputStream = null;
			String svg = content.getString(SVG);
			//Don't change ISO-8859-1 because it's the only way to export specific symbols
			inputStream = new ByteArrayInputStream(svg.getBytes("ISO-8859-1"));
			String ext = ".jpg";
			exportFile = File.createTempFile("chart", ext);
			outputStream = new FileOutputStream(exportFile);
			transformSVGIntoJPEG(inputStream, outputStream);
		} catch (IOException e) {
			logger.error(e);
		} catch (JSONException e) {
			logger.error(e);
		}
		exportFiles.add(exportFile);
		return exportFiles;
	}

	public static void transformSVGIntoJPEG(InputStream inputStream,
			OutputStream outputStream) {
		// create a JPEG transcoder
		JPEGTranscoder t = new JPEGTranscoder();

		// set the transcoding hints
		t.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, new Float(1));
		t.addTranscodingHint(JPEGTranscoder.KEY_WIDTH, new Float(1000));
		t.addTranscodingHint(JPEGTranscoder.KEY_ALLOWED_SCRIPT_TYPES, "*");
		t.addTranscodingHint(JPEGTranscoder.KEY_CONSTRAIN_SCRIPT_ORIGIN,
				new Boolean(true));
		t.addTranscodingHint(JPEGTranscoder.KEY_EXECUTE_ONLOAD, new Boolean(
				true));

		// create the transcoder input
		Reader reader = new InputStreamReader(inputStream);
		TranscoderInput input = new TranscoderInput(reader);

		// create the transcoder output
		TranscoderOutput output = new TranscoderOutput(outputStream);

		// save the image
		try {
			t.transcode(input, output);
		} catch (TranscoderException e) {
			logger.error("Impossible to convert svg to jpeg: " + e.getCause(),
					e);
			throw new SpagoBIEngineRuntimeException(
					"Impossible to convert svg to jpeg: " + e.getCause(), e);
		}
	}
	
	public int getCellTypeNumeric () {
		return HSSFCell.CELL_TYPE_NUMERIC;
	}
	
	public int getCellTypeString () {
		return HSSFCell.CELL_TYPE_STRING;
	}
	
	public int getCellTypeBoolean () {
		return HSSFCell.CELL_TYPE_BOOLEAN;
	}
	
	protected ClientAnchor getClientAnchor(int col, int colend, int sheetRow,
			int height, int dx1, int dy1, int dx2, int dy2) {
		HSSFClientAnchor anchor = new HSSFClientAnchor(dx1, dy1, dx2, dy2,
				(short) col, sheetRow, (short) colend, sheetRow + height);
		return anchor;
	}

}
