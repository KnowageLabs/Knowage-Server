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


import it.eng.spagobi.tools.dataset.cache.impl.sqldbcache.SQLDBCache;
import it.eng.spagobi.tools.dataset.cache.impl.sqldbcache.SQLDBCacheConfiguration;
import it.eng.spagobi.tools.dataset.cache.impl.sqldbcache.SQLDBCacheMetadata;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public abstract class CacheFactory {

	public static ICache getCache(ICacheConfiguration cacheConfiguration){
		Assert.assertNotNull(cacheConfiguration, "Impossible to initialize cache. The cache configuration object cannot be null");
		IDataSource dataSource = cacheConfiguration.getCacheDataSource();
		Assert.assertNotNull(dataSource, "Datasource cannot be null");

		return new SQLDBCache(dataSource, new SQLDBCacheMetadata((SQLDBCacheConfiguration) cacheConfiguration));
	}

}
