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
package it.eng.spagobi.hotlink.rememberme.dao;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiSubObjects;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.engines.config.metadata.SbiEngines;
import it.eng.spagobi.hotlink.rememberme.bo.RememberMe;
import it.eng.spagobi.hotlink.rememberme.metadata.SbiRememberMe;

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

/**
 * 
 * @author Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class RememberMeDAOHibImpl extends AbstractHibernateDAO implements IRememberMeDAO {

	private static transient Logger logger = Logger.getLogger(RememberMeDAOHibImpl.class);
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.hotlink.rememberme.dao.IRememberMeDAO#delete(java.lang.Integer)
	 */
	public void delete(Integer rememberMeId) throws EMFInternalError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			//String hql = "from SbiRememberMe srm where srm.id=" + rememberMeId;
			String hql = "from SbiRememberMe srm where srm.id=?" ;
			Query query = aSession.createQuery(hql);
			query.setInteger(0, rememberMeId.intValue());
			SbiRememberMe hibObj = (SbiRememberMe) query.uniqueResult();
			if (hibObj == null) {
				logger.warn("SbiRememberMe with id = " + rememberMeId + " not found!");
				return;
			}
			aSession.delete(hibObj);
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			if (tx != null) tx.rollback();	
			throw new EMFInternalError(EMFErrorSeverity.ERROR, "100");  
		} finally {
			if (aSession != null) {
				if (aSession.isOpen()) aSession.close();
			}
			logger.debug("OUT");
		}
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.hotlink.rememberme.dao.IRememberMeDAO#getMyRememberMe(java.lang.String)
	 */
	public List<RememberMe> getMyRememberMe(String userId) throws EMFInternalError {
		logger.debug("IN");
		logger.debug("*** RememberMe - userId: "+ userId);
		Session aSession = null;
		Transaction tx = null;
		List toReturn = new ArrayList();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Criterion userIdCriterion = Expression.eq("userName", userId);
			Criteria criteria = aSession.createCriteria(SbiRememberMe.class);
			criteria.add(userIdCriterion);
			//criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
			List list = criteria.list();
			Iterator it = list.iterator();
			while (it.hasNext()) {
				SbiRememberMe hibObj = (SbiRememberMe) it.next();
				toReturn.add(toRememberMe(hibObj));
			}
			return toReturn;
		} catch (HibernateException he) {
			logException(he);
			if (tx != null) tx.rollback();	
			throw new EMFInternalError(EMFErrorSeverity.ERROR, "100");  
		} finally {
			if (aSession != null) {
				if (aSession.isOpen()) aSession.close();
			}
			logger.debug("OUT");
		}
	}

	private RememberMe toRememberMe(SbiRememberMe hibObj) {
		RememberMe toReturn = new RememberMe();
		toReturn.setId(hibObj.getId());
		toReturn.setName(hibObj.getName());
		toReturn.setDescription(hibObj.getDescription());
		toReturn.setUserName(hibObj.getUserName());
		SbiObjects obj = hibObj.getSbiObject();
		toReturn.setObjId(obj.getBiobjId());
		toReturn.setDocumentLabel(obj.getLabel());
		toReturn.setDocumentName(obj.getName());
		toReturn.setDocumentDescription(obj.getDescr());
		SbiDomains docType = obj.getObjectType();
		toReturn.setDocumentType(docType.getValueCd());
		toReturn.setParameters(hibObj.getParameters());
		SbiEngines engine = obj.getSbiEngines();
		toReturn.setEngineName(engine.getName());
		SbiSubObjects subObj = hibObj.getSbiSubObject();
		if (subObj != null) {
			toReturn.setSubObjId(subObj.getSubObjId());
			toReturn.setSubObjName(subObj.getName());
		}
		return toReturn;
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.hotlink.rememberme.dao.IRememberMeDAO#saveRememberMe(java.lang.String, java.lang.String, java.lang.Integer, java.lang.Integer, java.lang.String, java.lang.String)
	 */
	public boolean saveRememberMe(String name, String description, Integer docId, Integer subObjId, String userId, String parameters) throws EMFInternalError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			
			/*
			 * The following code checks if a equal Remember Me exists, 
			 * i.e. if a Remember Me for the same user, same document, same 
			 * parameters or same subobject already exists. In this case 
			 * the remember me is not saved. 
			 * This does not work on Ingres because the '=' function 
			 * (which is the sql translation of hibernate Expression.eq)
			 * does not work on Long nvarchar fields.
			Criteria criteria = aSession.createCriteria(SbiRememberMe.class);
			Criterion userIdCriterion = Expression.eq("userName", userId);
			criteria.add(userIdCriterion);
			SbiObjects obj = (SbiObjects) aSession.load(SbiObjects.class, docId);
			Criterion docIdCriterion = Expression.eq("sbiObject", obj);
			criteria.add(docIdCriterion);
			SbiSubObjects subObj = null;
			if (subObjId != null) {
				subObj = (SbiSubObjects) aSession.load(SbiSubObjects.class, subObjId);
				Criterion subObjIdCriterion = Expression.eq("sbiSubObject", subObj);
				criteria.add(subObjIdCriterion);
			}
			Criterion parametersCriterion = Expression.eq("parameters", parameters);
			criteria.add(parametersCriterion);
			//criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
			List list = criteria.list();
			if (list.isEmpty()) {
				SbiRememberMe temp = new SbiRememberMe();
				temp.setName(name);
				temp.setDescription(description);
				temp.setUserName(userId);
				temp.setSbiObject(obj);
				temp.setSbiSubObject(subObj);
				temp.setParameters(parameters);
				aSession.save(temp);
				tx.commit();
				return true;
			} else {
				logger.debug("RememberMe for user " + userId + " for document with id " + docId + " with parameters [" 
						+ parameters + "] is already present.");
				return false;
			}
			*/
			
			/*
			 * The following code does not check if an equivalent Remember Me already exists,
			 * it simply inserts it.
			 */
			SbiRememberMe temp = new SbiRememberMe();
			temp.setName(name);
			temp.setDescription(description);
			temp.setUserName(userId);
			SbiObjects obj = (SbiObjects) aSession.load(SbiObjects.class, docId);
			temp.setSbiObject(obj);
			SbiSubObjects subObj = null;
			if (subObjId != null) {
				subObj = (SbiSubObjects) aSession.load(SbiSubObjects.class, subObjId);
			}
			temp.setSbiSubObject(subObj);
			temp.setParameters(parameters);
			updateSbiCommonInfo4Insert(temp);
			aSession.save(temp);
			tx.commit();
			return true;
			
		} catch (HibernateException he) {
			logException(he);
			if (tx != null) tx.rollback();	
			throw new EMFInternalError(EMFErrorSeverity.ERROR, "100");  
		} finally {
			if (aSession != null) {
				if (aSession.isOpen()) aSession.close();
			}
			logger.debug("OUT");
		}
	}

	public RememberMe getRememberMe(Integer rememberMeId) throws EMFInternalError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		RememberMe toReturn = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Criterion userIdCriterion = Expression.eq("id", rememberMeId);
			Criteria criteria = aSession.createCriteria(SbiRememberMe.class);
			criteria.add(userIdCriterion);
			//criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
			List list = criteria.list();
			Iterator it = list.iterator();
			while (it.hasNext()) {
				SbiRememberMe hibObj = (SbiRememberMe) it.next();
				toReturn = toRememberMe(hibObj);
			}
			return toReturn;
		} catch (HibernateException he) {
			logException(he);
			if (tx != null) tx.rollback();	
			throw new EMFInternalError(EMFErrorSeverity.ERROR, "100");  
		} finally {
			if (aSession != null) {
				if (aSession.isOpen()) aSession.close();
			}
			logger.debug("OUT");
		}
	}

}
