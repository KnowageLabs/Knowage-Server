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
package it.eng.spagobi.tools.datasource.bo;

import org.apache.log4j.Logger;

import it.eng.spagobi.services.datasource.bo.SpagoBiDataSource;
import it.eng.spagobi.tools.datasource.bo.deserializer.JDBCDataSourcePoolConfigurationJSONDeserializer;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public abstract class DataSourceFactory {

	private static transient Logger logger = Logger.getLogger(DataSourceFactory.class);

	public static IDataSource getDataSource(SpagoBiDataSource dataSourceConfig) {
		IDataSource dataSource = null;

		if (dataSourceConfig == null) {
			throw new IllegalArgumentException("datasource-config parameter cannot be null");
		}

		dataSource = DataSourceFactory.getDataSource();

		dataSource.setDsId(dataSourceConfig.getId());
		dataSource.setDriver(dataSourceConfig.getDriver());
		dataSource.setJndi(dataSourceConfig.getJndiName());
		dataSource.setLabel(dataSourceConfig.getLabel());
		dataSource.setPwd(dataSourceConfig.getPassword());
		dataSource.setUrlConnection(dataSourceConfig.getUrl());
		dataSource.setUser(dataSourceConfig.getUser());
		dataSource.setMultiSchema(dataSourceConfig.getMultiSchema());
		dataSource.setSchemaAttribute(dataSourceConfig.getSchemaAttribute());
		dataSource.setReadOnly(dataSourceConfig.getReadOnly());
		dataSource.setWriteDefault(dataSourceConfig.getWriteDefault());
		dataSource.setHibDialectClass(dataSourceConfig.getHibDialectClass());
		dataSource.setUseForDataprep(dataSourceConfig.getUseForDataprep());
		if (dataSourceConfig.getJdbcPoolConfiguration() != null) {
			JDBCDataSourcePoolConfiguration jdbcDataSourcePoolConfiguration = (JDBCDataSourcePoolConfiguration) new JDBCDataSourcePoolConfigurationJSONDeserializer()
					.deserialize(dataSourceConfig.getJdbcPoolConfiguration());
			dataSource.setJdbcPoolConfiguration(jdbcDataSourcePoolConfiguration);
		}
		return dataSource;
	}

	public static IDataSource getDataSource() {
		return new DataSource();
	}
}
