/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.alarm.dao;

/**
 * Title: SpagoBI
 * Description: SpagoBI
 * Copyright: Copyright (c) 2008
 * Company: Xaltia S.p.A.
 * 
 * @author Enrico Cesaretti
 * @version 1.0
 */




import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.kpi.alarm.bo.AlarmContact;
import it.eng.spagobi.kpi.alarm.metadata.SbiAlarmContact;
import it.eng.spagobi.kpi.model.bo.Resource;
import it.eng.spagobi.kpi.model.metadata.SbiResources;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * 
 * @see it.eng.spagobi.kpi.alarm.bo.AlarmContact
 * @author Enrico Cesaretti
 */
public class SbiAlarmContactDAOHibImpl extends AbstractHibernateDAO implements ISbiAlarmContactDAO {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SbiAlarmContactDAOHibImpl.class);

	
    public Integer insert(SbiAlarmContact item) {
        Session session = getSession();
        Transaction tx = null;
        Integer id = null;
        try {
        	tx = session.beginTransaction();
        	updateSbiCommonInfo4Insert(item);
			id = (Integer)session.save(item);
			tx.commit();
			
		} catch (HibernateException e) {
			if( tx != null && tx.isActive() ){
				tx.rollback();
			}
			throw e;
			
		}finally{
			if(session != null){
				session.close();
			}
			
			return id;
		}
    }
    
 /*   
    public void insert(Session session, SbiAlarmContact item) {
    	updateSbiCommonInfo4Insert(item);
        session.save(item);
    }
*/
    public void update(SbiAlarmContact item) {
        Session session = getSession();
        Transaction tx = null;
        try {
        	tx = session.beginTransaction();
        	updateSbiCommonInfo4Update(item);
			session.update(item);
			tx.commit();
			
		} catch (HibernateException e) {
			if( tx != null && tx.isActive() ){
				tx.rollback();
			}
			throw e;
			
		}finally{
			session.close();
		}
    }	
