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
public class SbiMetaTableDAOHibImpl extends AbstractHibernateDAO implements ISbiMetaTableDAO {

	static private Logger logger = Logger.getLogger(SbiMetaTableDAOHibImpl.class);

	/**
	 * Load table by id.
	 *
	 * @param id
	 *            the table is
	 *
	 * @return the meta table
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaTableDAOHibImpl#loadTableByID(integer)
	 */
	@Override
	public SbiMetaTable loadTableByID(Integer id) throws EMFUserError {
		logger.debug("IN");

		SbiMetaTable toReturn = null;
		Session tmpSession = null;
		Transaction tx = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			toReturn = (SbiMetaTable) tmpSession.load(SbiMetaTable.class, id);
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
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaTableDAOHibImpl#loadTableByName(string)
	 */
	@Override
	public SbiMetaTable loadTableByName(String name) throws EMFUserError {
		logger.debug("IN");

		SbiMetaTable toReturn = null;
		Session tmpSession = null;
		Transaction tx = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			Criterion labelCriterrion = Expression.eq("name", name);
			Criteria criteria = tmpSession.createCriteria(SbiMetaTable.class);
			criteria.add(labelCriterrion);
			toReturn = (SbiMetaTable) criteria.uniqueResult();
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
	 * Load all tables.
	 *
	 * @return List of meta tables
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaTableDAOHibImpl#loadAllTables()
	 */
	@Override
	public List<SbiMetaTable> loadAllTables() throws EMFUserError {
		logger.debug("IN");

		Session tmpSession = null;
		Transaction tx = null;
		List<SbiMetaTable> toReturn = new ArrayList();
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			Query hibQuery = tmpSession.createQuery(" from SbiMetaTable");

			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();
			while (it.hasNext()) {
				SbiMetaTable hibMeta = (SbiMetaTable) it.next();
				toReturn.add(hibMeta);
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

	@Override
	public List<SbiMetaTable> loadTablesFromSource(int sourceId) throws EMFUserError {
		logger.debug("IN");

		List toReturn = null;
		Session tmpSession = null;
		Transaction tx = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			Criterion labelCriterrion = Expression.eq("sourceid", sourceId);
			Criteria criteria = tmpSession.createCriteria(SbiMetaTable.class);
			criteria.add(labelCriterrion);

			toReturn = criteria.list();
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
	 * Modify a metatable.
	 *
	 * @param aMetaTable
	 *            the sbimetatable changed
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaTableDAOHibImpl#modifyTable(SbiMetaTable)
	 */
	@Override
	public void modifyTable(SbiMetaTable aMetaTable) throws EMFUserError {
		logger.debug("IN");

		Session tmpSession = null;
		Transaction tx = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			SbiMetaTable hibMeta = (SbiMetaTable) tmpSession.load(SbiMetaTable.class, aMetaTable.getTableId());

			hibMeta.setName(aMetaTable.getName());
			hibMeta.setDeleted(aMetaTable.isDeleted());

			SbiMetaSource metaSource = null;
			if (aMetaTable.getSbiMetaSource().getSourceId() < 0) {
				Criterion aCriterion = Expression.eq("valueId", aMetaTable.getSbiMetaSource().getSourceId());
				Criteria criteria = tmpSession.createCriteria(SbiMetaSource.class);
				criteria.add(aCriterion);
				metaSource = (SbiMetaSource) criteria.uniqueResult();
				if (metaSource == null) {
					throw new SpagoBIDOAException("The Domain with value_id= " + aMetaTable.getSbiMetaSource().getSourceId() + " does not exist");
				}
				hibMeta.setSbiMetaSource(metaSource);
			}

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
	 * Insert a metatable.
	 *
	 * @param aMetaSource
	 *            the sbimetatable to insert
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaTableDAOHibImpl#insertSource(SbiMetaTable)
	 */
	@Override
	public void insertTable(SbiMetaTable aMetaTable) throws EMFUserError {
		logger.debug("IN");

		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			SbiMetaTable hibMeta = new SbiMetaTable();
			hibMeta.setName(aMetaTable.getName());
			hibMeta.setDeleted(aMetaTable.isDeleted());

			SbiMetaSource metaSource = null;
			if (aMetaTable.getSbiMetaSource() != null) {
				Criterion aCriterion = Expression.eq("valueId", aMetaTable.getSbiMetaSource().getSourceId());
				Criteria criteria = tmpSession.createCriteria(SbiMetaSource.class);
				criteria.add(aCriterion);
				metaSource = (SbiMetaSource) criteria.uniqueResult();
				if (metaSource == null) {
					throw new SpagoBIDOAException("The Domain with value_id= " + aMetaTable.getSbiMetaSource().getSourceId() + " does not exist");
				}
				hibMeta.setSbiMetaSource(metaSource);
			}

			updateSbiCommonInfo4Insert(hibMeta);
			tmpSession.save(hibMeta);
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
	public void deleteTable(SbiMetaTable aMetaTable) throws EMFUserError {
		logger.debug("IN");

		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			// // check if table is used by job by querying SBI_META_JOB_TABLE table
			// List<SbiMetaJobTable>jobsAssociated = DAOFactory.get().loadFederationsUsingDataset(datasetId, session);
			//
			// if (!federationsAssociated.isEmpty()) {
			//
			// // check if its a derived dataset.. In this case delete also the federation..
			//
			// for (Iterator iterator = federationsAssociated.iterator(); iterator.hasNext();) {
			// FederationDefinition fedDef = (FederationDefinition) iterator.next();
			// logger.debug("Dataset with id " + datasetId + " is used by Federation with label " + fedDef.getLabel());
			// }
			//
			// }

			SbiMetaTable hibMeta = (SbiMetaTable) tmpSession.load(SbiMetaTable.class, new Integer(aMetaTable.getTableId()));

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

	/**
	 * Checks for BC associated.
	 *
	 * @param bcId
	 *            the BC id
	 *
	 * @return true, if checks for BC associated
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaSourceDAOHibImpl#hasBcAssociated(int)
	 */
	@Override
	public boolean hasBcAssociated(Integer id) throws EMFUserError {
		boolean bool = false;

		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			String hql = " from SbiMetaTableBc s where s.id.tableId = ?";
			Query aQuery = tmpSession.createQuery(hql);
			aQuery.setInteger(0, id);

			List bcAssociated = aQuery.list();
			if (bcAssociated.size() > 0)
				bool = true;
			else
				bool = false;
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
		return bool;

	}

	@Override
	public boolean hasJobsAssociated(Integer id) throws EMFUserError {
		boolean bool = false;

		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			String hql = " from SbiMetaTableJob s where s.id.tableId = ?";
			Query aQuery = tmpSession.createQuery(hql);
			aQuery.setInteger(0, id);

			List bcAssociated = aQuery.list();
			if (bcAssociated.size() > 0)
				bool = true;
			else
				bool = false;
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
		return bool;
	}

}