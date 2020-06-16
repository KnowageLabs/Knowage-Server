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
package it.eng.spagobi.utilities.cache;

import java.io.IOException;
import java.io.Serializable;

import org.apache.log4j.Logger;

import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * Implementation of the cache that uses the EhCache library (http://www.ehcache.org/)
 *
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */

public class GenericEhCacheImpl implements CacheInterface {

	private static transient Logger logger = Logger.getLogger(GenericEhCacheImpl.class);

	private Cache cache = null;

	private static CacheManager manager;

	static {
		try {
			manager = CacheManager.create();
		} catch (Exception e) {
			logger.error("Error while initialization CacheManager instance", e);
		}
	}

	public GenericEhCacheImpl(String cacheName) {
		super();
		try {
			if (manager == null) {
				throw new SpagoBIRuntimeException("Cache manager was not initialized properly");
			}
			cache = manager.getCache(cacheName);
		} catch (Exception e) {
			logger.error("Exception when inizializating cache", e);
		}
	}

	@Override
	public boolean contains(String code) {
		logger.debug("Searching element with code [" + code + "] inside cache");
		Element el = null;
		if (cache != null) {
			try {
				el = cache.get(code);
			} catch (IllegalStateException e) {
				logger.error("IllegalStateException", e);
			} catch (CacheException e) {
				logger.error("CacheException", e);
			}
			if (el == null)
				return false;
			if (el.getValue() == null)
				return false;
			return true;
		} else
			return false;
	}

	@Override
	public void put(String code, Serializable obj) {
		if (cache != null) {
			Element element = new Element(code, obj);
			if (code != null && obj != null) {
				logger.debug("Inserting element with code [" + code + "] inside the cache");
				cache.put(element);
			}
		}
	}

	@Override
	public Serializable get(String code) {
		if (cache != null && code != null) {
			try {
				Element el = cache.get(code);
				logger.debug("Retrieving element with code [" + code + "] from the cache");
				return el != null ? el.getValue() : null;
			} catch (IllegalStateException e) {
				logger.error("IllegalStateException", e);
			} catch (CacheException e) {
				logger.error("CacheException", e);
			}
		}
		return null;
	}

	@Override
	public void clear() {
		if (cache != null) {
			try {
				cache.removeAll();
			} catch (IllegalStateException | IOException e) {
				logger.error("An error occurred while clearing cache", e);
			}
		}
	}

}
