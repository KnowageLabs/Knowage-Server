/**

SpagoBI - The Business Intelligence Free Platform

Copyright (C) 2005-2010 Engineering Ingegneria Informatica S.p.A.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

**/
package it.eng.spagobi.tools.dataset.normalization;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class XLSFileNormalizer {
	
	File datasetFile;
	Map<Object, Object> levelSiblingsValue;
	String columnNameOnFile;
	private String skipRows;
	private String limitRows;
	private String xslSheetNumber;
	private String levelName;
	private int numberOfColumns = 0;

	private static transient Logger logger = Logger.getLogger(XLSFileNormalizer.class);
	
	private String newColumnName;
	private String newColumnType;

	
	public XLSFileNormalizer(File datasetFile, Map<Object, Object> levelSiblingsValue,  String columnNameOnFile, String levelName){
		this.datasetFile = datasetFile;
		this.levelSiblingsValue = levelSiblingsValue;
		this.columnNameOnFile = columnNameOnFile;
		this.levelName = levelName;
	}
	
	
	public void normalizeFile(){
		try{
			//Open the XLS file
			InputStream inputDataStream = new FileInputStream(datasetFile);
			HSSFWorkbook wb = new HSSFWorkbook(inputDataStream);
			HSSFSheet sheet = getSheet(wb);
			
			//Check the Excel file configuration options
			int initialRow = 0;		
			if ((skipRows != null) && (!skipRows.isEmpty())){
				initialRow = Integer.parseInt(skipRows);
				logger.debug("Skipping first "+skipRows+" rows");
	
			}
			int rowsLimit;
			if ((limitRows != null) && (!limitRows.isEmpty())){
				rowsLimit = initialRow+Integer.parseInt(limitRows)-1;
				//if the calculated limit exceed the physical number of rows, just read all the rows
				if (rowsLimit > sheet.getPhysicalNumberOfRows()){
					rowsLimit = sheet.getPhysicalNumberOfRows();
				}
			} else {
				rowsLimit = sheet.getPhysicalNumberOfRows();
			}
			
			//For each row of the file, look at the value on the column columnNameOnFile
			int columnPositionOnFile = -1;
			
			for (int r = initialRow; r <= rowsLimit; r++) {
				
				HSSFRow row = sheet.getRow(r);
				if (row == null) {
					continue;
				}
				
				if (r == initialRow){
					try {
						
						//for the header row find the columnNameOnFile position
						columnPositionOnFile = getColumnPosition(columnNameOnFile, row);
						if (columnPositionOnFile == -1){
							throw new RuntimeException("Impossible to find column "+columnNameOnFile+" on file");
						}

						//add a new column in the header row
						addColumnHeader(row);
						
					} catch (Throwable t) {
						throw new RuntimeException("Impossible to parse header row", t);
					}
				} else {
					try {
						//on a new column (with a new name not already present) write the corresponding value on the level defined on the metadata model
						addColumnValue(row,columnPositionOnFile);
					} catch (Throwable t) {
						throw new RuntimeException("Impossible to parse row [" + r + "]", t);
					}
				}
			}
			
			//************************************************************
			// Finalize changes on file 
			//************************************************************
			FileOutputStream outputDataStream = new FileOutputStream(datasetFile);
			wb.write(outputDataStream);
			outputDataStream.close();
			
			
			
			
		} catch (IOException e){
			logger.error("IOException in XLSFileNormalizer: "+e.getMessage());
			throw new RuntimeException("IOException in XLSFileNormalizer",e);
		}
		

	}
	
	/*
	 * Add Value to the last column just created
	 */
	private void addColumnValue(HSSFRow row,int columnPositionOnFile){
		int lastColumn =  this.getNumberOfColumns();
		
		//get value on the cell on the position columnPositionOnFile
		HSSFCell cell = row.getCell(columnPositionOnFile);
		
		String valueField = null;
		try {
			valueField = parseCell(cell);			
			
		} catch(Throwable t) {
			throw new RuntimeException("Impossible to parse cell [" + columnPositionOnFile + "]", t);
		}
		if ((valueField != null) && (!valueField.isEmpty())){
			//Search this value on the Map levelSiblingsValue and get the corresponding level value
			
			//try first searching valueField as a String
			Object levelValue = levelSiblingsValue.get(valueField);
			
			//else try searching valueField as an Integer
			if (levelValue == null){
				try{
					Integer valueFieldInteger = Integer.parseInt(valueField);
					levelValue = levelSiblingsValue.get(valueFieldInteger);					
				} catch (Exception ex){
					logger.debug("Cannot cast "+valueField+" to Integer");
				}

				
				//else try searching valueField as a Double
				if (levelValue == null){
					try{
						Double valueFieldDouble = Double.parseDouble(valueField);
						levelValue = levelSiblingsValue.get(valueFieldDouble);
					} catch (Exception ex){
						logger.debug("Cannot cast "+valueField+" to Double");
					}
					
					if (levelValue == null){
						logger.error("Value corresponding to "+valueField+" not found on level values");
					}
				}
			}
			
			if (levelValue != null){
				//create a new cell and set the value with levelValue
				HSSFCell newCell = row.createCell(lastColumn);
				if (levelValue instanceof String){
					String levelValueString = (String)levelValue;
					newCell.setCellValue(levelValueString);
					this.setNewColumnType("java.lang.String");				
				} else if (levelValue instanceof Double){
					Double levelValueDouble = (Double)levelValue;
					newCell.setCellValue(levelValueDouble);
					this.setNewColumnType("java.lang.Double");				
				} else if (levelValue instanceof Integer){
					Integer levelValueInteger = (Integer)levelValue;
					Double levelValueDouble = levelValueInteger.doubleValue();
					newCell.setCellValue(levelValueDouble);
					this.setNewColumnType("java.lang.Double");				
				}
				
			} else {
				//For values not found in the levelSiblingsValue Map, create an empty cell
				HSSFCell newCell = row.createCell(lastColumn);
				newCell.setCellValue("");
				this.setNewColumnType("java.lang.String");				
			}
		} else {
			//create an empty cell
			HSSFCell newCell = row.createCell(lastColumn);
			newCell.setCellValue("");
			this.setNewColumnType("java.lang.String");				
		}
	
	}
	
	
	/*
	 * To Use on the header row
	 */
	private void addColumnHeader(HSSFRow row){
		//Get all existing columns names
		List<String> columnsNames = new ArrayList<String>();
		int cells = row.getPhysicalNumberOfCells();
		this.setNumberOfColumns(cells);

		for (int c = 0; c < cells; c++) {
			//get single cell
			HSSFCell cell = row.getCell(c);
			
			String valueField = null;
			try {
				valueField = parseCell(cell);
				columnsNames.add(valueField);
				
				
			} catch(Throwable t) {
				throw new RuntimeException("Impossible to parse cell [" + c + "]", t);
			}
		}
		
		String newColumnName = "ref_"+levelName;
		
		//Check if exist already a column with the same name, otherwise generate a new name
		while( columnsNames.contains(newColumnName)){
			int count = 2;
			newColumnName = newColumnName+"_"+count;
			count++;
		}
		
		//Add a new column header
		HSSFCell newCell = row.createCell(cells);
		newCell.setCellValue(newColumnName);
		
		this.setNewColumnName(newColumnName);
	}
	
	/*
	 * To Use on the header row
	 */
	private int getColumnPosition(String columnName, HSSFRow row){
		int cells = row.getPhysicalNumberOfCells();
		logger.debug("\nROW " + row.getRowNum() + " has " + cells
				+ " cell(s).");
		for (int c = 0; c < cells; c++) {
			//get single cell
			HSSFCell cell = row.getCell(c);
			
			String valueField = null;
			try {
				valueField = parseCell(cell);
				
				if (valueField.equals(columnName)){
					return c;
				}
				
			} catch(Throwable t) {
				throw new RuntimeException("Impossible to parse cell [" + c + "]", t);
			}

		}
		return -1;
		
	}
	
 	private String parseCell(HSSFCell cell) {
 		String valueField = null;
 		
 		if(cell == null) return "";
 		
 		switch (cell.getCellType()) {
			case HSSFCell.CELL_TYPE_FORMULA:
				valueField = cell.getCellFormula().toString();
				break;
	
			case HSSFCell.CELL_TYPE_NUMERIC:
				Double numericValue = cell.getNumericCellValue();
				//testing if the double is an integer value
				if ((numericValue == Math.floor(numericValue)) && !Double.isInfinite(numericValue)) {
				    //the number is an integer, this will remove the .0 trailing zeros
					int numericInt = numericValue.intValue();
					valueField = String.valueOf(numericInt);
				} else {
					valueField = String.valueOf(cell.getNumericCellValue());

				}
				break;
	
			case HSSFCell.CELL_TYPE_STRING:
				valueField = cell.getStringCellValue();
				break;
	
			default:
		}
 		
 		return valueField;
 	}
	
 	private HSSFSheet getSheet(HSSFWorkbook workbook) {
 		HSSFSheet sheet;
 		
 		int numberOfSheets = workbook.getNumberOfSheets();
		if ((xslSheetNumber != null) && (!xslSheetNumber.isEmpty())){
			
			int sheetNumber = Integer.parseInt(xslSheetNumber)-1;
			if (sheetNumber > numberOfSheets){
				logger.error("Wrong sheet number, using first sheet as default");
				//if not specified take first sheet
				sheet = workbook.getSheetAt(0);
			}
			sheet = workbook.getSheetAt(sheetNumber);

		} else {
			//if not specified take first sheet
			sheet = workbook.getSheetAt(0);

		}
		
		return sheet;
 	}

	/**
	 * @return the skipRows
	 */
	public String getSkipRows() {
		return skipRows;
	}

	/**
	 * @param skipRows the skipRows to set
	 */
	public void setSkipRows(String skipRows) {
		this.skipRows = skipRows;
	}

	/**
	 * @return the limitRows
	 */
	public String getLimitRows() {
		return limitRows;
	}

	/**
	 * @param limitRows the limitRows to set
	 */
	public void setLimitRows(String limitRows) {
		this.limitRows = limitRows;
	}

	/**
	 * @return the xslSheetNumber
	 */
	public String getXslSheetNumber() {
		return xslSheetNumber;
	}

	/**
	 * @param xslSheetNumber the xslSheetNumber to set
	 */
	public void setXslSheetNumber(String xslSheetNumber) {
		this.xslSheetNumber = xslSheetNumber;
	}


	/**
	 * @return the numberOfColumns
	 */
	public int getNumberOfColumns() {
		return numberOfColumns;
	}


	/**
	 * @param numberOfColumns the numberOfColumns to set
	 */
	public void setNumberOfColumns(int numberOfColumns) {
		this.numberOfColumns = numberOfColumns;
	}


	/**
	 * @return the newColumnName
	 */
	public String getNewColumnName() {
		return newColumnName;
	}


	/**
	 * @param newColumnName the newColumnName to set
	 */
	public void setNewColumnName(String newColumnName) {
		this.newColumnName = newColumnName;
	}


	/**
	 * @return the newColumnType
	 */
	public String getNewColumnType() {
		return newColumnType;
	}


	/**
	 * @param newColumnType the newColumnType to set
	 */
	public void setNewColumnType(String newColumnType) {
		this.newColumnType = newColumnType;
	}
	
	

}
