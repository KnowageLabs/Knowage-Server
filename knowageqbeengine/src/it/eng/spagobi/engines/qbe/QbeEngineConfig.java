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
package it.eng.spagobi.engines.qbe;

import it.eng.qbe.datasource.naming.IDataSourceNamingStrategy;
import it.eng.qbe.datasource.naming.SimpleDataSourceNamingStrategy;
import it.eng.spago.base.SourceBean;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spagobi.services.common.EnginConf;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.file.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

/**
 * The Class QbeEngineConf.
 *
 * @author Andrea Gioia
 */
public class QbeEngineConfig {

	private EnginConf engineConfig;

	private final Locale locale = null;

	public static String QBE_MODE = "QBE_MODE";
	public static String QBE_DATAMART_DIR = "QBE_DATAMART_DIR";
	public static String WORKSHEET_DIR = "WORKSHEET_DIR";
	public static String WORKSHEET_IMAGES_MAX_SIZE = "WORKSHEET_IMAGES_MAX_SIZE";
	public static String WORKSHEET_IMAGES_MAX_NUMBER = "WORKSHEET_IMAGES_MAX_NUMBER";
	public static String QBE_DATAMART_RETRIVER = "QBE_DATAMART_RETRIVER";
	public static String SPAGOBI_SERVER_URL = "SPAGOBI_SERVER_URL";
	public static String DEFAULT_SPAGOBI_SERVER_URL = "http://localhost:8080/SpagoBI";

	private static transient Logger logger = Logger.getLogger(QbeEngineConfig.class);

	// -- singleton pattern --------------------------------------------
	private static QbeEngineConfig instance;

	public static QbeEngineConfig getInstance() {
		if (instance == null) {
			instance = new QbeEngineConfig();
		}
		return instance;
	}

	private QbeEngineConfig() {
		setEngineConfig(EnginConf.getInstance());
	}

	// -- singleton pattern --------------------------------------------

	// -- CORE SETTINGS ACCESSOR Methods--------------------------------

	public File getQbeDataMartDir() {
		File qbeDataMartDir;

		qbeDataMartDir = null;

		String property = getProperty(QBE_DATAMART_DIR);
		if (property != null) {
			String baseDirStr = getEngineResourcePath();
			File baseDir = new File(baseDirStr);
			if (!FileUtils.isAbsolutePath(property)) {
				property = baseDir + File.separator + property;
				qbeDataMartDir = new File(property);
			}
		}

		return qbeDataMartDir;
	}

	// engine settings

	public String getEngineResourcePath() {
		String path = null;
		if (getEngineConfig().getResourcePath() != null) {
			path = getEngineConfig().getResourcePath() + File.separatorChar + "qbe";
		} else {
			throw new SpagoBIRuntimeException("Impossible to get the resource path for the engine");
		}

		return path;
	}

	public boolean isWebModalityActive() {
		boolean isWebModalityActive;

		isWebModalityActive = true;

		String property = getProperty(QBE_MODE);
		if (property != null) {
			isWebModalityActive = property.equalsIgnoreCase("WEB");
		}

		return isWebModalityActive;
	}

	// utils

	public String getProperty(String propertName) {
		String propertyValue = null;
		SourceBean sourceBeanConf;

		Assert.assertNotNull(getConfigSourceBean(), "Impossible to parse engine-config.xml file");

		sourceBeanConf = (SourceBean) getConfigSourceBean().getAttribute(propertName);
		if (sourceBeanConf != null) {
			propertyValue = sourceBeanConf.getCharacters();
			logger.debug("Configuration attribute [" + propertName + "] is equals to: [" + propertyValue + "]");
		}

		return propertyValue;
	}

	public File getWorksheetDir() {
		File worksheetDir;

		worksheetDir = null;

		String property = getProperty(WORKSHEET_DIR);
		if (property != null) {
			String baseDirStr = getEngineResourcePath();
			File baseDir = new File(baseDirStr);
			if (!FileUtils.isAbsolutePath(property)) {
				String fs = File.separator;
				property = baseDir + fs + property;
				worksheetDir = new File(property);
			}
		}

		return worksheetDir;
	}

