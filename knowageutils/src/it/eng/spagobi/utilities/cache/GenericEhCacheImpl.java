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

import java.io.Serializable;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.log4j.Logger;

/**
 * Implementation of the cache that uses the EhCache library (http://www.ehcache.org/)
 *
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */

public class GenericEhCacheImpl implements CacheInterface {

	private static transient Logger logger = Logger.getLogger(GenericEhCacheImpl.class);

	private static GenericEhCacheImpl instance;

	private Cache cache = null;

	private CacheManager manager;

	public GenericEhCacheImpl(String cacheName) {
		super();
		try {
			manager = CacheManager.create();
			cache = manager.getCache(cacheName);
		} catch (CacheException e) {
			logger.error("CacheException in inizialization", e);
		}

	}

	public synchronized static CacheInterface getInstance(String cacheName) {
		if (instance == null) {
			logger.debug("Creating cache for[" + cacheName + "]");
			instance = new GenericEhCacheImpl(cacheName);
		}
		logger.debug("Cache for[" + cacheName + "] already found");
		return instance;
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
				return el.getValue();
			} catch (IllegalStateException e) {
				logger.error("IllegalStateException", e);
			} catch (CacheException e) {
				logger.error("CacheException", e);
			}
		}
		return null;
	}
}
