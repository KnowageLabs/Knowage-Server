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

package it.eng.spagobi.tools.dataset.bo;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spagobi.container.ObjectUtils;
import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;
import it.eng.spagobi.tools.dataset.common.iterator.DataIterator;
import it.eng.spagobi.tools.dataset.common.iterator.ResultSetIterator;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.datasource.bo.DataSourceFactory;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.json.JSONUtils;

/**
 * @authors Davide Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class FlatDataSet extends ConfigurableDataSet {

	public static String DS_TYPE = "SbiFlatDataSet";

	public static final String FLAT_TABLE_NAME = "flatTableName";
	public static final String DATA_SOURCE = "dataSourceFlat";
	public static final String OLD_DATA_SOURCE = "dataSource";

	private static transient Logger logger = Logger.getLogger(FlatDataSet.class);

	private String tableName = null;
	private IDataSource dataSource = null;
	private IMetaData metadata;

	public FlatDataSet() {
		super();
	}

	public FlatDataSet(SpagoBiDataSet dataSetConfig) {
		super(dataSetConfig);

		logger.debug("IN");

		try {
			this.setDataSource(DataSourceFactory.getDataSource(dataSetConfig.getDataSource()));
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error setting datasource", e);
		}
		try {
			String config = JSONUtils.escapeJsonString(dataSetConfig.getConfiguration());
			JSONObject jsonConf = ObjectUtils.toJSONObject(config);
			this.setTableName((jsonConf.get(FLAT_TABLE_NAME) != null) ? jsonConf.get(FLAT_TABLE_NAME).toString() : "");
		} catch (Exception e) {
			logger.error("Error while reading dataset configuration. Error:", e);
			throw new SpagoBIRuntimeException("Error while reading dataset configuration", e);
		}

		logger.debug("OUT");
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	@Override
	public void setDataSource(IDataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public void setMetadata(IMetaData metadata) {
		this.metadata = metadata;
	}

	@Override
	public IMetaData getMetadata() {
		return this.metadata;
	}

	@Override
	public IDataSource getDataSource() {
		return dataSource;
	}

	@Override
	public SpagoBiDataSet toSpagoBiDataSet() {
		SpagoBiDataSet toReturn;

		toReturn = super.toSpagoBiDataSet();

		toReturn.setType(DS_TYPE);

		toReturn.setDataSource(this.getDataSource().toSpagoBiDataSource());

		try {
			JSONObject jsonConf = new JSONObject();
			jsonConf.put(FLAT_TABLE_NAME, (this.getTableName() == null) ? "" : this.getTableName());
			jsonConf.put(DATA_SOURCE, (this.getDataSource() == null) ? "" : this.getDataSource().getLabel());
			toReturn.setConfiguration(jsonConf.toString());
		} catch (Exception e) {
			logger.error("Error while defining dataset configuration. Error:", e);
			throw new SpagoBIRuntimeException("Error while defining dataset configuration. Error:", e);
		}

		return toReturn;
	}

	@Override
	public void setConfiguration(String configuration) {
		/*
		 * WORKAROUND : in the past the datasource attribute was dataSource and not dataSourceFlat.
		 */
		String config = JSONUtils.escapeJsonString(configuration);
		JSONObject jsonConf = ObjectUtils.toJSONObject(config);
		if (jsonConf.has(OLD_DATA_SOURCE)) {
			try {
				String string = jsonConf.getString(OLD_DATA_SOURCE);
				jsonConf.put(DATA_SOURCE, string);
				jsonConf.remove(OLD_DATA_SOURCE);
				configuration = jsonConf.toString();
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}
		}

		super.setConfiguration(configuration);
	}

	@Override
	public DataIterator iterator() {
		logger.debug("IN");
		try {
			String query = "select * from " + this.getTableName();
			Connection connection = dataSource.getConnection();
			Statement stmt = connection.createStatement();
			stmt.setFetchSize(5000);
			ResultSet rs = stmt.executeQuery(query);
			DataIterator iterator = new ResultSetIterator(connection, stmt, rs);
			return iterator;
		} catch (Exception e) {
			throw new SpagoBIRuntimeException(e);
		} finally {
			logger.debug("OUT");
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

}
