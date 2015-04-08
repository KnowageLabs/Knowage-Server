/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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

		IConfigDAO dao = null;
		try {
			dao = DAOFactory.getSbiConfigDAO();
			List<Config> allConfig = dao.loadAllConfigParameters();
			if (allConfig.size() == 0)
				logger.error("The table sbi_config is EMPTY");
			for (Config config : allConfig) {
				cache.put(config.getLabel(), config.getValueCheck());
				logger.info("Add: " + config.getLabel() + " / " + config.getValueCheck());
			}
		} catch (Exception e) {
			logger.error("Impossible to load configuration for report engine", e);
		} finally {
			logger.debug("OUT");
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
