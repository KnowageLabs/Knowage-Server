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
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.metadata.metadata.SbiMetaDsTable;
import it.eng.spagobi.metadata.metadata.SbiMetaObjDs;
import it.eng.spagobi.metadata.metadata.SbiMetaObjDsId;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSet;

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
public class SbiObjDsDAOHibImpl extends AbstractHibernateDAO implements ISbiObjDsDAO {

	static private Logger logger = Logger.getLogger(SbiDsBcDAOHibImpl.class);

	@Override
	public List<BIObject> loadObjByDsId(Integer dsId) throws EMFUserError {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		List<BIObject> toReturn = new ArrayList();
		Query hqlQuery = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			hqlQuery = aSession.createQuery(" from SbiMetaObjDs as db where db.id.dsId = ? ");
			hqlQuery.setInteger(0, dsId);
			List hibList = hqlQuery.list();

			Iterator it = hibList.iterator();
			while (it.hasNext()) {
				SbiMetaObjDs tmpRel = (SbiMetaObjDs) it.next();
				BIObject tmpTable = DAOFactory.getBIObjectDAO().loadBIObjectById(new Integer(tmpRel.getId().getObjId()));

				if (tmpTable != null)
					toReturn.add(tmpTable);
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
	public List<SbiDataSet> loadDsByObjId(Integer objId) throws EMFUserError {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		List<SbiDataSet> toReturn = new ArrayList();
		Query hqlQuery = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			hqlQuery = aSession.createQuery(" from SbiMetaObjDs as db where db.id.objId = ? ");
			hqlQuery.setInteger(0, objId);
			List hibList = hqlQuery.list();

			Iterator it = hibList.iterator();
			while (it.hasNext()) {
				SbiMetaDsTable tmpRel = (SbiMetaDsTable) it.next();
				SbiDataSet tmpTable = DAOFactory.getSbiDataSetDAO().loadSbiDataSetByIdAndOrganiz(new Integer(tmpRel.getId().getTableId()), null);

				if (tmpTable != null)
					toReturn.add(tmpTable);
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
	public void modifyObjDs(SbiMetaObjDs aMetaObjDs) throws EMFUserError {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiMetaObjDsId hibId = new SbiMetaObjDsId();
			hibId.setObjId(aMetaObjDs.getId().getObjId());
			hibId.setDsId(aMetaObjDs.getId().getDsId());

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
	public int insertObjDs(SbiMetaObjDs aMetaObjDs) throws EMFUserError {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		Integer idToReturn = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiMetaObjDs hib = new SbiMetaObjDs();

			SbiMetaObjDsId hibId = new SbiMetaObjDsId();
			hibId.setObjId(aMetaObjDs.getId().getObjId());
			hibId.setDsId(aMetaObjDs.getId().getDsId());
			aMetaObjDs.setId(hibId);

			updateSbiCommonInfo4Insert(aMetaObjDs);
			idToReturn = (Integer) aSession.save(aMetaObjDs);
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
		return idToReturn;
	}

	@Override
	public void deleteObjDs(SbiMetaObjDs aMetaObjDs) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiMetaObjDsId hibId = new SbiMetaObjDsId();
			hibId.setObjId(aMetaObjDs.getId().getObjId());
			hibId.setDsId(aMetaObjDs.getId().getDsId());

			SbiMetaDsTable hib = (SbiMetaDsTable) aSession.load(SbiMetaDsTable.class, hibId);

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