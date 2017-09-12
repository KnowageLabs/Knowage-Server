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
package it.eng.spagobi.tools.dataset.cache;

import org.apache.log4j.Logger;

/**
 * The object that manage application cache. It's implemented as a singelton. If you dont want to use a global cache use CacheFactory instead to control the
 * instatiation of the cache.
 *
 *
 * @authors Marco Cortella (marco.cortella@eng.it), Andrea Gioia (andrea.gioia@eng.it)
 */
public class SpagoBICacheManager {

	/**
	 * The global cache shared by all application's modules
	 */
	private static ICache cache = null;

	private static transient Logger logger = Logger.getLogger(SpagoBICacheManager.class);

	/**
	 *
	 * @return The application cache.
	 */
	public static ICache getCache() {
		if (cache == null) {
			initializeCache();
		}
		return cache;
	}

	public static void removeCache() {
		cache.deleteAll();
		cache = null;
	}

	private static void initializeCache() {
		logger.trace("IN");
		try {
			ICacheConfiguration cacheConfiguration = SpagoBICacheConfiguration.getInstance();
			if (cacheConfiguration.getCacheDataSource() == null) {
				logger.error("Impossible to initialize cache because there are no datasource defined as defualt write datasource");
			} else {
				CacheFactory cacheFactory = new CacheFactory();
				cache = cacheFactory.getCache(cacheConfiguration);
			}
		} catch (Throwable t) {
			if (t instanceof CacheException)
				throw (CacheException) t;
			else
				throw new CacheException("An unexpected error occured while initializing cache", t);
		} finally {
			logger.trace("OUT");
		}
	}
}
