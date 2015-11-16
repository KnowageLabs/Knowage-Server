/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.crosstable.exporter;


import it.eng.qbe.serializer.SerializationException;
import it.eng.spagobi.engines.qbe.crosstable.CrossTab;
import it.eng.spagobi.engines.qbe.crosstable.CrossTab.CellType;
import it.eng.spagobi.engines.qbe.crosstable.Node;
import it.eng.spagobi.engines.worksheet.bo.MeasureScaleFactorOption;
import it.eng.spagobi.engines.worksheet.services.export.MeasureFormatter;
import it.eng.spagobi.utilities.messages.EngineMessageBundle;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Exports the crosstab data (formatted as a JSON object in input) into a XLS file.
 * The JSON object should have this structure (a node is {node_key:"Text", node_childs:[...]}):
 * 		columns: {...} contains tree node structure of the columns' headers
 * 		rows: {...} contains tree node structure of the rows' headers
 * 		data: [[...], [...], ...] 2-dimensional matrix containing crosstab data
 * 
 * @author Alberto Ghedin (alberto.ghedin@eng.it), Davide Zerbetto (davide.zerbetto@eng.it)
 */
public class CrosstabXLSExporter {
	
	/** Logger component. */
    public static transient Logger logger = Logger.getLogger(CrosstabXLSExporter.class);
	
    
    /** Configuration properties */
    public static final String PROPERTY_HEADER_FONT_SIZE = "HEADER_FONT_SIZE";
    public static final String PROPERTY_HEADER_COLOR = "HEADER_COLOR";
    public static final String PROPERTY_HEADER_BACKGROUND_COLOR = "HEADER_BACKGROUND_COLOR";
    public static final String PROPERTY_HEADER_BORDER_COLOR = "HEADER_BORDER_COLOR";
    public static final String PROPERTY_CELL_FONT_SIZE = "CELL_FONT_SIZE";
    public static final String PROPERTY_CELL_COLOR = "CELL_COLOR";
    public static final String PROPERTY_CELL_BACKGROUND_COLOR = "CELL_BACKGROUND_COLOR";
    public static final String PROPERTY_CELL_BORDER_COLOR = "CELL_BORDER_COLOR";
    public static final String PROPERTY_DIMENSION_NAME_COLOR = "DIMENSION_NAME_COLOR";
    public static final String PROPERTY_DIMENSION_NAME_BACKGROUND_COLOR = "HEADER_DIMENSION_NAME_BACKGROUND_COLOR";
    public static final String PROPERTY_FONT_NAME = "FONT_NAME";
    
    public static final String PROPERTY_CALCULATED_FIELD_DECIMALS = "CALCULATED_FIELD_DECIMALS";
    
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
	public static final int DEFAULT_CALCULATED_FIELD_DECIMALS = 2;
	
	private Properties properties;
	

	public CrosstabXLSExporter(Properties properties) {
		super();
		if (properties == null) {
			this.properties = new Properties();
		} else {
			this.properties = properties;
		}
		
	}
	
	public void setProperty(String propertyName, Object propertyValue) {
		this.properties.put(propertyName, propertyValue);
	}
	
	public Object getProperty(String propertyName) {
		return this.properties.get(propertyName);
	}

	public int fillAlreadyCreatedSheet(Sheet sheet, CrossTab cs,
			JSONObject crosstabJSON, CreationHelper createHelper, int startRow,
			Locale locale) throws JSONException, SerializationException {
		// we enrich the JSON object putting every node the descendants_no
		// property: it is useful when merging cell into rows/columns headers
		// and when initializing the sheet
		int totalRowNum = commonFillSheet(sheet, cs, crosstabJSON,
				createHelper, startRow, locale);
		return totalRowNum;
	}
	
	public int commonFillSheet(Sheet sheet, CrossTab cs,
			JSONObject crosstabJSON, CreationHelper createHelper, int startRow,
			Locale locale) throws SerializationException, JSONException {
		int columnsDepth = cs.getColumnsRoot().getSubTreeDepth();
		int rowsDepth = cs.getRowsRoot().getSubTreeDepth();

		MeasureFormatter measureFormatter = new MeasureFormatter(cs);
		int rowsNumber = cs.getDataMatrix().length;
		// + 1 because there may be also the bottom row with the totals
		int totalRowsNumber = columnsDepth + rowsNumber + 1;
		for (int i = 0; i < totalRowsNumber + 5; i++) {
			sheet.createRow(startRow + i);
		}

		CellStyle memberCellStyle = this.buildHeaderCellStyle(sheet);
		CellStyle dimensionCellStyle = this.buildDimensionCellStyle(sheet);

		// build headers for column first ...
		buildColumnsHeader(sheet, cs, cs.getColumnsRoot().getChilds(),
				startRow, rowsDepth - 1, createHelper, locale, memberCellStyle,
				dimensionCellStyle);
		// ... then build headers for rows ....
		buildRowsHeaders(sheet, cs, cs.getRowsRoot().getChilds(), columnsDepth
				- 1 + startRow, 0, createHelper, locale, memberCellStyle);
		// then put the matrix data
		buildDataMatrix(sheet, cs, columnsDepth + startRow - 1, rowsDepth - 1,
				createHelper, measureFormatter);

		buildRowHeaderTitle(sheet, cs, columnsDepth - 2, 0, startRow,
				createHelper, locale, dimensionCellStyle);

		return startRow + totalRowsNumber;
	}
	

