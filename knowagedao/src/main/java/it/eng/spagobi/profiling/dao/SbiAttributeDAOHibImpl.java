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
package it.eng.spagobi.profiling.dao;

import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.profiling.bean.SbiAttribute;
import it.eng.spagobi.profiling.bean.SbiUserAttributes;
import it.eng.spagobi.profiling.bo.ProfileAttributesValueTypes;

public class SbiAttributeDAOHibImpl extends AbstractHibernateDAO implements ISbiAttributeDAO {
	private static Logger logger = Logger.getLogger(SbiAttributeDAOHibImpl.class);

	@Override
	public List<SbiUserAttributes> loadSbiUserAttributesById(Integer id) throws EMFUserError {
		logger.debug("IN");
		List<SbiUserAttributes> toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			String q = "from SbiUserAttributes att where att.id = :id";
			Query query = aSession.createQuery(q);
			query.setInteger("id", id);

			toReturn = query.list();
			tx.commit();
		} catch (HibernateException he) {
			logger.error(he.getMessage(), he);
			rollbackIfActive(tx);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		logger.debug("OUT");
		return toReturn;

	}

	@Override
	public SbiUserAttributes loadSbiAttributesByUserAndId(Integer userId, Integer attributeId) throws EMFUserError {
		logger.debug("IN");
		SbiUserAttributes toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			String q = "from SbiUserAttributes att where att.id.attributeId = :id and att.id.id = :userId ";
			Query query = aSession.createQuery(q);
			query.setInteger("id", attributeId);
			query.setInteger("userId", userId);

			toReturn = (SbiUserAttributes) query.uniqueResult();
			tx.commit();
		} catch (HibernateException he) {
			logger.error(he.getMessage(), he);
			rollbackIfActive(tx);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		logger.debug("OUT");
		return toReturn;

	}

	@Override
	public Integer saveSbiAttribute(SbiAttribute attribute) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			updateSbiCommonInfo4Insert(attribute);
			Integer id = (Integer) aSession.save(attribute);

			tx.commit();
			logger.debug("OUT");
			return id;
		} catch (HibernateException he) {
			logger.error(he.getMessage(), he);
			rollbackIfActive(tx);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}

	}

	@Override
	public Integer saveOrUpdateSbiAttribute(SbiAttribute attribute) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		Integer idToReturn = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiAttribute hibAttribute = attribute;
			int id = attribute.getAttributeId();
			if (id != 0) {
				hibAttribute = (SbiAttribute) aSession.load(SbiAttribute.class, attribute.getAttributeId());
				String name = attribute.getAttributeName();
				String description = attribute.getDescription();
				Short allowUser = attribute.getAllowUser();
				Short multivalue = attribute.getMultivalue();
				Short syntax = attribute.getSyntax();
				Integer lovId = attribute.getLovId();
				ProfileAttributesValueTypes value = attribute.getValue();

				if (name != null) {
					hibAttribute.setAttributeName(name);
				}
				if (description != null) {
					hibAttribute.setDescription(description);
				}
				if (allowUser != null) {
					hibAttribute.setAllowUser(allowUser);
				}
				if (multivalue != null) {
					hibAttribute.setMultivalue(multivalue);
				}
				if (syntax != null) {
					hibAttribute.setSyntax(syntax);
				}

				hibAttribute.setLovId(lovId);

				if (value != null) {
					hibAttribute.setValue(value);
				}

			}
			Integer idAttrPassed = attribute.getAttributeId();
			ProfileAttributesValueTypes value = attribute.getValue();
			if (value != null) {
				hibAttribute.setValue(value);
			}
			if (idAttrPassed != null && !String.valueOf(idAttrPassed.intValue()).equals("")) {

				updateSbiCommonInfo4Insert(hibAttribute);
				idToReturn = (Integer) aSession.save(hibAttribute);
			} else {

				updateSbiCommonInfo4Update(hibAttribute);
				aSession.saveOrUpdate(hibAttribute);
				idToReturn = idAttrPassed;
			}
			tx.commit();
			logger.debug("OUT");
		} catch (HibernateException he) {
			logger.error(he.getMessage(), he);
			rollbackIfActive(tx);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		return idToReturn;

	}

	@Override
	public List<SbiAttribute> loadSbiAttributes() throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Criteria finder = aSession.createCriteria(SbiAttribute.class);
			finder.addOrder(Order.asc("attributeName"));
			List<SbiAttribute> hibList = finder.list();

			tx.commit();
			return hibList;
		} catch (HibernateException he) {
			logException(he);

			rollbackIfActive(tx);

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			closeSessionIfOpen(aSession);
			logger.debug("OUT");
		}

	}

	@Override
	public SbiAttribute loadSbiAttributeByName(String name) throws EMFUserError {
		logger.debug("IN");
		SbiAttribute toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			String q = "from SbiAttribute att where att.attributeName = :name";
			Query query = aSession.createQuery(q);
			query.setString("name", name);

			toReturn = (SbiAttribute) query.uniqueResult();
			tx.commit();
		} catch (HibernateException he) {
			logger.error(he.getMessage(), he);
			rollbackIfActive(tx);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		logger.debug("OUT");
		return toReturn;
	}

	@Override
	public SbiAttribute loadSbiAttributeById(Integer id) throws EMFUserError {
		logger.debug("IN");
		SbiAttribute toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			toReturn = (SbiAttribute) aSession.load(SbiAttribute.class, id);
			Hibernate.initialize(toReturn);
			tx.commit();
		} catch (HibernateException he) {
			logger.error(he.getMessage(), he);
			rollbackIfActive(tx);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		logger.debug("OUT");
		return toReturn;
	}

	@Override
	public HashMap<Integer, String> loadSbiAttributesByIds(List<String> ids) throws EMFUserError {
		logger.debug("IN");
		List<SbiUserAttributes> dbResult = null;
		HashMap<Integer, String> toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			StringBuilder q = new StringBuilder("from SbiUserAttributes att where ");
			q.append(" att.id in (");
			for (int i = 0; i < ids.size(); i++) {
				q.append(" :id" + i);
				if (i != ids.size() - 1) {
					q.append(" , ");
				}
			}
			q.append(" )");

			Query query = aSession.createQuery(q.toString());
			for (int i = 0; i < ids.size(); i++) {
				query.setInteger("id" + i, Integer.valueOf(ids.get(i)));
			}

			dbResult = query.list();
			if (dbResult != null && !dbResult.isEmpty()) {
				toReturn = new HashMap<>();
				for (int i = 0; i < dbResult.size(); i++) {
					SbiUserAttributes res = dbResult.get(i);
					toReturn.put(res.getId().getAttributeId(), res.getAttributeValue());
				}
			}

			tx.commit();
		} catch (HibernateException he) {
			logger.error(he.getMessage(), he);
			rollbackIfActive(tx);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		logger.debug("OUT");
		return toReturn;
	}

	@Override
	public void deleteSbiAttributeById(Integer id) throws EMFUserError {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiAttribute attrToDelete = (SbiAttribute) aSession.load(SbiAttribute.class, id);
			aSession.delete(attrToDelete);
			tx.commit();
		} catch (HibernateException he) {
			logger.error(he.getMessage(), he);
			rollbackIfActive(tx);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
			logger.debug("OUT");
		}

	}

}
