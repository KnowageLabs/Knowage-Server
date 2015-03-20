/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.monitoring.dao;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiSubObjects;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.engines.config.metadata.SbiEngines;
import it.eng.spagobi.hotlink.rememberme.bo.HotLink;
import it.eng.spagobi.monitoring.metadata.SbiAudit;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class DbAuditImpl extends AbstractHibernateDAO implements IAuditDAO {
	
	private static transient Logger logger = Logger.getLogger(DbAuditImpl.class);
	
	/**
	 * Insert audit.
	 * 
	 * @param aSbiAudit the a sbi audit
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.monitoring.dao.IAuditDAO#insertAudit(it.eng.spagobi.bo.SbiAudit)
	 */
	public void insertAudit(SbiAudit aSbiAudit) throws EMFUserError {
		logger.debug("IN");
		Session session = null;
		Transaction tx = null;
		try {
			session = getSession();;
			tx = session.beginTransaction();
			if (aSbiAudit.getSbiObject() == null) {
				Integer objectId = aSbiAudit.getDocumentId();
				SbiObjects sbiObject = (SbiObjects) session.load(SbiObjects.class, objectId);
				aSbiAudit.setSbiObject(sbiObject);
			}
			if (aSbiAudit.getSbiSubObject() == null) {
				Integer subObjId = aSbiAudit.getSubObjId();
				if (subObjId != null) {
					SbiSubObjects subObj = (SbiSubObjects) session.load(SbiSubObjects.class, subObjId);
					aSbiAudit.setSbiSubObject(subObj);
				}
			}
			if (aSbiAudit.getSbiEngine() == null) {
				Integer engineId = aSbiAudit.getEngineId();
				SbiEngines sbiEngine = (SbiEngines) session.load(SbiEngines.class, engineId);
				aSbiAudit.setSbiEngine(sbiEngine);
			}
			updateSbiCommonInfo4Insert(aSbiAudit);
			session.save(aSbiAudit);
			session.flush();
			tx.commit();
		} catch (HibernateException he) {
			logger.error(he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (session != null) {
				if (session.isOpen()) session.close();
			}
			logger.debug("OUT");
		}
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.monitoring.dao.IAuditDAO#loadAllAudits()
	 */
	public List loadAllAudits() throws EMFUserError {
		logger.debug("IN");
		logger.error("this method is not implemented!!");
		logger.debug("OUT");
		return null;
	}
	
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.monitoring.dao.IAuditDAO#loadAuditByID(java.lang.Integer)
	 */
	public SbiAudit loadAuditByID(Integer id) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		SbiAudit aSbiAudit = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			aSbiAudit = (SbiAudit) aSession.load(SbiAudit.class, id);
			aSbiAudit.getSbiObject();
			aSbiAudit.getDocumentLabel();
			aSbiAudit.getDocumentId();
			aSbiAudit.getDocumentName();
			aSbiAudit.getDocumentParameters();
			aSbiAudit.getDocumentState();
			aSbiAudit.getDocumentType();
			aSbiAudit.getSbiSubObject();
			aSbiAudit.getSubObjId();
			aSbiAudit.getSubObjName();
			aSbiAudit.getSubObjOwner();
			aSbiAudit.getSubObjIsPublic();
			aSbiAudit.getSbiEngine();
			aSbiAudit.getEngineClass();
			aSbiAudit.getEngineDriver();
			aSbiAudit.getEngineId();
			aSbiAudit.getEngineLabel();
			aSbiAudit.getEngineName();
			aSbiAudit.getEngineType();
			aSbiAudit.getEngineUrl();
			aSbiAudit.getExecutionModality();
			aSbiAudit.getRequestTime();
			aSbiAudit.getId();
			aSbiAudit.getUserName();
			aSbiAudit.getUserGroup();
			aSbiAudit.getExecutionStartTime();
			aSbiAudit.getExecutionEndTime();
			aSbiAudit.getExecutionTime();
			aSbiAudit.getExecutionState();
			aSbiAudit.getError();
			aSbiAudit.getErrorMessage();
			aSbiAudit.getErrorCode();
			tx.commit();
		} catch (HibernateException he) {
			logger.error(he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
			logger.debug("OUT");
		}
		return aSbiAudit;
	}
	

	/* (non-Javadoc)
	 * @see it.eng.spagobi.monitoring.dao.IAuditDAO#modifyAudit(it.eng.spagobi.monitoring.metadata.SbiAudit)
	 */
	public void modifyAudit(SbiAudit aSbiAudit) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			// TODO forse mettere un controllo per vedere se ci sono sbiobject e sbiengine?
			updateSbiCommonInfo4Update(aSbiAudit);
			aSession.saveOrUpdate(aSbiAudit);
			tx.commit();
		} catch (HibernateException he) {
			logger.error(he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
			logger.debug("OUT");
		}	

	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.monitoring.dao.IAuditDAO#loadAuditsByDocumentLabel(java.lang.String)
	 */
	public List loadAuditsByDocumentLabel(String documentLabel) throws EMFUserError {
		logger.debug("IN");
		logger.error("this method is not implemented!!");
		logger.debug("OUT");
		return null;
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.monitoring.dao.IAuditDAO#loadAuditsByEngineLabel(java.lang.String)
	 */
	public List loadAuditsByEngineLabel(String engineLabel) throws EMFUserError {
		logger.debug("IN");
		logger.error("this method is not implemented!!");
		logger.debug("OUT");
		return null;
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.monitoring.dao.IAuditDAO#loadAuditsByUserName(java.lang.String)
	 */
	public List loadAuditsByUserName(String userName) throws EMFUserError {
		logger.debug("IN");
		logger.error("this method is not implemented!!");
		logger.debug("OUT");
		return null;
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.monitoring.dao.IAuditDAO#eraseAudit(java.lang.Integer)
	 */
	public void eraseAudit(Integer id) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();			
			SbiAudit sbiAudit = (SbiAudit) aSession.load(SbiAudit.class, id);
			aSession.delete(sbiAudit);
			tx.commit();
		} catch (HibernateException he) {
			logger.error(he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} catch (Exception ex) {
			logger.error(ex);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100); 
		}	finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
			logger.debug("OUT");
		}
		
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.monitoring.dao.IAuditDAO#getMostPopular(java.util.Collection, int)
	 */
	public List getMostPopular(Collection roles, int limit) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List toReturn = new ArrayList();
		List userGroups = new ArrayList();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			String usergroups = "";
			Iterator it = roles.iterator();
			while (it.hasNext()) {
				String roleName = (String) it.next();
				//usergroups += "'" + roleName + "'";
				//if (it.hasNext()) usergroups += ",";
				 if (!userGroups.contains(roleName)) userGroups.add(roleName);
			}
			StringBuffer hql = new StringBuffer();
			hql.append("select ");
			hql.append("		count(a.sbiObject.biobjId), ");
			hql.append(	"		a.sbiObject.biobjId, ");
			hql.append(	"		a.sbiObject.label, ");
			hql.append(	"		a.sbiObject.name, ");
			hql.append(	"		a.sbiObject.descr, ");
			hql.append(	"		a.sbiObject.objectTypeCode, ");
			hql.append(	"		a.subObjId, ");
			hql.append(	"		a.subObjName, ");
			hql.append(	"		a.documentParameters, ");
			hql.append(	"		a.sbiEngine.name "); 
			hql.append(	"from ");
			hql.append(	"		SbiAudit a ");
			hql.append(	"where 	");
			hql.append(	"		a.sbiObject is not null and ");
			hql.append(	"		a.sbiEngine is not null and ");
			hql.append(	"		a.sbiObject.label not like 'SBI_%' and ");
			hql.append(	"		a.userGroup in (:USER_GROUPS) and ");
			hql.append(	"		(a.sbiSubObject is null or a.sbiSubObject.subObjId = a.subObjId) ");
			hql.append(	"group by 	a.sbiObject.biobjId, ");
			hql.append(	"			a.sbiObject.label, ");
			hql.append(	"			a.sbiObject.name, ");
			hql.append(	"			a.sbiObject.descr, ");
			hql.append(	"			a.sbiObject.objectTypeCode, ");
			hql.append(	"			a.subObjId, ");
			hql.append(	"			a.subObjName, ");
			hql.append(	"			a.documentParameters, ");
			hql.append(	"			a.sbiEngine.name ");
			hql.append(	"order by count(a.sbiObject.biobjId) desc ");
			Query hqlQuery = aSession.createQuery(hql.toString());
			hqlQuery.setParameterList("USER_GROUPS", userGroups);
			hqlQuery.setMaxResults(limit);
			List result = hqlQuery.list();
			Iterator resultIt = result.iterator();
			while (resultIt.hasNext()) {
				Object[] row = (Object[]) resultIt.next();
				toReturn.add(toHotLink(row));
			}
		} catch (Exception ex) {
			logger.error(ex);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100); 
		} finally {
			if (aSession != null){
				if (aSession.isOpen()) aSession.close();
			}
			logger.debug("OUT");
		}
		return toReturn;
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.monitoring.dao.IAuditDAO#getMyRecentlyUsed(java.lang.String, int)
	 */
	public List getMyRecentlyUsed(String userId, int limit) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List toReturn = new ArrayList();
		if (userId == null || userId.trim().equals("")) {
			logger.warn("The user id in input is null or empty.");
			return toReturn;
		}
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			StringBuffer hql = new StringBuffer();
			hql.append(	"select ");
			hql.append(	"		max(a.requestTime), ");
			hql.append(	"		a.sbiObject.biobjId, ");
			hql.append(	"		a.sbiObject.label, ");
			hql.append(	"		a.sbiObject.name, ");
			hql.append(	"		a.sbiObject.descr, ");
			hql.append(	"		a.sbiObject.objectTypeCode, ");
			hql.append(	"		a.subObjId, ");
			hql.append(	"		a.subObjName, ");
			hql.append(	"		a.documentParameters, ");
			hql.append(	"		a.sbiEngine.name "); 
			hql.append(	"from ");
			hql.append(	"		SbiAudit a ");
			hql.append(	"where 	");
			hql.append(	"		a.sbiObject is not null and ");
			hql.append(	"		a.sbiEngine is not null and ");
			hql.append(	"		a.sbiObject.label not like 'SBI_%' and ");
			hql.append(	"		a.userName = ? and ");
			hql.append(	"		(a.sbiSubObject is null or a.sbiSubObject.subObjId = a.subObjId) ");
			hql.append(	"group by 	a.sbiObject.biobjId, ");
			hql.append(	"			a.sbiObject.label, ");
			hql.append(	"			a.sbiObject.name, ");
			hql.append(	"			a.sbiObject.descr, ");
			hql.append(	"			a.sbiObject.objectTypeCode, ");
			hql.append(	"			a.subObjId, ");
			hql.append(	"			a.subObjName, ");
			hql.append(	"			a.documentParameters, ");
			hql.append(	"			a.sbiEngine.name ");
			hql.append(	"order by max(a.requestTime) desc ");
			Query hqlQuery = aSession.createQuery(hql.toString());
			hqlQuery.setString(0, userId);
			hqlQuery.setMaxResults(limit);
			List result = hqlQuery.list();
			Iterator resultIt = result.iterator();
			while (resultIt.hasNext()) {
				Object[] row = (Object[]) resultIt.next();
				toReturn.add(toHotLink(row));
			}
		} catch (Exception ex) {
			logger.error(ex);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100); 
		} finally {
			if (aSession != null){
				if (aSession.isOpen()) aSession.close();
			}
			logger.debug("OUT");
		}
		return toReturn;
	}
	
	private Object toHotLink(Object[] row) {
		HotLink toReturn = new HotLink();
		toReturn.setObjId((Integer) row[1]);
		toReturn.setDocumentLabel((String) row[2]);
		toReturn.setDocumentName((String) row[3]);
		toReturn.setDocumentDescription((String) row[4]);
		toReturn.setDocumentType((String) row[5]);
		toReturn.setSubObjId((Integer) row[6]);
		toReturn.setSubObjName((String) row[7]);
		toReturn.setParameters((String) row[8]);
		toReturn.setEngineName((String) row[9]);
		return toReturn;
	}
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.monitoring.dao.IAuditDAO#getLastExecution(java.lang.Integer)
	 */
	public SbiAudit getLastExecution(Integer objId) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		SbiAudit toReturn = new SbiAudit();
		if (objId == null) {
			logger.warn("The object id in input is null or empty.");
			return toReturn;
		}
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			StringBuffer hql = new StringBuffer();
			hql.append(	"select ");
			hql.append(	"		max(a.executionStartTime)");
			hql.append(	"from ");
			hql.append(	"		SbiAudit a ");
			hql.append(	"where 	");
			hql.append(	"		a.sbiObject is not null and ");
			hql.append(	"		a.sbiObject.biobjId = ? ");
			Query hqlQuery = aSession.createQuery(hql.toString());
			hqlQuery.setInteger(0, objId.intValue());
			Timestamp date = (Timestamp) hqlQuery.uniqueResult();
			toReturn.setDocumentId(objId);
			toReturn.setExecutionStartTime(date);
			
			StringBuffer hql2 = new StringBuffer();
			hql2.append(	"select ");
			hql2.append(	"		a.userName, ");
			hql2.append(	"		a.documentParameters, ");
			hql2.append(	"		a.requestTime, ");
			hql2.append(	"		a.executionEndTime, ");
			hql2.append(	"		a.executionState ");
			hql2.append(	"from ");
			hql2.append(	"		SbiAudit a ");
			hql2.append(	"where 	");
			hql2.append(	"		a.sbiObject is not null and ");
			hql2.append(	"		a.sbiObject.biobjId = ? and ");	
			hql2.append(	"		a.executionStartTime = ? ");
			Query hqlQuery2 = aSession.createQuery(hql2.toString());
			hqlQuery2.setInteger(0, objId.intValue());
			hqlQuery2.setTimestamp(1, date);
			Object[] row = (Object[]) hqlQuery2.uniqueResult();

			toReturn.setUserName((String) row[0]);
			toReturn.setDocumentParameters((String) row[1]);
			toReturn.setRequestTime((Timestamp) row[2]);
			toReturn.setExecutionEndTime((Timestamp) row[3]);
			toReturn.setExecutionState((String) row[4]);			
			
		} catch (Exception ex) {
			logger.error(ex);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100); 
		} finally {
			if (aSession != null){
				if (aSession.isOpen()) aSession.close();
			}
			logger.debug("OUT");
		}
		return toReturn;
	}
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.monitoring.dao.IAuditDAO#getMediumExecTime(java.lang.Integer)
	 */
	public Double getMediumExecTime(Integer objId) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		Double toReturn = new Double(0) ;
		if (objId == null) {
			logger.warn("The object id in input is null or empty.");
			return toReturn;
		}
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			StringBuffer hql = new StringBuffer();
			hql.append(	"select ");
			hql.append(	"		a.executionTime ");
			hql.append(	"from ");
			hql.append(	"		SbiAudit a ");
			hql.append(	"where 	");
			hql.append(	"		a.sbiObject is not null and ");
			hql.append(	"		a.sbiObject.biobjId = ? ");
			Query hqlQuery = aSession.createQuery(hql.toString());
			hqlQuery.setInteger(0, objId.intValue());
			List l = hqlQuery.list();
			int x = 0 ;
			int count = 1 ;
			if (!l.isEmpty()){
				Iterator it = l.iterator();
				while(it.hasNext()){
					Integer tosum = (Integer)it.next();
					if (tosum != null ){ x = x + tosum.intValue() ;
					count ++ ;			}		
				}
			}
			if (x != 0) toReturn =new Double( x / count) ;
			
		} catch (Exception ex) {
			logger.error(ex);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100); 
		} finally {
			if (aSession != null){
				if (aSession.isOpen()) aSession.close();
			}
			logger.debug("OUT");
		}
		return toReturn;
	}

}
