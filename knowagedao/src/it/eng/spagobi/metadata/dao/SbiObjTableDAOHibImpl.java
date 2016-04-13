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
import it.eng.spagobi.metadata.metadata.SbiMetaObjTable;
import it.eng.spagobi.metadata.metadata.SbiMetaObjTableId;
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
public class SbiObjTableDAOHibImpl extends AbstractHibernateDAO implements ISbiObjTableDAO {

	static private Logger logger = Logger.getLogger(SbiDsBcDAOHibImpl.class);

	@Override
	public List<BIObject> loadObjByTableId(Integer tableId) throws EMFUserError {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		List<BIObject> toReturn = new ArrayList();
		Query hqlQuery = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			hqlQuery = aSession.createQuery(" from SbiMetaObjTable as db where db.id.tableId = ? ");
			hqlQuery.setInteger(0, tableId);
			List hibList = hqlQuery.list();

			Iterator it = hibList.iterator();
			while (it.hasNext()) {
				SbiMetaObjTable tmpRel = (SbiMetaObjTable) it.next();
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
	public List<SbiMetaTable> loadTablesByObjId(Integer objId) throws EMFUserError {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		List<SbiMetaTable> toReturn = new ArrayList();
		Query hqlQuery = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			hqlQuery = aSession.createQuery(" from SbiMetaObjTable as db where db.id.objId = ? ");
			hqlQuery.setInteger(0, objId);
			List hibList = hqlQuery.list();

			Iterator it = hibList.iterator();
			while (it.hasNext()) {
				SbiMetaDsTable tmpRel = (SbiMetaDsTable) it.next();
				SbiMetaTable tmpTable = DAOFactory.getSbiMetaTableDAO().loadTableByID(new Integer(tmpRel.getId().getTableId()));

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
	public void modifyObjDTable(SbiMetaObjTable aMetaObjTable) throws EMFUserError {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiMetaObjTableId hibId = new SbiMetaObjTableId();
			hibId.setObjId(aMetaObjTable.getId().getObjId());
			hibId.setTableId(aMetaObjTable.getId().getTableId());

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
	public void insertObjTable(SbiMetaObjTable aMetaObjTable) throws EMFUserError {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiMetaObjTable hib = new SbiMetaObjTable();

			SbiMetaObjTableId hibId = new SbiMetaObjTableId();
			hibId.setObjId(aMetaObjTable.getId().getObjId());
			hibId.setTableId(aMetaObjTable.getId().getTableId());
			aMetaObjTable.setId(hibId);

			updateSbiCommonInfo4Insert(aMetaObjTable);
			aSession.save(aMetaObjTable);
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
	public void deleteObjTable(SbiMetaObjTable aMetaObjTable) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiMetaObjTableId hibId = new SbiMetaObjTableId();
			hibId.setObjId(aMetaObjTable.getId().getObjId());
			hibId.setTableId(aMetaObjTable.getId().getTableId());

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