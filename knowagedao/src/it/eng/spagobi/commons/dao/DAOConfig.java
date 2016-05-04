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
package it.eng.spagobi.commons.dao;

import it.eng.spago.base.SourceBean;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.services.common.EnginConf;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class DAOConfig {

	private static Map<String, String> mappings;
	private static String hibernateConfigurationFile;
	/**
	 * For testing purpose
	 */
	private static File hibernateConfigurationFileFile;
	private static String resourcePath;

	static private Logger logger = Logger.getLogger(DAOConfig.class);

	public static String getResourcePath() {
		if (DAOConfig.resourcePath == null) {
			try {
				DAOConfig.resourcePath = SpagoBIUtilities.getResourcePath();
			} catch (Throwable t) {
				logger.debug(t);
				DAOConfig.resourcePath = EnginConf.getInstance().getResourcePath();
			}
		}
		return resourcePath;
	}

	public static void setResourcePath(String resourcePath) {
		DAOConfig.resourcePath = resourcePath;
	}

	public static void setMappings(Map<String, String> mappings) {
		DAOConfig.mappings = mappings;
	}

	public static Map<String, String> getMappings() {
		if (DAOConfig.mappings == null) {
			DAOConfig.mappings = new HashMap<String, String>();
			ConfigSingleton configSingleton = ConfigSingleton.getInstance();
			List<SourceBean> daoConfigSourceBeans = configSingleton.getAttributeAsList("SPAGOBI.DAO-CONF.DAO");
			for (SourceBean daoConfigSourceBean : daoConfigSourceBeans) {
				String daoName = (String) daoConfigSourceBean.getAttribute("name");
				String daoClass = (String) daoConfigSourceBean.getAttribute("implementation");
				DAOConfig.mappings.put(daoName, daoClass);
			}
		}
		return DAOConfig.mappings;
	}

	public static String getHibernateConfigurationFile() {
		if (DAOConfig.hibernateConfigurationFile == null) {
			DAOConfig.hibernateConfigurationFile = "hibernate.cfg.xml";
		}
		return DAOConfig.hibernateConfigurationFile;
	}

	public static void setHibernateConfigurationFile(String hibernateConfigurationFile) {
		DAOConfig.hibernateConfigurationFile = hibernateConfigurationFile;
	}

	public static File getHibernateConfigurationFileFile() {
		return hibernateConfigurationFileFile;
	}

	public static void setHibernateConfigurationFileFile(File hibernateConfigurationFileFile) {
		DAOConfig.hibernateConfigurationFileFile = hibernateConfigurationFileFile;
	}
}
