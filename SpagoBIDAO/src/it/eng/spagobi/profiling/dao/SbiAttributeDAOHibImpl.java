/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.profiling.dao;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.engines.config.metadata.SbiEngines;
import it.eng.spagobi.profiling.bean.SbiAttribute;
import it.eng.spagobi.profiling.bean.SbiUserAttributes;

import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;

public class SbiAttributeDAOHibImpl extends AbstractHibernateDAO implements
		ISbiAttributeDAO {
	static private Logger logger = Logger
			.getLogger(SbiAttributeDAOHibImpl.class);

	public List<SbiUserAttributes> loadSbiUserAttributesById(Integer id)
			throws EMFUserError {
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

	public SbiUserAttributes loadSbiAttributesByUserAndId(Integer userId, Integer attributeId)
			throws EMFUserError {
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

			toReturn = (SbiUserAttributes)query.uniqueResult();
			tx.commit();
		} catch (HibernateException he) {
			logger.error(he.getMessage(), he);
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
			logger.error(he.getMessage(),he);
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
			if(id!=0){
				hibAttribute = (SbiAttribute)aSession.load(SbiAttribute.class,  attribute.getAttributeId());
				String name = attribute.getAttributeName();
				String description = attribute.getDescription();
				if(name!=null){
					hibAttribute.setAttributeName(name);
				}
				if(description!=null){
					hibAttribute.setDescription(description);
				}
			}
			Integer idAttrPassed = attribute.getAttributeId();
			if(idAttrPassed != null && !String.valueOf(idAttrPassed.intValue()).equals("")){
				updateSbiCommonInfo4Insert(hibAttribute);
				idToReturn = (Integer)aSession.save(hibAttribute);
			}else{
				updateSbiCommonInfo4Update(hibAttribute);
				aSession.saveOrUpdate(hibAttribute);
				idToReturn = idAttrPassed;
			}
			tx.commit();
			logger.debug("OUT");
		} catch (HibernateException he) {
			logger.error(he.getMessage(),he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		return idToReturn;

	}

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

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			logger.debug("OUT");
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}

	}

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
			logger.error(he.getMessage(),he);
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
	
	public SbiAttribute loadSbiAttributeById(Integer id) throws EMFUserError {
		logger.debug("IN");
		SbiAttribute toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			toReturn = (SbiAttribute) aSession.load(SbiAttribute.class,  id);
			tx.commit();
		} catch (HibernateException he) {
			logger.error(he.getMessage(),he);
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

	public HashMap<Integer, String> loadSbiAttributesByIds(List<String> ids)
			throws EMFUserError {
		logger.debug("IN");
		List<SbiUserAttributes> dbResult = null;
		HashMap<Integer, String> toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			StringBuffer q = new StringBuffer("from SbiUserAttributes att where ");
			q.append(" att.id in (");
			for(int i=0; i<ids.size(); i++){				
				q.append(" :id"+i);				
				if(i != ids.size()-1){
					q.append(" , ");
				}
			}
			q.append(" )");
			
			Query query = aSession.createQuery(q.toString());
			for(int i=0; i<ids.size(); i++){				
				query.setInteger("id"+i, Integer.valueOf(ids.get(i)));
			}
			

			dbResult = query.list();
			if(dbResult != null && !dbResult.isEmpty()){
				toReturn = new HashMap<Integer, String>();
				for(int i=0; i< dbResult.size(); i++){
					SbiUserAttributes res = (SbiUserAttributes)dbResult.get(i);
					toReturn.put(res.getId().getAttributeId(), res.getAttributeValue());
				}
			}

			tx.commit();
		} catch (HibernateException he) {
			logger.error(he.getMessage(), he);
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

	public void deleteSbiAttributeById(Integer id) throws EMFUserError {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiAttribute attrToDelete =(SbiAttribute)aSession.load(SbiAttribute.class, id);
			aSession.delete(attrToDelete);
			tx.commit();
		} catch (HibernateException he) {
			logger.error(he.getMessage(), he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			logger.debug("OUT");
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}
		
	}

}