	/**
	 * Sheet initialization. We create as many rows as it is required to contain the crosstab.
	 * 
	 * @param sheet The XLS sheet
	 * @param json The crosstab data (it must have been enriched with the calculateDescendants method)
	 * @throws JSONException
	 */
	public int initSheet(Sheet sheet, CrossTab cs) throws JSONException {

		int columnsDepth = cs.getColumnsRoot().getSubTreeDepth();
		int rowsNumber = cs.getRowsRoot().getSubTreeDepth();
		// + 1 because there may be also the bottom row with the totals
		int totalRowsNumber = columnsDepth + rowsNumber + 1;
		for (int i = 0; i < totalRowsNumber + 4; i++) {
			sheet.createRow(i);
		}
		return totalRowsNumber + 4;
	}

	protected int buildDataMatrix(Sheet sheet, CrossTab cs, int rowOffset,
			int columnOffset, CreationHelper createHelper,
			MeasureFormatter measureFormatter) throws JSONException {

		CellStyle cellStyleForNA = buildNACellStyle(sheet);

		Map<Integer, CellStyle> decimalFormats = new HashMap<Integer, CellStyle>();
		int endRowNum = 0;
		for (int i = 0; i < cs.getDataMatrix().length; i++) {
			for (int j = 0; j < cs.getDataMatrix()[0].length; j++) {
				String text = (String) cs.getDataMatrix()[i][j];
				int rowNum = rowOffset + i;
				int columnNum = columnOffset + j;
				Row row = sheet.getRow(rowNum);
				if (row == null) {
					row = sheet.createRow(rowNum);
				}
				endRowNum = rowNum;
				Cell cell = row.createCell(columnNum);
				try {
					double value = Double.parseDouble(text);
					int decimals = measureFormatter.getFormatXLS(i, j);
					Double valueFormatted = measureFormatter.applyScaleFactor(
							value, i, j);
					cell.setCellValue(valueFormatted);
					cell.setCellType(this.getCellTypeNumeric());
					cell.setCellStyle(getNumberFormat(decimals, decimalFormats,
							sheet, createHelper, cs.getCellType(i, j)));
				} catch (NumberFormatException e) {
					logger.debug("Text " + text
							+ " is not recognized as a number");
					cell.setCellValue(createHelper.createRichTextString(text));
					cell.setCellType(this.getCellTypeString());
					cell.setCellStyle(cellStyleForNA);
				}

			}
		}
		return endRowNum;
	}

	protected int getCellTypeNumeric () {
		return HSSFCell.CELL_TYPE_NUMERIC;
	}
	
	protected int getCellTypeString () {
		return HSSFCell.CELL_TYPE_STRING;
	}
	

	public CellStyle buildNACellStyle(Sheet sheet) {
		CellStyle cellStyleForNA = this.buildDataCellStyle(sheet);
		cellStyleForNA.setAlignment(CellStyle.ALIGN_CENTER);
		return cellStyleForNA;
	}

