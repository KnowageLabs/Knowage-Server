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
import it.eng.spagobi.metadata.metadata.SbiMetaBc;
import it.eng.spagobi.metadata.metadata.SbiMetaBcAttribute;
import it.eng.spagobi.metadata.metadata.SbiMetaSource;
import it.eng.spagobi.metadata.metadata.SbiMetaTable;
import it.eng.spagobi.tools.catalogue.metadata.SbiMetaModel;

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
public class SbiMetaBcDAOHibImpl extends AbstractHibernateDAO implements ISbiMetaBCDAO {

	static private Logger logger = Logger.getLogger(SbiMetaBcDAOHibImpl.class);

	/**
	 * Load BC by id.
	 *
	 * @param id
	 *            the bc is
	 *
	 * @return the meta bc
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaBcDAOHibImpl#loadBcByID(integer)
	 */
	@Override
	public SbiMetaBc loadBcByID(Integer id) throws EMFUserError {
		logger.debug("IN");

		SbiMetaBc toReturn = null;
		Session tmpSession = null;
		Transaction tx = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			toReturn = (SbiMetaBc) tmpSession.load(SbiMetaBc.class, id);
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
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaBcDAOHibImpl#loadBcByName(string)
	 */
	@Override
	public SbiMetaBc loadBcByName(String name) throws EMFUserError {
		logger.debug("IN");

		SbiMetaBc toReturn = null;
		Session tmpSession = null;
		Transaction tx = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			toReturn = loadBcByName(tmpSession, name);
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
	 * Load all BCs.
	 *
	 * @return List of meta bc
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaBcDAOHibImpl#loadAllBCs()
	 */
	@Override
	public List<SbiMetaBc> loadAllBCs() throws EMFUserError {
		logger.debug("IN");

		Session tmpSession = null;
		Transaction tx = null;
		List<SbiMetaBc> toReturn = new ArrayList();
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			Query hibQuery = tmpSession.createQuery(" from SbiMetaBc");

			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();
			while (it.hasNext()) {
				SbiMetaBc hibMeta = (SbiMetaBc) it.next();
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
	public List<SbiMetaBc> loadAllBCFromTable(int tableId) throws EMFUserError {
		logger.debug("IN");

		List toReturn = null;
		Session tmpSession = null;
		Transaction tx = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			Criterion labelCriterrion = Expression.eq("tableId", tableId);
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

	@Override
	public List<SbiMetaBc> loadAllBCFromModel(int modelId) throws EMFUserError {
		logger.debug("IN");

		List toReturn = null;
		Session tmpSession = null;
		Transaction tx = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			Criterion labelCriterrion = Expression.eq("id", modelId);
			Criteria criteria = tmpSession.createCriteria(SbiMetaModel.class);
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
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaBcDAOHibImpl#modifyTable(SbiMetaTable)
	 */
	@Override
	public void modifyBc(SbiMetaBc aMetaBc) throws EMFUserError {
		logger.debug("IN");

		Session tmpSession = null;
		Transaction tx = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			modifyBc(tmpSession, aMetaBc);
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
	 * Insert a metabc.
	 *
	 * @param aMetaSource
	 *            the sbimetabc to insert
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaBcDAOHibImpl#insertBc(SbiMetaBc)
	 */
	@Override
	public Integer insertBc(SbiMetaBc aMetaBc) throws EMFUserError {
		logger.debug("IN");

		Session tmpSession = null;
		Transaction tx = null;
		Integer idToReturn = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			idToReturn = insertBc(tmpSession, aMetaBc);
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
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaBcDAOHibImpl#deleteSource(SbiMetaSource)
	 */
	@Override
	public void deleteBc(SbiMetaBc aMetaBc) throws EMFUserError {
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

			SbiMetaBc hibMeta = (SbiMetaBc) tmpSession.load(SbiMetaBc.class, new Integer(aMetaBc.getBcId()));

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
	 * Checks for tables associated.
	 *
	 * @param bcId
	 *            the BC id
	 *
	 * @return true, if checks for tables associated
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaBcDAOHibImpl#hasTablesAssociated(int)
	 */
	@Override
	public boolean hasTablesAssociated(Integer id) throws EMFUserError {
		boolean bool = false;

		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			String hql = " from SbiMetaTableBc s where s.id.bcId = ?";
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

	/**
	 * Checks for dataset associated.
	 *
	 * @param bcId
	 *            the BC id
	 *
	 * @return true, if checks for datasets associated
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaBcDAOHibImpl#hasDsAssociated(int)
	 */
	@Override
	public boolean hasDsAssociated(Integer id) throws EMFUserError {
		boolean bool = false;

		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			String hql = " from SbiMetaDsBc s where s.id.bcId = ?";
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

	/**
	 * Load source by name.
	 *
	 * @param session
	 *            the session
	 *
	 * @param name
	 *            the source name
	 *
	 * @return the meta source
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaBcDAOHibImpl#loadBcByName(session, string)
	 */
	@Override
	public SbiMetaBc loadBcByName(Session session, String name) throws EMFUserError {
		logger.debug("IN");

		SbiMetaBc toReturn = null;

		try {
			Criterion labelCriterrion = Expression.eq("name", name);
			Criteria criteria = session.createCriteria(SbiMetaBc.class);
			criteria.add(labelCriterrion);
			toReturn = (SbiMetaBc) criteria.uniqueResult();
		} catch (HibernateException he) {
			logException(he);
			throw new HibernateException(he);
		} finally {
			logger.debug("OUT");
		}
		return toReturn;
	}

	/**
	 * Modify a metaBC.
	 *
	 * * @param aSession the hibernate session
	 *
	 * @param aMetaBC
	 *            the sbimetaBC changed
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaBcDAOHibImpl#modifyBc(Session, SbiMetaTable)
	 */
	@Override
	public void modifyBc(Session aSession, SbiMetaBc aMetaBc) throws EMFUserError {
		logger.debug("IN");

		try {
			SbiMetaBc hibMeta = (SbiMetaBc) aSession.load(SbiMetaBc.class, aMetaBc.getBcId());

			hibMeta.setName(aMetaBc.getName());
			hibMeta.setDeleted(aMetaBc.isDeleted());

			SbiMetaModel metaModel = null;
			if (aMetaBc.getSbiMetaModel() != null) {
				Criterion aCriterion = Expression.eq("id", aMetaBc.getSbiMetaModel().getId());
				Criteria criteria = aSession.createCriteria(SbiMetaModel.class);
				criteria.add(aCriterion);
				metaModel = (SbiMetaModel) criteria.uniqueResult();
				if (metaModel == null) {
					throw new SpagoBIDOAException("The sbiMetaModel with id= " + aMetaBc.getSbiMetaModel().getId() + " does not exist");
				}
				hibMeta.setSbiMetaModel(metaModel);
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
	 * Insert a metaBC.
	 *
	 * * @param aSession the hibernate session
	 *
	 * @param aMetaBC
	 *            the sbimetaBC to insert
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaBcDAOHibImpl#insertBc(Session, SbiMetaTable)
	 */
	@Override
	public Integer insertBc(Session session, SbiMetaBc aMetaBc) throws EMFUserError {
		logger.debug("IN");

		Integer idToReturn = null;

		try {
			SbiMetaBc hibMeta = new SbiMetaBc();
			hibMeta.setName(aMetaBc.getName());
			hibMeta.setDeleted(aMetaBc.isDeleted());

			SbiMetaModel metaModel = null;
			if (aMetaBc.getSbiMetaModel() != null) {
				Criterion aCriterion = Expression.eq("id", aMetaBc.getSbiMetaModel().getId());
				Criteria criteria = session.createCriteria(SbiMetaModel.class);
				criteria.add(aCriterion);
				metaModel = (SbiMetaModel) criteria.uniqueResult();
				if (metaModel == null) {
					throw new SpagoBIDOAException("The sbiMetaModel with id= " + aMetaBc.getSbiMetaModel().getId() + " does not exist");
				}
				hibMeta.setSbiMetaModel(metaModel);
			}

			updateSbiCommonInfo4Insert(hibMeta);
			idToReturn = (Integer) session.save(hibMeta);

		} catch (HibernateException he) {
			logException(he);
			throw new HibernateException(he);
		} finally {
			logger.debug("OUT");
		}
		return idToReturn;
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
	public List<SbiMetaBc> loadPaginatedMetaBC(Integer page, Integer item_per_page, String search) throws EMFUserError {
		logger.debug("IN");

		Session tmpSession = null;
		Transaction tx = null;
		List<SbiMetaBc> toReturn = new ArrayList();
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			Criteria c = tmpSession.createCriteria(SbiMetaBc.class);
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
	public Integer countSbiMetaBC(String search) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		Integer resultNumber;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			String hql = "select count(*) from SbiMetaBc where name like '%" + search + "%'";
			Query hqlQuery = aSession.createQuery(hql);
			Long temp = (Long) hqlQuery.uniqueResult();
			resultNumber = new Integer(temp.intValue());

		} catch (HibernateException he) {
			logger.error("Error while loading the list of SbiMetaBc", he);
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

	/**
	 * Load BC by id.
	 *
	 * @param id
	 *            the bc is
	 *
	 * @return the meta bc
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaBcDAOHibImpl#loadBcByID(integer)
	 */
	@Override
	public SbiMetaBc loadBcWithAttributesByID(Integer id) throws EMFUserError {
		logger.debug("IN");

		SbiMetaBc tmpBC = null;
		Session tmpSession = null;
		Transaction tx = null;
		SbiMetaBc smbc = new SbiMetaBc();

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			tmpBC = (SbiMetaBc) tmpSession.load(SbiMetaBc.class, id);
			smbc.setBcId(tmpBC.getBcId());
			smbc.setName(tmpBC.getName());

			Set<SbiMetaBcAttribute> smtc = new HashSet<SbiMetaBcAttribute>();
			for (Iterator<SbiMetaBcAttribute> iterator = tmpBC.getSbiMetaBcAttributes().iterator(); iterator.hasNext();) {
				SbiMetaBcAttribute smc = iterator.next();
				SbiMetaBcAttribute tmp = new SbiMetaBcAttribute();
				tmp.setAttributeId(smc.getAttributeId());
				tmp.setName(smc.getName());
				tmp.setType(smc.getType());
				smtc.add(tmp);
			}

			smbc.setSbiMetaBcAttributes(smtc);
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
		return smbc;
	}

}