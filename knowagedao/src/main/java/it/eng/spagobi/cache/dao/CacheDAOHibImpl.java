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

package it.eng.spagobi.cache.dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.eng.spagobi.cache.metadata.SbiCacheItem;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.SpagoBIDAOException;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.cache.CacheItem;

/**
 * @author Alessandro Portosa (alessandro.portosa@eng.it)
 */

public class CacheDAOHibImpl extends AbstractHibernateDAO implements ICacheDAO {

	private static Logger logger = Logger.getLogger(CacheDAOHibImpl.class);

	// ========================================================================================
	// READ operations (cRud)
	// ========================================================================================

	@Override
	public List<CacheItem> loadAllCacheItems() {
		logger.debug("IN");

		List<CacheItem> toReturn = new ArrayList<>();
		Session session = null;
		Transaction transaction = null;
		try {
			session = getSession();
			transaction = session.beginTransaction();

			Query hibQuery = session.createQuery("from SbiCacheItem");
			List<SbiCacheItem> hibList = hibQuery.list();
			Iterator<SbiCacheItem> it = hibList.iterator();
			while (it.hasNext()) {
				SbiCacheItem hibMap = it.next();
				if (hibMap != null) {
					CacheItem cacheItem = toCacheItem(hibMap);
					toReturn.add(cacheItem);
				}
			}
			transaction.commit();
		} catch (Throwable t) {
			rollbackIfActive(transaction);
			throw new SpagoBIDAOException("An unexpected error occured while loading all cache items", t);

		} finally {
			closeSessionIfOpen(session);
			logger.debug("OUT");
		}
		return toReturn;
	}

	@Override
	public CacheItem loadCacheItemByTableName(String tableName) {
		CacheItem toReturn;
		Session session;
		Transaction transaction;

		logger.debug("IN");

		toReturn = null;
		session = null;
		transaction = null;
		try {
			if (tableName == null) {
				throw new IllegalArgumentException("Input parameter [tableName] cannot be null");
			}

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Throwable t) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", t);
			}

			Query hibQuery = session.createQuery("from SbiCacheItem h where h.tableName = ?");
			hibQuery.setString(0, tableName);
			SbiCacheItem hibMap = (SbiCacheItem) hibQuery.uniqueResult();
			if (hibMap != null) {
				toReturn = toCacheItem(hibMap);
			}

