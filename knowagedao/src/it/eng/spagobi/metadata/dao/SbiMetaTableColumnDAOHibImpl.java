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
			toReturn = (SbiMetaTableColumn) tmpSession.load(SbiMetaTableColumn.class, id);
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
			Criterion labelCriterrion = Expression.eq("name", name);
			Criteria criteria = tmpSession.createCriteria(SbiMetaTableColumn.class);
			criteria.add(labelCriterrion);
			toReturn = (SbiMetaTableColumn) criteria.uniqueResult();
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

			Criterion labelCriterrion = Expression.eq("tableId", tableId);
			Criteria criteria = tmpSession.createCriteria(SbiMetaTableColumn.class);
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
	public void insertTableColumn(SbiMetaTableColumn aMetaTableColumn) throws EMFUserError {
		logger.debug("IN");

		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			SbiMetaTableColumn hibMeta = new SbiMetaTableColumn();
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

}