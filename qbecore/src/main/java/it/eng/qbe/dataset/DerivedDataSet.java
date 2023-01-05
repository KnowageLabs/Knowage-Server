/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.
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

/**
 *
 */
package it.eng.qbe.dataset;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import it.eng.qbe.datasource.DriverManager;
import it.eng.qbe.datasource.configuration.CompositeDataSourceConfiguration;
import it.eng.qbe.datasource.configuration.DataSetDataSourceConfiguration;
import it.eng.qbe.datasource.dataset.DataSetDataSource;
import it.eng.qbe.datasource.dataset.DataSetDriver;
import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.iterator.DataIterator;
import it.eng.spagobi.tools.dataset.common.iterator.ResultSetIterator;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Alberto Nale
 */
public class DerivedDataSet extends QbeDataSet {

	public static final String DS_TYPE = "SbiDerivedDataSet";

	public static final String TABLE_NAME = "tableName";
	public static final String DATA_SOURCE = "dataSource";

	private static final Logger LOGGER = Logger.getLogger(DerivedDataSet.class);

	private String tableName = null;
	private IDataSource dataSource = null;
	private IMetaData metadata;

	public DerivedDataSet() {
		super();
	}

	public DerivedDataSet(SpagoBiDataSet dataSetConfig) {
		super(dataSetConfig);

		LOGGER.debug("IN");

		LOGGER.debug("OUT");
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	@Override
	public IMetaData getMetadata() {
		return this.metadata;
	}

	@Override
	public IDataSource getDataSource() {
		return this.dataSource;
	}

	@Override
	public SpagoBiDataSet toSpagoBiDataSet() {
		SpagoBiDataSet sbd;

		sbd = super.toSpagoBiDataSet();

		sbd.setType(DS_TYPE);
		if (getDataSource() != null) {
			sbd.setDataSource(getDataSource().toSpagoBiDataSource());
		}

		return sbd;
	}

	@Override
	public it.eng.qbe.datasource.IDataSource getQbeDataSource() {
		Map<String, Object> dataSourceProperties = new HashMap<String, Object>();

		String modelName = getDatamarts();
		List<String> modelNames = new ArrayList<String>();
		modelNames.add(modelName);
		dataSourceProperties.put("datasource", super.dataSource);
		dataSourceProperties.put("dblinkMap", new HashMap());

		if (this.getSourceDataset() != null) {
			List<IDataSet> dataSets = new ArrayList<IDataSet>();
			dataSets.add(this.getSourceDataset());
			dataSourceProperties.put(EngineConstants.ENV_DATASETS, dataSets);
		}

		return getDataSourceFromDataSet(dataSourceProperties, useCache);

	}

	@Override
	public it.eng.qbe.datasource.IDataSource getDataSourceFromDataSet(Map<String, Object> dataSourceProperties, boolean useCache) {

		it.eng.qbe.datasource.IDataSource dataSource;
		List<IDataSet> dataSets = (List<IDataSet>) dataSourceProperties.get(EngineConstants.ENV_DATASETS);
		dataSourceProperties.remove(EngineConstants.ENV_DATASETS);

		CompositeDataSourceConfiguration compositeConfiguration = new CompositeDataSourceConfiguration(DataSetDataSource.EMPTY_MODEL_NAME);
		Iterator<String> it = dataSourceProperties.keySet().iterator();
		while (it.hasNext()) {
			String propertyName = it.next();
			compositeConfiguration.loadDataSourceProperties().put(propertyName, dataSourceProperties.get(propertyName));
		}
		if (dataSets != null) {
			for (int i = 0; i < dataSets.size(); i++) {
				DataSetDataSourceConfiguration c = new DataSetDataSourceConfiguration((dataSets.get(i)).getLabel(), dataSets.get(i));
				compositeConfiguration.addSubConfiguration(c);
			}
		}
		dataSource = DriverManager.getDataSource(DataSetDriver.DRIVER_ID, compositeConfiguration, useCache);

		return dataSource;
	}

//	@Override
//	public String getSignature() {
//		return this.getTableName();
//	}

	@Override
	public DataIterator iterator() {
		LOGGER.debug("IN");
		try {
			String table = (this.getTableName() == null ? this.getPersistTableName() : this.getTableName());
			String query = "select * from " + table;
			Connection connection = dataSource.getConnection();
			Statement stmt = connection.createStatement();
			stmt.setFetchSize(5000);
			ResultSet rs = stmt.executeQuery(query);
			DataIterator iterator = new ResultSetIterator(connection, stmt, rs);
			return iterator;
		} catch (Exception e) {
			throw new SpagoBIRuntimeException(e);
		} finally {
			LOGGER.debug("OUT");
		}
	}

	@Override
	public String getDsType() {
		return DS_TYPE;
	}

	@Override
	public IDataSource getDataSourceForReading() {
		return this.getDataSource();
	}

	@Override
	public void setDataSourceForReading(IDataSource datasourceForReading) {
		this.setDataSource(datasourceForReading);
	}

	@Override
	public boolean isIterable() {
		return true;
	}

}
