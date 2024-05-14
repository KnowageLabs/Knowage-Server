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

package it.eng.qbe.statement.sql;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import it.eng.qbe.datasource.dataset.DataSetDataSource;
import it.eng.qbe.statement.AbstractQbeDataSet;
import it.eng.qbe.statement.AbstractStatement;
import it.eng.spagobi.tools.dataset.bo.AbstractJDBCDataset;
import it.eng.spagobi.tools.dataset.bo.FileDataSet;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCDatasetFactory;
import it.eng.spagobi.tools.dataset.bo.JDBCPostgreSQLDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.DataStore;
import it.eng.spagobi.tools.dataset.common.iterator.DataIterator;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 */

public class SQLDataSet extends AbstractQbeDataSet {

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(SQLDataSet.class);

	public SQLDataSet(SQLStatement statement) {
		super(statement);
	}

	@Override
	public void loadData(int offset, int fetchSize, int maxResults) {

		AbstractJDBCDataset dataset = null;
		if (persisted) {
			dataset = new JDBCDataSet();
			dataset.setDataSource(getDataSourceForReading());
			dataset.setQuery("select * from " + getTableNameForReading());
			dataset.loadData(offset, fetchSize, maxResults);
		} else {
			DataSetDataSource ds = (DataSetDataSource) statement.getDataSource();
			String statementStr = "";
			// SpagoBiDataSet dataSetConfig = new SpagoBiDataSet();
			// dataSetConfig.setDataSource( ds.getSpagoBiDataSource() );
			// dataSetConfig.setQuery(statementStr);
			if (this.getWrappedDataset() instanceof AbstractJDBCDataset && StringUtils.isEmpty(this.getWrappedDataset().getPersistTableName())) {
				AbstractJDBCDataset datasetJDBC = null;
				if (this.getWrappedDataset() instanceof JDBCDataSet) {
					dataset = new JDBCDataSet();
					datasetJDBC = (JDBCDataSet) this.getWrappedDataset();
				} else if (this.getWrappedDataset() instanceof JDBCPostgreSQLDataSet) {
					dataset = new JDBCPostgreSQLDataSet();
					datasetJDBC = (JDBCPostgreSQLDataSet) this.getWrappedDataset();
				} else {
					logger.error("Dataset type is not handled [" + this.getWrappedDataset().getClass().getName() + "]");
					String message = "Dataset type is not handled";
					throw new SpagoBIRuntimeException(message);
				}

				String queryJDBC = datasetJDBC.getQuery().toString();

				statementStr = statement.getQuerySQLString(queryJDBC);

				dataset.setDataSource(this.getWrappedDataset().getDataSource());
			} else if (this.getWrappedDataset() instanceof VersionedDataSet) {
				VersionedDataSet vds = (VersionedDataSet) this.getWrappedDataset();
				AbstractJDBCDataset datasetJDBC = null;
				if (vds.getWrappedDataset() instanceof JDBCDataSet) {
					dataset = new JDBCDataSet();
					datasetJDBC = (JDBCDataSet) vds.getWrappedDataset();
					String queryJDBC = datasetJDBC.getQuery().toString();

					statementStr = statement.getQuerySQLString(queryJDBC);
					dataset.setDataSource(this.getWrappedDataset().getDataSource());
				} else if (vds.getWrappedDataset() instanceof JDBCPostgreSQLDataSet) {
					dataset = new JDBCPostgreSQLDataSet();
					datasetJDBC = (JDBCPostgreSQLDataSet) vds.getWrappedDataset();
					String queryJDBC = datasetJDBC.getQuery().toString();

					statementStr = statement.getQuerySQLString(queryJDBC);
					dataset.setDataSource(this.getWrappedDataset().getDataSource());
				} else if (vds.getWrappedDataset() instanceof FileDataSet && StringUtils.isNotEmpty(vds.getWrappedDataset().getPersistTableName())) {
					dataset = new JDBCDataSet();
					dataset.setPersistTableName(vds.getWrappedDataset().getPersistTableName());
					dataset.setDataSource(vds.getWrappedDataset().getDataSourceForWriting());
					statementStr = statement.getQueryString();
				} else {
					dataset = new JDBCDataSet();
					dataset.setDataSource(ds.getDataSourceForReading());
					statementStr = statement.getQueryString();
				}

			} else {
				dataset = new JDBCDataSet();
				dataset.setDataSource(ds.getDataSourceForReading());
				statementStr = statement.getQueryString();
			}

			dataset.setQuery(statementStr);
			dataset.loadData(offset, fetchSize, maxResults);
		}

		dataStore = dataset.getDataStore();

		IMetaData jdbcMetadata = dataStore.getMetaData();
		IMetaData qbeQueryMetaData = ((AbstractStatement) this.getStatement()).getDataStoreMeta();
		IMetaData merged = mergeMetadata(jdbcMetadata, qbeQueryMetaData);
		((DataStore) dataStore).setMetaData(merged);

		if (hasDataStoreTransformers()) {
			executeDataStoreTransformers(dataStore);
		}

	}

	@Override
	public void setDataSource(IDataSource dataSource) {

	}

	@Override
	public IDataSource getDataSourceForReading() {
		SQLStatement statement = (SQLStatement) this.getStatement();
		DataSetDataSource ds = (DataSetDataSource) statement.getDataSource();
		if (ds.getDataSourceForReading() == null && this.datasourceForReading != null)
			return this.datasourceForReading;
		return ds.getDataSourceForReading();
	}

	@Override
	public Integer getCategoryId() {
		IDataSet wrapped = this.getWrappedDataset();
		return wrapped.getCategoryId();
	}

	@Override
	public String getCategoryCd() {
		IDataSet wrapped = this.getWrappedDataset();
		return wrapped.getCategoryCd();
	}

	private IDataSet getWrappedDataset() {
		SQLStatement statement = (SQLStatement) this.getStatement();
		DataSetDataSource ds = (DataSetDataSource) statement.getDataSource();
		IDataSet toReturn = ds.getRootEntities().get(0);
		return toReturn;
	}

	@Override
	public boolean isIterable() {
		return true;
	}

	@Override
	public DataIterator iterator() {
		logger.debug("IN");
		try {
			if (this.isPersisted()) {
				JDBCDataSet jdbcDataset = (JDBCDataSet) JDBCDatasetFactory.getJDBCDataSet(this.getDataSourceForReading());
				jdbcDataset.setQuery("select * from " + this.getPersistTableName());
				return jdbcDataset.iterator();

			} else {

				IDataSource daS = this.getDataSource();
				if (daS == null && this.datasourceForReading != null)
					daS = this.datasourceForReading;
				JDBCDataSet jdbcDataset = (JDBCDataSet) JDBCDatasetFactory.getJDBCDataSet(daS);
				jdbcDataset.setDataSource(daS);
				if (this.getWrappedDataset() instanceof VersionedDataSet) {
					VersionedDataSet vds = (VersionedDataSet) this.getWrappedDataset();
					if ((vds.getWrappedDataset() instanceof JDBCDataSet)) {
						JDBCDataSet jDataset = (JDBCDataSet) vds.getWrappedDataset();
						statement.getQuerySQLString(jDataset.getQuery().toString());
					}
				} else if (this.getWrappedDataset() instanceof JDBCDataSet) {
					JDBCDataSet jDataset = (JDBCDataSet) this.getWrappedDataset();
					statement.getQuerySQLString(jDataset.getQuery().toString());
				}
				jdbcDataset.setQuery(statement.getQueryString());
				return jdbcDataset.iterator();
			}

		} finally {
			logger.debug("OUT");
		}
	}

}