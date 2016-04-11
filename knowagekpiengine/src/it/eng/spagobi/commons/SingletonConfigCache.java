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

import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IConfigDAO;

import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * This object is instantiated and used by SingletonConfig to read config
 * parameters. SpagoBi project have its own iplementation. The engines have
 * different implementation.
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 * 
 */
public class SingletonConfigCache implements ISingletonConfigCache {

	private static transient Logger logger = Logger.getLogger(SingletonConfigCache.class);
	private final HashMap<String, String> cache = new HashMap<String, String>();

	public SingletonConfigCache() {
		logger.debug("IN");
		System.out.println("SingletonConfigCache in ");

		IConfigDAO dao = null;
		try {
			dao = DAOFactory.getSbiConfigDAO();
			System.out.println("SingletonConfigCache dao ");
			List<Config> allConfig = dao.loadAllConfigParameters();
			System.out.println("SingletonConfigCache allConfig " + (allConfig!=null ? allConfig.size() : "null"));
			if (allConfig.size() == 0)
				logger.error("The table sbi_config is EMPTY");
			for (Config config : allConfig) {
				cache.put(config.getLabel(), config.getValueCheck());
				logger.info("Add: " + config.getLabel() + " / " + config.getValueCheck());
			}
		} catch (Exception e) {
			logger.error("Impossible to load configuration for full kpi engine", e);
			e.printStackTrace();
		} finally {
			logger.debug("OUT");
			System.out.println("SingletonConfigCache out ");
		}
	}

	@Override
	public String get(String key) {
		if (cache.get(key) == null) {
			logger.info("The property '" + key + "' doens't have any value assigned, check SBI_CONFIG table");
			return null;
		}
		logger.debug("GET :" + key + "=" + cache.get(key));
		return cache.get(key);
	}
}
