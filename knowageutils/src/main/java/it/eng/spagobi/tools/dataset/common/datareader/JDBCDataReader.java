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
package it.eng.spagobi.tools.dataset.common.datareader;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import it.eng.spago.base.SourceBean;
import it.eng.spago.dbaccess.sql.result.ScrollableDataResult;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.tools.dataset.common.datastore.DataStore;
import it.eng.spagobi.tools.dataset.common.datastore.Field;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.datastore.Record;
import it.eng.spagobi.tools.dataset.common.metadata.FieldMetadata;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.MetaData;

/**
 * @author Angelo Bernabei
 *         angelo.bernabei@eng.it
 */
public class JDBCDataReader extends AbstractDataReader {

	private static transient Logger logger = Logger.getLogger(JDBCDataReader.class);
    
	public JDBCDataReader() {
		
	}

    
    public IDataStore read(Object data) throws EMFUserError, EMFInternalError {
    	
    	logger.debug("IN");
    	DataStore dataStore;
    	MetaData dataStoreMeta;
    	FieldMetadata fieldMeta;
    	
    	ScrollableDataResult scrollableDataResult;
    	List columnsNames;
    	SourceBean resultSB;
    	
    	
    	try {
    	
	    	scrollableDataResult = (ScrollableDataResult)data;
	    	
	    	dataStore = new DataStore();
	    	dataStoreMeta = new MetaData();
	    	
	    	logger.debug("Reading dataStore metadata ...");
	    	columnsNames = Arrays.asList(scrollableDataResult.getColumnNames());
	    	for(int i = 0; i < columnsNames.size(); i++) {
	    		fieldMeta = new FieldMetadata();
	    		fieldMeta.setName( (String)columnsNames.get(i) );
	    		dataStoreMeta.addFiedMeta(fieldMeta);
	    		logger.debug("Field [" + (i+1) + "] name is equak to [" + fieldMeta.getName() + "]");
	    	}    	
			dataStore.setMetaData(dataStoreMeta);
			logger.debug("dataStore metadata read succefully");
			
			try {
				resultSB = scrollableDataResult.getSourceBean();
			} catch(Throwable t ) {
				throw new RuntimeException("Impossible to extract xml data", t);
			}	
			if( resultSB != null) {
				List rows;
				Iterator rowIterator;
				
				rows = null;
				rowIterator = null;
				try {
					rows = resultSB.getAttributeAsList("ROW");
					rowIterator = rows.iterator(); 
				} catch(Throwable t ) {
					throw new RuntimeException("Impossible to extract rows content from sourcebean [" + resultSB + "]", t);
				}	
					
				while(rowIterator.hasNext()) {		
					SourceBean rowSB = (SourceBean) rowIterator.next();
					IRecord record = new Record(dataStore);
						
					for(int i = 0; i < dataStoreMeta.getFieldCount(); i++) {
						IFieldMetaData fieldMetaData = dataStoreMeta.getFieldMeta(i);
						try {
							Object value = rowSB.getAttribute( dataStoreMeta.getFieldAlias(i) );
							logger.debug("Column [" + fieldMetaData.getName() + "] of type [" + (value!=null? value.getClass(): "undef") + "] is equal to [" + value + "]");					
							IField field = new Field( value );
							if(value != null) {
								dataStoreMeta.getFieldMeta(i).setType( value.getClass() );
							}
							record.appendField( field );
						} catch(Throwable t ) {
							throw new RuntimeException("Impossible to read column [" + fieldMetaData.getName()+ "] value", t);
						}
					}
					dataStore.appendRecord(record);
				}
			}				
		
    	} catch(Throwable t) {
			throw new RuntimeException("An umpredeicted error occurred while reading data", t);
		} finally {
			logger.debug("OUT");
		}
		return dataStore;
    }
}
