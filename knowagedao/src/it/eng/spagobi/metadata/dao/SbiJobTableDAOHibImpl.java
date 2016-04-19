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
import it.eng.spagobi.metadata.metadata.SbiMetaJobTable;
import it.eng.spagobi.metadata.metadata.SbiMetaJobTableId;
import it.eng.spagobi.metadata.metadata.SbiMetaTable;

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
public class SbiJobTableDAOHibImpl extends AbstractHibernateDAO implements ISbiJobTableDAO {

	static private Logger logger = Logger.getLogger(SbiDsBcDAOHibImpl.class);

	@Override
	public List<SbiMetaJob> loadJobsByTableId(Integer tableId) throws EMFUserError {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		List<SbiMetaJob> toReturn = new ArrayList();
		Query hqlQuery = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			hqlQuery = aSession.createQuery(" from SbiMetaJobTable as db where db.id.tableId = ? ");
			hqlQuery.setInteger(0, tableId);
			List hibList = hqlQuery.list();

			Iterator it = hibList.iterator();
			while (it.hasNext()) {
				SbiMetaJobTable tmpRel = (SbiMetaJobTable) it.next();
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
	public List<SbiMetaTable> loadTablesByJobId(Integer jobId) throws EMFUserError {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		List<SbiMetaTable> toReturn = new ArrayList();
		Query hqlQuery = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			hqlQuery = aSession.createQuery(" from SbiMetaJobTable as db where db.id.jobId = ? ");
			hqlQuery.setInteger(0, jobId);
			List hibList = hqlQuery.list();

			Iterator it = hibList.iterator();
			while (it.hasNext()) {
				SbiMetaJobTable tmpRel = (SbiMetaJobTable) it.next();
				SbiMetaTable tmpJob = DAOFactory.getSbiMetaTableDAO().loadTableByID(new Integer(tmpRel.getId().getTableId()));

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
	public void modifyJobTable(SbiMetaJobTable aMetaJobTable) throws EMFUserError {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiMetaJobTableId hibId = new SbiMetaJobTableId();
			hibId.setTableId(aMetaJobTable.getId().getTableId());
			hibId.setJobId(aMetaJobTable.getId().getJobId());

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
	public void insertJobTable(SbiMetaJobTable aMetaJobTable) throws EMFUserError {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiMetaJobTableId hibId = new SbiMetaJobTableId();
			hibId.setTableId(aMetaJobTable.getId().getTableId());
			hibId.setJobId(aMetaJobTable.getId().getJobId());
			aMetaJobTable.setId(hibId);

			updateSbiCommonInfo4Insert(aMetaJobTable);
			aSession.save(aMetaJobTable);
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
	public void deleteJobTable(SbiMetaJobTable aMetaJobTable) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiMetaJobTableId hibId = new SbiMetaJobTableId();
			hibId.setTableId(aMetaJobTable.getId().getTableId());
			hibId.setJobId(aMetaJobTable.getId().getJobId());

			SbiMetaJobTable hib = (SbiMetaJobTable) aSession.load(SbiMetaJobTable.class, hibId);

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