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
package it.eng.spagobi.tools.dataset.bo;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.log4j.Logger;
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
 * @author Marco Libanori
 */
public class PreparedDataSet extends ConfigurableDataSet {

	public static final String DS_TYPE = "SbiPreparedDataSet";

	public static final String TABLE_NAME = "tableName";
	public static final String DATA_SOURCE = "dataSource";
	public static final String DATA_PREPARATION_INSTANCE = "dataPreparationInstance";

	private static final Logger LOGGER = Logger.getLogger(PreparedDataSet.class);

	private String tableName = null;
	private IDataSource dataSource = null;
	private String dataPreparationInstance = null;
	private IMetaData metadata;

	public PreparedDataSet() {
		super();
	}

	public PreparedDataSet(SpagoBiDataSet dataSetConfig) {
		super(dataSetConfig);

		LOGGER.debug("IN");

		try {
			this.setDataSource(DataSourceFactory.getDataSource(dataSetConfig.getDataSource()));
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error setting datasource", e);
		}
		try {
			String config = JSONUtils.escapeJsonString(dataSetConfig.getConfiguration());
			JSONObject jsonConf = ObjectUtils.toJSONObject(config);

			this.setTableName(jsonConf.getString(TABLE_NAME));
			this.setDataPreparationInstance(jsonConf.getString(DATA_PREPARATION_INSTANCE));
		} catch (Exception e) {
			LOGGER.error("Error while reading dataset configuration. Error:", e);
			throw new SpagoBIRuntimeException("Error while reading dataset configuration", e);
		}

		LOGGER.debug("OUT");
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
			jsonConf.put(TABLE_NAME, (this.getTableName() == null) ? "" : this.getTableName());
			jsonConf.put(DATA_SOURCE, (this.getDataSource() == null) ? "" : this.getDataSource().getLabel());
			toReturn.setConfiguration(jsonConf.toString());
		} catch (Exception e) {
			LOGGER.error("Error while defining dataset configuration. Error:", e);
			throw new SpagoBIRuntimeException("Error while defining dataset configuration. Error:", e);
		}

		return toReturn;
	}

	@Override
	public DataIterator iterator() {
		LOGGER.debug("IN");
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

	/**
	 * @return the dataPreparationInstance
	 */
	public String getDataPreparationInstance() {
		return dataPreparationInstance;
	}

	/**
	 * @param dataPreparationInstance the dataPreparationInstance to set
	 */
	public void setDataPreparationInstance(String dataPreparationInstance) {
		this.dataPreparationInstance = dataPreparationInstance;
	}

}
