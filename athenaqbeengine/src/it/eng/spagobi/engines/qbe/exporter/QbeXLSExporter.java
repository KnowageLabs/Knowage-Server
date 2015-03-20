/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.exporter;

import it.eng.spagobi.engines.qbe.query.Field;
import it.eng.spagobi.engines.worksheet.bo.MeasureScaleFactorOption;
import it.eng.spagobi.engines.worksheet.serializer.json.WorkSheetSerializationUtils;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class QbeXLSExporter {
	
	/** Logger component. */
    public static transient Logger logger = Logger.getLogger(QbeXLSExporter.class);
    
    /** Configuration properties */
    public static final String PROPERTY_HEADER_FONT_SIZE = "HEADER_FONT_SIZE";
    public static final String PROPERTY_HEADER_COLOR = "HEADER_COLOR";
    public static final String PROPERTY_HEADER_BACKGROUND_COLOR = "HEADER_BACKGROUND_COLOR";
    public static final String PROPERTY_HEADER_BORDER_COLOR = "HEADER_BORDER_COLOR";
    public static final String PROPERTY_CELL_FONT_SIZE = "CELL_FONT_SIZE";
    public static final String PROPERTY_CELL_COLOR = "CELL_COLOR";
    public static final String PROPERTY_CELL_BACKGROUND_COLOR = "CELL_BACKGROUND_COLOR";
    public static final String PROPERTY_CELL_BORDER_COLOR = "CELL_BORDER_COLOR";
    public static final String PROPERTY_FONT_NAME = "FONT_NAME";
    
    public static final short DEFAULT_HEADER_FONT_SIZE = 8;
    public static final String DEFAULT_HEADER_COLOR = "BLACK";
    public static final String DEFAULT_HEADER_BACKGROUND_COLOR = "GREY_25_PERCENT";
    public static final String DEFAULT_HEADER_BORDER_COLOR = "WHITE";
    public static final short DEFAULT_CELL_FONT_SIZE = 8;
    public static final String DEFAULT_CELL_COLOR = "BLACK";
    public static final String DEFAULT_CELL_BACKGROUND_COLOR = "WHITE";
    public static final String DEFAULT_CELL_BORDER_COLOR = "BLACK";
    public static final String DEFAULT_DIMENSION_NAME_COLOR = "BLACK";
    public static final String DEFAULT_DIMENSION_NAME_BACKGROUND_COLOR = "LIGHT_BLUE";
	public static final String DEFAULT_FONT_NAME = "Verdana";
    
	public static final int DEFAULT_DECIMAL_PRECISION = 2;
    
	public static final int DEFAULT_START_COLUMN = 0;
	
	private Locale locale;
	private Map<String, Object> properties;
    
	IDataStore dataStore = null;
	Vector extractedFields = null;
	Map<Integer, CellStyle> decimalFormats = new HashMap<Integer, CellStyle>();

	public QbeXLSExporter(IDataStore dataStore, Locale locale ) {
		super();
		this.dataStore = dataStore;
		this.locale = locale;
		this.properties = new HashMap<String, Object>();
	}

	public IDataStore getDataStore() {
		return dataStore;
	}

	public void setDataStore(IDataStore dataStore) {
		this.dataStore = dataStore;
	}

	public QbeXLSExporter() {
		super();
		this.properties = new HashMap<String, Object>();
	}
	
	public void setProperty(String propertyName, Object propertyValue) {
		this.properties.put(propertyName, propertyValue);
	}
	
	public Object getProperty(String propertyName) {
		return this.properties.get(propertyName);
	}
	
	public Workbook export() {
		Workbook workbook = this.instantiateWorkbook();
	    CreationHelper createHelper = workbook.getCreationHelper();
	    Sheet sheet = workbook.createSheet("new sheet");
	    for(int j = 0; j < 50; j++){
			sheet.createRow(j);
		}
	    fillSheet(sheet, workbook, createHelper, 0);
	    return workbook;
	}
	
	public void fillSheet(Sheet sheet, Workbook wb,
			CreationHelper createHelper, int startRow) {
		// we enrich the JSON object putting every node the descendants_no
		// property: it is useful when merging cell into rows/columns headers
		// and when initializing the sheet
		if (dataStore != null && !dataStore.isEmpty()) {
			CellStyle[] cellTypes = fillSheetHeader(sheet, wb, createHelper,
					startRow, DEFAULT_START_COLUMN);
			fillSheetData(sheet, wb, createHelper, cellTypes, startRow + 1,
					DEFAULT_START_COLUMN);
		}
	}
	/**
	 * 
	 * @param sheet ...
	 * @param workbook ...
	 * @param createHelper ...
	 * @param beginRowHeaderData header's vertical offset. Expressed in number of rows
	 * @param beginColumnHeaderData header's horizontal offset. Expressed in number of columns
	 
	 * @return ...
	 */
	private CellStyle[] fillSheetHeader(
			Sheet sheet, 
			Workbook workbook,
			CreationHelper createHelper, 
			int beginRowHeaderData,
			int beginColumnHeaderData
	) {
		
		CellStyle[] cellTypes;
		
		logger.trace("IN");
		
		try  {
		
			
			IMetaData dataStoreMetaData = dataStore.getMetaData();
			int colnumCount = dataStoreMetaData.getFieldCount();
			
			Row headerRow = sheet.getRow(beginRowHeaderData);
			CellStyle headerCellStyle = buildHeaderCellStyle(sheet);
			
			cellTypes = new CellStyle[colnumCount]; 
			for (int j = 0; j < colnumCount; j++) {
				Cell cell = headerRow.createCell(j + beginColumnHeaderData);
				cell.setCellType(getCellTypeString());
				String fieldName = dataStoreMetaData.getFieldAlias(j);
				IFieldMetaData fieldMetaData = dataStoreMetaData.getFieldMeta(j);
				String format = (String) fieldMetaData.getProperty("format");
				String alias = (String) fieldMetaData.getAlias();
				String scaleFactorHeader = (String) fieldMetaData.getProperty(
						WorkSheetSerializationUtils.WORKSHEETS_ADDITIONAL_DATA_FIELDS_OPTIONS_SCALE_FACTOR);
	
				String header;
				if (extractedFields != null && j < extractedFields.size() && extractedFields.get(j) != null) {
					Field field = (Field) extractedFields.get(j);
					fieldName = field.getAlias();
					if (field.getPattern() != null) {
						format = field.getPattern();
					}
				}
				CellStyle aCellStyle = this.buildCellStyle(sheet);
				if (format != null) {
					short formatInt = this.getBuiltinFormat(format);
					aCellStyle.setDataFormat(formatInt);
					cellTypes[j] = aCellStyle;
				}
	
				if (alias != null && !alias.equals("")) {
					header = alias;
				} else {
					header = fieldName;
				}
	
				header = MeasureScaleFactorOption.getScaledName(header,
						scaleFactorHeader, locale);
				cell.setCellValue(createHelper.createRichTextString(header));
	
				cell.setCellStyle(headerCellStyle);
	
			}
		
		} catch(Throwable t) {
			throw new SpagoBIRuntimeException("An unexpected error occured while filling sheet header", t);
		} finally {
			logger.trace("OUT");
		}
		
		return cellTypes;
	}
	
	public CellStyle buildHeaderCellStyle(Sheet sheet) {
		
		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
        cellStyle.setAlignment(CellStyle.ALIGN_LEFT);
        cellStyle.setVerticalAlignment(CellStyle.ALIGN_CENTER);
        
        String headerBGColor = (String) this.getProperty(PROPERTY_HEADER_BACKGROUND_COLOR);
        logger.debug("Header background color : " + headerBGColor);
		short backgroundColorIndex = headerBGColor != null ? IndexedColors.valueOf(
				headerBGColor).getIndex() : IndexedColors.valueOf(
				DEFAULT_HEADER_BACKGROUND_COLOR).getIndex();
		cellStyle.setFillForegroundColor(backgroundColorIndex);
		
        cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        
        cellStyle.setBorderBottom(CellStyle.BORDER_THIN);
        cellStyle.setBorderLeft(CellStyle.BORDER_THIN);
        cellStyle.setBorderRight(CellStyle.BORDER_THIN);
        cellStyle.setBorderTop(CellStyle.BORDER_THIN);

        String bordeBorderColor = (String) this.getProperty(PROPERTY_HEADER_BORDER_COLOR);
        logger.debug("Header border color : " + bordeBorderColor);
		short borderColorIndex = bordeBorderColor != null ? IndexedColors.valueOf(
				bordeBorderColor).getIndex() : IndexedColors.valueOf(
				DEFAULT_HEADER_BORDER_COLOR).getIndex();
				
        cellStyle.setLeftBorderColor(borderColorIndex);
        cellStyle.setRightBorderColor(borderColorIndex);
        cellStyle.setBottomBorderColor(borderColorIndex);
        cellStyle.setTopBorderColor(borderColorIndex);
        
        Font font = sheet.getWorkbook().createFont();
        
        Short headerFontSize = (Short) this.getProperty(PROPERTY_HEADER_FONT_SIZE);
        logger.debug("Header font size : " + headerFontSize);
		short headerFontSizeShort = headerFontSize != null ? headerFontSize.shortValue() : DEFAULT_HEADER_FONT_SIZE;
        font.setFontHeightInPoints(headerFontSizeShort);
        
        String fontName = (String) this.getProperty(PROPERTY_FONT_NAME);
        logger.debug("Font name : " + fontName);
        fontName = fontName != null ? fontName : DEFAULT_FONT_NAME;
        font.setFontName(fontName);
        
        String headerColor = (String) this.getProperty(PROPERTY_HEADER_COLOR);
        logger.debug("Header color : " + headerColor);
		short headerColorIndex = bordeBorderColor != null ? IndexedColors.valueOf(
				headerColor).getIndex() : IndexedColors.valueOf(
				DEFAULT_HEADER_COLOR).getIndex();
        font.setColor(headerColorIndex);
        
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        cellStyle.setFont(font);
        return cellStyle;
	}
	
	public CellStyle buildCellStyle(Sheet sheet) {
		
		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
        cellStyle.setAlignment(CellStyle.ALIGN_RIGHT);
        cellStyle.setVerticalAlignment(CellStyle.ALIGN_CENTER);
        
        String cellBGColor = (String) this.getProperty(PROPERTY_CELL_BACKGROUND_COLOR);
        logger.debug("Cell background color : " + cellBGColor);
		short backgroundColorIndex = cellBGColor != null ? IndexedColors.valueOf(
				cellBGColor).getIndex() : IndexedColors.valueOf(
				DEFAULT_CELL_BACKGROUND_COLOR).getIndex();
		cellStyle.setFillForegroundColor(backgroundColorIndex);
		
        cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        
        cellStyle.setBorderBottom(CellStyle.BORDER_THIN);
        cellStyle.setBorderLeft(CellStyle.BORDER_THIN);
        cellStyle.setBorderRight(CellStyle.BORDER_THIN);
        cellStyle.setBorderTop(CellStyle.BORDER_THIN);

        String bordeBorderColor = (String) this.getProperty(PROPERTY_CELL_BORDER_COLOR);
        logger.debug("Cell border color : " + bordeBorderColor);
		short borderColorIndex = bordeBorderColor != null ? IndexedColors.valueOf(
				bordeBorderColor).getIndex() : IndexedColors.valueOf(
				DEFAULT_CELL_BORDER_COLOR).getIndex();
				
        cellStyle.setLeftBorderColor(borderColorIndex);
        cellStyle.setRightBorderColor(borderColorIndex);
        cellStyle.setBottomBorderColor(borderColorIndex);
        cellStyle.setTopBorderColor(borderColorIndex);
        
        Font font = sheet.getWorkbook().createFont();
        
        Short cellFontSize = (Short) this.getProperty(PROPERTY_CELL_FONT_SIZE);
        logger.debug("Cell font size : " + cellFontSize);
		short cellFontSizeShort = cellFontSize != null ? cellFontSize.shortValue() : DEFAULT_CELL_FONT_SIZE;
        font.setFontHeightInPoints(cellFontSizeShort);
        
        String fontName = (String) this.getProperty(PROPERTY_FONT_NAME);
        logger.debug("Font name : " + fontName);
        fontName = fontName != null ? fontName : DEFAULT_FONT_NAME;
        font.setFontName(fontName);
        
        String cellColor = (String) this.getProperty(PROPERTY_CELL_COLOR);
        logger.debug("Cell color : " + cellColor);
		short cellColorIndex = cellColor != null ? IndexedColors.valueOf(
				cellColor).getIndex() : IndexedColors.valueOf(
				DEFAULT_CELL_COLOR).getIndex();
        font.setColor(cellColorIndex);
        
        cellStyle.setFont(font);
        return cellStyle;
	}

	public void fillSheetData(Sheet sheet,Workbook wb, CreationHelper createHelper,CellStyle[] cellTypes, int beginRowData, int beginColumnData) {	
		CellStyle dCellStyle = this.buildCellStyle(sheet);
		Iterator it = dataStore.iterator();
    	int rownum = beginRowData;
    	short formatIndexInt = this.getBuiltinFormat("#,##0");
	    CellStyle cellStyleInt = this.buildCellStyle(sheet); // cellStyleInt is the default cell style for integers
	    cellStyleInt.cloneStyleFrom(dCellStyle);
	    cellStyleInt.setDataFormat(formatIndexInt);
	    
		CellStyle cellStyleDate = this.buildCellStyle(sheet); // cellStyleDate is the default cell style for dates
		cellStyleDate.cloneStyleFrom(dCellStyle);
		cellStyleDate.setDataFormat(createHelper.createDataFormat().getFormat("m/d/yy"));
		
		IMetaData d = dataStore.getMetaData();	
		
		while(it.hasNext()){
			Row rowVal = sheet.getRow(rownum);
			IRecord record =(IRecord)it.next();
			List fields = record.getFields();
			int length = fields.size();
			for ( int fieldIndex = 0 ; fieldIndex < length ; fieldIndex++ ){
				IField f = (IField)fields.get(fieldIndex);
				if (f != null && f.getValue()!= null) {

					Class c = d.getFieldType(fieldIndex);
					logger.debug("Column [" + (fieldIndex) + "] class is equal to [" + c.getName() + "]");
					if (rowVal == null) {
						rowVal = sheet.createRow(rownum);
					}
					Cell cell = rowVal.createCell(fieldIndex + beginColumnData);
					cell.setCellStyle(dCellStyle);
					if( Integer.class.isAssignableFrom(c) || Short.class.isAssignableFrom(c)) {
						logger.debug("Column [" + (fieldIndex+1) + "] type is equal to [" + "INTEGER" + "]");
						IFieldMetaData fieldMetaData = d.getFieldMeta(fieldIndex);
						String scaleFactor = (String) fieldMetaData.getProperty(WorkSheetSerializationUtils.WORKSHEETS_ADDITIONAL_DATA_FIELDS_OPTIONS_SCALE_FACTOR);
					    Number val = (Number)f.getValue();
					    Double doubleValue = MeasureScaleFactorOption.applyScaleFactor(val.doubleValue(), scaleFactor);
					    cell.setCellValue(doubleValue);
					    cell.setCellType(this.getCellTypeNumeric());
					    cell.setCellStyle((cellTypes[fieldIndex] != null) ? cellTypes[fieldIndex] : cellStyleInt);
					}else if( Number.class.isAssignableFrom(c) ) {
						logger.debug("Column [" + (fieldIndex+1) + "] type is equal to [" + "NUMBER" + "]");
			    	    IFieldMetaData fieldMetaData = d.getFieldMeta(fieldIndex);	    
						String decimalPrecision = (String)fieldMetaData.getProperty(IFieldMetaData.DECIMALPRECISION);
						CellStyle cs ;
					    if (decimalPrecision != null) {
					    	cs = getDecimalNumberFormat(new Integer(decimalPrecision), sheet, createHelper, dCellStyle);
					    } else {
					    	cs = getDecimalNumberFormat(DEFAULT_DECIMAL_PRECISION, sheet, createHelper, dCellStyle);
					    }
					    Number val = (Number)f.getValue();
					    Double value = val.doubleValue();
						String scaleFactor = (String) fieldMetaData.getProperty(WorkSheetSerializationUtils.WORKSHEETS_ADDITIONAL_DATA_FIELDS_OPTIONS_SCALE_FACTOR);
					    cell.setCellValue(MeasureScaleFactorOption.applyScaleFactor(value, scaleFactor));
					    cell.setCellType(this.getCellTypeNumeric());
					    cell.setCellStyle((cellTypes[fieldIndex] != null) ? cellTypes[fieldIndex] : cs);
					}else if( String.class.isAssignableFrom(c)){
						logger.debug("Column [" + (fieldIndex+1) + "] type is equal to [" + "STRING" + "]");	    
					    String val = (String)f.getValue();
					    cell.setCellValue(createHelper.createRichTextString(val));
					    cell.setCellType(this.getCellTypeString());
					}else if( Boolean.class.isAssignableFrom(c) ) {
						logger.debug("Column [" + (fieldIndex+1) + "] type is equal to [" + "BOOLEAN" + "]");
					    Boolean val = (Boolean)f.getValue();
					    cell.setCellValue(val.booleanValue());
					    cell.setCellType(this.getCellTypeBoolean());
					}else if(Date.class.isAssignableFrom(c)){
						logger.debug("Column [" + (fieldIndex+1) + "] type is equal to [" + "DATE" + "]");	    
					    Date val = (Date)f.getValue();
					    cell.setCellValue(val);	
					    cell.setCellStyle(cellStyleDate);
					}else{
						logger.warn("Column [" + (fieldIndex+1) + "] type is equal to [" + "???" + "]");
					    String val = f.getValue().toString();
					    cell.setCellValue(createHelper.createRichTextString(val));
					    cell.setCellType(this.getCellTypeString());	    
					}
				}
			}
		   rownum ++;
		}
	}
	

	public void setExtractedFields(Vector extractedFields) {
		this.extractedFields = extractedFields;
	}
	
	
	private CellStyle getDecimalNumberFormat(int j, Sheet sheet, CreationHelper createHelper, CellStyle dCellStyle) {

		if (decimalFormats.get(j) != null)
			return decimalFormats.get(j);
		String decimals = "";
		for (int i = 0; i < j; i++) {
			decimals += "0";
		}
		
	    CellStyle cellStyleDoub = this.buildCellStyle(sheet); // cellStyleDoub is the default cell style for doubles
	    cellStyleDoub.cloneStyleFrom(dCellStyle);
	    DataFormat df = createHelper.createDataFormat();
		String format = "#,##0";
		if (decimals.length() > 0) {
			format += "." + decimals;
		}
	    cellStyleDoub.setDataFormat(df.getFormat(format));
		
		decimalFormats.put(j, cellStyleDoub);
		return cellStyleDoub;
	}

	protected Workbook instantiateWorkbook() {
		Workbook workbook = new HSSFWorkbook();
		return workbook;
	}
	
	protected int getCellTypeNumeric () {
		return HSSFCell.CELL_TYPE_NUMERIC;
	}
	
	protected int getCellTypeString () {
		return HSSFCell.CELL_TYPE_STRING;
	}

	protected int getCellTypeBoolean () {
		return HSSFCell.CELL_TYPE_BOOLEAN;
	}
	
	protected short getBuiltinFormat (String formatStr) {
		short format = HSSFDataFormat.getBuiltinFormat(formatStr); 
		return format;
	}
	
}
