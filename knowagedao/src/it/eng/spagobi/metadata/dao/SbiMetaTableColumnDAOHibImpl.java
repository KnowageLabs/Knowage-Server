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
public class SbiMetaTableColumnDAOHibImpl extends AbstractHibernateDAO implements ISbiMetaTableColumnDAO {

	static private Logger logger = Logger.getLogger(SbiMetaTableColumnDAOHibImpl.class);

	/**
	 * Load table column by id.
	 *
	 * @param id
	 *            the table column id
	 *
	 * @return the meta table column
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaTableColumnDAOHibImpl#loadTableColumnByID(integer)
	 */
	@Override
	public SbiMetaTableColumn loadTableColumnByID(Integer id) throws EMFUserError {
		logger.debug("IN");

		SbiMetaTableColumn toReturn = null;
		Session tmpSession = null;
		Transaction tx = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			toReturn = loadTableColumnByID(tmpSession, id);
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
	 * Load table column by name.
	 *
	 * @param name
	 *            the table column name
	 *
	 * @return the meta table column
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaTableColumnDAOHibImpl#loadTableByName(string)
	 */
	@Override
	public SbiMetaTableColumn loadTableColumnByName(String name) throws EMFUserError {
		logger.debug("IN");

		SbiMetaTableColumn toReturn = null;
		Session tmpSession = null;
		Transaction tx = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			toReturn = loadTableColumnByName(tmpSession, name);
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
	 * Load table column by name.
	 *
	 * @param name
	 *            the table column name
	 * @param tableId
	 *            the table id
	 * @return the meta table column
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaTableColumnDAOHibImpl#loadTableByName(string)
	 */
	@Override
	public SbiMetaTableColumn loadTableColumnByNameAndTable(String name, Integer tableId) throws EMFUserError {
		logger.debug("IN");

		SbiMetaTableColumn toReturn = null;
		Session tmpSession = null;
		Transaction tx = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			toReturn = loadTableColumnByNameAndTable(tmpSession, name, tableId);
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
	 * Load all tablecolumn column linked to a table.
	 *
	 * @return List of meta tables
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaTableColumnDAOHibImpl#loadTableColumnsFromTable()
	 */
	@Override
	public List<SbiMetaTableColumn> loadTableColumnsFromTable(int tableId) throws EMFUserError {
		logger.debug("IN");

		List toReturn = null;
		Session tmpSession = null;
		Transaction tx = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			toReturn = loadTableColumnsFromTable(tmpSession, tableId);

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
	 * Modify a metatablecolumn.
	 *
	 * @param aMetaTable
	 *            the metatablecolumn changed
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaTableColumnDAOHibImpl#modifyTable(SbiMetaTableColumn)
	 */
	@Override
	public void modifyTableColumn(SbiMetaTableColumn aMetaTableColumn) throws EMFUserError {
		logger.debug("IN");

		Session tmpSession = null;
		Transaction tx = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			modifyTableColumn(tmpSession, aMetaTableColumn);
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
	 * Insert a metatablecolumn.
	 *
	 * @param aMetaTableColumn
	 *            the sbimetatablecolumn to insert
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaTableColumnDAOHibImpl#insertTableColumn(SbiMetaTableColumn)
	 */
	@Override
	public Integer insertTableColumn(SbiMetaTableColumn aMetaTableColumn) throws EMFUserError {
		logger.debug("IN");

		Session tmpSession = null;
		Transaction tx = null;
		Integer idToReturn = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			idToReturn = insertTableColumn(tmpSession, aMetaTableColumn);
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
	public void deleteTableColumn(SbiMetaTableColumn aMetaTableColumn) throws EMFUserError {
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

			SbiMetaTableColumn hibMeta = (SbiMetaTableColumn) tmpSession.load(SbiMetaTableColumn.class, new Integer(aMetaTableColumn.getColumnId()));

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
	public SbiMetaTableColumn loadTableColumnByID(Session session, Integer id) throws EMFUserError {
		logger.debug("IN");

		SbiMetaTableColumn toReturn = null;
		Session tmpSession = session;

		try {
			toReturn = (SbiMetaTableColumn) tmpSession.load(SbiMetaTableColumn.class, id);
		} catch (HibernateException he) {
			logException(he);
			throw new HibernateException(he);
		} finally {
			logger.debug("OUT");
		}
		return toReturn;
	}

	@Override
	public SbiMetaTableColumn loadTableColumnByName(Session session, String name) throws EMFUserError {
		logger.debug("IN");

		SbiMetaTableColumn toReturn = null;
		Session tmpSession = session;
		try {
			Criterion labelCriterrion = Expression.eq("name", name);
			Criteria criteria = tmpSession.createCriteria(SbiMetaTableColumn.class);
			criteria.add(labelCriterrion);
			toReturn = (SbiMetaTableColumn) criteria.uniqueResult();
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

	/**
	 * Load all tablecolumn column linked to a table.
	 *
	 * @param session
	 *            the session
	 *
	 * @return List of meta tables
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaTableColumnDAOHibImpl#loadTableColumnsFromTable(session, tableId)
	 */
	@Override
	public List<SbiMetaTableColumn> loadTableColumnsFromTable(Session session, int tableId) throws EMFUserError {
		logger.debug("IN");

		List<SbiMetaTableColumn> toReturn = null;

		try {
			Criterion labelCriterrion = Expression.eq("sbiMetaTable.tableId", tableId);
			Criteria criteria = session.createCriteria(SbiMetaTableColumn.class);
			criteria.add(labelCriterrion);

			toReturn = criteria.list();

		} catch (HibernateException he) {
			logException(he);
			throw new HibernateException(he);
		} finally {
			logger.debug("OUT");
		}
		return toReturn;
	}

	@Override
	public SbiMetaTableColumn loadTableColumnByNameAndTable(Session session, String name, Integer tableId) throws EMFUserError {
		logger.debug("IN");

		SbiMetaTableColumn toReturn = null;
		Session tmpSession = session;

		try {
			String hql = " from SbiMetaTableColumn c where c.name = ? and c.sbiMetaTable.tableId = ? ";
			Query aQuery = tmpSession.createQuery(hql);
			aQuery.setString(0, name);
			aQuery.setInteger(1, tableId);
			toReturn = (SbiMetaTableColumn) aQuery.uniqueResult();

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
	public void modifyTableColumn(Session session, SbiMetaTableColumn aMetaTableColumn) throws EMFUserError {
		logger.debug("IN");

		Session tmpSession = session;

		try {
			SbiMetaTableColumn hibMeta = (SbiMetaTableColumn) tmpSession.load(SbiMetaTableColumn.class, aMetaTableColumn.getColumnId());

			hibMeta.setName(aMetaTableColumn.getName());
			hibMeta.setType(aMetaTableColumn.getType());
			hibMeta.setDeleted(aMetaTableColumn.isDeleted());

			SbiMetaTable metaTable = null;
			if (aMetaTableColumn.getSbiMetaTable().getTableId() < 0) {
				Criterion aCriterion = Expression.eq("valueId", aMetaTableColumn.getSbiMetaTable().getTableId());
				Criteria criteria = tmpSession.createCriteria(SbiMetaSource.class);
				criteria.add(aCriterion);
				metaTable = (SbiMetaTable) criteria.uniqueResult();
				if (metaTable == null) {
					throw new SpagoBIDOAException("The SbiMetaTable with id= " + aMetaTableColumn.getSbiMetaTable().getTableId() + " does not exist");
				}
				hibMeta.setSbiMetaTable(metaTable);
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
	public Integer insertTableColumn(Session session, SbiMetaTableColumn aMetaTableColumn) throws EMFUserError {
		logger.debug("IN");

		Session tmpSession = session;
		Integer idToReturn = null;

		try {
			SbiMetaTableColumn hibMeta = new SbiMetaTableColumn();
			hibMeta.setName(aMetaTableColumn.getName());
			hibMeta.setType(aMetaTableColumn.getType());
			hibMeta.setDeleted(aMetaTableColumn.isDeleted());

			SbiMetaTable metaTable = null;
			if (aMetaTableColumn.getSbiMetaTable() != null) {
				Criterion aCriterion = Expression.eq("tableId", aMetaTableColumn.getSbiMetaTable().getTableId());
				Criteria criteria = tmpSession.createCriteria(SbiMetaTable.class);
				criteria.add(aCriterion);
				metaTable = (SbiMetaTable) criteria.uniqueResult();
				if (metaTable == null) {
					throw new SpagoBIDOAException("The SbiMetaTable with id= " + aMetaTableColumn.getSbiMetaTable().getTableId() + " does not exist");
				}
				hibMeta.setSbiMetaTable(metaTable);
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

}