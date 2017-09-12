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
import it.eng.spagobi.metadata.metadata.SbiMetaDsTabRel;

public class SbiMetaDsTabRelDAOHibImpl extends AbstractHibernateDAO implements ISbiMetaDsTabRel {

	static private Logger logger = Logger.getLogger(SbiMetaDsTabRelDAOHibImpl.class);

	@Override
	public SbiMetaDsTabRel loadRelationById(Integer relationId) throws EMFUserError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SbiMetaDsTabRel> loadAllRelations() throws EMFUserError {

		logger.debug("IN");

		Session tmpSession = null;
		Transaction tx = null;
		List<SbiMetaDsTabRel> toReturn = new ArrayList();
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			Query hibQuery = tmpSession.createQuery(" from SbiMetaDsTabRel");

			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();
			while (it.hasNext()) {
				SbiMetaDsTabRel hibMeta = (SbiMetaDsTabRel) it.next();
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
	public void modifyRelation(SbiMetaDsTabRel SbiMetaDsTabRel) throws EMFUserError {
		// TODO Auto-generated method stub

	}

	@Override
	public void insertRelation(SbiMetaDsTabRel SbiMetaDsTabRel) throws EMFUserError {

		logger.debug("IN");

		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			SbiMetaDsTabRel hibMeta = new SbiMetaDsTabRel();
			hibMeta.setDatasetId(SbiMetaDsTabRel.getDatasetId());
			hibMeta.setTableId(SbiMetaDsTabRel.getTableId());
			hibMeta.setRelationId(SbiMetaDsTabRel.getRelationId());

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
	public void deleteRelation(SbiMetaDsTabRel SbiMetaDsTabRel) throws EMFUserError {

		logger.debug("IN");

		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			SbiMetaDsTabRel hibMeta = (SbiMetaDsTabRel) tmpSession.load(SbiMetaDsTabRel.class, new Integer(SbiMetaDsTabRel.getRelationId()));

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
	public List<SbiMetaDsTabRel> loadByDatasetId(Integer datasetId) throws EMFUserError {

		logger.debug("IN");

		List<SbiMetaDsTabRel> toReturn = null;
		Session tmpSession = null;
		Transaction tx = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			Criterion labelCriterrion = Expression.eq("datasetId", datasetId);
			Criteria criteria = tmpSession.createCriteria(SbiMetaDsTabRel.class);
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
	public SbiMetaDsTabRel loadDsIdandTableId(Integer datasetId, Integer tableId) throws EMFUserError {

		logger.debug("IN");

		SbiMetaDsTabRel toReturn = null;
		Session tmpSession = null;
		Transaction tx = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			// Criterion labelCriterrion1 = Expression.eq("datasetId",
			// datasetId);
			// Criterion labelCriterrion2 = Expression.eq("tableId", tableId);
			List<SbiMetaDsTabRel> relations = tmpSession.createCriteria(SbiMetaDsTabRel.class).add(Restrictions.eq("datasetId", datasetId))
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