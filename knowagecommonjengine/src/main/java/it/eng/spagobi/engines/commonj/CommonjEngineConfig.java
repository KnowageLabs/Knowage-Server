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

import java.io.File;

import org.apache.log4j.Logger;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.services.common.EnginConf;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class CommonjEngineConfig {

	private static final Logger LOGGER = Logger.getLogger(CommonjEngineConfig.class);
	private static final CommonjEngineConfig INSTANCE = new CommonjEngineConfig();

	public static final String COMMONJ_REPOSITORY_ROOT_DIR = "commonjRepository_root_dir";

	private EnginConf engineConfig;

	public static CommonjEngineConfig getInstance() {
		return INSTANCE;
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
		LOGGER.debug("IN");
		String property = getProperty(COMMONJ_REPOSITORY_ROOT_DIR);

		File dir = null;
		if (!isAbsolutePath(property)) {
			property = getEngineResourcePath() + System.getProperty("file.separator") + property;
		}

		if (property != null)
			dir = new File(property);
		LOGGER.debug("OUT");
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
			LOGGER.debug("Configuration attribute [" + propertName + "] is equals to: [" + propertyValue + "]");
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
		return config.getCharacters("java_install_dir");
	}

	public EnginConf getEngineConfig() {
		return engineConfig;
	}

	public void setEngineConfig(EnginConf engineConfig) {
		this.engineConfig = engineConfig;
	}
}
