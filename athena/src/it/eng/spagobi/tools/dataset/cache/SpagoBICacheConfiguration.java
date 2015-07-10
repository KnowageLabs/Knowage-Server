/*
 * SpagoBI, the Open Source Business Intelligence suite
 * © 2005-2015 Engineering Group
 *
 * This file is part of SpagoBI. SpagoBI is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 2.1 of the License, or any later version.
 * SpagoBI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You should have received
 * a copy of the GNU Lesser General Public License along with SpagoBI. If not, see: http://www.gnu.org/licenses/.
 * The complete text of SpagoBI license is included in the COPYING.LESSER file.
 */
package it.eng.spagobi.tools.dataset.cache;

import it.eng.spago.base.SourceBean;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IConfigDAO;
import it.eng.spagobi.tools.dataset.cache.impl.sqldbcache.SQLDBCacheConfiguration;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.tools.datasource.dao.IDataSourceDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.threadmanager.WorkManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class SpagoBICacheConfiguration {

	public static final String CACHE_NAME_PREFIX_CONFIG = "SPAGOBI.CACHE.NAMEPREFIX";
	public static final String CACHE_SPACE_AVAILABLE_CONFIG = "SPAGOBI.CACHE.SPACE_AVAILABLE";
	public static final String CACHE_LIMIT_FOR_CLEAN_CONFIG = "SPAGOBI.CACHE.LIMIT_FOR_CLEAN";
	public static final String CACHE_DS_LAST_ACCESS_TTL = "SPAGOBI.CACHE.DS_LAST_ACCESS_TTL";
	public static final String CACHE_SCHEDULING_FULL_CLEAN = "SPAGOBI.CACHE.SCHEDULING_FULL_CLEAN";
	public static final String CACHE_DATABASE_SCHEMA = "SPAGOBI.CACHE.DATABASE_SCHEMA";
	private static final String JNDI_THREAD_MANAGER = "JNDI_THREAD_MANAGER";

	private static transient Logger logger = Logger.getLogger(SpagoBICacheConfiguration.class);

	public static ICacheConfiguration getInstance() {
		SQLDBCacheConfiguration cacheConfiguration = new SQLDBCacheConfiguration();
		cacheConfiguration.setCacheDataSource(getCacheDataSource());
		cacheConfiguration.setTableNamePrefix(getTableNamePrefix());
		cacheConfiguration.setCacheSpaceAvailable(getCacheSpaceAvailable());
		cacheConfiguration.setCachePercentageToClean(getCachePercentageToClean());
		cacheConfiguration.setCacheDsLastAccessTtl(getCacheDsLastAccessTtl());
		cacheConfiguration.setCacheSchedulingFullClean(getCacheSchedulingFullClean());
		cacheConfiguration.setSchema(getCacheDatabaseSchema());
		cacheConfiguration.setObjectsTypeDimension(getDimensionTypes());
		cacheConfiguration.setWorkManager(getWorkManager());
		return cacheConfiguration;
	}

	private static WorkManager getWorkManager() {
		try {
			WorkManager spagoBIWorkManager = new WorkManager(getSpagoBIConfigurationProperty(JNDI_THREAD_MANAGER));
			return spagoBIWorkManager;
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("An unexpected exception occured while loading cache configuration property", t);
		}
	}

	private static IDataSource getCacheDataSource() {
		try {
			IDataSourceDAO dataSourceDAO = DAOFactory.getDataSourceDAO();
			IDataSource dataSource = dataSourceDAO.loadDataSourceWriteDefault();
			if (dataSource == null) {
				throw new SpagoBIRuntimeException(
						"Cannot configure cache: Data source for writing is not defined. Please select one in the data sources definition panel.");
			}
			return dataSource;
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("An unexpected exception occured while loading cache datasource", t);
		}
	}

	private static String getTableNamePrefix() {
		try {
			String tableNamePrefix = getSpagoBIConfigurationProperty(CACHE_NAME_PREFIX_CONFIG);
			return tableNamePrefix;
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("An unexpected exception occured while loading cache configuration property", t);
		}
	}

	private static BigDecimal getCacheSpaceAvailable() {
		try {
			BigDecimal cacheSpaceAvailable = null;
			String propertyValue = getSpagoBIConfigurationProperty(CACHE_SPACE_AVAILABLE_CONFIG);
			if (propertyValue != null) {
				cacheSpaceAvailable = BigDecimal.valueOf(Double.valueOf(propertyValue));
			}
			return cacheSpaceAvailable;
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("An unexpected exception occured while loading cache configuration property", t);
		}
	}

	private static Integer getCachePercentageToClean() {
		try {
			Integer cachePercentageToClean = null;
			String propertyValue = getSpagoBIConfigurationProperty(CACHE_LIMIT_FOR_CLEAN_CONFIG);
			if (propertyValue != null) {
				cachePercentageToClean = Integer.valueOf(propertyValue);
			}
			return cachePercentageToClean;
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("An unexpected exception occured while loading cache configuration property", t);
		}
	}

	private static Integer getCacheDsLastAccessTtl() {
		try {
			Integer cacheDsLastAccessTtl = null;
			String propertyValue = getSpagoBIConfigurationProperty(CACHE_DS_LAST_ACCESS_TTL);
			if (propertyValue != null) {
				cacheDsLastAccessTtl = Integer.valueOf(propertyValue);
			}
			return cacheDsLastAccessTtl;
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("An unexpected exception occured while loading cache configuration property", t);
		}
	}

	private static String getCacheSchedulingFullClean() {
		try {
			String cacheSchedulingFullClean = null;
			String propertyValue = getSpagoBIConfigurationProperty(CACHE_SCHEDULING_FULL_CLEAN);
			if (propertyValue != null) {
				cacheSchedulingFullClean = propertyValue;
			}
			return cacheSchedulingFullClean;
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("An unexpected exception occured while loading cache configuration property", t);
		}
	}

	private static String getCacheDatabaseSchema() {
		try {
			String cacheDatabaseSchema = null;
			String propertyValue = getSpagoBIConfigurationProperty(CACHE_DATABASE_SCHEMA);
			if (propertyValue != null) {
				cacheDatabaseSchema = propertyValue;
			}
			return cacheDatabaseSchema;
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("An unexpected exception occured while loading cache configuration property", t);
		}
	}

	private static String getSpagoBIConfigurationProperty(String propertyName) {
		try {
			String propertyValue = null;
			IConfigDAO configDao = DAOFactory.getSbiConfigDAO();
			Config cacheSpaceCleanableConfig = configDao.loadConfigParametersByLabel(propertyName);
			if ((cacheSpaceCleanableConfig != null) && (cacheSpaceCleanableConfig.isActive())) {
				propertyValue = cacheSpaceCleanableConfig.getValueCheck();
			}
			return propertyValue;
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("An unexpected exception occured while loading spagobi property [" + propertyName + "]", t);
		}
	}

	// ===============================================================================
	// TYPE DIMENSIONS
	// ===============================================================================
	private static List<Properties> dimensionTypes = null;

	public static List<Properties> getDimensionTypes() {

		if (dimensionTypes == null) {
			initCacheConfiguration();
		}

		return dimensionTypes;
	}

	private final static String CACHE_CONFIG_TAG = "CACHE_CONFIG";
	private final static String DATA_TYPES_TAG = "DATA_TYPES";
	private final static String TYPE_TAG = "TYPE";

	public static void initCacheConfiguration() {
		logger.trace("IN");
		try {
			SourceBean configSB = (SourceBean) ConfigSingleton.getInstance().getAttribute(CACHE_CONFIG_TAG);
			if (configSB == null) {
				throw new CacheException("Impossible to find configuartion block [" + CACHE_CONFIG_TAG + "]");
			}

			SourceBean typesSB = (SourceBean) configSB.getAttribute(DATA_TYPES_TAG);
			if (typesSB == null) {
				throw new CacheException("Impossible to find configuartion block [" + CACHE_CONFIG_TAG + "." + DATA_TYPES_TAG + "]");
			}

			List<SourceBean> typesList = typesSB.getAttributeAsList(TYPE_TAG);
			if (typesSB == null) {
				throw new CacheException("Impossible to find configuartion blocks [" + CACHE_CONFIG_TAG + "." + DATA_TYPES_TAG + "." + TYPE_TAG + "]");
			}

			logger.trace("Initializing types' default dimension");
			logger.trace("Types' default dimension configuration block is equal to " + typesList.toString());
			dimensionTypes = new ArrayList<Properties>();
			for (SourceBean type : typesList) {
				String name = (String) type.getAttribute("name");
				String bytes = (String) type.getAttribute("bytes");

				Properties props = new Properties();
				if (name != null) {
					props.setProperty("name", name);
				}
				if (bytes != null) {
					props.setProperty("bytes", bytes);
				}
				logger.trace("Type [" + name + "] defualt dimension is equal to [" + bytes + "]");
				dimensionTypes.add(props);
			}
			logger.trace("Types' default dimension succesfully initialized");
		} catch (Throwable t) {
			throw new RuntimeException("An error occured while loading geo dimension levels' properties from file engine-config.xml", t);
		} finally {
			logger.debug("OUT");
		}
	}

}
