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

import org.apache.log4j.Logger;

import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * Defines the Singleton SpagoBI implementations.
 *
 * TODO refactor this class since it is not a real cache!!!!
 *
 * @author Monia Spinelli
 *
 */
public class SingletonConfig {

	private static String CONFIG_CACHE_CLASS_NAME = "it.eng.spagobi.commons.SingletonConfigCache";

	private static transient Logger logger = Logger.getLogger(SingletonConfig.class);

	private ISingletonConfigCache cache;

	private static SingletonConfig ISTANCE = new SingletonConfig();

	public static SingletonConfig getInstance() {
		return ISTANCE;
	}

	private SingletonConfig() {
		logger.debug("IN");
		try {
			cache = (ISingletonConfigCache) Class.forName(CONFIG_CACHE_CLASS_NAME).newInstance();
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Impossible to create " + CONFIG_CACHE_CLASS_NAME, e);
		}
	}

	/**
	 * Gets the config.
	 *
	 * @return SourceBean contain the configuration
	 *
	 *         QUESTO METODO LO UTILIZZI PER LEGGERE LA CONFIGURAZIONE DEI SINGOLI ELEMENTI: ES: String configurazione=
	 *         SingletonConfig.getInstance().getConfigValue("home.banner");
	 */
	public String getConfigValue(String key) {
		return cache.get(key);

	}

	public void clearCache() {
	}

	/**
	 * for testing
	 *
	 * @return
	 */
	public ISingletonConfigCache getCache() {
		return cache;
	}

	/**
	 * for testing
	 *
	 * @param cache
	 */
	public void setCache(ISingletonConfigCache cache) {
		this.cache = cache;
	}

}