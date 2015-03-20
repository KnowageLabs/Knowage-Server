/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.worksheet.exporter;

import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;

import java.awt.Color;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;


import com.lowagie.text.BadElementException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class DataSourceTablePDFExporter {
	
	private static final Color headerbackgroundColor = new Color(228,236,242);
	private static final Color evenrowsBackgroundColor = new Color(238,238,238);
	private static final Color oddrowsBackgroundColor = new Color(255,255,255);
	private static final Color cellsBorderColor = new Color(208,208,208);
	
	private IDataStore dataStore;
	private DecimalFormat numberFormat;
	private String userDateFormat;

	public static transient Logger logger = Logger.getLogger(DataSourceTablePDFExporter.class);
	
	public DataSourceTablePDFExporter(IDataStore dataStore, DecimalFormat numberFormat, String userDateFormat){
		this.dataStore=dataStore;
		this.numberFormat=numberFormat;
	}
	
	/**
	 * Create the object table and put it in the 
	 * pdf document
	 * @param pdfDocument the destination document
	 * @throws BadElementException
	 * @throws DocumentException
	 */
	public void export(Document pdfDocument) throws BadElementException, DocumentException{
		PdfPTable table = buildTableHeader(dataStore);
		buildTableContent(dataStore,table); 	
	    pdfDocument.add(table);
	} 
	
	/**
	 * Builds the header of the table..
	 * It creates also the object table..
	 * @param dataStore 
	 * @return the table object
	 * @throws BadElementException
	 */
	public PdfPTable buildTableHeader(IDataStore dataStore) throws BadElementException{	
		logger.debug("IN: building the headers of the table");
		IMetaData dataStoreMetaData = dataStore.getMetaData();	
    	int colunum = dataStoreMetaData.getFieldCount();
	    List<String> columnsName = new ArrayList<String>();
    	
	    //reads the names of the visible table columns
    	for(int j = 0; j < colunum; j++){
    	    String fieldName = dataStoreMetaData.getFieldAlias(j);
    	    IFieldMetaData fieldMetaData = dataStoreMetaData.getFieldMeta(j);
//    	    String format = (String) fieldMetaData.getProperty("format");
    	    String alias = (String) fieldMetaData.getAlias();

           	if(alias!=null && !alias.equals("")){
           		columnsName.add(alias);
           	}else{
           		columnsName.add(fieldName);
           	}	 
        }
    	
    	PdfPTable table = new PdfPTable(colunum);
    	
    	//For each column builds a cell
		PdfPCell d = table.getDefaultCell();

    	

		if(colunum<4){
			table.setWidthPercentage(colunum*25);
		}else{
			table.setWidthPercentage(100);
		}
		
    	for(int j = 0; j < colunum; j++){
    		PdfPCell cell = new PdfPCell(new Phrase(columnsName.get(j)));
    		//cell.setHeader(true);
    		cell.setBorderColor(cellsBorderColor);
    		cell.setBackgroundColor(headerbackgroundColor);
    		table.addCell(cell);
    	}
    	
    	table.setHeaderRows(1);
    	
    	logger.debug("Out: built the headers of the table");
    	return table;
	}
	
	/**
	 * Build the content of the table
	 * @param dataStore
	 * @param table the table with the headers
	 * @throws BadElementException
	 */
	public void buildTableContent(IDataStore dataStore, PdfPTable table) throws BadElementException{	
		logger.debug("IN: building the conetent of the table");
		boolean oddRows  = true;
		PdfPCell cell;
		
		Iterator it = dataStore.iterator();
    	
    	IMetaData d = dataStore.getMetaData();	
		
		while(it.hasNext()){//for each record
			IRecord record =(IRecord)it.next();
			List fields = record.getFields();
			int length = fields.size();
			//build the row
			for(int fieldIndex =0; fieldIndex<length; fieldIndex++){
				IField f = (IField)fields.get(fieldIndex);
				IFieldMetaData fieldMetaData = d.getFieldMeta(fieldIndex);
				String decimalPrecision = (String)fieldMetaData.getProperty(IFieldMetaData.DECIMALPRECISION);
				if (f == null || f.getValue()== null) {
		    		cell = new PdfPCell(new Phrase(""));
		    	}else{
					Class c = d.getFieldType(fieldIndex);
					cell = new PdfPCell(new Phrase(formatPDFCell(c, f, decimalPrecision)));
					if(oddRows){
						cell.setBackgroundColor(oddrowsBackgroundColor);
					}else{
						cell.setBackgroundColor(evenrowsBackgroundColor);
					}
		    	}
		    	cell.setBorderColor(cellsBorderColor);
		    	table.addCell(cell);
				
			}
			oddRows = !oddRows;
		}
		logger.debug("Out: built the conetent of the table");
	}
	
	/**
	 * Formats the cell
	 * @param c
	 * @param f
	 * @return
	 */
	private String formatPDFCell(Class c, IField f, String numberFormat){
		DecimalFormat aNumberFormat = new DecimalFormat("##,##0.00");
		if(numberFormat!=null){
			try {
				aNumberFormat.setMaximumFractionDigits(new Integer(numberFormat)) ;
				aNumberFormat.setMinimumFractionDigits(new Integer(numberFormat)) ;
			} catch (Exception e) {
				// its ok empty... if an error occours use the default
				logger.debug("The decimal precison is not valid:  "+numberFormat);
			}
		}
		String cellValue ="";
		if( Integer.class.isAssignableFrom(c) || Short.class.isAssignableFrom(c)) {
			Number val = (Number)f.getValue();
			cellValue = aNumberFormat.format(val);
		}else if( Number.class.isAssignableFrom(c) ) {
		    Number val = (Number)f.getValue();
		    cellValue = aNumberFormat.format(val);
		}else if( String.class.isAssignableFrom(c)){
			cellValue = (String)f.getValue();
		}else if( Boolean.class.isAssignableFrom(c) ) {
		    Boolean val = (Boolean)f.getValue();
		    cellValue = val.toString();
		}else if(Date.class.isAssignableFrom(c)){	    
		    Date val = (Date)f.getValue();
		    if(userDateFormat==null){
		    	userDateFormat = "MM/dd/yyyy";
		    }
			DateFormat userDataFormat = new SimpleDateFormat(userDateFormat);		
			cellValue = userDataFormat.format(val);		
		}else{
			cellValue = f.getValue().toString();
		}
		return cellValue;
	}
	

	
}