/*    
    public void update(Session session, SbiAlarmContact item) {
        session.update(item);
    }	
*/	
    public void delete(SbiAlarmContact item) {
        Session session = getSession();
        Transaction tx = null;
        try {
        	tx = session.beginTransaction();
			session.delete(item);
			tx.commit();
			
		} catch (HibernateException e) {
			if( tx != null && tx.isActive() ){
				tx.rollback();
			}
			throw e;
			
		}finally{
			session.close();
		}
    }
    
    public void delete(Session session, SbiAlarmContact item) {
       session.delete(item);
    }

    public void delete(Integer id) {
        Session session = getSession();
        Transaction tx = null;
        try {
        	tx = session.beginTransaction();
        	session.delete(session.load(SbiAlarmContact.class, id));
			tx.commit();
			
		} catch (HibernateException e) {
			if( tx != null && tx.isActive() ){
				tx.rollback();
			}
			throw e;
			
		}finally{
			session.close();
		}
    }
    
    
    public void delete(Session session, Integer id) {
       	session.delete(session.load(SbiAlarmContact.class, id));
    }
	
    @SuppressWarnings("unchecked")
    public SbiAlarmContact findById(Integer id) {
        Session session = getSession();
        Transaction tx = null;
        try {
        	tx = session.beginTransaction();
			SbiAlarmContact item = (SbiAlarmContact)session.get(SbiAlarmContact.class, id);
			tx.commit();
			return item;
			
		} catch (HibernateException e) {
			if( tx != null && tx.isActive() ){
				tx.rollback();
			}
			throw e;
			
		}finally{
			session.close();
		}
    }
    
    
    
    public AlarmContact loadById(Integer id) {
        Session session = getSession();
        Transaction tx = null;
        try {
        	tx = session.beginTransaction();
			SbiAlarmContact item = (SbiAlarmContact)session.get(SbiAlarmContact.class, id);
			tx.commit();
			AlarmContact alarmContact=toAlarmContact(item);
			return alarmContact;
			
		} catch (HibernateException e) {
			if( tx != null && tx.isActive() ){
				tx.rollback();
			}
			throw e;
			
		}finally{
			session.close();
		}
    }
    
    

    @SuppressWarnings("unchecked")
	public List<SbiAlarmContact> findAll() {
        Session session = getSession();
        Transaction tx = null;
        try {
        	tx = session.beginTransaction();
			
			List<SbiAlarmContact> list = (List<SbiAlarmContact>)session.createQuery("from SbiAlarmContact").list();
			tx.commit();
			return list;
			
		} catch (HibernateException e) {
			if( tx != null && tx.isActive() ){
				tx.rollback();
			}
			throw e;
			
		}finally{
			session.close();
		}
    }	
    
    
    @SuppressWarnings("unchecked")
    public List<SbiAlarmContact> findByCsp(String csp) {
        Session session = getSession();
        Transaction tx = null;
        try {
        	tx = session.beginTransaction();
        	List<SbiAlarmContact> list = (List<SbiAlarmContact>)session.createQuery("from SbiAlarmContact where RESOURCES=?").setParameter(0, csp).list();
		tx.commit();
		return list;
			
		} catch (HibernateException e) {
			if( tx != null && tx.isActive() ){
				tx.rollback();
			}
			throw e;
			
		}finally{
			session.close();
		}
    }
    
    @SuppressWarnings("unchecked")
    public List<SbiAlarmContact> findByCsp(Session session, String csp) {
        List<SbiAlarmContact> list = (List<SbiAlarmContact>)session.createQuery("from SbiAlarmContact as contact where contact.RESOURCES=?").setParameter(0, csp).list();
	return list;
    }
	
    @SuppressWarnings("unchecked")
    public List<SbiAlarmContact> findGenericContact(Session session) {
        List<SbiAlarmContact> list = (List<SbiAlarmContact>)session.createQuery("from SbiAlarmContact where RESOURCES IS NULL ").list();
	return list;
    }
    
    
	public AlarmContact toAlarmContact(SbiAlarmContact sbiAlarmContact){
		AlarmContact toReturn=new AlarmContact();
		toReturn.setEmail(sbiAlarmContact.getEmail());
		toReturn.setId(sbiAlarmContact.getId());
		toReturn.setMobile(sbiAlarmContact.getMobile());
		toReturn.setName(sbiAlarmContact.getName());
		toReturn.setResources(sbiAlarmContact.getResources());
		return toReturn;
	}


	public Integer countContacts() throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		Integer resultNumber;
		
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
		
			String hql = "select count(*) from SbiAlarmContact ";
			Query hqlQuery = aSession.createQuery(hql);
			Long temp = (Long)hqlQuery.uniqueResult();
			resultNumber = new Integer(temp.intValue());

		} catch (HibernateException he) {
			logger.error("Error while loading the list of Contacts", he);	
			if (tx != null)
				tx.rollback();	
			throw new EMFUserError(EMFErrorSeverity.ERROR, 9104);
		
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}
		return resultNumber;
	}


	public List<SbiAlarmContact> loadPagedContactsList(Integer offset,
			Integer fetchSize) throws EMFUserError {
		logger.debug("IN");
		List<SbiAlarmContact> toTransform = null;
		Session aSession = null;
		Transaction tx = null;
		Integer resultNumber;
		Query hibernateQuery;
		
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();		
		
			String hql = "select count(*) from SbiAlarmContact ";
			Query hqlQuery = aSession.createQuery(hql);
			Long temp = (Long)hqlQuery.uniqueResult();
			resultNumber = new Integer(temp.intValue());
			
			offset = offset < 0 ? 0 : offset;
			if(resultNumber > 0) {
				fetchSize = (fetchSize > 0)? Math.min(fetchSize, resultNumber): resultNumber;
			}
			
			hibernateQuery = aSession.createQuery("from SbiAlarmContact order by name ");
			hibernateQuery.setFirstResult(offset);
			if(fetchSize > 0) hibernateQuery.setMaxResults(fetchSize);			

			toTransform = (List<SbiAlarmContact>)hibernateQuery.list();	
				
		} catch (HibernateException he) {
			logger.error("Error while loading the list of Resources", he);	
			if (tx != null)
				tx.rollback();	
			throw new EMFUserError(EMFErrorSeverity.ERROR, 9104);
		
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}
		return toTransform;
	}
    

}