	public File getWorksheetImagesDir() {
		File worksheetDir = getWorksheetDir();
		File worksheetImagesDir = new File(worksheetDir, "images");
		if (worksheetImagesDir.exists() && !worksheetImagesDir.isDirectory()) {
			throw new SpagoBIEngineRuntimeException("Cannot create worksheet images dir! A file with the same name exists!");
		}
		if (!worksheetImagesDir.exists()) {
			boolean success = worksheetImagesDir.mkdirs();
			if (!success) {
				throw new SpagoBIEngineRuntimeException("Cannot create worksheet images dir!");
			}
		}
		return worksheetImagesDir;
	}

	public int getWorksheetImagesMaxSize() {
		String property = getProperty(WORKSHEET_IMAGES_MAX_SIZE);
		int toReturn = Integer.parseInt(property);
		return toReturn;
	}

	public int getWorksheetImagesMaxNumber() {
		String property = getProperty(WORKSHEET_IMAGES_MAX_NUMBER);
		int toReturn = Integer.parseInt(property);
		return toReturn;
	}

	public Integer getResultLimit() {
		Integer resultLimit = null;
		String resultLimitStr = (String) ConfigSingleton.getInstance().getAttribute("QBE.QBE-SQL-RESULT-LIMIT.value");
		if (resultLimitStr == null || resultLimitStr.equalsIgnoreCase("none")) {
			resultLimit = null;
		} else {
			try {
				resultLimit = new Integer(resultLimitStr);
			} catch (Throwable t) {
				logger.error(t);
			}
		}
		return resultLimit;
	}

	/**
	 * Returns true if the query must be validated before saving, false otherwise
	 *
	 * @return true if the query must be validated before saving, false otherwise
	 */
	public boolean isQueryValidationEnabled() {
		boolean isEnabled = false;
		String isEnabledStr = (String) ConfigSingleton.getInstance().getAttribute("QBE.QUERY-VALIDATION.enabled");
		isEnabled = Boolean.parseBoolean(isEnabledStr);
		return isEnabled;
	}

	/**
	 * Returns true if query validation before saving is blocking (i.e. incorrect queries cannot be saved), false otherwise
	 *
	 * @return true if query validation before saving is blocking (i.e. incorrect queries cannot be saved), false otherwise
	 */
	public boolean isQueryValidationBlocking() {
		boolean isBlocking = false;
		String isBlockingStr = (String) ConfigSingleton.getInstance().getAttribute("QBE.QUERY-VALIDATION.isBlocking");
		isBlocking = Boolean.parseBoolean(isBlockingStr);
		return isBlocking;
	}

	public boolean isMaxResultLimitBlocking() {
		boolean isBlocking = false;
		String isBlockingStr = (String) ConfigSingleton.getInstance().getAttribute("QBE.QBE-SQL-RESULT-LIMIT.isBlocking");
		isBlocking = Boolean.parseBoolean(isBlockingStr);
		return isBlocking;
	}

	/**
	 * Gets the naming strategy.
	 *
	 * @return the naming strategy
	 */
	public IDataSourceNamingStrategy getNamingStrategy() {
		return new SimpleDataSourceNamingStrategy();
	}

	// -- ACCESS Methods --------------------------------------------

	public EnginConf getEngineConfig() {
		return engineConfig;
	}

	public void setEngineConfig(EnginConf engineConfig) {
		this.engineConfig = engineConfig;
	}

	public SourceBean getConfigSourceBean() {
		return getEngineConfig().getConfig();
	}

	public int getQueryExecutionTimeout() {
		int timeout = 300000;
		String timeoutStr = (String) ConfigSingleton.getInstance().getAttribute("QBE.QBE-TIMEOUT-FOR-QUERY-EXECUTION.value");
		if (timeoutStr != null) {
			try {
				timeout = Integer.parseInt(timeoutStr);
			} catch (Throwable t) {
				logger.error("Wrong value for 'value' attribute in tag QBE-TIMEOUT-FOR-QUERY-EXECUTION in qbe.xml: it must be an integer and instead it is "
						+ timeoutStr + ". Using default that is 300000", t);
			}
		} else {
			logger.warn("No value for 'value' attribute in tag QBE-TIMEOUT-FOR-QUERY-EXECUTION in qbe.xml. Using default that is 300000");
		}
		logger.debug("Returning " + timeout);
		return timeout;
	}

