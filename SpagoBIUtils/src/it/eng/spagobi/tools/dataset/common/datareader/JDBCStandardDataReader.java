/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.common.datareader;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.tools.dataset.common.datastore.DataStore;
import it.eng.spagobi.tools.dataset.common.datastore.Field;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.datastore.Record;
import it.eng.spagobi.tools.dataset.common.metadata.FieldMetadata;
import it.eng.spagobi.tools.dataset.common.metadata.MetaData;
import it.eng.spagobi.utilities.assertion.Assert;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class JDBCStandardDataReader extends AbstractDataReader {

	private static transient Logger logger = Logger.getLogger(JDBCStandardDataReader.class);
    
	
	public JDBCStandardDataReader() { }
	
	public boolean isOffsetSupported() {return true;}	
	public boolean isFetchSizeSupported() {return true;}	
	public boolean isMaxResultsSupported() {return true;}
    
    public IDataStore read(Object data) throws EMFUserError, EMFInternalError {
    	DataStore dataStore;
    	MetaData dataStoreMeta;
    	FieldMetadata fieldMeta;
    	String fieldName;
    	String fieldType;
    	ResultSet rs;
    	int columnCount;
    	int columnIndex;
    	
    	logger.debug("IN");
    	
    	dataStore = null;
    	
    	try {
    		
    		Assert.assertNotNull(data, "Input parameter [data] cannot be null");
    		Assert.assertTrue(data instanceof ResultSet, "Input parameter [data] cannot be of type [" + data.getClass().getName() + "]");
    		
    		rs = (ResultSet)data;
    		    		
    		dataStore = new DataStore();
        	dataStoreMeta = new MetaData();
        	
        	logger.debug("Reading metadata ...");
        	columnCount = rs.getMetaData().getColumnCount();
    		for(columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
        		fieldMeta = new FieldMetadata();
        		fieldName = rs.getMetaData().getColumnLabel(columnIndex);
        		fieldType = rs.getMetaData().getColumnClassName(columnIndex);
        		//logger.debug("Field [" + columnIndex + "] name is equal to [" + fieldName + "]. TYPE= "+fieldType);
        		fieldMeta.setName( fieldName );
        		if(fieldType!=null){
        			// Patch for hsql..  TODO
        			if ("double".equals(fieldType.trim())){
        				fieldMeta.setType(Class.forName("java.lang.Double"));
        			}else if ("int".equals(fieldType.trim())){
        				fieldMeta.setType(Class.forName("java.lang.Integer"));
        			}else if ("String".equals(fieldType.trim())){
        				fieldMeta.setType(Class.forName("java.lang.String"));
        			}else{
        				fieldMeta.setType(Class.forName(fieldType.trim()));
        			}
        		}
        		dataStoreMeta.addFiedMeta(fieldMeta);
        	}    
    		dataStore.setMetaData(dataStoreMeta);
    		logger.debug("Metadata readed succcesfully");
    		
    		
    		logger.debug("Reading data ...");
    		if(getOffset() > 0) {
    			logger.debug("Offset is equal to [" + getOffset() + "]");
    			
    			/*
    			 * The following invokation causes an error on Oracle: java.sql.SQLException: Nessuna riga corrente: relative
    			 * rs.relative(getOffset());
    			 */
    			
    			rs.first();
    			rs.relative(getOffset() - 1);
    			
    		} else {
    			logger.debug("Offset not set");
    		}
    		
    		long maxRecToParse = Long.MAX_VALUE;
    		if(getFetchSize() > 0) {
    			maxRecToParse = getFetchSize();
    			logger.debug("FetchSize is equal to [" + maxRecToParse + "]");
    		} else {
    			logger.debug("FetchSize not set");
    		}
    		
    		int recCount = 0;
    		int resultNumber = 0;
    		while ((recCount < maxRecToParse) && rs.next()) {
    			IRecord record = new Record(dataStore);
    			for(columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
    				Object columnValue = rs.getObject(columnIndex);
    				IField field = new Field( columnValue );
					if(columnValue != null) {
						dataStoreMeta.getFieldMeta(columnIndex-1).setType( columnValue.getClass() );
					}
					record.appendField( field );
    			}
    			dataStore.appendRecord(record);
    			recCount++;
    			//logger.debug("[" + recCount + "] - Records [" + rs.getRow()  + "] succesfully readed");
    		}
    		logger.debug("Readed [" + recCount+ "] records");
    		logger.debug("Data readed succcesfully");
    		
    		if (this.isCalculateResultNumberEnabled()) {
    			logger.debug("Calculation of result set number is enabled");
    			resultNumber = getResultNumber(rs, maxRecToParse, recCount);
    			dataStore.getMetaData().setProperty("resultNumber", new Integer(resultNumber));
    		} else {
    			logger.debug("Calculation of result set number is NOT enabled");
    		}

    	} catch (Throwable t) {
			logger.error("An unexpected error occured while resding resultset", t);
		} finally {
    		logger.debug("OUT");
    	}
    	
    	return dataStore;
    }

	private int getResultNumber(ResultSet rs, long maxRecToParse, int recCount)
			throws SQLException {
		logger.debug("IN");
		
		int toReturn;
		
		logger.debug("resultset type [" + rs.getType() + "] (" + (rs.getType()  == ResultSet.TYPE_FORWARD_ONLY) + ")");
		if (rs.getType()  == ResultSet.TYPE_FORWARD_ONLY) {
//    			while (!rs.isLast()) {
//    				rs.next();    // INFINITE LOOP ON MYSQL!!!!!
//    			}
			
//    			while (rs.next()) {
//    								// IT DOES NOT WORK SINCE, WHEN EXECUTING rs.next() ON LAST ROW,
//									// THEN rs.getRow() RETURNS 0, SINCE THE ROW IN NOT VALID
//    			}
			
			int recordsCount = 0;
			if (recCount < maxRecToParse) {
				// records read where less then max records to read, therefore the resultset has been completely read
				recordsCount = getOffset() + recCount;
			} else {
				recordsCount = rs.getRow();
				while (rs.next()) {
					recordsCount++;
					// do nothing, just scroll result set
				}
			}

			toReturn = recordsCount;
		} else {
			rs.last();
			toReturn = rs.getRow();
		}
		
		logger.debug("Reading total record numeber is equal to [" + toReturn + "]");
		logger.debug("OUT " + toReturn);
		return toReturn;
	}
	
}

