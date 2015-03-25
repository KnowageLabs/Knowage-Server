/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.document.handlers;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParuse;
import it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.QueryDetail;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.utilities.cache.CacheInterface;
import it.eng.spagobi.utilities.cache.CacheSingleton;

import java.util.List;

import org.apache.log4j.Logger;

/**
 * This class caches LOV (list of values) executions' result.
 * The key of the cache element is composed by the user's identifier and the LOV definition.
 * In case the LOV is a query and there are dependencies, the wrapped statement is used instead of the original statement. 
 * 
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class LovResultCacheManager {
	
	private static Logger logger = Logger.getLogger(LovResultCacheManager.class);
	
	private CacheInterface cache = null;
	
	public LovResultCacheManager() {
		this.cache = CacheSingleton.getInstance();
	}

	/**
	 * Returns the LOV result. If the LOV result is in cache, it is returned;
	 * otherwise, if retrieveIfNotcached is true, the LOV is executed and
	 * returned, otherwise null is returned.
	 * 
	 * @param profile
	 *            The user profile object
	 * @param lovDefinition
	 *            The LOV definition
	 * @param dependencies
	 *            The dependencies to be considered          
	 * @param executionInstance
	 *            The execution instance
	 * @param retrieveIfNotcached
	 *            If true and LOV is not cached, the LOV executed and cached,
	 *            otherwise the LOV is not executed (and not cached)
	 * @return the LOV result, or null if the LOV is not cached and
	 *         retrieveIfNotcached is false
	 * @throws Exception
	 */
	public String getLovResult(IEngUserProfile profile, ILovDetail lovDefinition, List<ObjParuse> dependencies,
			ExecutionInstance executionInstance, boolean retrieveIfNotcached) throws Exception {
		logger.debug("IN");
		
		String lovResult = null;
		
		if (lovDefinition instanceof QueryDetail) {
			// queries are cached
			String cacheKey = getCacheKey(profile, lovDefinition, dependencies, executionInstance);
			logger.info("Cache key : " + cacheKey);
			if (cache.contains(cacheKey)) {
				logger.info("Retrieving lov result from cache...");
				// lov provider is present, so read the DATA in cache
				lovResult = cache.get(cacheKey);
				logger.debug(lovResult);
			} else if (retrieveIfNotcached) {
				logger.info("Executing lov to get result ...");
				lovResult = lovDefinition.getLovResult(profile, dependencies, executionInstance.getBIObject().getBiObjectParameters(),executionInstance.getLocale());
				logger.debug(lovResult);
				// insert the data in cache
				if (lovResult != null) 
					cache.put(cacheKey, lovResult);
			}
		} else {
			// scrips, fixed list and java classes are not cached, and returned without considering retrieveIfNotcached input
			logger.info("Executing lov (NOT QUERY TYPE) to get result ...");
			lovResult = lovDefinition.getLovResult(profile, dependencies, executionInstance.getBIObject().getBiObjectParameters(),executionInstance.getLocale());
			logger.debug(lovResult);
		}
		
		logger.debug("OUT");
		return lovResult;
	}

	/**
	 * This method finds out the cache to be used for lov's result cache. This
	 * key is composed mainly by the user identifier and the lov definition.
	 * Note that, in case when the lov is a query and there is correlation, the
	 * executed statement if different from the original query (since
	 * correlation expression is injected inside SQL query using in-line view
	 * construct), therefore we should consider the modified query.
	 * 
	 * @param profile
	 *            The user profile
	 * @param lovDefinition
	 *            The lov original definition
	 * @param dependencies
	 *            The dependencies to be considered (if any)
	 * @param executionInstance
	 *            The execution instance (it may be null, since a lov can be
	 *            executed outside an execution instance context)
	 * @return The key to be used in cache
	 */
	private String getCacheKey(IEngUserProfile profile,
			ILovDetail lovDefinition, List<ObjParuse> dependencies,
			ExecutionInstance executionInstance) {
		logger.debug("IN");
		String toReturn = null;
		String userID = (String)((UserProfile)profile).getUserId();
		if (lovDefinition instanceof QueryDetail) {
			QueryDetail queryDetail = (QueryDetail) lovDefinition;
			QueryDetail clone = queryDetail.clone();
			clone.setQueryDefinition(queryDetail.getWrappedStatement(dependencies, executionInstance.getBIObject().getBiObjectParameters()));
			toReturn = userID + ";" + clone.toXML();
		} else {
			toReturn = userID + ";" + lovDefinition.toXML();
		}
		logger.debug("OUT: returning [" + toReturn + "]");
		return toReturn;
	}

}
