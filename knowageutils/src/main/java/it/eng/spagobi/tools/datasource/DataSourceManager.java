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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.log4j.Logger;

import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;

/**
 * @author Alessandro Portosa (alessandro.portosa@eng.it)
 *
 */
public class DataSourceManager {

	private static Logger logger = Logger.getLogger(DataSourceManager.class);

	private static final DataSourceManager INSTANCE = new DataSourceManager();

	public static DataSourceManager getInstance() {
		return INSTANCE;
	}

	private final Map<IDataSource, BasicDataSource> dataSources = new HashMap<>();
	private final Map<Integer, Semaphore> semaphores = new HashMap<>();

	public Connection getConnection(IDataSource dataSource) throws SQLException {

		BasicDataSource ret = null;
		Semaphore semaphore = null;

		try {

			int dsId = dataSource.getDsId();
			String label = dataSource.getLabel();

			semaphores.putIfAbsent(dsId, new Semaphore(1));
			semaphore = semaphores.get(dsId);

			semaphore.acquire();

			boolean containsByKeyId = containsByIdOnly(dataSources, dataSource);
			boolean containsByKey = dataSources.containsKey(dataSource);

			if (containsByKeyId && containsByKey) {
				logger.debug("Use old connection pool for datasource " + label);
			} else if (containsByKeyId && !containsByKey) {
				logger.debug("Replacing connection for datasource " + label);
				ret = getByIdOnly(dataSources, dataSource);
				try {
					ret.close();
				} catch (SQLException e) {
					logger.warn("Non-fatal error closing old connection pool for datasource " + label);
				}
				createPoolIfAbsent(dataSource);
			} else {
				logger.warn("Creating connection pool for datasource " + label);
				createPoolIfAbsent(dataSource);
			}
			ret = dataSources.get(dataSource);
		} catch (InterruptedException e) {
			logger.debug("Datasource " + dataSource.getLabel() + " not found as connection pool...", e);
		} finally {
			 semaphore.release();
		}

		return ret.getConnection();
	}

	private boolean containsByIdOnly(Map<IDataSource, BasicDataSource> dataSources, IDataSource dataSource) {
		return dataSources.keySet()
				.stream()
				.anyMatch(e -> e.getDsId() == dataSource.getDsId());
	}

	private BasicDataSource getByIdOnly(Map<IDataSource, BasicDataSource> dataSources, IDataSource dataSource) {
		return dataSources.entrySet()
				.stream()
				.filter(e -> e.getKey().getDsId() == dataSource.getDsId())
				.findFirst()
				.get()
				.getValue();
	}

	private void createPoolIfAbsent(IDataSource dataSource) {
		Assert.assertNotNull(dataSource, "Missing input datasource");
		Assert.assertNotNull(dataSource.getJdbcPoolConfiguration(), "Connection pool information is not provided");

		logger.debug("Creating connection pool for datasource " + dataSource.getLabel());
		BasicDataSource pool = new BasicDataSource();
		pool.setJmxName("org.apache.dbcp:DataSource=" + dataSource.getLabel());
		pool.setDriverClassName(dataSource.getDriver());
		pool.setUrl(dataSource.getUrlConnection());
		pool.setUsername(dataSource.getUser());
		pool.setPassword(dataSource.getPwd());
		pool.setMaxTotal(dataSource.getJdbcPoolConfiguration().getMaxTotal());
		pool.setMaxWaitMillis(dataSource.getJdbcPoolConfiguration().getMaxWait());
		Integer maxIdle = dataSource.getJdbcPoolConfiguration().getMaxIdle();
		if (maxIdle != null)
			pool.setMaxIdle(maxIdle);
		pool.setRemoveAbandonedOnBorrow(dataSource.getJdbcPoolConfiguration().getRemoveAbandonedOnBorrow());
		pool.setRemoveAbandonedOnMaintenance(dataSource.getJdbcPoolConfiguration().getRemoveAbandonedOnMaintenance());
		pool.setRemoveAbandonedTimeout(dataSource.getJdbcPoolConfiguration().getAbandonedTimeout());
		pool.setLogAbandoned(dataSource.getJdbcPoolConfiguration().getLogAbandoned());
		pool.setTestOnReturn(dataSource.getJdbcPoolConfiguration().getTestOnReturn());
		pool.setTestWhileIdle(dataSource.getJdbcPoolConfiguration().getTestWhileIdle());
		pool.setTimeBetweenEvictionRunsMillis(dataSource.getJdbcPoolConfiguration().getTimeBetweenEvictionRuns());
		pool.setMinEvictableIdleTimeMillis(dataSource.getJdbcPoolConfiguration().getMinEvictableIdleTimeMillis());
		pool.setValidationQuery(dataSource.getJdbcPoolConfiguration().getValidationQuery());
		Integer validationQueryTimeout = dataSource.getJdbcPoolConfiguration().getValidationQueryTimeout();
		if (validationQueryTimeout != null)
			pool.setValidationQueryTimeout(validationQueryTimeout);

		dataSources.put(dataSource, pool);
	}
}