	/**
	 * Builds the rows' headers recursively with this order:
	 * |-----|-----|-----|
	 * |     |     |  3  |
	 * |     |     |-----|
	 * |     |  2  |  4  |
	 * |     |     |-----|
	 * |  1  |     |  5  |
	 * |     |-----|-----|
	 * |     |     |  7  |
	 * |     |  6  |-----|
	 * |     |     |  8  |
	 * |-----|-----|-----|
	 * |     |     |  11 |
	 * |  9  |  10 |-----|
	 * |     |     |  12 |
	 * |-----|-----|-----|
	 * 
	 * @param sheet The sheet of the XLS file
	 * @param siblings The siblings nodes of the headers structure
	 * @param rowNum The row number where the first sibling must be inserted
	 * @param columnNum The column number where the siblings must be inserted
	 * @param createHelper The file creation helper
	 * @throws JSONException
	 */
	protected void buildRowsHeaders(Sheet sheet, CrossTab cs,
			List<Node> siblings, int rowNum, int columnNum,
			CreationHelper createHelper, Locale locale, CellStyle cellStyle)
			throws JSONException {
		int rowsCounter = rowNum;
		
		for (int i = 0; i < siblings.size(); i++) {
			Node aNode =  siblings.get(i);
			List<Node> childs = aNode.getChilds();
			Row row = sheet.getRow(rowsCounter);
			Cell cell = row.createCell(columnNum);
			String text = (String) aNode.getDescription();
			
			if (cs.isMeasureOnRow() && (childs == null || childs.size() <= 0)) {
		    	//apply the measure scale factor
		    	text = MeasureScaleFactorOption.getScaledName(text, cs.getMeasureScaleFactor(text), locale);
			}
			cell.setCellValue(createHelper.createRichTextString(text));
		    cell.setCellType(this.getCellTypeString());

	        cell.setCellStyle(cellStyle);
	       
		    int descendants = aNode.getLeafsNumber();
		    if (descendants > 1) {
			    sheet.addMergedRegion(new CellRangeAddress(
			    		rowsCounter, //first row (0-based)
			    		rowsCounter + descendants - 1, //last row  (0-based)
			    		columnNum, //first column (0-based)
			    		columnNum //last column  (0-based)
			    ));
		    }
		   
		    if (childs != null && childs.size() > 0) {
		    	buildRowsHeaders(sheet,cs, childs, rowsCounter, columnNum + 1, createHelper, locale, cellStyle);
		    }
		    int increment = descendants > 1 ? descendants : 1;
		    rowsCounter = rowsCounter + increment;
		}
	}
	
	/**
	 * Add the title of the columns in the row headers
	 * @param sheet
	 * @param titles list of titles
	 * @param columnHeadersNumber number of column headers
	 * @param startColumn first column of the crosstab in the xls
	 * @param startRow first row of the crosstab in the xls
	 * @param createHelper
	 * @throws JSONException
	 */
	protected void buildRowHeaderTitle(Sheet sheet, CrossTab cs,
			int columnHeadersNumber, int startColumn, int startRow,
			CreationHelper createHelper, Locale locale, CellStyle cellStyle)
			throws JSONException {
		List<String> titles = cs.getRowHeadersTitles();
		 
		if (titles != null) {

			Row row = sheet.getRow(startRow + columnHeadersNumber);
			for (int i = 0; i < titles.size(); i++) {

				Cell cell = row.createCell(startColumn + i);
				String text = titles.get(i);
				cell.setCellValue(createHelper.createRichTextString(text));
				cell.setCellType(this.getCellTypeString());
				cell.setCellStyle(cellStyle);
			}
			if (cs.isMeasureOnRow()) {
				Cell cell = row.createCell(startColumn + titles.size());
				String text = "Measures";
				if (locale != null) {
					text = EngineMessageBundle
							.getMessage(
									"worksheet.export.crosstab.header.measures",
									locale);
				}
				cell.setCellValue(createHelper.createRichTextString(text));
				cell.setCellType(this.getCellTypeString());
				cell.setCellStyle(cellStyle);
			}
		}
	}
	
