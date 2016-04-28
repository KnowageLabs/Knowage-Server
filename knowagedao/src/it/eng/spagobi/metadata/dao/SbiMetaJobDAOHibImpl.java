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
import it.eng.spagobi.metadata.metadata.SbiMetaJob;

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
public class SbiMetaJobDAOHibImpl extends AbstractHibernateDAO implements ISbiMetaJobDAO {

	static private Logger logger = Logger.getLogger(SbiMetaJobDAOHibImpl.class);

	/**
	 * Load job by id.
	 *
	 * @param id
	 *            the job id
	 *
	 * @return the meta job
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaJobDAOHibImpl#loadJobByID(integer)
	 */
	@Override
	public SbiMetaJob loadJobByID(Integer id) throws EMFUserError {
		logger.debug("IN");

		SbiMetaJob toReturn = null;
		Session tmpSession = null;
		Transaction tx = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			toReturn = (SbiMetaJob) tmpSession.load(SbiMetaJob.class, id);
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
	 * Load job by name.
	 *
	 * @param name
	 *            the job name
	 *
	 * @return the meta job
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaJobDAOHibImpl#loadJobByName(string)
	 */
	@Override
	public SbiMetaJob loadJobByName(String name) throws EMFUserError {
		logger.debug("IN");

		SbiMetaJob toReturn = null;
		Session tmpSession = null;
		Transaction tx = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			toReturn = loadJobByName(tmpSession, name);
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

		return toReturn;
	}

	@Override
	public SbiMetaJob loadJobByName(Session session, String name) throws EMFUserError {
		logger.debug("IN");

		SbiMetaJob toReturn = null;
		Session tmpSession = session;

		try {
			tmpSession = getSession();
			Criterion labelCriterrion = Expression.eq("name", name);
			Criteria criteria = tmpSession.createCriteria(SbiMetaJob.class);
			criteria.add(labelCriterrion);
			toReturn = (SbiMetaJob) criteria.uniqueResult();
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

	/**
	 * Load all jobs.
	 *
	 * @return List of meta jobs
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaJobDAOHibImpl#loadAllJobs()
	 */
	@Override
	public List<SbiMetaJob> loadAllJobs() throws EMFUserError {
		logger.debug("IN");

		Session tmpSession = null;
		Transaction tx = null;
		List<SbiMetaJob> toReturn = new ArrayList();
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			Query hibQuery = tmpSession.createQuery(" from SbiMetaJob");

			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();
			while (it.hasNext()) {
				SbiMetaJob hibMeta = (SbiMetaJob) it.next();
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
	 * Modify a metajob.
	 *
	 * @param aMetaJob
	 *            the sbimetajob changed
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaJobDAOHibImpl#modifyJob(SbiMetaJob)
	 */
	@Override
	public void modifyJob(SbiMetaJob aMetaJob) throws EMFUserError {
		logger.debug("IN");

		Session tmpSession = null;
		Transaction tx = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			modifyJob(tmpSession, aMetaJob);
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
	public void modifyJob(Session session, SbiMetaJob aMetaJob) throws EMFUserError {
		logger.debug("IN");

		Session tmpSession = session;

		try {

			SbiMetaJob hibMeta = (SbiMetaJob) tmpSession.load(SbiMetaJob.class, aMetaJob.getJobId());

			hibMeta.setName(aMetaJob.getName());
			hibMeta.setDeleted(aMetaJob.isDeleted());

			updateSbiCommonInfo4Update(hibMeta);

		} catch (HibernateException he) {
			logException(he);

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			logger.debug("OUT");
		}

	}

	/**
	 * Insert a metajob.
	 *
	 * @param aMetaSource
	 *            the sbimetajob to insert
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaJobDAOHibImpl#insertJob(SbiMetaJob)
	 */
	@Override
	public Integer insertJob(SbiMetaJob aMetaJob) throws EMFUserError {
		logger.debug("IN");

		Session tmpSession = null;
		Transaction tx = null;
		Integer idToReturn = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			idToReturn = insertJob(tmpSession, aMetaJob);
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

	@Override
	public Integer insertJob(Session session, SbiMetaJob aMetaJob) throws EMFUserError {
		logger.debug("IN");

		Session tmpSession = null;

		Integer idToReturn = null;

		try {
			tmpSession = session;

			SbiMetaJob hibMeta = new SbiMetaJob();
			hibMeta.setName(aMetaJob.getName());
			hibMeta.setDeleted(aMetaJob.isDeleted());

			updateSbiCommonInfo4Insert(hibMeta);
			idToReturn = (Integer) tmpSession.save(hibMeta);

		} catch (HibernateException he) {
			logException(he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			logger.debug("OUT");
		}
		return idToReturn;
	}

	/**
	 * Delete a metajob.
	 *
	 * @param aMetaJob
	 *            the sbimetajob to delete
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaJobDAOHibImpl#deleteJob(SbiMetaJob)
	 */
	@Override
	public void deleteJob(SbiMetaJob aMetaJob) throws EMFUserError {
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

			SbiMetaJob hibMeta = (SbiMetaJob) tmpSession.load(SbiMetaJob.class, new Integer(aMetaJob.getJobId()));

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
	 * Checks for sources associated.
	 *
	 * @param sourceId
	 *            the metasource id
	 *
	 * @return true, if checks for sources associated
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaJobDAOHibImpl#hasSourcesAssociated(int)
	 */
	@Override
	public boolean hasSourcesAssociated(Integer id) throws EMFUserError {
		boolean bool = false;

		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			String hql = " from SbiMetaJobSource s where s.id.sourceId = ?";
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
	 * Checks for tables associated.
	 *
	 * @param sourceId
	 *            the metatable id
	 *
	 * @return true, if checks for tables associated
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.metadata.dao.ISbiMetaJobDAOHibImpl#hasTablesAssociated(int)
	 */
	@Override
	public boolean hasTablesAssociated(Integer id) throws EMFUserError {
		boolean bool = false;

		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			String hql = " from SbiMetaJobSource s where s.id.jobId = ?";
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