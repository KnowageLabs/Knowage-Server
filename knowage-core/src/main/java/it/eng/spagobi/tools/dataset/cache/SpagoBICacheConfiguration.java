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
package it.eng.spagobi.tools.dataset.cache;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IConfigDAO;
import it.eng.spagobi.tools.dataset.cache.impl.sqldbcache.SQLDBCacheConfiguration;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.tools.datasource.dao.IDataSourceDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

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
	public static final String CACHE_LIMIT_FOR_STORE_CONFIG = "SPAGOBI.CACHE.LIMIT_FOR_STORE";
	public static final String CACHE_REFRESH = "SPAGOBI.CACHE.REFRESH";

	private static transient Logger logger = Logger.getLogger(SpagoBICacheConfiguration.class);

	public static ICacheConfiguration getInstance() throws Exception {
		SQLDBCacheConfiguration cacheConfiguration = new SQLDBCacheConfiguration();
		cacheConfiguration.setCacheDataSource(getCacheDataSource());
		cacheConfiguration.setTableNamePrefix(getTableNamePrefix());
		cacheConfiguration.setCacheSpaceAvailable(getCacheSpaceAvailable());
		cacheConfiguration.setCachePercentageToClean(getCachePercentageToClean());
		cacheConfiguration.setCacheDsLastAccessTtl(getCacheDsLastAccessTtl());
		cacheConfiguration.setCacheSchedulingFullClean(getCacheSchedulingFullClean());
		cacheConfiguration.setSchema(getCacheDatabaseSchema());
		cacheConfiguration.setCachePercentageToStore(getCachePercentageToStore());
		cacheConfiguration.setObjectsTypeDimension(getDimensionTypes());
		cacheConfiguration.setCacheRefresh(getCacheRefresh());
		return cacheConfiguration;
	}

	private static IDataSource getCacheDataSource() throws Exception {
			IDataSourceDAO dataSourceDAO = DAOFactory.getDataSourceDAO();
			IDataSource dataSource = dataSourceDAO.loadDataSourceWriteDefault();
			if (dataSource == null) {
				throw new Exception(
						"Cannot configure cache: Data source for writing is not defined. Please select one in the data sources definition panel.");
			}
			return dataSource;
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

	private static Integer getCachePercentageToStore() {
		try {
			Integer cachePercentageToStore = null;
			String propertyValue = getSpagoBIConfigurationProperty(CACHE_LIMIT_FOR_STORE_CONFIG);
			if (propertyValue != null) {
				cachePercentageToStore = Integer.valueOf(propertyValue);
			}
			return cachePercentageToStore;
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("An unexpected exception occured while loading cache configuration property", t);
		}
	}

	private static String getCacheRefresh() {
		try {
			String cacheRefresh = null;
			String propertyValue = getSpagoBIConfigurationProperty(CACHE_REFRESH);
			if (propertyValue != null) {
				cacheRefresh = propertyValue;
			}
			return cacheRefresh;
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

	private static final String CACHE_CONFIG_TAG = "CACHE_CONFIG";
	private static final String DATA_TYPES_TAG = "DATA_TYPES";
	private static final String TYPE_TAG = "TYPE";

	public static void initCacheConfiguration() {
		logger.trace("IN");
		try (InputStream xmlStream = SpagoBICacheConfiguration.class.getResourceAsStream("/conf/cache.xml")) {
			if (xmlStream == null) {
				throw new CacheException("Provided XML InputStream is null");
			}

			// Parse XML
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			// Disable external entity references
			dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
			dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			// Disable DTDs entirely
			dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			// Enable secure processing
			dbf.setFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, true);
			// Prevent external entity resolution
			dbf.setExpandEntityReferences(false);

			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(xmlStream);
			doc.getDocumentElement().normalize();

			// <CACHE_CONFIG>
			Element cacheConfigEl = getSingleElementByTagName(doc, CACHE_CONFIG_TAG);
			if (cacheConfigEl == null) {
				throw new CacheException("Impossible to find configuration block [" + CACHE_CONFIG_TAG + "]");
			}

			// <DATA_TYPES>
			Element dataTypesEl = getSingleElementByTagName(cacheConfigEl, DATA_TYPES_TAG);
			if (dataTypesEl == null) {
				throw new CacheException("Impossible to find configuration block [" + CACHE_CONFIG_TAG + "." + DATA_TYPES_TAG + "]");
			}

			// <TYPE .../>
			NodeList typesNodes = dataTypesEl.getElementsByTagName(TYPE_TAG);
			if (typesNodes == null || typesNodes.getLength() == 0) {
				throw new CacheException("Impossible to find configuration blocks [" + CACHE_CONFIG_TAG + "." + DATA_TYPES_TAG + "." + TYPE_TAG + "]");
			}

			logger.trace("Initializing types' default dimension");
			// logger.trace("Types' default dimension configuration block is equal to " + typesNodesToString(typesNodes));

			dimensionTypes = new ArrayList<>();
			for (int i = 0; i < typesNodes.getLength(); i++) {
				Element typeEl = (Element) typesNodes.item(i);

				String name = typeEl.getAttribute("name");
				String bytes = typeEl.getAttribute("bytes");

				Properties props = new Properties();
				if (name != null && !name.isEmpty()) {
					props.setProperty("name", name);
				}
				if (bytes != null && !bytes.isEmpty()) {
					props.setProperty("bytes", bytes);
				}

				logger.trace("Type [" + name + "] defualt dimension is equal to [" + bytes + "]");
				dimensionTypes.add(props);
			}

			logger.trace("Types' default dimension succesfully initialized");
		} catch (Throwable t) {
			// Mantengo la semantica del catch del tuo metodo (solo allineo il messaggio al contesto cache)
			throw new RuntimeException("An error occured while loading cache dimension levels' properties from XML", t);
		} finally {
			logger.debug("OUT");
		}
	}

	private static Element getSingleElementByTagName(Element parent, String tagName) {
		NodeList nl = parent.getElementsByTagName(tagName);
		if (nl == null || nl.getLength() == 0) {
			return null;
		}
		return (Element) nl.item(0);
	}

	private static Element getSingleElementByTagName(Document doc, String tagName) {
		NodeList nl = doc.getElementsByTagName(tagName);
		if (nl == null || nl.getLength() == 0) {
			return null;
		}
		return (Element) nl.item(0);
	}

}
