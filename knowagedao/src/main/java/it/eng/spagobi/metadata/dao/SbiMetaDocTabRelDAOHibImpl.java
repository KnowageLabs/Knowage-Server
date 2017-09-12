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
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Restrictions;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.metadata.metadata.SbiMetaDocTabRel;

/**
 * @author Pirkovic_Dragan (Dragan.Pirkovic@mht.net)
 *
 */
public class SbiMetaDocTabRelDAOHibImpl extends AbstractHibernateDAO implements ISbiMetaDocTabRelDAO {

	static private Logger logger = Logger.getLogger(SbiMetaDocTabRelDAOHibImpl.class);

	@Override
	public SbiMetaDocTabRel loadDocRelationByID(Integer id) throws EMFUserError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void modifyDocRelation(SbiMetaDocTabRel sbiMetaDocTabRel) throws EMFUserError {
		// TODO Auto-generated method stub

	}

	@Override
	public void insertDocRelation(SbiMetaDocTabRel sbiMetaDocTabRel) throws EMFUserError {
		logger.debug("IN");

		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			SbiMetaDocTabRel hibMeta = new SbiMetaDocTabRel();
			hibMeta.setDocumentId(sbiMetaDocTabRel.getDocumentId());
			hibMeta.setTableId(sbiMetaDocTabRel.getTableId());
			hibMeta.setRelationId(sbiMetaDocTabRel.getRelationId());

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

	@Override
	public void deleteDocRelation(SbiMetaDocTabRel sbiMetaDocTabRel) throws EMFUserError {
		logger.debug("IN");

		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			SbiMetaDocTabRel hibMeta = (SbiMetaDocTabRel) tmpSession.load(SbiMetaDocTabRel.class, new Integer(sbiMetaDocTabRel.getRelationId()));

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

	@Override
	public List<SbiMetaDocTabRel> loadAllDocRelations() throws EMFUserError {
		logger.debug("IN");

		Session tmpSession = null;
		Transaction tx = null;
		List<SbiMetaDocTabRel> toReturn = new ArrayList();
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			Query hibQuery = tmpSession.createQuery(" from SbiMetaDocTabRel");

			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();
			while (it.hasNext()) {
				SbiMetaDocTabRel hibMeta = (SbiMetaDocTabRel) it.next();
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
	public List<SbiMetaDocTabRel> loadByDocumentId(Integer documentId) throws EMFUserError {

		logger.debug("IN");

		List<SbiMetaDocTabRel> toReturn = null;
		Session tmpSession = null;
		Transaction tx = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			Criterion labelCriterrion = Expression.eq("documentId", documentId);
			Criteria criteria = tmpSession.createCriteria(SbiMetaDocTabRel.class);
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
	public SbiMetaDocTabRel loadDocIdandTableId(Integer documentId, Integer tableId) throws EMFUserError {

		logger.debug("IN");

		SbiMetaDocTabRel toReturn = null;
		Session tmpSession = null;
		Transaction tx = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			// Criterion labelCriterrion1 = Expression.eq("datasetId",
			// datasetId);
			// Criterion labelCriterrion2 = Expression.eq("tableId", tableId);
			List<SbiMetaDocTabRel> relations = tmpSession.createCriteria(SbiMetaDocTabRel.class).add(Restrictions.eq("documentId", documentId))
					.add(Restrictions.eq("tableId", tableId)).list();
			toReturn = relations.get(0);
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

}