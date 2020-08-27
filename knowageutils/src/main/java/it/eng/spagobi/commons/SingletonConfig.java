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

package it.eng.spagobi.commons;

import java.util.List;

import org.apache.log4j.Logger;

import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * Defines the Singleton SpagoBI implementations.
 *
 * @author Monia Spinelli
 */

public class SingletonConfig {

	private static final String CONFIG_RETRIEVER_CLASS_NAME = "it.eng.spagobi.commons.MetadataDatabaseConfigurationRetriever";

	private static Logger logger = Logger.getLogger(SingletonConfig.class);

	private static final SingletonConfig instance = new SingletonConfig();

	private IConfigurationRetriever retriever;

	public static SingletonConfig getInstance() {
		return instance;
	}

	private SingletonConfig() {
		logger.debug("IN");
		try {
			retriever = (IConfigurationRetriever) Class.forName(CONFIG_RETRIEVER_CLASS_NAME).newInstance();
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Impossible to instantiate " + CONFIG_RETRIEVER_CLASS_NAME, e);
		}
		logger.debug("OUT");
	}

	/**
	 * Gets the configuration corresponding to the input key.
	 *
	 * @return Returns the value of configuration by its key
	 */
	public String getConfigValue(String key) {
		logger.debug("Retrieving configuration parameter with key [" + key + "]");
		String toReturn = retriever.get(key);
		logger.debug("Retrieved configuration parameter with key [" + key + "]: [" + toReturn + "]");
		return toReturn;
	}

	/**
	 * Gets the configuration corresponding to the input key.
	 * @return
	 *
	 * @return Returns the value of configuration by its key
	 */
	public List<IConfiguration> getConfigsValueByCategory(String category) {
		logger.debug("Retrieving configuration parameters for category [" + category + "]");
		List<IConfiguration> toReturn = retriever.getByCategory(category);
		logger.debug("Retrieved configuration parameter for category [" + category + "]: [" + toReturn + "]");
		return toReturn;
	}

	/**
	 * for testing
	 *
	 * @return
	 */
	public IConfigurationRetriever getConfigurationRetriever() {
		return retriever;
	}

	/**
	 * for testing
	 *
	 * @param retriever
	 */
	public void setConfigurationRetriever(IConfigurationRetriever retriever) {
		this.retriever = retriever;
	}

}