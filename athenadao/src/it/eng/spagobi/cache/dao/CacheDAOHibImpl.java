/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.cache.dao;

import it.eng.spagobi.cache.metadata.SbiCacheItem;
import it.eng.spagobi.cache.metadata.SbiCacheJoinedItem;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.SpagoBIDOAException;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.cache.CacheItem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Alessandro Portosa (alessandro.portosa@eng.it)
 */

public class CacheDAOHibImpl extends AbstractHibernateDAO implements ICacheDAO {

	static private Logger logger = Logger.getLogger(CacheDAOHibImpl.class);

	// ========================================================================================
	// READ operations (cRud)
	// ========================================================================================

	@Override
	public List<CacheItem> loadAllCacheItems() {
		logger.debug("IN");

		List<CacheItem> toReturn = new ArrayList<CacheItem>();
		Session session = null;
		Transaction transaction = null;
		try {
			session = getSession();
			transaction = session.beginTransaction();

			Query hibQuery = session.createQuery("from SbiCacheItem");
			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();
			while (it.hasNext()) {
				SbiCacheItem hibMap = (SbiCacheItem) it.next();
				if (hibMap != null) {
					CacheItem cacheItem = toCacheItem(hibMap);
					toReturn.add(cacheItem);
				}
			}
			transaction.commit();
		} catch (Throwable t) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDOAException("An unexpected error occured while loading all cache items", t);

		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
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
				throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
			}

			Query hibQuery = session.createQuery("from SbiCacheItem h where h.tableName = ?");
			hibQuery.setString(0, tableName);
			SbiCacheItem hibMap = (SbiCacheItem) hibQuery.uniqueResult();
			if (hibMap != null) {
				toReturn = toCacheItem(hibMap);
			}

