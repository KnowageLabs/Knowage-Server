/**

SpagoBI - The Business Intelligence Free Platform

Copyright (C) 2005-2010 Engineering Ingegneria Informatica S.p.A.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

**/
package it.eng.spagobi.tools.dataset.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import it.eng.spago.base.SourceBean;
import it.eng.spago.configuration.ConfigSingleton;

import it.eng.spagobi.tools.dataset.cache.SpagoBICacheManager;
import it.eng.spagobi.tools.dataset.cache.ICache;
import it.eng.spagobi.tools.dataset.cache.impl.sqldbcache.SQLDBCache;

/**
 * The object that manage application cache. It's implemented as a singelton. If you dont want to
 * use a global cache use CacheFactory instead to control the instatiation of the cache.
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
	public static ICache getCache(){
		if (cache == null){
			initializeCache();
		}  
		return cache;
	}	
	
	private static void initializeCache() {		
		logger.trace("IN");
		try {		
			ICacheConfiguration cacheConfiguration = SpagoBICacheConfiguration.getInstance();
			if(cacheConfiguration.getCacheDataSource() == null) {
				logger.error("Impossible to initialize cache because there are no datasource defined as defualt write datasource");
			} else {
				CacheFactory cacheFactory = new CacheFactory();
				cache = cacheFactory.getCache( cacheConfiguration );
			}
		} catch (Throwable t) {
			if(t instanceof CacheException) throw (CacheException)t;
			else throw new CacheException("An unexpected error occured while initializing cache", t);
		} finally {
			logger.trace("OUT");
		}
	}
}
