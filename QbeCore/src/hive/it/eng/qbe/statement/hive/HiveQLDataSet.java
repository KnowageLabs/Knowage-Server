/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

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