			transaction.commit();

		} catch (Throwable t) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDOAException("An unexpected error occured while loading cache item whose table name is equal to [" + tableName + "]", t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
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
				throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
			}

			Query hibQuery = session.createQuery("from SbiCacheItem h where h.signature = ?");
			hibQuery.setString(0, signature);
			SbiCacheItem hibMap = (SbiCacheItem) hibQuery.uniqueResult();
			if (hibMap != null) {
				toReturn = toCacheItem(hibMap);
			}

			transaction.commit();

		} catch (Throwable t) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDOAException("An unexpected error occured while loading cache item whose signature is equal to [" + signature + "]", t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}

		return toReturn;
	}

	@Override
	public List<CacheItem> loadCacheJoinedItemsReferringTo(String signature) {
		List<CacheItem> toReturn;
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
				throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
			}

			toReturn = new ArrayList<CacheItem>();

			Query hibQuery = session.createQuery("from SbiCacheJoinedItem h where h.sbiCacheItemBySignature.signature = ?");
			hibQuery.setString(0, signature);
			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();
			while (it.hasNext()) {
				SbiCacheJoinedItem hibMap = (SbiCacheJoinedItem) it.next();
				if (hibMap != null) {
					CacheItem cacheItem = toCacheItem(hibMap.getSbiCacheItemByJoinedSignature());
					toReturn.add(cacheItem);
				}
			}

			transaction.commit();

		} catch (Throwable t) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDOAException("An unexpected error occured while loading cache item whose signature is equal to [" + signature + "]", t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}

		return toReturn;
	}

	@Override
	public boolean hasCacheItemReferenceToCacheJoinedItem(String signature, String joinedSignature) {
		boolean toReturn;
		Session session;
		Transaction transaction;

		logger.debug("IN");

		toReturn = false;
		session = null;
		transaction = null;
		try {
			if (signature == null || joinedSignature == null) {
				throw new IllegalArgumentException("Input parameter [signature|joinedSignature] cannot be null");
			}

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Throwable t) {
				throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
			}

			Query hibQuery = session
					.createQuery("from SbiCacheJoinedItem h where h.sbiCacheItemBySignature.signature = ? and h.sbiCacheItemByJoinedSignature.signature = ?");
			// Query hibQuery = session.createQuery("from SbiCacheJoinedItem h where h.sbiCacheItemBySignature = ? and h.joinedSignature = ?");
			hibQuery.setString(0, signature);
			hibQuery.setString(1, joinedSignature);
			SbiCacheJoinedItem hibMap = (SbiCacheJoinedItem) hibQuery.uniqueResult();
			if (hibMap != null) {
				toReturn = true;
			}

			transaction.commit();

		} catch (Throwable t) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDOAException("An unexpected error occured while checking if cache item [" + signature + "] already has a reference to ["
					+ joinedSignature + "]", t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
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
				throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
			}
			SbiCacheItem hibMap = toSbiCacheItem(cacheItem);
			updateSbiCommonInfo4Insert(hibMap);
			tableNameToReturn = (String) session.save(hibMap);

			transaction.commit();

		} catch (Throwable t) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}

			throw new SpagoBIDOAException("An unexpected error occured while inserting cache item", t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
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
				throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
			}

			SbiCacheItem hibMap = toSbiCacheItem(cacheItem);
			updateSbiCommonInfo4Update(hibMap);
			session.update(hibMap);

			transaction.commit();

		} catch (Throwable t) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}

			throw new SpagoBIDOAException("An unexpected error occured while update cache item", t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
	}

	@Override
	public Integer insertCacheJoinedItem(CacheItem cacheItem, CacheItem joinedCacheitem) {
		Integer id;
		Session session;
		Transaction transaction;

		logger.debug("IN");

		id = null;
		session = null;
		transaction = null;
		try {
			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Throwable t) {
				throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
			}
			SbiCacheItem tempHibMap = toSbiCacheItem(cacheItem);
			SbiCacheItem tempHibMapJoined = toSbiCacheItem(joinedCacheitem);
			updateSbiCommonInfo4Update(tempHibMap);
			updateSbiCommonInfo4Update(tempHibMapJoined);
			SbiCacheJoinedItem hibMap = new SbiCacheJoinedItem();
			updateSbiCommonInfo4Insert(hibMap);
			hibMap.setSbiCacheItemBySignature(tempHibMap);
			hibMap.setSbiCacheItemByJoinedSignature(tempHibMapJoined);
			id = (Integer) session.save(hibMap);

			transaction.commit();

		} catch (Throwable t) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}

			throw new SpagoBIDOAException("An unexpected error occured while inserting cache item", t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}

		return id;
	}

	// ========================================================================================
	// DELETE operations (cruD)
	// ========================================================================================

	@Override
	public void deleteCacheItemByTableName(String tableName) {
		Session session;
		Transaction transaction;
		boolean deleted;

		logger.debug("IN");

		session = null;
		transaction = null;
		deleted = false;

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
				throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
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

		} catch (Throwable t) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			String msg = (t.getMessage() != null) ? t.getMessage() : "An unexpected error occured while deleting cache item " + "whose tableName is equal to ["
					+ tableName + "]";
			throw new SpagoBIDOAException(msg, t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
	}

	@Override
	public void deleteCacheItemBySignature(String signature) {
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
				throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
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

		} catch (Throwable t) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			String msg = (t.getMessage() != null) ? t.getMessage() : "An unexpected error occured while deleting cache item " + "whose signature is equal to ["
					+ signature + "]";
			throw new SpagoBIDOAException(msg, t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
	}

	@Override
	public boolean deleteAllCacheItem() {
		// First delete the joined cache items
		deleteAllCacheJoinedItem();

		Session session;
		Transaction transaction;
		boolean deleted;

		logger.debug("IN");

		session = null;
		transaction = null;
		deleted = false;

		try {

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Throwable t) {
				throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
			}

			Query hibernateQuery = session.createQuery("from SbiCacheItem");
			List toBeDeleted = hibernateQuery.list();
			if (toBeDeleted != null && toBeDeleted.isEmpty() == false) {
				Iterator it = toBeDeleted.iterator();
				while (it.hasNext()) {
					SbiCacheItem sbiCacheItem = (SbiCacheItem) it.next();
					if (sbiCacheItem != null) {
						session.delete(sbiCacheItem);
					}
				}
				transaction.commit();
				deleted = true;
			}

		} catch (Throwable t) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			String msg = (t.getMessage() != null) ? t.getMessage() : "An unexpected error occured while deleting all cache items ";
			throw new SpagoBIDOAException(msg, t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
		return deleted;
	}

	@Override
	public boolean deleteAllCacheJoinedItem() {
		Session session;
		Transaction transaction;
		boolean deleted;

		logger.debug("IN");

		session = null;
		transaction = null;
		deleted = false;

		try {

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Throwable t) {
				throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
			}

			Query hibernateQuery = session.createQuery("from SbiCacheJoinedItem");
			List toBeDeleted = hibernateQuery.list();
			if (toBeDeleted != null && toBeDeleted.isEmpty() == false) {
				Iterator it = toBeDeleted.iterator();
				while (it.hasNext()) {
					SbiCacheJoinedItem sbiCacheJoinedItem = (SbiCacheJoinedItem) it.next();
					if (sbiCacheJoinedItem != null) {
						session.delete(sbiCacheJoinedItem);
					}
				}
				transaction.commit();
				deleted = true;
			}

		} catch (Throwable t) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			String msg = (t.getMessage() != null) ? t.getMessage() : "An unexpected error occured while deleting all cache joined items ";
			throw new SpagoBIDOAException(msg, t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
		return deleted;
	}

	private SbiCacheItem toSbiCacheItem(CacheItem cacheItem) {

		String properties = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			properties = mapper.writeValueAsString(cacheItem.getProperties());
		} catch (Throwable t) {
			throw new SpagoBIDOAException("An error occured while creating a SbiCacheItem from CacheItem:", t);
		}
		SbiCacheItem hibCacheItem = new SbiCacheItem();
		hibCacheItem.setTableName(cacheItem.getTable());
		hibCacheItem.setSignature(cacheItem.getSignature());
		hibCacheItem.setName(cacheItem.getName());
		hibCacheItem.setDimension(cacheItem.getDimension().longValue());
		hibCacheItem.setCreationDate(cacheItem.getCreationDate());
		hibCacheItem.setLastUsedDate(cacheItem.getLastUsedDate());
		if (properties != null) {
			hibCacheItem.setProperties(properties);
		}

		return hibCacheItem;
	}

	private CacheItem toCacheItem(SbiCacheItem hibCacheItem) {

		HashMap<String, Object> properties = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
			};
			properties = mapper.readValue(hibCacheItem.getProperties(), typeRef);
		} catch (Throwable t) {
			throw new SpagoBIDOAException("An error occured while creating a CacheItem from SbiCacheItem:", t);
		}
		CacheItem cacheItem = new CacheItem();
		cacheItem.setName(hibCacheItem.getTableName());
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
