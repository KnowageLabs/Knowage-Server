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
package it.eng.spagobi.commons.dao;

import it.eng.spago.base.SourceBean;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.services.common.EnginConf;

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
	private static String resourcePath;
	
	static private Logger logger = Logger.getLogger(DAOConfig.class);
	

	public static String getResourcePath() {
		if(DAOConfig.resourcePath == null) {
			try {
				String jndiName = SingletonConfig.getInstance().getConfigValue("SPAGOBI.RESOURCE_PATH_JNDI_NAME");
				DAOConfig.resourcePath = SpagoBIUtilities.readJndiResource(jndiName);
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
		if(DAOConfig.mappings == null) {
			DAOConfig.mappings = new HashMap<String, String>();
			ConfigSingleton configSingleton = ConfigSingleton.getInstance();
			List<SourceBean> daoConfigSourceBeans = (List<SourceBean>) configSingleton.getAttributeAsList("SPAGOBI.DAO-CONF.DAO");
			for(SourceBean daoConfigSourceBean : daoConfigSourceBeans) {
				String daoName = (String)daoConfigSourceBean.getAttribute("name");
				String daoClass = (String)daoConfigSourceBean.getAttribute("implementation");
				DAOConfig.mappings.put(daoName, daoClass);
			}			
		}
		return DAOConfig.mappings;
	}
	
	public static String getHibernateConfigurationFile() {
		if(DAOConfig.hibernateConfigurationFile == null) {
			DAOConfig.hibernateConfigurationFile = "hibernate.cfg.xml";
		}
		return DAOConfig.hibernateConfigurationFile;
	}

	public static void setHibernateConfigurationFile(String hibernateConfigurationFile) {
		DAOConfig.hibernateConfigurationFile = hibernateConfigurationFile;
	}
}
