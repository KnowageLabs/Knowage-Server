/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.events.dao;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.events.bo.Event;
import it.eng.spagobi.events.metadata.SbiEvents;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * @author Gioia
 *
 */
public class EventDAOHibImpl extends AbstractHibernateDAO implements IEventDAO{
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.events.dao.IEventDAO#loadEvent(java.lang.Integer, java.lang.String)
	 */
	public Event loadEvent(Integer eventId, String user) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		Event realResult = null;
		String hql = null;
		Query hqlQuery = null;
		
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
		
			SbiEvents hibEvent = (SbiEvents)aSession.load(SbiEvents.class, eventId);
			
			realResult = toEvent(hibEvent);
			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}
		return realResult;
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.events.dao.IEventDAO#loadEvents(java.lang.String)
	 */
	public List loadEvents(String user) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		List realResult = new ArrayList();
		String hql = null;
		Query hqlQuery = null;
		
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			/*hql = "from SbiEvents as event " + 
	         "where event.user = '" + user + "'";*/
			
			hql = "from SbiEvents as event " + 
	         "where event.user = ?" ;
			
			hqlQuery = aSession.createQuery(hql);
			hqlQuery.setString(0, user);
			List hibList = hqlQuery.list();
			
			Iterator it = hibList.iterator();

			while (it.hasNext()) {
				realResult.add(toEvent((SbiEvents) it.next()));
			}
			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}
		return realResult;
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.events.dao.IEventDAO#registerEvent(java.lang.String)
	 */
	public Integer registerEvent(String user) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		aSession = getSession();
		
		tx = aSession.beginTransaction();
		SbiEvents hibEvent = new SbiEvents();
		hibEvent.setUser(user);
		updateSbiCommonInfo4Insert(hibEvent);
		aSession.save(hibEvent);	
		tx.commit();
		return hibEvent.getId();
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.events.dao.IEventDAO#unregisterEvent(java.lang.Integer, java.lang.String)
	 */
	public void unregisterEvent(Integer id, String user) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		String hql = null;
		Query hqlQuery = null;
		
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();			
			SbiEvents hibEvent = new SbiEvents(id, user);			
			aSession.delete(hibEvent);
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} catch (Exception ex) {
			logException(ex);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100); 
		}	finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.events.dao.IEventDAO#unregisterEvent(it.eng.spagobi.events.bo.Event)
	 */
	public void unregisterEvent(Event event) throws EMFUserError {
		unregisterEvent(event.getId(), event.getUser());		
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.events.dao.IEventDAO#unregisterEvents(java.lang.String)
	 */
	public void unregisterEvents(String user) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		String hql = null;
		Query hqlQuery = null;
		List events = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			
			/*hql = "from SbiEvents as event " + 
	         "where event.user = '" + user + "'";*/
			
			hql = "from SbiEvents as event " + 
	         "where event.user = ?" ;
			
			hqlQuery = aSession.createQuery(hql);
			hqlQuery.setString(0, user);
			events = hqlQuery.list();
			
			Iterator it = events.iterator();
			while (it.hasNext()) {
				aSession.delete((SbiEvents) it.next());
			}			
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} catch (Exception ex) {
			logException(ex);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100); 
		}	finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}		
	}	

	private Event toEvent(SbiEvents hibEvent) {
		Event event = new Event();
		event.setId(hibEvent.getId());
		event.setUser(hibEvent.getUser());
		return event;
	}
	

}
