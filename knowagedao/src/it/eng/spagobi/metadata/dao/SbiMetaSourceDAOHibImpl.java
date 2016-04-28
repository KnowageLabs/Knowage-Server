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
package it.eng.spagobi.metadata.dao;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.SpagoBIDOAException;
import it.eng.spagobi.metadata.metadata.SbiMetaSource;
import it.eng.spagobi.metadata.metadata.SbiMetaTable;
import it.eng.spagobi.utilities.assertion.Assert;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Expression;

/**
 * @author Antonella Giachino (antonella.giachino@eng.it)
 *
 */
public class SbiMetaSourceDAOHibImpl extends AbstractHibernateDAO implements ISbiMetaSourceDAO {

	static private Logger logger = Logger.getLogger(SbiMetaSourceDAOHibImpl.class);

	/**
	 * Load source by id.
	 *
	 * @param id
	 *            the source is
	 *
	 * @return the meta source
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaSourceDAOHibImpl#loadSourceByID(integer)
	 */
	@Override
	public SbiMetaSource loadSourceByID(Integer id) throws SpagoBIDOAException {
		logger.debug("IN");

		SbiMetaSource toReturn = null;
		Session tmpSession = null;
		Transaction tx = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			SbiMetaSource hibSource = (SbiMetaSource) tmpSession.load(SbiMetaSource.class, id);
			toReturn = new SbiMetaSource();
			toReturn.setSourceId(hibSource.getSourceId());
			toReturn.setName(hibSource.getName());
			toReturn.setType(hibSource.getType());
			toReturn.setUrl(hibSource.getUrl());
			toReturn.setSourceSchema(hibSource.getSourceSchema());
			toReturn.setSourceCatalogue(hibSource.getSourceCatalogue());

			tx.commit();

		} catch (ObjectNotFoundException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new SpagoBIDOAException("There is no sbiMetaSource with sourceId " + id);

		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new SpagoBIDOAException(he.getMessage());

		} finally {
			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();

			}
		}
		logger.debug("OUT");
		return toReturn;
	}

	/**
	 * Load source by name.
	 *
	 * @param name
	 *            the source name
	 *
	 * @return the meta source
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaSourceDAOHibImpl#loadSourceByName(string)
	 */
	@Override
	public SbiMetaSource loadSourceByName(String name) throws EMFUserError {
		logger.debug("IN");

		SbiMetaSource toReturn = null;
		Session tmpSession = null;
		Transaction tx = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			toReturn = loadSourceByName(tmpSession, name);
			tx.commit();

		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}
		}

		logger.debug("OUT");
		return toReturn;
	}

	@Override
	public SbiMetaSource loadSourceByName(Session session, String name) throws EMFUserError {
		logger.debug("IN");

		SbiMetaSource toReturn = null;
		Session tmpSession = session;

		try {

			Criterion labelCriterrion = Expression.eq("name", name);
			Criteria criteria = tmpSession.createCriteria(SbiMetaSource.class);
			criteria.add(labelCriterrion);
			toReturn = (SbiMetaSource) criteria.uniqueResult();
			if (toReturn == null)
				return null;

		} catch (HibernateException he) {
			logException(he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			logger.debug("OUT");
		}

		return toReturn;
	}

	@Override
	public SbiMetaSource loadSourceByNameAndType(String name, String type) throws EMFUserError {
		logger.debug("IN");

		SbiMetaSource toReturn = null;
		Session tmpSession = null;
		Transaction tx = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			toReturn = loadSourceByNameAndType(tmpSession, name, type);
			tx.commit();

		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}
		}

		logger.debug("OUT");
		return toReturn;
	}

	/**
	 * Load all sources.
	 *
	 * @return List of meta sources
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaSourceDAOHibImpl#loadAllSources()
	 */
	@Override
	public List<SbiMetaSource> loadAllSources() throws EMFUserError {
		logger.debug("IN");

		Session tmpSession = null;
		Transaction tx = null;
		List<SbiMetaSource> toReturn = new ArrayList();
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			Query hibQuery = tmpSession.createQuery(" from SbiMetaSource");

			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();
			while (it.hasNext()) {
				SbiMetaSource hibMetaSource = (SbiMetaSource) it.next();
				toReturn.add(hibMetaSource);
			}
			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {

			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}

		}
		logger.debug("OUT");
		return toReturn;
	}

	/**
	 * Modify a metasource.
	 *
	 * @param aMetaSource
	 *            the sbimetasource changed
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaSourceDAOHibImpl#modifySource(SbiMetaSource)
	 */
	@Override
	public void modifySource(SbiMetaSource aMetaSource) throws SpagoBIDOAException {
		logger.debug("IN");

		Session tmpSession = null;
		Transaction tx = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			SbiMetaSource hibMeta = (SbiMetaSource) tmpSession.load(SbiMetaSource.class, aMetaSource.getSourceId());

			hibMeta.setName(aMetaSource.getName());
			hibMeta.setType(aMetaSource.getType());
			hibMeta.setUrl(aMetaSource.getUrl());
			hibMeta.setLocation(aMetaSource.getLocation());
			hibMeta.setSourceSchema(aMetaSource.getSourceSchema());
			hibMeta.setSourceCatalogue(aMetaSource.getSourceCatalogue());

			updateSbiCommonInfo4Update(hibMeta);
			tx.commit();

		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new SpagoBIDOAException(he.getMessage());

		} finally {
			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}
		}
		logger.debug("OUT");
	}

	/**
	 * Insert a metasource.
	 *
	 * @param aMetaSource
	 *            the sbimetasource to insert
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaSourceDAOHibImpl#insertSource(SbiMetaSource)
	 */
	@Override
	public Integer insertSource(SbiMetaSource aMetaSource) throws EMFUserError {
		logger.debug("IN");

		Session tmpSession = null;
		Transaction tx = null;
		Integer idToReturn;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			SbiMetaSource hibMeta = new SbiMetaSource();
			hibMeta.setName(aMetaSource.getName());
			hibMeta.setType(aMetaSource.getType());
			hibMeta.setUrl(aMetaSource.getUrl());
			hibMeta.setLocation(aMetaSource.getLocation());
			hibMeta.setSourceSchema(aMetaSource.getSourceSchema());
			hibMeta.setSourceCatalogue(aMetaSource.getSourceCatalogue());

			updateSbiCommonInfo4Insert(hibMeta);
			idToReturn = (Integer) tmpSession.save(hibMeta);
			tx.commit();

		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {

			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();

			}

		}
		logger.debug("OUT");
		return idToReturn;
	}

	/**
	 * Delete a metasource.
	 *
	 * @param aMetaSource
	 *            the sbimetasource to delete
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaSourceDAOHibImpl#deleteSource(SbiMetaSource)
	 */
	@Override
	public void deleteSource(SbiMetaSource aMetaSource) throws EMFUserError {
		logger.debug("IN");

		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			SbiMetaSource hibMeta = (SbiMetaSource) tmpSession.load(SbiMetaSource.class, new Integer(aMetaSource.getSourceId()));

			tmpSession.delete(hibMeta);

			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {

			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}

		}
		logger.debug("OUT");
	}

	@Override
	public List<SbiMetaTable> loadMetaTables(Integer sourceId) throws EMFUserError {

		LogMF.debug(logger, "IN: id = [{0}]", sourceId);

		List<SbiMetaTable> toReturn = new ArrayList<SbiMetaTable>();
		Session session = null;
		Transaction transaction = null;

		try {
			if (sourceId == null) {
				throw new IllegalArgumentException("Input parameter [sourceId] cannot be null");
			}

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Throwable t) {
				throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
			}

			toReturn = loadMetaTables(session, sourceId);

			transaction.rollback();
		} catch (Throwable t) {
			logException(t);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDOAException("An unexpected error occured while loading meta tables of meta source with sourceId [" + sourceId + "]", t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}

		LogMF.debug(logger, "OUT: returning [{0}]", toReturn);
		return toReturn;
	}

	@Override
	public List<SbiMetaTable> loadMetaTables(Session aSession, Integer sourceId) throws EMFUserError {

		LogMF.debug(logger, "IN: id = [{0}]", sourceId);

		List<SbiMetaTable> toReturn = new ArrayList<SbiMetaTable>();
		Session session = aSession;

		try {
			if (sourceId == null) {
				throw new IllegalArgumentException("Input parameter [sourceId] cannot be null");
			}

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
			} catch (Throwable t) {
				throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
			}

			Query query = session.createQuery(" from SbiMetaTable smt where smt.sbiMetaSource.sourceId = ? ");
			query.setInteger(0, sourceId);
			List<SbiMetaTable> list = query.list();
			Iterator<SbiMetaTable> it = list.iterator();
			while (it.hasNext()) {
				toReturn.add(toMetaTable(it.next()));
			}
			logger.debug("Contents loaded");

		} catch (Throwable t) {
			logException(t);

			throw new SpagoBIDOAException("An unexpected error occured while loading meta tables of meta source with sourceId [" + sourceId + "]", t);
		} finally {
			LogMF.debug(logger, "OUT: returning [{0}]", toReturn);
		}

		return toReturn;
	}

	private SbiMetaTable toMetaTable(SbiMetaTable hibMetaTable) {
		logger.debug("IN");
		SbiMetaTable toReturn = null;
		if (hibMetaTable != null) {
			toReturn = new SbiMetaTable();
			toReturn.setTableId(hibMetaTable.getTableId());
			toReturn.setName(hibMetaTable.getName());
			toReturn.setDeleted(hibMetaTable.isDeleted());

		}
		logger.debug("OUT");
		return toReturn;
	}

	@Override
	public SbiMetaSource loadSourceByNameAndType(Session session, String name, String type) throws EMFUserError {
		logger.debug("IN");

		SbiMetaSource toReturn = null;
		Session tmpSession = session;

		try {
			String hql = " from SbiMetaSource s where s.name = ? and s.type = ? ";
			Query aQuery = tmpSession.createQuery(hql);
			aQuery.setString(0, name);
			aQuery.setString(1, type);
			toReturn = (SbiMetaSource) aQuery.uniqueResult();

			if (toReturn == null)
				return null;
		} catch (HibernateException he) {
			logException(he);
			throw new HibernateException(he);
		} finally {
			logger.debug("OUT");
		}

		return toReturn;
	}

	@Override
	public void modifySource(Session session, SbiMetaSource aMetaSource) throws EMFUserError {
		logger.debug("IN");

		Session tmpSession = session;

		try {
			SbiMetaSource hibMeta = (SbiMetaSource) tmpSession.load(SbiMetaSource.class, aMetaSource.getSourceId());

			hibMeta.setName(aMetaSource.getName());
			hibMeta.setType(aMetaSource.getType());
			hibMeta.setUrl(aMetaSource.getUrl());
			hibMeta.setLocation(aMetaSource.getLocation());
			hibMeta.setSourceSchema(aMetaSource.getSourceSchema());
			hibMeta.setSourceCatalogue(aMetaSource.getSourceCatalogue());

			updateSbiCommonInfo4Update(hibMeta);

		} catch (HibernateException he) {
			logException(he);
			throw new HibernateException(he);
		} finally {
			logger.debug("OUT");
		}
	}

	@Override
	public Integer insertSource(Session session, SbiMetaSource aMetaSource) throws EMFUserError {
		logger.debug("IN");

		Session tmpSession = session;
		Integer idToReturn;

		try {

			SbiMetaSource hibMeta = new SbiMetaSource();
			hibMeta.setName(aMetaSource.getName());
			hibMeta.setType(aMetaSource.getType());
			hibMeta.setUrl(aMetaSource.getUrl());
			hibMeta.setLocation(aMetaSource.getLocation());
			hibMeta.setSourceSchema(aMetaSource.getSourceSchema());
			hibMeta.setSourceCatalogue(aMetaSource.getSourceCatalogue());

			updateSbiCommonInfo4Insert(hibMeta);
			idToReturn = (Integer) tmpSession.save(hibMeta);

		} catch (HibernateException he) {
			logException(he);
			throw new HibernateException(he);
		} finally {
			logger.debug("OUT");
		}

		return idToReturn;
	}

}