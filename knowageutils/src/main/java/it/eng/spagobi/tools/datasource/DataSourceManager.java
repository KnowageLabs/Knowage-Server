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

package it.eng.spagobi.tools.datasource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.log4j.Logger;

import it.eng.spagobi.tools.datasource.bo.IDataSource;

/**
 * @author Alessandro Portosa (alessandro.portosa@eng.it)
 *
 */
public abstract class DataSourceManager {

	private static Logger logger = Logger.getLogger(DataSourceManager.class);

	private static ConcurrentHashMap<IDataSource, BasicDataSource> dataSources = new ConcurrentHashMap<>();

	public static Connection getConnection(IDataSource dataSource) throws SQLException {
		if (!dataSources.contains(dataSource)) {
			logger.debug("Datasource " + dataSource.getLabel() + " not found as connection pool...");
			createPoolIfAbsent(dataSource);
		}
		return dataSources.get(dataSource).getConnection();
	}

	private static void createPoolIfAbsent(IDataSource dataSource) {
		logger.debug("Creating connection pool for datasource " + dataSource.getLabel());
		BasicDataSource pool = new BasicDataSource();
		pool.setDriverClassName(dataSource.getDriver());
		pool.setUrl(dataSource.getUrlConnection());
		pool.setUsername(dataSource.getUser());
		pool.setPassword(dataSource.getPwd());

		if (dataSource.getJdbcPoolConfiguration() != null) {
			if (dataSource.getJdbcPoolConfiguration().getMaxWait() != null)
				pool.setMaxWaitMillis(dataSource.getJdbcPoolConfiguration().getMaxWait());
			if (dataSource.getJdbcPoolConfiguration().getRemoveAbandonedOnBorrow() != null)
				pool.setRemoveAbandonedOnBorrow(dataSource.getJdbcPoolConfiguration().getRemoveAbandonedOnBorrow());
			if (dataSource.getJdbcPoolConfiguration().getRemoveAbandonedOnMaintenance() != null)
				pool.setRemoveAbandonedOnMaintenance(dataSource.getJdbcPoolConfiguration().getRemoveAbandonedOnMaintenance());
			pool.setRemoveAbandonedTimeout(dataSource.getJdbcPoolConfiguration().getAbandonedTimeout());
			if (dataSource.getJdbcPoolConfiguration().getLogAbandoned() != null)
				pool.setLogAbandoned(dataSource.getJdbcPoolConfiguration().getLogAbandoned());
			if (dataSource.getJdbcPoolConfiguration().getTestOnReturn() != null)
				pool.setTestOnReturn(dataSource.getJdbcPoolConfiguration().getTestOnReturn());
			if (dataSource.getJdbcPoolConfiguration().getTestWhileIdle() != null)
				pool.setTestWhileIdle(dataSource.getJdbcPoolConfiguration().getTestWhileIdle());
			if (dataSource.getJdbcPoolConfiguration().getTimeBetweenEvictionRuns() != null)
				pool.setTimeBetweenEvictionRunsMillis(dataSource.getJdbcPoolConfiguration().getTimeBetweenEvictionRuns());
			pool.setValidationQuery(dataSource.getJdbcPoolConfiguration().getValidationQuery());
		} else {
			pool.setMaxWaitMillis(60000);
			pool.setRemoveAbandonedOnBorrow(true);
			pool.setRemoveAbandonedOnMaintenance(true);
			pool.setRemoveAbandonedTimeout(60);
			pool.setLogAbandoned(true);
			pool.setTestOnReturn(true);
			pool.setTestWhileIdle(true);
			pool.setTimeBetweenEvictionRunsMillis(10000);
		}

		dataSources.putIfAbsent(dataSource, pool);
	}
}
