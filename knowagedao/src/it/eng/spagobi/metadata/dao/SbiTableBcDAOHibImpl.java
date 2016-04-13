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
import it.eng.spagobi.metadata.metadata.SbiMetaBc;
import it.eng.spagobi.metadata.metadata.SbiMetaTable;
import it.eng.spagobi.metadata.metadata.SbiMetaTableBc;
import it.eng.spagobi.metadata.metadata.SbiMetaTableBcId;

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
public class SbiTableBcDAOHibImpl extends AbstractHibernateDAO implements ISbiTableBcDAO {

	static private Logger logger = Logger.getLogger(SbiDsBcDAOHibImpl.class);

	@Override
	public List<SbiMetaTable> loadTablesByBcId(Integer bcId) throws EMFUserError {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		List<SbiMetaTable> toReturn = new ArrayList();
		Query hqlQuery = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			hqlQuery = aSession.createQuery(" from SbiMetaTableBc as db where db.id.bcId = ? ");
			hqlQuery.setInteger(0, bcId);
			List hibList = hqlQuery.list();

			Iterator it = hibList.iterator();
			while (it.hasNext()) {
				SbiMetaTableBc tmpRel = (SbiMetaTableBc) it.next();
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
	public List<SbiMetaBc> loadBcByTableId(Integer tableId) throws EMFUserError {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		List<SbiMetaBc> toReturn = new ArrayList();
		Query hqlQuery = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			hqlQuery = aSession.createQuery(" from SbiMetaTableBc as db where db.id.tableId = ? ");
			hqlQuery.setInteger(0, tableId);
			List hibList = hqlQuery.list();

			Iterator it = hibList.iterator();
			while (it.hasNext()) {
				SbiMetaTableBc tmpRel = (SbiMetaTableBc) it.next();
				SbiMetaBc tmpTable = DAOFactory.getSbiMetaBCDAO().loadBcByID(new Integer(tmpRel.getId().getBcId()));

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
	public void modifyTableBc(SbiMetaTableBc aMetaTableBc) throws EMFUserError {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiMetaTableBcId hibId = new SbiMetaTableBcId();
			hibId.setTableId(aMetaTableBc.getId().getTableId());
			hibId.setBcId(aMetaTableBc.getId().getBcId());

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
	public void insertTableBc(SbiMetaTableBc aMetaTableBc) throws EMFUserError {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiMetaTableBc hib = new SbiMetaTableBc();

			SbiMetaTableBcId hibId = new SbiMetaTableBcId();
			hibId.setTableId(aMetaTableBc.getId().getTableId());
			hibId.setBcId(aMetaTableBc.getId().getBcId());
			aMetaTableBc.setId(hibId);

			updateSbiCommonInfo4Insert(aMetaTableBc);
			aSession.save(aMetaTableBc);
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
	public void deleteTableBc(SbiMetaTableBc aMetaTableBc) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiMetaTableBcId hibId = new SbiMetaTableBcId();
			hibId.setTableId(aMetaTableBc.getId().getTableId());
			hibId.setBcId(aMetaTableBc.getId().getBcId());

			SbiMetaTableBc hib = (SbiMetaTableBc) aSession.load(SbiMetaTableBc.class, hibId);

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