	public int getCrosstabCellLimit() {
		int cellLimit = 0;
		try {
			cellLimit = new Integer((String) ConfigSingleton.getInstance().getAttribute("QBE.QBE-CROSSTAB-CELLS-LIMIT.value"));
		} catch (Exception e) {
			logger.debug("No cell limit has been defined in the qbe.xml");
		}
		return cellLimit;
	}

	public boolean isRemoveSubpaths() {
		boolean remove = true;
		try {
			remove = new Boolean((String) ConfigSingleton.getInstance().getAttribute("QBE.GRAPH-PATH.removeSubPaths"));
		} catch (Exception e) {
			logger.debug("No GRAPH PATH configuration has been specified in the qbe.xml");
		}
		return remove;
	}

	public String getPathsOrder() {
		String pathsOrder = "ASC";
		try {
			pathsOrder = ((String) ConfigSingleton.getInstance().getAttribute("QBE.GRAPH-PATH.pathsOrder"));
		} catch (Exception e) {
			logger.debug("No GRAPH PATH configuration has been specified in the qbe.xml");
		}
		return pathsOrder;
	}

	public String getGraphValidatorImpl() {
		String validatorClassName = null;
		try {
			validatorClassName = (String) ConfigSingleton.getInstance().getAttribute("QBE.GRAPH-PATH.graphValidatorImpl");
		} catch (Exception e) {
			logger.debug("No Graph Validator class name specified in the qbe.xml");
		}
		return validatorClassName;
	}

	public String getDefaultCoverImpl() {
		String defaultCoverClass = null;
		try {
			defaultCoverClass = ((String) ConfigSingleton.getInstance().getAttribute("QBE.GRAPH-PATH.defaultCoverImpl"));
		} catch (Exception e) {
			logger.debug("No Default cover graph algorithm class specified in the qbe.xml");
		}
		return defaultCoverClass;
	}

	public String getPathsFiltersImpl() {
		String pathFilters = null;
		try {
			pathFilters = ((String) ConfigSingleton.getInstance().getAttribute("QBE.GRAPH-PATH.pathsFiltersImpl"));
		} catch (Exception e) {
			logger.debug("No PATH filters defined in the qbe.xml");
		}
		return pathFilters;
	}

	public int getCrosstabCFDecimalPrecision() {
		int precision = 2;
		try {
			precision = new Integer((String) ConfigSingleton.getInstance().getAttribute("QBE.QBE-CROSSTAB-CALCULATEDFIELDS-DECIMAL.value"));
		} catch (Exception e) {
			logger.debug("No decimal precision for the crosstab has been defined in the qbe.xml");
		}
		return precision;
	}

	public boolean isDataSourceCacheEnabled() {
		boolean isDataSourceCacheEnabled = false;
		String isDataSourceCacheEnabledStr = (String) ConfigSingleton.getInstance().getAttribute("QBE.DATASOURCE_CACHE.enabled");
		isDataSourceCacheEnabled = Boolean.parseBoolean(isDataSourceCacheEnabledStr);
		return isDataSourceCacheEnabled;
	}

	public String getExportCsvSeparator() {
		List separators = new ArrayList<SourceBean>();
		String separator = "\t";
		try {
			separators = ConfigSingleton.getInstance().getAttributeAsList("QBE.EXPORT-CSV-SEPARATOR.SEPARATOR");
			for (int i = 0; i < separators.size(); i++) {
				SourceBean separBean = (SourceBean) separators.get(i);
				String defaultattr = (String) separBean.getAttribute("default");
				if (Boolean.parseBoolean(defaultattr)) {
					separator = (String) separBean.getAttribute("value");
				}
			}

		} catch (Exception e) {
			logger.debug("No EXPORT-CSV-SEPARATOR.SEPARATOR configuration has been specified in the qbe.xml");
		}
		return separator;
	}

}