	public CellStyle buildDimensionCellStyle(Sheet sheet) {
		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
        cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
        cellStyle.setVerticalAlignment(CellStyle.ALIGN_CENTER);
        
        String headerBGColor = (String) this.getProperty(PROPERTY_DIMENSION_NAME_BACKGROUND_COLOR);
        logger.debug("Header background color : " + headerBGColor);
		short backgroundColorIndex = headerBGColor != null ? IndexedColors.valueOf(
				headerBGColor).getIndex() : IndexedColors.valueOf(
				DEFAULT_DIMENSION_NAME_BACKGROUND_COLOR).getIndex();
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
        
        String color = (String) this.getProperty(PROPERTY_DIMENSION_NAME_COLOR);
        logger.debug("Dimension color : " + color);
		short colorIndex = bordeBorderColor != null ? IndexedColors.valueOf(
				color).getIndex() : IndexedColors.valueOf(
				DEFAULT_DIMENSION_NAME_COLOR).getIndex();
        font.setColor(colorIndex);
        
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        font.setItalic(true);
        cellStyle.setFont(font);
        return cellStyle;
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
	
	public CellStyle buildDataCellStyle(Sheet sheet){
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



	/**
	 * Builds the columns' headers recursively with this order:
	 * |------------------------------------------|
	 * |              1              |     9      |
	 * |------------------------------------------|
	 * |     2     |        5        |     10     |
	 * |-----------|-----------------|------------|
	 * |  3  |  4  |  6  |  7  |  8  |  11  | 12  |
	 * |------------------------------------------|
	 * 
	 * @param sheet The sheet of the XLS file
	 * @param siblings The siblings nodes of the headers structure
	 * @param rowNum The row number where the siblings must be inserted
	 * @param columnNum The column number where the first sibling must be inserted
	 * @param createHelper The file creation helper
	 * @param dimensionCellStyle The cell style for cells containing dimensions (i.e. attributes' names)
	 * @param memberCellStyle The cell style for cells containing members (i.e. attributes' values)
	 * @throws JSONException
	 */
	protected void buildColumnsHeader(Sheet sheet, CrossTab cs,
			List<Node> siblings, int rowNum, int columnNum,
			CreationHelper createHelper, Locale locale,
			CellStyle memberCellStyle, CellStyle dimensionCellStyle)
			throws JSONException {
		int columnCounter = columnNum;
		
		for (int i = 0; i < siblings.size(); i++) {
			Node aNode = (Node) siblings.get(i);
			List<Node> childs = aNode.getChilds();
			Row row = sheet.getRow(rowNum);
			Cell cell = row.createCell(columnCounter);
			String text = (String) aNode.getDescription();
			if (!cs.isMeasureOnRow() && (childs == null || childs.size() <= 0)) {
		    	//apply the measure scale factor
		    	text = MeasureScaleFactorOption.getScaledName(text, cs.getMeasureScaleFactor(text), locale);
			}
			
			cell.setCellValue(createHelper.createRichTextString(text));
		    cell.setCellType(this.getCellTypeString());	    
		    int descendants = aNode.getLeafsNumber();
		    if (descendants > 1) {
			    sheet.addMergedRegion(new CellRangeAddress(
			    		rowNum, //first row (0-based)
			    		rowNum, //last row  (0-based)
			    		columnCounter, //first column (0-based)
			    		columnCounter + descendants - 1  //last column  (0-based)
			    ));
		    }
		    
			/*
			 * Now we have to set the style properly according to the nature of
			 * the node: if it contains the name of a dimension or a member.
			 * Since the structure foresees that a list of members follows a
			 * dimension, we calculate the position of the node with respect to
			 * the leaves; in case it is odd, the cell contains a dimension; in
			 * case it is even, the cell contains a dimension.
			 */
		    int distanceToLeaves = aNode.getDistanceFromLeaves();
		    if ( !cs.isMeasureOnRow() ) {
		    	distanceToLeaves--;
		    }
		    boolean isDimensionNameCell = distanceToLeaves > 0 && (distanceToLeaves % 2) == 1;
		    if (isDimensionNameCell) {
		    	cell.setCellStyle(dimensionCellStyle);
		    } else {
		    	cell.setCellStyle(memberCellStyle);
		    }
		    
		    if (childs != null && childs.size() > 0) {
				buildColumnsHeader(sheet, cs, childs, rowNum + 1,
						columnCounter, createHelper, locale, memberCellStyle,
						dimensionCellStyle);
		    }
		    int increment = descendants > 1 ? descendants : 1;
		    columnCounter = columnCounter + increment;
		}
	}

	
	public CellStyle getNumberFormat(int j,
			Map<Integer, CellStyle> decimalFormats, Sheet sheet,
			CreationHelper createHelper, CellType celltype) {

		int mapPosition = j;

		if (celltype.equals(CellType.TOTAL)) {
			mapPosition = j + 90000;
		} else if (celltype.equals(CellType.SUBTOTAL)) {
			mapPosition = j + 80000;
		} else if (celltype.equals(CellType.CF)) {
			mapPosition = j + 60000;
		}

		if (decimalFormats.get(mapPosition) != null)
			return decimalFormats.get(mapPosition);

		if (celltype.equals(CellType.CF)) {
			j = this.getCalculatedFieldDecimals();
		}

		String decimals = "";

		for (int i = 0; i < j; i++) {
			decimals += "0";
		}

		CellStyle cellStyle = this.buildDataCellStyle(sheet);
		DataFormat df = createHelper.createDataFormat();
		String format = "#,##0";
		if (decimals.length() > 0) {
			format += "." + decimals;
		}
		cellStyle.setDataFormat(df.getFormat(format));

		if (celltype.equals(CellType.TOTAL)) {
			cellStyle.setFillForegroundColor(IndexedColors.GREY_40_PERCENT
					.getIndex());
		}
		if (celltype.equals(CellType.CF)) {
			cellStyle.setFillForegroundColor(IndexedColors.DARK_YELLOW
					.getIndex());
		}
		if (celltype.equals(CellType.SUBTOTAL)) {
			cellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT
					.getIndex());
		}

		decimalFormats.put(mapPosition, cellStyle);
		return cellStyle;
	}
	
	public int getCalculatedFieldDecimals() {
		Integer decimals = (Integer) this.getProperty(PROPERTY_CALCULATED_FIELD_DECIMALS);
		if (decimals == null) {
			return DEFAULT_CALCULATED_FIELD_DECIMALS;
		}
		return decimals;
	}


}
