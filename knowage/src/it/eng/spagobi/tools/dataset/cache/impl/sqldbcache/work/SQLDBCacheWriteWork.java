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
package it.eng.spagobi.tools.dataset.cache.impl.sqldbcache.work;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.cache.ICache;
import it.eng.spagobi.tools.dataset.cache.SpagoBICacheManager;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;

import org.apache.log4j.Logger;

import commonj.work.Work;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class SQLDBCacheWriteWork implements Work {

	ICache cache;
	IDataStore dataStore;
	IDataSet dataSet;
	UserProfile userProfile;

	private static transient Logger logger = Logger.getLogger(SpagoBICacheManager.class);

	/**
	 * @param cache
	 * @param dataStore
	 * @param signature
	 * @param dataSet
	 */
	public SQLDBCacheWriteWork(ICache cache, IDataStore dataStore, IDataSet dataSet) {
		super();
		this.cache = cache;
		this.dataStore = dataStore;
		this.dataSet = dataSet;
	}

	/**
	 * @param cache
	 * @param dataStore
	 * @param signature
	 * @param dataSet
	 */
	public SQLDBCacheWriteWork(ICache cache, IDataStore dataStore, IDataSet dataSet, UserProfile userProfile) {
		super();
		this.cache = cache;
		this.dataStore = dataStore;
		this.dataSet = dataSet;
		this.userProfile = userProfile;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		logger.trace("IN");
		try {
			if (userProfile != null) {
				TenantManager.setTenant(new Tenant(userProfile.getOrganization()));
			}
			cache.put(dataSet, dataStore);
		} catch (Throwable t) {
			// who is catching this exception in the end? Verify and push the log there
			logger.error("An unexpected error occured while adding store to cache", t);
			throw new RuntimeException("An unexpected error occured while adding store to cache", t);
		} finally {
			TenantManager.unset();
			logger.trace("OUT");
		}
		
	}

	/* (non-Javadoc)
	 * @see commonj.work.Work#isDaemon()
	 */
	public boolean isDaemon() {
		return false;
	}

	/* (non-Javadoc)
	 * @see commonj.work.Work#release()
	 */
	public void release() {
		// TODO Auto-generated method stub

	}

}