			transaction.commit();

		} catch (Throwable t) {
			rollbackIfActive(transaction);
			throw new SpagoBIDAOException("An unexpected error occured while loading cache item whose table name is equal to [" + tableName + "]", t);
		} finally {
			closeSessionIfOpen(session);
			logger.debug("OUT");
		}

		return toReturn;
	}

	@Override
	public CacheItem loadCacheItemBySignature(String signature) {
		CacheItem toReturn;
		Session session;
		Transaction transaction;

		logger.debug("IN");

		toReturn = null;
		session = null;
		transaction = null;
		try {
			if (signature == null) {
				throw new IllegalArgumentException("Input parameter [signature] cannot be null");
			}

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Throwable t) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", t);
			}

			Query hibQuery = session.createQuery("from SbiCacheItem h where h.signature = ?");
			hibQuery.setString(0, signature);
			SbiCacheItem hibMap = (SbiCacheItem) hibQuery.uniqueResult();
			if (hibMap != null) {
				toReturn = toCacheItem(hibMap);
			}

			transaction.commit();

		} catch (Throwable t) {
			rollbackIfActive(transaction);
			throw new SpagoBIDAOException("An unexpected error occured while loading cache item whose signature is equal to [" + signature + "]", t);
		} finally {
			closeSessionIfOpen(session);
			logger.debug("OUT");
		}

		return toReturn;
	}

	// ========================================================================================
	// CREATE operations (Crud)
	// ========================================================================================

	@Override
	public String insertCacheItem(CacheItem cacheItem) {
		String tableNameToReturn;
		Session session;
		Transaction transaction;

		logger.debug("IN");

		tableNameToReturn = null;
		session = null;
		transaction = null;
		try {
			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Throwable t) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", t);
			}
			SbiCacheItem hibMap = toSbiCacheItem(cacheItem);
			updateSbiCommonInfo4Insert(hibMap);
			tableNameToReturn = (String) session.save(hibMap);

			transaction.commit();

		} catch (Throwable t) {
			rollbackIfActive(transaction);
			throw new SpagoBIDAOException("An unexpected error occured while inserting cache item", t);
		} finally {
			closeSessionIfOpen(session);
			logger.debug("OUT");
		}

		return tableNameToReturn;
	}

	@Override
	public void updateCacheItem(CacheItem cacheItem) {
		Session session;
		Transaction transaction;

		logger.debug("IN");

		session = null;
		transaction = null;
		try {
			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Throwable t) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", t);
			}

			SbiCacheItem hibCacheItem = (SbiCacheItem) session.get(SbiCacheItem.class, cacheItem.getSignature());
			if (hibCacheItem != null) {
				update(hibCacheItem, cacheItem);
				updateSbiCommonInfo4Update(hibCacheItem);
				session.update(hibCacheItem);
			}

			transaction.commit();

		} catch (Throwable t) {
			rollbackIfActive(transaction);
			throw new SpagoBIDAOException("An unexpected error occured while update cache item", t);
		} finally {
			closeSessionIfOpen(session);
			logger.debug("OUT");
		}
	}

	// ========================================================================================
	// DELETE operations (cruD)
	// ========================================================================================
	/**
	 * @return {@link SbiCacheItem} instances of the removed cache items
	 */
	@Override
	public List<SbiCacheItem> deleteCacheItemByTableName(String tableName) {
		Session session;
		Transaction transaction;

		logger.debug("IN");

		session = null;
		transaction = null;

		try {
			if (tableName == null) {
				throw new IllegalArgumentException("Input parameter [tableName] cannot be null");
			}

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Throwable t) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", t);
			}

			Query hibernateQuery = session.createQuery("from SbiCacheItem h where h.tableName = ? ");
			hibernateQuery.setString(0, tableName);
			List<SbiCacheItem> sbiCacheItemList = hibernateQuery.list();
			for (SbiCacheItem sbiCacheItem : sbiCacheItemList) {
				if (sbiCacheItem != null) {
					session.delete(sbiCacheItem);
				}
			}

			transaction.commit();

			return sbiCacheItemList;

		} catch (Throwable t) {
			rollbackIfActive(transaction);
			String msg = (t.getMessage() != null) ? t.getMessage()
					: "An unexpected error occured while deleting cache item " + "whose tableName is equal to [" + tableName + "]";
			throw new SpagoBIDAOException(msg, t);
		} finally {
			closeSessionIfOpen(session);
			logger.debug("OUT");
		}
	}

	/**
	 * @return {@link SbiCacheItem} instances of the removed cache items
	 */
	@Override
	public List<SbiCacheItem> deleteCacheItemBySignature(String signature) {
		Session session;
		Transaction transaction;

		logger.debug("IN");

		session = null;
		transaction = null;

		try {
			if (signature == null) {
				throw new IllegalArgumentException("Input parameter [signature] cannot be null");
			}

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Throwable t) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", t);
			}

			Query hibernateQuery = session.createQuery("from SbiCacheItem h where h.signature = ? ");
			hibernateQuery.setString(0, signature);
			List<SbiCacheItem> sbiCacheItemList = hibernateQuery.list();
			for (SbiCacheItem sbiCacheItem : sbiCacheItemList) {
				if (sbiCacheItem != null) {
					session.delete(sbiCacheItem);
				}
			}

			transaction.commit();

			return sbiCacheItemList;

		} catch (Throwable t) {
			rollbackIfActive(transaction);
			String msg = (t.getMessage() != null) ? t.getMessage()
					: "An unexpected error occured while deleting cache item " + "whose signature is equal to [" + signature + "]";
			throw new SpagoBIDAOException(msg, t);
		} finally {
			closeSessionIfOpen(session);
			logger.debug("OUT");
		}
	}

	/**
	 * @return {@link SbiCacheItem} instances of the removed cache items
	 */
	@Override
	public List<SbiCacheItem> deleteAllCacheItem() {

		Session session;
		Transaction transaction;

		logger.debug("IN");

		session = null;
		transaction = null;
		List<SbiCacheItem> toBeDeleted = Collections.emptyList();

		try {

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Throwable t) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", t);
			}

			Query hibernateQuery = session.createQuery("from SbiCacheItem");
			toBeDeleted = hibernateQuery.list();
			if (toBeDeleted != null && !toBeDeleted.isEmpty()) {
				Iterator<SbiCacheItem> it = toBeDeleted.iterator();
				while (it.hasNext()) {
					SbiCacheItem sbiCacheItem = it.next();
					if (sbiCacheItem != null) {
						session.delete(sbiCacheItem);
					}
				}
				transaction.commit();
			}

		} catch (Throwable t) {
			rollbackIfActive(transaction);
			String msg = (t.getMessage() != null) ? t.getMessage() : "An unexpected error occured while deleting all cache items ";
			throw new SpagoBIDAOException(msg, t);
		} finally {
			closeSessionIfOpen(session);
			logger.debug("OUT");
		}
		return toBeDeleted;
	}

	private SbiCacheItem toSbiCacheItem(CacheItem cacheItem) {

		String properties = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			properties = mapper.writeValueAsString(cacheItem.getProperties());
		} catch (Throwable t) {
			throw new SpagoBIDAOException("An error occured while creating a SbiCacheItem from CacheItem:", t);
		}
		SbiCacheItem hibCacheItem = new SbiCacheItem(cacheItem.getSignature());
		hibCacheItem.setTableName(cacheItem.getTable());
		hibCacheItem.setName(cacheItem.getName());
		hibCacheItem.setDimension(cacheItem.getDimension().longValue());
		hibCacheItem.setCreationDate(cacheItem.getCreationDate());
		hibCacheItem.setLastUsedDate(cacheItem.getLastUsedDate());
		if (properties != null) {
			hibCacheItem.setProperties(properties);
		}

		return hibCacheItem;
	}

	private void update(SbiCacheItem target, CacheItem source) {
		String properties = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			properties = mapper.writeValueAsString(source.getProperties());
		} catch (Throwable t) {
			throw new SpagoBIDAOException("An error occured while creating a SbiCacheItem from CacheItem:", t);
		}
		target.setTableName(source.getTable());
		target.changeSignature(source.getSignature());
		target.setName(source.getName());
		target.setDimension(source.getDimension().longValue());
		target.setCreationDate(source.getCreationDate());
		target.setLastUsedDate(source.getLastUsedDate());
		if (properties != null) {
			target.setProperties(properties);
		}
	}

	private CacheItem toCacheItem(SbiCacheItem hibCacheItem) {

		HashMap<String, Object> properties = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
			};
			properties = mapper.readValue(hibCacheItem.getProperties(), typeRef);
		} catch (Throwable t) {
			throw new SpagoBIDAOException("An error occured while creating a CacheItem from SbiCacheItem:", t);
		}
		CacheItem cacheItem = new CacheItem();
		cacheItem.setSignature(hibCacheItem.getSignature());
		cacheItem.setTable(hibCacheItem.getTableName());
		cacheItem.setName(hibCacheItem.getName());
		cacheItem.setDimension(new BigDecimal(hibCacheItem.getDimension()));
		cacheItem.setCreationDate(hibCacheItem.getCreationDate());
		cacheItem.setLastUsedDate(hibCacheItem.getLastUsedDate());
		if (properties != null) {
			cacheItem.setProperties(properties);
		}
		return cacheItem;
	}
}
