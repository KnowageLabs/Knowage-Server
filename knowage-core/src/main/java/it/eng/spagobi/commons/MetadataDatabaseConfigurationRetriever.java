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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;

import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IConfigDAO;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.cache.CacheInterface;
import it.eng.spagobi.utilities.cache.ConfigurationCache;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * This object is instantiated and used by SingletonConfig to read config parameters. SpagoBi project have its own implementation. The engines may have
 * different implementations.
 *
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class MetadataDatabaseConfigurationRetriever implements IConfigurationRetriever {

	private static Logger logger = Logger.getLogger(MetadataDatabaseConfigurationRetriever.class);

	private static final String CONFIGURATION_MAP = "CONFIGURATION_MAP";

	private final Lock lock = new ReentrantLock();

	public MetadataDatabaseConfigurationRetriever() {
	}

	@Override
	public String get(String key) {
		CacheInterface cache = ConfigurationCache.getCache();
		HashMap<String, String> configurations = (HashMap<String, String>) cache.get(CONFIGURATION_MAP);
		if (configurations == null) {
			lock.lock();
			try {
				configurations = (HashMap<String, String>) cache.get(CONFIGURATION_MAP);
				if (configurations == null) {
					HashMap<String, String> newConfiguration = loadConfigurations();
					if (newConfiguration.size() > 0) {
						cache.put(CONFIGURATION_MAP, newConfiguration);
					}
					configurations = newConfiguration;
				}
			} finally {
				lock.unlock();
			}
		}
		String toReturn = configurations.get(key);
		LogMF.debug(logger, "GET : [{0}] = [{01]", key, toReturn);
		return toReturn;
	}

	private HashMap<String, String> loadConfigurations() {
		logger.debug("IN");
		IConfigDAO dao = null;
		HashMap<String, String> configurations = null;
		try {
			configurations = new HashMap<String, String>();
			dao = DAOFactory.getSbiConfigDAO();
			List<Config> allConfig = dao.loadAllConfigParameters();
			if (allConfig.isEmpty()) {
				logger.error("The table sbi_config is EMPTY");
			}
			for (Config config : allConfig) {
				configurations.put(config.getLabel(), config.getValueCheck() != null ? config.getValueCheck() : "");
				logger.info("Retrieved configuration: " + config.getLabel() + " / " + config.getValueCheck());
			}
		} catch (Exception e) {
			logger.error("Impossible to get configuration", e);
			throw new SpagoBIRuntimeException("Impossible to get configuration", e);
		} finally {
			logger.debug("OUT");
		}
		return configurations;
	}

	@Override
	public List<IConfiguration> getByCategory(String category) {
		try {
			IConfigDAO configsDao = DAOFactory.getSbiConfigDAO();
			configsDao.setUserProfile(UserProfileManager.getProfile());
			List<Config> returnedVals = configsDao.loadConfigParametersByCategory(category);
			return new ArrayList<IConfiguration>(returnedVals);
		} catch (Exception e) {
			logger.error("Error while getting the list of configs", e);
			throw new SpagoBIRuntimeException("Error while getting the list of configs", e);
		}
	}

}
