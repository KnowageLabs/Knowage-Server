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
import it.eng.spagobi.tools.dataset.utils.DatasetMetadataParser;
import it.eng.spagobi.tools.datasource.bo.DataSourceFactory;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.json.JSONUtils;

/**
 * @authors Davide Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class FlatDataSet extends ConfigurableDataSet {

	private static final Logger LOGGER = Logger.getLogger(FlatDataSet.class);

	public static final String DS_TYPE = "SbiFlatDataSet";

	public static final String FLAT_TABLE_NAME = "flatTableName";
	public static final String DATA_SOURCE = "dataSourceFlat";
	public static final String OLD_DATA_SOURCE = "dataSource";

	private String tableName = null;
	private IDataSource dataSource = null;

	public FlatDataSet() {
	}

	public FlatDataSet(SpagoBiDataSet dataSetConfig) {
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
			this.setTableName((jsonConf.get(FLAT_TABLE_NAME) != null) ? jsonConf.get(FLAT_TABLE_NAME).toString() : "");
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
	public IMetaData getMetadata() {
		IMetaData currMetadata = null;
		try {
			DatasetMetadataParser dsp = new DatasetMetadataParser();
			currMetadata = dsp.xmlToMetadata(getDsMetadata());
		} catch (Exception e) {
			LOGGER.error("Error loading the metadata", e);
			throw new SpagoBIEngineRuntimeException("Error loading the metadata", e);
		}
		return currMetadata;
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
			LOGGER.error("Error while defining dataset configuration. Error:", e);
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
				throw new SpagoBIRuntimeException(e);
			}
		}

		super.setConfiguration(configuration);
	}

	@Override
	public DataIterator iterator() {
		LOGGER.debug("IN");
		try {
			IMetaData currMetadata = getMetadata();
			String query = String.format("select * from %s", this.getTableName());
			Connection connection = dataSource.getConnection();
			Statement stmt = connection.createStatement();

			connection.setAutoCommit(false); // PostgreSQL requires disabling auto-commit for setFetchSize to work
			stmt.setFetchSize(5000);

			ResultSet rs = stmt.executeQuery(query);
			return new ResultSetIterator(connection, stmt, rs, currMetadata);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException(e);
		} finally {
			LOGGER.debug("OUT");
		}
	}

	@Override
	public DataIterator iterator(IMetaData dsMetadata) {
		return null;
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
