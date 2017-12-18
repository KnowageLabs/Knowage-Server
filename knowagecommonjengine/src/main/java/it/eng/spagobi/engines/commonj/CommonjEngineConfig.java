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
package it.eng.spagobi.engines.commonj;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.services.common.EnginConf;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.File;

import org.apache.log4j.Logger;

public class CommonjEngineConfig {

	private EnginConf engineConfig;

	private static CommonjEngineConfig instance;

	public static String COMMONJ_REPOSITORY_ROOT_DIR = "commonjRepository_root_dir";

	private static transient Logger logger = Logger.getLogger(CommonjEngineConfig.class);

	public static CommonjEngineConfig getInstance() {
		if (instance == null) {
			instance = new CommonjEngineConfig();
		}

		return instance;
	}

	private CommonjEngineConfig() {
		setEngineConfig(EnginConf.getInstance());
	}

	public SourceBean getConfigSourceBean() {
		return getEngineConfig().getConfig();
	}

	// core settings

	/**
	 * Checks if is absolute path.
	 *
	 * @param path
	 *            the path
	 *
	 * @return true, if is absolute path
	 */
	public static boolean isAbsolutePath(String path) {
		if (path == null)
			return false;
		return (path.startsWith("/") || path.startsWith("\\") || path.charAt(1) == ':');
	}

	/**
	 * Gets the runtime repository root dir.
	 *
	 * @return the runtime repository root dir
	 */
	public File getWorksRepositoryRootDir() {
		logger.debug("IN");
		String property = getProperty(COMMONJ_REPOSITORY_ROOT_DIR);

		SourceBean config = EnginConf.getInstance().getConfig();

		File dir = null;
		if (!isAbsolutePath(property)) {
			property = getEngineResourcePath() + System.getProperty("file.separator") + property;
		}

		if (property != null)
			dir = new File(property);
		logger.debug("OUT");
		return dir;
	}

	// engine settings

	public String getEngineResourcePath() {
		String path = null;
		if (getEngineConfig().getResourcePath() != null) {
			path = getEngineConfig().getResourcePath() + File.separatorChar + "commonj";
		} else {
			throw new SpagoBIRuntimeException("Impossible to get the resource path for the engine");
		}

		return path;
	}

	// utils

	private String getProperty(String propertName) {
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

	// java properties
	/**
	 * Gets the java install dir.
	 *
	 * @return the java install dir
	 */
	public String getJavaInstallDir() {
		SourceBean config = EnginConf.getInstance().getConfig();
		String installDir = config.getCharacters("java_install_dir");
		return installDir;
	}

	// /**
	// * Gets the word separator.
	// *
	// * @return the word separator
	// */
	// public String getWordSeparator() {
	// SourceBean config = EnginConf.getInstance().getConfig();
	// String wordS= (String)config.getCharacters("wordSeparator");
	// return wordS;
	// }

	public EnginConf getEngineConfig() {
		return engineConfig;
	}

	public void setEngineConfig(EnginConf engineConfig) {
		this.engineConfig = engineConfig;
	}
}
