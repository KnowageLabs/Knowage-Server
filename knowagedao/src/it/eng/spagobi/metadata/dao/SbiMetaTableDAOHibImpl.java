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
import it.eng.spagobi.metadata.metadata.SbiMetaTableColumn;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

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
			toReturn = loadTableByID(tmpSession, id);
			SbiMetaTable hibTable = (SbiMetaTable) tmpSession.load(SbiMetaTable.class, id);
			toReturn = new SbiMetaTable();
			toReturn.setTableId(hibTable.getTableId());
			toReturn.setName(hibTable.getName());
			toReturn.setDeleted(hibTable.isDeleted());
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
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaTableDAOHibImpl#loadTableWithColumnByID(integer)
	 */
	@Override
	public SbiMetaTable loadTableWithColumnByID(Integer id) throws EMFUserError {
		logger.debug("IN");

		SbiMetaTable toReturn = new SbiMetaTable();
		Session tmpSession = null;
		Transaction tx = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			SbiMetaTable smt = loadTableByID(tmpSession, id);
			toReturn.setTableId(smt.getTableId());
			toReturn.setName(smt.getName());

			Set<SbiMetaTableColumn> smtc = new HashSet<SbiMetaTableColumn>();
			for (Iterator<SbiMetaTableColumn> iterator = smt.getSbiMetaTableColumns().iterator(); iterator.hasNext();) {
				SbiMetaTableColumn smc = iterator.next();
				SbiMetaTableColumn tmp = new SbiMetaTableColumn();
				tmp.setColumnId(smc.getColumnId());
				tmp.setName(smc.getName());
				tmp.setType(smc.getType());
				smtc.add(tmp);
			}

			toReturn.setSbiMetaTableColumns(smtc);

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
	public SbiMetaTable loadTableByNameAndSource(String name, Integer sourceId) throws EMFUserError {
		logger.debug("IN");

		SbiMetaTable toReturn = null;
		Session tmpSession = null;
		Transaction tx = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			toReturn = loadTableByNameAndSource(tmpSession, name, sourceId);
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
	 * Load table by name.
	 *
	 * @param name
	 *            the table name
	 *
	 * @return the meta table
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

	/**
	 * Load paginated tables.
	 *
	 * @return List of meta tables
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaTableDAOHibImpl#loadAllTables()
	 */
	@Override
	public List<SbiMetaTable> loadPaginatedTables(Integer page, Integer item_per_page, String search) throws EMFUserError {
		logger.debug("IN");

		Session tmpSession = null;
		Transaction tx = null;
		List<SbiMetaTable> toReturn = new ArrayList();
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			Criteria c = tmpSession.createCriteria(SbiMetaTable.class);
			c.addOrder(Order.asc("name"));

			c.setFirstResult((page - 1) * item_per_page);
			c.setMaxResults(item_per_page);

			c.add(Restrictions.like("name", search == null ? "" : search, MatchMode.ANYWHERE).ignoreCase());
			tx.commit();
			toReturn = c.list();
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
	public Integer insertTable(SbiMetaTable aMetaTable) throws EMFUserError {
		logger.debug("IN");

		Session tmpSession = null;
		Transaction tx = null;
		Integer idToReturn = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			SbiMetaTable hibMeta = new SbiMetaTable();
			hibMeta.setName(aMetaTable.getName());
			hibMeta.setDeleted(aMetaTable.isDeleted());

			SbiMetaSource metaSource = null;
			if (aMetaTable.getSbiMetaSource() != null) {
				Criterion aCriterion = Expression.eq("sourceId", aMetaTable.getSbiMetaSource().getSourceId());
				Criteria criteria = tmpSession.createCriteria(SbiMetaSource.class);
				criteria.add(aCriterion);
				metaSource = (SbiMetaSource) criteria.uniqueResult();
				if (metaSource == null) {
					throw new SpagoBIDOAException("The Domain with value_id= " + aMetaTable.getSbiMetaSource().getSourceId() + " does not exist");
				}
				hibMeta.setSbiMetaSource(metaSource);
			}

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
	public void deleteTable(SbiMetaTable aMetaTable) throws EMFUserError {
		logger.debug("IN");

		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			// // check if table is used by job by querying SBI_META_JOB_TABLE
			// table
			// List<SbiMetaJobTable>jobsAssociated =
			// DAOFactory.get().loadFederationsUsingDataset(datasetId, session);
			//
			// if (!federationsAssociated.isEmpty()) {
			//
			// // check if its a derived dataset.. In this case delete also the
			// federation..
			//
			// for (Iterator iterator = federationsAssociated.iterator();
			// iterator.hasNext();) {
			// FederationDefinition fedDef = (FederationDefinition)
			// iterator.next();
			// logger.debug("Dataset with id " + datasetId +
			// " is used by Federation with label " + fedDef.getLabel());
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

	@Override
	public SbiMetaTable loadTableByID(Session session, Integer id) throws EMFUserError {
		logger.debug("IN");

		SbiMetaTable toReturn = null;
		Session tmpSession = session;

		try {
			toReturn = (SbiMetaTable) tmpSession.load(SbiMetaTable.class, id);

		} catch (HibernateException he) {
			logException(he);
			throw new HibernateException(he);
		} finally {
			logger.debug("OUT");
		}
		return toReturn;
	}

	@Override
	public SbiMetaTable loadTableByName(Session session, String name) throws EMFUserError {
		logger.debug("IN");

		SbiMetaTable toReturn = null;
		Session tmpSession = session;

		try {
			Criterion labelCriterrion = Expression.eq("name", name);
			Criteria criteria = tmpSession.createCriteria(SbiMetaTable.class);
			criteria.add(labelCriterrion);
			toReturn = (SbiMetaTable) criteria.uniqueResult();
		} catch (HibernateException he) {
			logException(he);
			throw new HibernateException(he);
		} finally {
			logger.debug("OUT");
		}
		return toReturn;
	}

	@Override
	public SbiMetaTable loadTableByNameAndSource(Session session, String name, Integer sourceId) throws EMFUserError {
		logger.debug("IN");

		SbiMetaTable toReturn = null;
		Session tmpSession = session;

		try {
			// Criterion labelCriterrion = Expression.eq("name", name);
			// Criteria criteria = tmpSession.createCriteria(SbiMetaTable.class);
			// criteria.add(labelCriterrion);
			// toReturn = (SbiMetaTable) criteria.uniqueResult();

			String hql = " from SbiMetaTable c where c.name = ? and c.sbiMetaSource.sourceId = ? ";
			Query aQuery = tmpSession.createQuery(hql);
			aQuery.setString(0, name);
			aQuery.setInteger(1, sourceId);
			toReturn = (SbiMetaTable) aQuery.uniqueResult();
		} catch (HibernateException he) {
			logException(he);
			throw new HibernateException(he);
		} finally {
			logger.debug("OUT");
		}
		return toReturn;
	}

	@Override
	public void modifyTable(Session session, SbiMetaTable aMetaTable) throws EMFUserError {
		logger.debug("IN");

		Session tmpSession = session;

		try {
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

		} catch (HibernateException he) {
			logException(he);
			throw new HibernateException(he);
		} finally {
			logger.debug("OUT");
		}
	}

	@Override
	public Integer insertTable(Session session, SbiMetaTable aMetaTable) throws EMFUserError {
		logger.debug("IN");

		Session tmpSession = session;
		Integer idToReturn = null;

		try {
			SbiMetaTable hibMeta = new SbiMetaTable();
			hibMeta.setName(aMetaTable.getName());
			hibMeta.setDeleted(aMetaTable.isDeleted());

			SbiMetaSource metaSource = null;
			if (aMetaTable.getSbiMetaSource() != null) {
				Criterion aCriterion = Expression.eq("sourceId", aMetaTable.getSbiMetaSource().getSourceId());
				Criteria criteria = tmpSession.createCriteria(SbiMetaSource.class);
				criteria.add(aCriterion);
				metaSource = (SbiMetaSource) criteria.uniqueResult();
				if (metaSource == null) {
					throw new SpagoBIDOAException("The Domain with value_id= " + aMetaTable.getSbiMetaSource().getSourceId() + " does not exist");
				}
				hibMeta.setSbiMetaSource(metaSource);
			}

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

	@Override
	public Integer countSbiMetaTable(String search) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		Integer resultNumber;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			String hql = "select count(*) from SbiMetaTable where name like '%" + search + "%'";
			Query hqlQuery = aSession.createQuery(hql);
			Long temp = (Long) hqlQuery.uniqueResult();
			resultNumber = new Integer(temp.intValue());

		} catch (HibernateException he) {
			logger.error("Error while loading the list of SbiMetaTable", he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 9104);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}
		return resultNumber;
	}

}