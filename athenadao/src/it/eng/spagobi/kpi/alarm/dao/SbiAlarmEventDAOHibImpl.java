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




import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.kpi.alarm.metadata.SbiAlarmEvent;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * 
 * @see it.eng.spagobi.kpi.alarm.metadata.SbiAlarmEvent
 * @author Enrico Cesaretti
 */
public class SbiAlarmEventDAOHibImpl extends AbstractHibernateDAO implements ISbiAlarmEventDAO {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(SbiAlarmEventDAOHibImpl.class);

	
    public void insert(SbiAlarmEvent item) {
        Session session = getSession();
        Transaction tx = null;
        try {
        	tx = session.beginTransaction();
        	updateSbiCommonInfo4Insert(item);
			session.save(item);
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
    
    
  /*  public void insert(Session session, SbiAlarmEvent item) {
        session.save(item);
    }
*/
    public void update(SbiAlarmEvent item) {
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
    public void update(Session session, SbiAlarmEvent item) {
        session.update(item);
    }	
	*/
    public void delete(SbiAlarmEvent item) {
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
    
    public void delete(Session session, SbiAlarmEvent item) {
       session.delete(item);
    }

    public void delete(Integer id) {
        Session session = getSession();
        Transaction tx = null;
        try {
        	tx = session.beginTransaction();
        	session.delete(session.load(SbiAlarmEvent.class, id));
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
       	session.delete(session.load(SbiAlarmEvent.class, id));
    }
	
	@SuppressWarnings("unchecked")
    public SbiAlarmEvent findById(Integer id) {
        Session session = getSession();
        Transaction tx = null;
        try {
        	tx = session.beginTransaction();
			SbiAlarmEvent item = (SbiAlarmEvent)session.get(SbiAlarmEvent.class, id);
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

    @SuppressWarnings("unchecked")
	public List<SbiAlarmEvent> findAll() {
        Session session = getSession();
        Transaction tx = null;
        try {
        	tx = session.beginTransaction();
			
			List<SbiAlarmEvent> list = (List<SbiAlarmEvent>)session.createQuery("from SbiAlarmEvent").list();
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
	public List<SbiAlarmEvent> findActive() 
	{
		Session session = getSession();
	        Transaction tx = null;
	        try {
	        	tx = session.beginTransaction();
	        	List<SbiAlarmEvent> list = (List<SbiAlarmEvent>)session.createQuery("from SbiAlarmEvent where active='T'").list();
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
	public List<SbiAlarmEvent> findActive(Session session) 
	{
	    List<SbiAlarmEvent> list = (List<SbiAlarmEvent>)session.createQuery("from SbiAlarmEvent where active='T'").list();	
	    return list;
	}


}

