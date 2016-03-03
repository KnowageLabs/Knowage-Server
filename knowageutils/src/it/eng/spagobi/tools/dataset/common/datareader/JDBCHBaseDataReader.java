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

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Set;

import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.client.HRecord;
import org.apache.hadoop.hbase.jdbc.impl.ResultSetImpl;
import org.apache.log4j.Logger;

/**
 * @author Monica Franceschini (monica.franceschini@eng.it)
 * This class manages only JDBC data source
 * find the patch on Jira for JNDI
 */
public class JDBCHBaseDataReader extends AbstractDataReader {

	private static transient Logger logger = Logger.getLogger(JDBCHBaseDataReader.class);
    
	
	public JDBCHBaseDataReader() { }
	
	public boolean isOffsetSupported() {return true;}	
	public boolean isFetchSizeSupported() {return true;}	
	public boolean isMaxResultsSupported() {return true;}
    
    public IDataStore read(Object data) throws EMFUserError, EMFInternalError {
    	DataStore dataStore = null;
		MetaData dataStoreMeta;

		ResultSetImpl rsh = (ResultSetImpl) data;
		
		logger.debug("IN");

		
		dataStore = new DataStore();
		dataStoreMeta = new MetaData();
		dataStore.setMetaData(dataStoreMeta);

		
		try {				

			while ( rsh.next() ){
				IRecord record = new Record(dataStore);
				HRecord hRec= rsh.getCurrentHRecord();
				Set<String> columns = hRec.getColumnNameList();
				Iterator<String> it = columns.iterator();
				while(it.hasNext()){
					String name= it.next();

					Object value = hRec.getCurrentValue(name);
							
					FieldMetadata fieldMeta = new FieldMetadata();
					fieldMeta.setName(name);
					fieldMeta.setType(String.class);
					if(dataStoreMeta.getFieldIndex(name) == -1){
						dataStoreMeta.addFiedMeta(fieldMeta);
					}
					IField field = new Field(value);
					record.appendField(field);
					
				}
				dataStore.appendRecord(record);				

			}
				
		} catch (HBqlException e) {
			logger.error("An unexpected error occured while reading resultset", e);
		} catch (SQLException e) {
			logger.error("An unexpected error occured while reading resultset", e);
		} catch (RuntimeException e) {
			logger.error("Must use a JDBC data source - not JNDI", e);
		}finally {
    		logger.debug("OUT");
    	}
		
		return dataStore;
    }


	
}

