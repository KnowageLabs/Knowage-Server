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

package it.eng.qbe.statement.hive;

import it.eng.qbe.datasource.dataset.DataSetDataSource;
import it.eng.qbe.statement.AbstractQbeDataSet;
import it.eng.spagobi.tools.dataset.bo.AbstractJDBCDataset;
import it.eng.spagobi.tools.dataset.bo.JDBCDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCHiveDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.DataStore;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.persist.DataSetTableDescriptor;
import it.eng.spagobi.tools.dataset.persist.IDataSetTableDescriptor;
import it.eng.spagobi.tools.dataset.persist.PersistedTableManager;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import org.apache.log4j.Logger;

/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 */

public class HiveQLDataSet extends AbstractQbeDataSet {


	
	/** Logger component. */
    public static transient Logger logger = Logger.getLogger(HiveQLDataSet.class);
    
	
	public HiveQLDataSet(HiveQLStatement statement) {
		super(statement);
	}
	
	
	public void loadData(int offset, int fetchSize, int maxResults) {
		AbstractJDBCDataset dataset;
		if (persisted) {
			dataset = new JDBCDataSet();
			dataset.setDataSource(getDataSourceForReading());
			dataset.setQuery("select * from " + getTableNameForReading());
			dataset.loadData(offset, fetchSize, maxResults);
		} else {
			DataSetDataSource ds = (DataSetDataSource) statement
					.getDataSource();
			String statementStr = statement.getQueryString();
			dataset = new JDBCHiveDataSet();
			dataset.setDataSource(ds.getDataSourceForReading());
			dataset.setQuery(statementStr);
			dataset.loadData(offset, fetchSize, maxResults);
		}

		dataStore = dataset.getDataStore();

		
		IMetaData jdbcMetadata = dataStore.getMetaData();
		IMetaData qbeQueryMetaData = getDataStoreMeta(this.getStatement().getQuery());
		IMetaData merged = mergeMetadata(jdbcMetadata, qbeQueryMetaData);
		((DataStore)dataStore).setMetaData(merged);
		
				
		if(hasDataStoreTransformer()) {
			getDataStoreTransformer().transform(dataStore);
		}

	
	}
	
	public void setDataSource(IDataSource dataSource) {
		// TODO Auto-generated method stub
		
	}
	
	public IDataSetTableDescriptor persist(String tableName, IDataSource dataSource) {
		IDataSetTableDescriptor td = null;
		// TODO check this
		PersistedTableManager ptm = new PersistedTableManager(null);
		try {
			ptm.persistDataSet(this, dataSource, tableName);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while persisting Hive dataset", e);
		}
		td = new DataSetTableDescriptor(this);
		td.setTableName(tableName);
		return td;
	}

}