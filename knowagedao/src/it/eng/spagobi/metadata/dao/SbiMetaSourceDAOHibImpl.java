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
import it.eng.spagobi.metadata.metadata.SbiMetaSource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
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
	public SbiMetaSource loadSourceByID(Integer id) throws EMFUserError {
		logger.debug("IN");

		SbiMetaSource toReturn = null;
		Session tmpSession = null;
		Transaction tx = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			SbiMetaSource hibSource = (SbiMetaSource) tmpSession.load(SbiMetaSource.class, id);
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
			Criterion labelCriterrion = Expression.eq("name", name);
			Criteria criteria = tmpSession.createCriteria(SbiMetaSource.class);
			criteria.add(labelCriterrion);
			toReturn = (SbiMetaSource) criteria.uniqueResult();
			if (toReturn == null)
				return null;
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
	public void modifySource(SbiMetaSource aMetaSource) throws EMFUserError {
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

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

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

}