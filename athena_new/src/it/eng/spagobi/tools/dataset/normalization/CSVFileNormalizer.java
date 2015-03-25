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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;


/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class CSVFileNormalizer {
	
	File datasetFile;
	Map<Object, Object> levelSiblingsValue;
	String columnNameOnFile;
	private String levelName;
	
	private static transient Logger logger = Logger.getLogger(CSVFileNormalizer.class);
	public static final String CSV_FILE_DELIMITER_CHARACTER = "csvDelimiter";
	public static final String CSV_FILE_QUOTE_CHARACTER = "csvQuote";
	public static final String CSV_FILE_ENCODING = "csvEncoding";
	private String csvDelimiter;
	private String csvQuote;
	private String csvEncoding;
	
	private String newColumnName;
	private String newColumnType;
	
	
	public CSVFileNormalizer(File datasetFile, Map<Object, Object> levelSiblingsValue,  String columnNameOnFile, String levelName){
		this.datasetFile = datasetFile;
		this.levelSiblingsValue = levelSiblingsValue;
		this.columnNameOnFile = columnNameOnFile;
		this.levelName = levelName;
	}
	
	
	public void normalizeFile(){

		ICsvMapReader mapReader = null;
	    ICsvMapWriter mapWriter = null;
	    File outputFile = null;
		try{
			
			//Check the CSV file configuration options
			char quote;
			if ((csvQuote != null) && (!csvQuote.isEmpty())){
				quote = csvQuote.charAt(0);
			} else {
				quote = "\"".charAt(0);
			}
			
			char delimiter;
			if ((csvDelimiter != null) && (!csvDelimiter.isEmpty())){
				delimiter = csvDelimiter.charAt(0);
			} else {
				delimiter = ",".charAt(0);
			}
			
			if((csvEncoding != null) && (!csvEncoding.isEmpty())){
				//do nothing
			} else {
				csvEncoding = "windows-1252";
			}
			//Open the CSV file
			InputStream inputDataStream = new FileInputStream(datasetFile);
	 		InputStreamReader inputStreamReader = new InputStreamReader(inputDataStream,csvEncoding);
	 		
	 		//Create a copy of the file with a .tmp extension for the MapWriter
	 		String absolutePath = datasetFile.getAbsolutePath();
	 		String filePath = absolutePath.substring(0,absolutePath.lastIndexOf(File.separator));
	 		String tempFileName = datasetFile.getName()+".tmp";
	 		outputFile = new File(filePath,tempFileName);
	 		outputFile.createNewFile();
			
			//Create the mapReader and mapWriter
       	 	CsvPreference customPreference = new CsvPreference.Builder(quote, delimiter, "\n").build(); 
       	 	mapReader = new CsvMapReader(inputStreamReader, customPreference);
       	 	OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(outputFile),csvEncoding);
       	 	//mapWriter = new CsvMapWriter(new FileWriter(outputFile), customPreference); 
       	 	mapWriter = new CsvMapWriter(outputStreamWriter, customPreference);
       	 	
       	 	// header used to read the original file
            final String[] readHeader = mapReader.getHeader(true);

            // header used to write the new file 
            final String[] writeHeader = new String[readHeader.length + 1];
            System.arraycopy(readHeader, 0, writeHeader, 0, readHeader.length);
            String newColumnName = generateNewColumnName(readHeader);
            writeHeader[writeHeader.length - 1] = newColumnName;
            
            //write the new header with the added column
            mapWriter.writeHeader(writeHeader);
            
            int columnsNumber = mapReader.length();

            final CellProcessor[] processors = new CellProcessor[columnsNumber];
            for (int i= 0; i<processors.length;i++){
            	processors[i] = null;
            }
            
            //Read the contents of the original file for the column columnNameOnFile
            //and get the corresponding level value then add that on the new column
            Map<String, Object> row;

            while( (row = mapReader.read(readHeader, processors)) != null ) {

            	addColumnValue(row,columnNameOnFile,mapWriter,writeHeader);
            	
            }

		} catch (IOException e){
			logger.error("IOException in CSVFileNormalizer: "+e.getMessage());
			throw new RuntimeException("IOException in CSVFileNormalizer",e);
		}
		finally {
			try {
				if( mapReader != null ) {
					mapReader.close();
				}
				if( mapWriter != null ) {
					mapWriter.close();
				}
					            
	            //Remove original file and rename the temp outputfile
	            datasetFile.delete();
	            outputFile.renameTo(datasetFile);
				
			} catch (IOException e){
				logger.error("IOException in CSVFileNormalizer: "+e.getMessage());
				throw new RuntimeException("IOException in CSVFileNormalizer",e);
			}
		}		

	}
	
	public void addColumnValue(Map<String, Object> row, String columnNameOnFile,ICsvMapWriter mapWriter,String[] writeHeader ){
		//get value on this row for the column named columnNameOnFile
		logger.debug(columnNameOnFile+" = "+row.get(columnNameOnFile)); 
    	Object cell = row.get(columnNameOnFile);
    	String valueField = parseValue(cell);
    	
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
				//create a new cell on the new Column and set the value with levelValue

				if (levelValue instanceof String){
					String levelValueString = (String)levelValue;
					row.put(getNewColumnName(), levelValueString);
					this.setNewColumnType("java.lang.String");				
				} else if (levelValue instanceof Double){
					Double levelValueDouble = (Double)levelValue;
					row.put(getNewColumnName(), levelValueDouble);
					this.setNewColumnType("java.lang.Double");				
				} else if (levelValue instanceof Integer){
					Integer levelValueInteger = (Integer)levelValue;
					Double levelValueDouble = levelValueInteger.doubleValue();
					row.put(getNewColumnName(), levelValueDouble);
					this.setNewColumnType("java.lang.Double");				
				}
				
			} else {
				//create an empty cell
				row.put(getNewColumnName(), "");
			}
		} else {
			//create an empty cell
			row.put(getNewColumnName(), "");
		}
    	 try {
    		//Write new row on the output file 
			mapWriter.write(row, writeHeader);
		} catch (IOException e) {
			logger.error("IOException in CSVFileNormalizer: "+e.getMessage());
			throw new RuntimeException("IOException in CSVFileNormalizer",e);
		}
    	
    	
	}
	
	/*
	 * Convert Object to String
	 */
	public String parseValue(Object cell){
		String valueField = null;
		if(cell == null) {
			return "";
		} else {
			if (cell instanceof String){
				valueField = (String)cell;
				return valueField;
			} else if (cell instanceof Integer){
				Integer intValue = (Integer)cell;
				valueField = String.valueOf(intValue);
				return valueField;
			} else if (cell instanceof Double){
				Double doubleValue = (Double)cell;
				valueField = String.valueOf(doubleValue);
				return valueField;
			}
		}
		return valueField;
	}
	
	public int getColumnIndex(String[] header, String columnName){
    	for (int i= 0; i<header.length;i++){
    		if (header[i].equals(columnName)){
    			return i;
    		}
    	}
    	return -1;
	}
	
	public String generateNewColumnName(String[] readHeader){
		
		List<String> columnsNames = Arrays.asList(readHeader);  
		String newColumnName = "ref_"+levelName;
		
		//Check if exist already a column with the same name, otherwise generate a new name
		while( columnsNames.contains(newColumnName)){
			int count = 2;
			newColumnName = newColumnName+"_"+count;
			count++;
		}
		this.setNewColumnName(newColumnName);
		return newColumnName;
		
	}


	/**
	 * @return the csvDelimiter
	 */
	public String getCsvDelimiter() {
		return csvDelimiter;
	}


	/**
	 * @param csvDelimiter the csvDelimiter to set
	 */
	public void setCsvDelimiter(String csvDelimiter) {
		this.csvDelimiter = csvDelimiter;
	}


	/**
	 * @return the csvQuote
	 */
	public String getCsvQuote() {
		return csvQuote;
	}


	/**
	 * @param csvQuote the csvQuote to set
	 */
	public void setCsvQuote(String csvQuote) {
		this.csvQuote = csvQuote;
	}


	/**
	 * @return the csvEncoding
	 */
	public String getCsvEncoding() {
		return csvEncoding;
	}


	/**
	 * @param csvEncoding the csvEncoding to set
	 */
	public void setCsvEncoding(String csvEncoding) {
		this.csvEncoding = csvEncoding;
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
