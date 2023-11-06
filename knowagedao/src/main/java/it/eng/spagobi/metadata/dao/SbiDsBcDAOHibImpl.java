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
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.metadata.metadata.SbiMetaBc;
import it.eng.spagobi.metadata.metadata.SbiMetaDsBc;
import it.eng.spagobi.metadata.metadata.SbiMetaDsBcId;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSet;

/**
 * @author Antonella Giachino (antonella.giachino@eng.it)
 *
 */
public class SbiDsBcDAOHibImpl extends AbstractHibernateDAO implements ISbiDsBcDAO {

	private static Logger logger = Logger.getLogger(SbiDsBcDAOHibImpl.class);

	@Override
	public List<SbiMetaBc> loadBcByDsId(Integer dsId) throws EMFUserError {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		List<SbiMetaBc> toReturn = new ArrayList();
		Query hqlQuery = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			hqlQuery = aSession.createQuery(" from SbiMetaDsBc as db where db.id.dsId = ? ");
			hqlQuery.setInteger(0, dsId);
			List hibList = hqlQuery.list();

			Iterator it = hibList.iterator();
			while (it.hasNext()) {
				SbiMetaDsBc tmpRel = (SbiMetaDsBc) it.next();
				SbiMetaBc tmpBC = DAOFactory.getSbiMetaBCDAO().loadBcByID(new Integer(tmpRel.getId().getBcId()));

				if (tmpBC != null)
					toReturn.add(tmpBC);
			}
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			rollbackIfActive(tx);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		logger.debug("OUT");
		return toReturn;
	}

	@Override
	public List<SbiDataSet> loadDsByBcId(Integer bcId) throws EMFUserError {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		List<SbiDataSet> toReturn = new ArrayList();
		Query hqlQuery = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			hqlQuery = aSession.createQuery(" from SbiMetaDsBc as db where db.id.bcId = ? ");
			hqlQuery.setInteger(0, bcId);
			List hibList = hqlQuery.list();

			Iterator it = hibList.iterator();
			while (it.hasNext()) {
				SbiMetaDsBc tmpRel = (SbiMetaDsBc) it.next();
				SbiDataSet tmpDS = DAOFactory.getSbiDataSetDAO()
						.loadSbiDataSetByIdAndOrganiz(new Integer(tmpRel.getId().getDsId()), null);

				if (tmpDS != null)
					toReturn.add(tmpDS);
			}
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			rollbackIfActive(tx);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		logger.debug("OUT");
		return toReturn;
	}

	@Override
	public List<SbiMetaDsBc> loadBcByDsIdAndTenant(Integer dsId, String organization) throws EMFUserError {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		List<SbiMetaDsBc> toReturn = new ArrayList();
		Query hqlQuery = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			hqlQuery = aSession.createQuery(" from SbiMetaDsBc as db where db.id.dsId = ? and db.id.organization = ? ");
			hqlQuery.setInteger(0, dsId);
			hqlQuery.setString(1, organization);
			toReturn = hqlQuery.list();

			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			rollbackIfActive(tx);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		logger.debug("OUT");
		return toReturn;
	}

	@Override
	public List<SbiMetaDsBc> loadDsBcByKey(SbiMetaDsBcId dsBcId) throws EMFUserError {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		List<SbiMetaDsBc> toReturn = null;
		Query hqlQuery = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			hqlQuery = aSession.createQuery(
					" from SbiMetaDsBc as db where db.id.dsId = ? and db.id.bcId = ? and db.id.organization = ?");
			hqlQuery.setInteger(0, dsBcId.getDsId());
			hqlQuery.setInteger(1, dsBcId.getBcId());
			hqlQuery.setString(2, dsBcId.getOrganization());
			toReturn = hqlQuery.list();

			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			rollbackIfActive(tx);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		logger.debug("OUT");
		return toReturn;
	}

	@Override
	public void modifyDsBc(SbiMetaDsBc aMeta) throws EMFUserError {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiMetaDsBcId hibId = new SbiMetaDsBcId();
			hibId.setBcId(aMeta.getId().getBcId());
			hibId.setDsId(aMeta.getId().getDsId());
			hibId.setOrganization(aMeta.getId().getOrganization());
			hibId.setVersionNum(aMeta.getId().getVersionNum());

			updateSbiCommonInfo4Update(hibId);
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			rollbackIfActive(tx);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		logger.debug("OUT");
	}

	@Override
	public void insertDsBc(SbiMetaDsBc aMeta) throws EMFUserError {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			updateSbiCommonInfo4Insert(aMeta);
			aSession.save(aMeta);
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			rollbackIfActive(tx);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		logger.debug("OUT");
	}

	@Override
	public void deleteDsBc(SbiMetaDsBc aMeta) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiMetaDsBcId hibId = new SbiMetaDsBcId();
			hibId.setBcId(aMeta.getId().getBcId());
			hibId.setDsId(aMeta.getId().getDsId());
			hibId.setOrganization(aMeta.getId().getOrganization());
			hibId.setVersionNum(aMeta.getId().getVersionNum());

			SbiMetaDsBc hib = (SbiMetaDsBc) aSession.load(SbiMetaDsBc.class, hibId);

			aSession.delete(hib);
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			rollbackIfActive(tx);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}

	}

}