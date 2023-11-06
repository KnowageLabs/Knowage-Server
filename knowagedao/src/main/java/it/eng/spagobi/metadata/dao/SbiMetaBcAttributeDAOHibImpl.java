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
import org.hibernate.criterion.Restrictions;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.SpagoBIDAOException;
import it.eng.spagobi.metadata.metadata.SbiMetaBc;
import it.eng.spagobi.metadata.metadata.SbiMetaBcAttribute;
import it.eng.spagobi.metadata.metadata.SbiMetaTable;
import it.eng.spagobi.metadata.metadata.SbiMetaTableColumn;

/**
 * @author Antonella Giachino (antonella.giachino@eng.it)
 *
 */
public class SbiMetaBcAttributeDAOHibImpl extends AbstractHibernateDAO implements ISbiMetaBCAttributeDAO {

	private static Logger logger = Logger.getLogger(SbiMetaBcAttributeDAOHibImpl.class);

	/**
	 * Load BCAttribute by id.
	 *
	 * @param id the bcattribute is
	 *
	 * @return the meta bcattribute
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaBcAttributeDAOHibImpl#loadBcAttributeByID(integer)
	 */
	@Override
	public SbiMetaBcAttribute loadBcAttributeByID(Integer id) throws EMFUserError {
		logger.debug("IN");

		SbiMetaBcAttribute toReturn = null;
		Session tmpSession = null;
		Transaction tx = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			toReturn = loadBcAttributeByID(tmpSession, id);
			tx.commit();

		} catch (HibernateException he) {
			logException(he);
			rollbackIfActive(tx);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(tmpSession);
		}
		logger.debug("OUT");
		return toReturn;
	}

	/**
	 * Load BCAttribute by name.
	 *
	 * @param name the BCAttribute name
	 *
	 * @return the meta BCAttribute
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaBcAttributeDAOHibImpl#loadBcAttributeByName(string)
	 */
	@Override
	public SbiMetaBcAttribute loadBcAttributeByName(String name) throws EMFUserError {
		logger.debug("IN");

		SbiMetaBcAttribute toReturn = null;
		Session tmpSession = null;
		Transaction tx = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			Criterion labelCriterrion = Restrictions.eq("name", name);
			Criteria criteria = tmpSession.createCriteria(SbiMetaBcAttribute.class);
			criteria.add(labelCriterrion);
			toReturn = (SbiMetaBcAttribute) criteria.uniqueResult();
			if (toReturn == null)
				return null;
			tx.commit();

		} catch (HibernateException he) {
			logException(he);
			rollbackIfActive(tx);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(tmpSession);
		}

		logger.debug("OUT");
		return toReturn;
	}

	/**
	 * Load all BCAttributes.
	 *
	 * @return List of meta bcAttributes
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaBcAttributeDAOHibImpl#loadAllBCAttributes()
	 */
	@Override
	public List<SbiMetaBcAttribute> loadAllBCAttributes() throws EMFUserError {
		logger.debug("IN");

		Session tmpSession = null;
		Transaction tx = null;
		List<SbiMetaBcAttribute> toReturn = new ArrayList();
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			Query hibQuery = tmpSession.createQuery(" from SbiMetaBcAttribute");

			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();
			while (it.hasNext()) {
				SbiMetaBcAttribute hibMeta = (SbiMetaBcAttribute) it.next();
				toReturn.add(hibMeta);
			}
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			rollbackIfActive(tx);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(tmpSession);
		}
		logger.debug("OUT");
		return toReturn;
	}

	@Override
	public List<SbiMetaBcAttribute> loadAllBCAttributeFromBC(int bcId) throws EMFUserError {
		logger.debug("IN");

		List toReturn = null;
		Session tmpSession = null;
		Transaction tx = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			Criterion labelCriterrion = Restrictions.eq("bcId", bcId);
			Criteria criteria = tmpSession.createCriteria(SbiMetaBcAttribute.class);
			criteria.add(labelCriterrion);

			toReturn = criteria.list();
			if (toReturn == null)
				return null;
			tx.commit();

		} catch (HibernateException he) {
			logException(he);
			rollbackIfActive(tx);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(tmpSession);
		}

		logger.debug("OUT");
		return toReturn;
	}

	@Override
	public List<SbiMetaBcAttribute> loadAllBCAttributeFromTableColumn(int columnId) throws EMFUserError {
		logger.debug("IN");

		List toReturn = null;
		Session tmpSession = null;
		Transaction tx = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			Criterion labelCriterrion = Restrictions.eq("columnId", columnId);
			Criteria criteria = tmpSession.createCriteria(SbiMetaTableColumn.class);
			criteria.add(labelCriterrion);

			toReturn = criteria.list();
			if (toReturn == null)
				return null;
			tx.commit();

		} catch (HibernateException he) {
			logException(he);
			rollbackIfActive(tx);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(tmpSession);
		}

		logger.debug("OUT");
		return toReturn;
	}

	/**
	 * Modify a metatable.
	 *
	 * @param aMetaTable the sbimetatable changed
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaBcDAOHibImpl#modifyTable(SbiMetaTable)
	 */
	@Override
	public void modifyBcAttribute(SbiMetaBcAttribute aMetaBcAttribute) throws EMFUserError {
		logger.debug("IN");

		Session tmpSession = null;
		Transaction tx = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			modifyBcAttribute(tmpSession, aMetaBcAttribute);

			tx.commit();

		} catch (HibernateException he) {
			logException(he);
			rollbackIfActive(tx);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(tmpSession);
		}
		logger.debug("OUT");

	}

	/**
	 * Insert a metabc.
	 *
	 * @param aMetaSource the sbimetabc to insert
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaBcDAOHibImpl#insertBc(SbiMetaBc)
	 */
	@Override
	public Integer insertBcAttribute(SbiMetaBcAttribute aMetaBcAttribute) throws EMFUserError {
		logger.debug("IN");

		Session tmpSession = null;
		Transaction tx = null;
		Integer idToReturn = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			idToReturn = insertBcAttribute(tmpSession, aMetaBcAttribute);
			tx.commit();

		} catch (HibernateException he) {
			logException(he);
			rollbackIfActive(tx);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(tmpSession);
		}
		logger.debug("OUT");
		return idToReturn;
	}

	/**
	 * Delete a BCAttribute.
	 *
	 * @param aMetaBcAttribute the sbibcattribute to delete
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaBcAttributeDAOHibImpl#deleteBcAttribute(SbiMetaBc)
	 */
	@Override
	public void deleteBcAttribute(SbiMetaBcAttribute aMetaBCAttribute) throws EMFUserError {
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

			SbiMetaBcAttribute hibMeta = (SbiMetaBcAttribute) tmpSession.load(SbiMetaBcAttribute.class,
					new Integer(aMetaBCAttribute.getAttributeId()));

			tmpSession.delete(hibMeta);

			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			rollbackIfActive(tx);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(tmpSession);
		}
		logger.debug("OUT");

	}

	@Override
	public SbiMetaBcAttribute loadBcAttributeByID(Session session, Integer id) throws HibernateException {
		logger.debug("IN");

		SbiMetaBcAttribute toReturn = null;
		Session tmpSession = session;

		try {
			toReturn = (SbiMetaBcAttribute) tmpSession.load(SbiMetaBcAttribute.class, id);
		} catch (HibernateException he) {
			logException(he);
			throw new HibernateException(he);
		} finally {
			logger.debug("OUT");
		}

		return toReturn;
	}

	@Override
	public SbiMetaBcAttribute loadBcAttributeByNameAndBc(Session session, String name, Integer bcId)
			throws EMFUserError {
		logger.debug("IN");

		SbiMetaBcAttribute toReturn = null;

		try {

			Query hqlQuery = session
					.createQuery(" from SbiMetaBcAttribute as db where db.name = ? and db.sbiMetaBc.bcId = ? ");
			hqlQuery.setString(0, name);
			hqlQuery.setInteger(1, bcId);
			toReturn = (SbiMetaBcAttribute) hqlQuery.uniqueResult();

		} catch (HibernateException he) {
			logException(he);
			throw new HibernateException(he);
		} finally {
			logger.debug("OUT");
		}

		return toReturn;
	}

	/**
	 * Modify a metaBCAttribute.
	 *
	 * * @param aSession the hibernate session
	 *
	 * @param aMetaBC the sbimetaBCAttribute changed
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaBcAttributeDAOHibImpl#modifyBcAttribute(Session, SbiMetaTable)
	 */
	@Override
	public void modifyBcAttribute(Session session, SbiMetaBcAttribute aMetaBcAttribute) throws EMFUserError {
		logger.debug("IN");

		Session tmpSession = session;

		try {

			SbiMetaBcAttribute hibMeta = (SbiMetaBcAttribute) tmpSession.load(SbiMetaBcAttribute.class,
					aMetaBcAttribute.getAttributeId());

			hibMeta.setName(aMetaBcAttribute.getName());
			hibMeta.setType(aMetaBcAttribute.getType());
			hibMeta.setDeleted(aMetaBcAttribute.isDeleted());

			SbiMetaBc metaBc = null;
			if (aMetaBcAttribute.getSbiMetaBc() != null) {
				Criterion aCriterion = Restrictions.eq("bcId", aMetaBcAttribute.getSbiMetaBc().getBcId());
				Criteria criteria = tmpSession.createCriteria(SbiMetaBc.class);
				criteria.add(aCriterion);
				metaBc = (SbiMetaBc) criteria.uniqueResult();
				if (metaBc == null) {
					throw new SpagoBIDAOException(
							"The sbiMetaBc with id= " + aMetaBcAttribute.getSbiMetaBc().getBcId() + " does not exist");
				}
				hibMeta.setSbiMetaBc(metaBc);
			}

			SbiMetaTableColumn metaTableColumn = null;
			if (aMetaBcAttribute.getSbiMetaTableColumn() != null) {
				Criterion aCriterion = Restrictions.eq("columnId",
						aMetaBcAttribute.getSbiMetaTableColumn().getColumnId());
				Criteria criteria = tmpSession.createCriteria(SbiMetaTableColumn.class);
				criteria.add(aCriterion);
				metaTableColumn = (SbiMetaTableColumn) criteria.uniqueResult();
				if (metaTableColumn == null) {
					throw new SpagoBIDAOException("The SbiMetaTableColumn with id= "
							+ aMetaBcAttribute.getSbiMetaTableColumn().getColumnId() + " does not exist");
				}
				hibMeta.setSbiMetaTableColumn(metaTableColumn);
			}

			updateSbiCommonInfo4Update(hibMeta);

		} catch (HibernateException he) {
			logException(he);
			throw new HibernateException(he);
		} finally {
			logger.debug("OUT");
		}

	}

	/**
	 * Insert a metaBCAttribute.
	 *
	 * * @param aSession the hibernate session
	 *
	 * @param aMetaBCAttribute the sbimetaBCAttribute to insert
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaBcAttributeDAOHibImpl#insertBcAttribute(Session, SbiMetaTable)
	 */
	@Override
	public Integer insertBcAttribute(Session session, SbiMetaBcAttribute aMetaBcAttribute) throws EMFUserError {
		logger.debug("IN");

		Session tmpSession = session;
		Integer idToReturn = null;

		try {
			SbiMetaBcAttribute hibMeta = new SbiMetaBcAttribute();
			hibMeta.setName(aMetaBcAttribute.getName());
			hibMeta.setType(aMetaBcAttribute.getType());
			hibMeta.setDeleted(aMetaBcAttribute.isDeleted());

			SbiMetaBc metaBc = null;
			if (aMetaBcAttribute.getSbiMetaBc() != null) {
				Criterion aCriterion = Restrictions.eq("bcId", aMetaBcAttribute.getSbiMetaBc().getBcId());
				Criteria criteria = tmpSession.createCriteria(SbiMetaBc.class);
				criteria.add(aCriterion);
				metaBc = (SbiMetaBc) criteria.uniqueResult();
				if (metaBc == null) {
					throw new SpagoBIDAOException(
							"The sbiMetaBc with id= " + aMetaBcAttribute.getSbiMetaBc().getBcId() + " does not exist");
				}
				hibMeta.setSbiMetaBc(metaBc);
			}

			SbiMetaTableColumn metaTableColumn = null;
			if (aMetaBcAttribute.getSbiMetaTableColumn() != null) {
				Criterion aCriterion = Restrictions.eq("columnId",
						aMetaBcAttribute.getSbiMetaTableColumn().getColumnId());
				Criteria criteria = tmpSession.createCriteria(SbiMetaTableColumn.class);
				criteria.add(aCriterion);
				metaTableColumn = (SbiMetaTableColumn) criteria.uniqueResult();
				if (metaTableColumn == null) {
					throw new SpagoBIDAOException("The SbiMetaTableColumn with id= "
							+ aMetaBcAttribute.getSbiMetaTableColumn().getColumnId() + " does not exist");
				}
				hibMeta.setSbiMetaTableColumn(metaTableColumn);
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