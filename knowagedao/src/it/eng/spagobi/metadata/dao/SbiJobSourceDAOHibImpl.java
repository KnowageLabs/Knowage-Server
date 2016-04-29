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
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.metadata.metadata.SbiMetaJob;
import it.eng.spagobi.metadata.metadata.SbiMetaJobSource;
import it.eng.spagobi.metadata.metadata.SbiMetaJobSourceId;
import it.eng.spagobi.metadata.metadata.SbiMetaSource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * @author Antonella Giachino (antonella.giachino@eng.it)
 *
 */
public class SbiJobSourceDAOHibImpl extends AbstractHibernateDAO implements ISbiJobSourceDAO {

	static private Logger logger = Logger.getLogger(SbiDsBcDAOHibImpl.class);

	@Override
	public List<SbiMetaJob> loadJobsBySourceId(Integer sourceId) throws EMFUserError {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		List<SbiMetaJob> toReturn = new ArrayList();
		Query hqlQuery = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			hqlQuery = aSession.createQuery(" from SbiMetaJobSource as db where db.id.sourceId = ? ");
			hqlQuery.setInteger(0, sourceId);
			List hibList = hqlQuery.list();

			Iterator it = hibList.iterator();
			while (it.hasNext()) {
				SbiMetaJobSource tmpRel = (SbiMetaJobSource) it.next();
				SbiMetaJob tmpJob = DAOFactory.getSbiMetaJobDAO().loadJobByID(new Integer(tmpRel.getId().getJobId()));

				if (tmpJob != null)
					toReturn.add(tmpJob);
			}
			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		logger.debug("OUT");
		return toReturn;
	}

	@Override
	public List<SbiMetaSource> loadSourcesByJobId(Integer jobId) throws EMFUserError {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		List<SbiMetaSource> toReturn = new ArrayList();
		Query hqlQuery = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			hqlQuery = aSession.createQuery(" from SbiMetaJobSource as db where db.id.jobId = ? ");
			hqlQuery.setInteger(0, jobId);
			List hibList = hqlQuery.list();

			Iterator it = hibList.iterator();
			while (it.hasNext()) {
				SbiMetaJobSource tmpRel = (SbiMetaJobSource) it.next();
				SbiMetaSource tmpJob = DAOFactory.getSbiMetaSourceDAO().loadSourceByID(new Integer(tmpRel.getId().getSourceId()));

				if (tmpJob != null)
					toReturn.add(tmpJob);
			}
			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		logger.debug("OUT");
		return toReturn;
	}

	@Override
	public SbiMetaJobSource loadJobSource(Session session, Integer jobId, Integer sourceId) throws EMFUserError {
		logger.debug("IN");

		Session aSession = session;
		SbiMetaJobSource toReturn = new SbiMetaJobSource();
		Query hqlQuery = null;

		try {
			hqlQuery = aSession.createQuery(" from SbiMetaJobSource as db where db.id.jobId = ? and db.id.sourceId = ? ");
			hqlQuery.setInteger(0, jobId);
			hqlQuery.setInteger(1, sourceId);

			toReturn = (SbiMetaJobSource) hqlQuery.uniqueResult();

		} catch (HibernateException he) {
			logException(he);

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			logger.debug("OUT");
		}

		return toReturn;
	}

	@Override
	public void modifyJobSource(SbiMetaJobSource aMetaJobSource) throws EMFUserError {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiMetaJobSourceId hibId = new SbiMetaJobSourceId();
			hibId.setJobId(aMetaJobSource.getId().getJobId());
			hibId.setSourceId(aMetaJobSource.getId().getSourceId());

			updateSbiCommonInfo4Update(hibId);
			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}

		}
		logger.debug("OUT");
	}

	@Override
	public void insertJobSource(SbiMetaJobSource aMetaJobSource) throws EMFUserError {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			insertJobSource(aSession, aMetaJobSource);

			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		logger.debug("OUT");
	}

	@Override
	public void insertJobSource(Session session, SbiMetaJobSource aMetaJobSource) throws EMFUserError {
		logger.debug("IN");

		Session aSession = null;

		try {
			aSession = session;
			SbiMetaJobSourceId hibId = new SbiMetaJobSourceId();
			hibId.setJobId(aMetaJobSource.getSbiMetaJob().getJobId());
			hibId.setSourceId(aMetaJobSource.getSbiMetaSource().getSourceId());
			aMetaJobSource.setId(hibId);

			updateSbiCommonInfo4Insert(aMetaJobSource);
			aSession.save(aMetaJobSource);
		} catch (HibernateException he) {
			logException(he);

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			logger.debug("OUT");
		}
	}

	@Override
	public void deleteJobSource(SbiMetaJobSource aMetaJobSource) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiMetaJobSourceId hibId = new SbiMetaJobSourceId();
			hibId.setJobId(aMetaJobSource.getId().getJobId());
			hibId.setSourceId(aMetaJobSource.getId().getSourceId());

			SbiMetaJobSource hib = (SbiMetaJobSource) aSession.load(SbiMetaJobSource.class, hibId);

			aSession.delete(hib);
			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}

	}

}