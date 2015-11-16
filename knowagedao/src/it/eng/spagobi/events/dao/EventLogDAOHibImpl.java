/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.events.dao;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.metadata.SbiEventRole;
import it.eng.spagobi.commons.metadata.SbiEventRoleId;
import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.events.bo.EventLog;
import it.eng.spagobi.events.metadata.SbiEventsLog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;


/**
 * @author Gioia
 *
 */
public class EventLogDAOHibImpl extends AbstractHibernateDAO implements IEventLogDAO {

	static private Logger logger = Logger.getLogger(EventLogDAOHibImpl.class);

	/**
	 * Load event log by id.
	 * 
	 * @param id the id
	 * 
	 * @return the event log
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.events.dao.IEventLogDAO#loadEventLogById(Integer)
	 */
	public EventLog loadEventLogById(Integer id) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		EventLog realResult = null;
//		String hql = null;
//		Query hqlQuery = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiEventsLog aSbiEventsLog = (SbiEventsLog) aSession.get(SbiEventsLog.class, id);
//			hql = "from SbiEventsLog as eventlog " + 
//			"where eventlog.user = '" + user + "' and " + 
//			"eventlog.id = '" + id + "' and " +
//			"eventlog.date = :eventDate";

//			long time = Long.valueOf(date).longValue();

//			hqlQuery = aSession.createQuery(hql);
//			hqlQuery.setTimestamp("eventDate", new Date(time));
//			SbiEventsLog aSbiEventsLog = (SbiEventsLog) hqlQuery.uniqueResult();

			if (aSbiEventsLog == null) return null; 

			realResult = toEventsLog(aSbiEventsLog);

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
		logger.debug("OUT");
		return realResult;

	}


	/**
	 * Load events log by user.
	 * 
	 * @param profile the profile
	 * 
	 * @return the list
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.events.dao.IEventLogDAO#loadEventsLogByUser(it.eng.spago.security.IEngUserProfile)
	 */
	public List loadEventsLogByUser(IEngUserProfile profile) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List realResult = new ArrayList();
		String hql = null;
		Query hqlQuery = null;
		Collection roles = null;

		try {
			roles = ((UserProfile)profile).getRolesForUse();
		} catch (EMFInternalError e) {
			logException(e);
			return new ArrayList();
		}

		if (roles == null || roles.size() == 0) return new ArrayList();
		boolean isFirtElement = true;
		String collectionRoles = "";
		List roleNames = new ArrayList();
		Iterator rolesIt = roles.iterator();
		while (rolesIt.hasNext()) {
			String roleName = (String) rolesIt.next();
			//if (isFirtElement) {
			//collectionRoles += roleName;
			//isFirtElement = false;
			//} else {
			//collectionRoles += "', '" + roleName;
			if (!roleNames.contains(roleName)) roleNames.add(roleName);
			//}
		}
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			/*hql = 
				"select " +
					"eventlog " +
				"from " +
					"SbiEventsLog as eventlog, " +
					"SbiEventRole as eventRole, " +
					"SbiExtRoles as roles " + 
	         	"where " +
	         		"eventlog.id = eventRole.id.event.id and " +
	         		"eventRole.id.role.extRoleId = roles.extRoleId " +
	         		"and " +
	         		"roles.name in ('" + collectionRoles + "') " +
	         	"order by " +
	         		"eventlog.date";*/

			hql = 
				"select " +
				"eventlog " +
				"from " +
				"SbiEventsLog as eventlog, " +
				"SbiEventRole as eventRole, " +
				"SbiExtRoles as roles " + 
				"where " +
				"eventlog.id = eventRole.id.event.id and " +
				"eventRole.id.role.extRoleId = roles.extRoleId " +
				"and " +
				"roles.name in (:ROLE_NAMES) " +
				"order by " +
				"eventlog.date";

			hqlQuery = aSession.createQuery(hql);
			//hqlQuery.setString(0, collectionRoles);
			hqlQuery.setParameterList("ROLE_NAMES", roleNames);
			List hibList = hqlQuery.list();

			Iterator it = hibList.iterator();

			while (it.hasNext()) {
				realResult.add(toEventsLog((SbiEventsLog) it.next()));
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
		logger.debug("OUT");
		return realResult;
	}

	/**
	 * Insert event log.
	 * 
	 * @param eventLog the event log
	 * 
	 * @return the integer
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.events.dao.IEventLogDAO#insertEventLog(it.eng.spagobi.events.bo.EventLog)
	 */
	public Integer insertEventLog(EventLog eventLog) throws EMFUserError {
		logger.debug("IN");
		Session session = null;
		Transaction tx = null;
		session = getSession();
		tx = session.beginTransaction();
		//SbiEventsLog hibEventLog = toSbiEventsLog(aSession, eventLog);

		SbiEventsLog hibEventLog = new SbiEventsLog();
		//hibEventLog.setId(eventLog.getId());
		hibEventLog.setUser(eventLog.getUser());
		hibEventLog.setDate(eventLog.getDate());
		hibEventLog.setDesc(eventLog.getDesc());
		hibEventLog.setParams(eventLog.getParams());
		hibEventLog.setHandlerClass(eventLog.getHandler());
		this.updateSbiCommonInfo4Insert(hibEventLog);
		session.save(hibEventLog);
		Set hibEventRoles = new HashSet();
		List roles = eventLog.getRoles();
		Iterator rolesIt = roles.iterator();
		while (rolesIt.hasNext()) {
			String roleName = (String) rolesIt.next();
			/*String hql = "from SbiExtRoles as roles " + 
	         "where roles.name = '" + roleName + "'";*/

			String hql = "from SbiExtRoles as roles " + 
			"where roles.name = ?";

			Query hqlQuery = session.createQuery(hql);
			hqlQuery.setString(0, roleName);
			SbiExtRoles aHibRole = (SbiExtRoles) hqlQuery.uniqueResult();
			if (aHibRole == null) {
				logger.error("Role with name = '" + roleName + "' does not exist!!");
				continue;
			}
			SbiEventRoleId eventRoleId = new SbiEventRoleId();
			eventRoleId.setEvent(hibEventLog);
			eventRoleId.setRole(aHibRole);
			SbiEventRole aSbiEventRole = new SbiEventRole(eventRoleId);
			session.save(aSbiEventRole);
			hibEventRoles.add(aSbiEventRole);
		}
		hibEventLog.setRoles(hibEventRoles);
		tx.commit();
		logger.debug("OUT");
		return hibEventLog.getId();
	}

	/**
	 * Erase event log.
	 * 
	 * @param eventLog the event log
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.events.dao.IEventLogDAO#eraseEventLog(it.eng.spagobi.events.bo.EventLog)
	 */
	public void eraseEventLog(EventLog eventLog) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();			
			SbiEventsLog hibEventLog = (SbiEventsLog) aSession.load(SbiEventsLog.class, eventLog.getId());
			Set roles = hibEventLog.getRoles();
			Iterator rolesIt = roles.iterator();
			while (rolesIt.hasNext()) {
				SbiEventRole aSbiEventRole = (SbiEventRole) rolesIt.next();
				aSession.delete(aSbiEventRole);
			}
			aSession.delete(hibEventLog);
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
		}logger.debug("OUT");
	}

	/**
	 * Erase events log by user.
	 * 
	 * @param user the user
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.events.dao.IEventLogDAO#eraseEventsLogByUser(String)
	 */
	public void eraseEventsLogByUser(String user) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		String hql = null;
		Query hqlQuery = null;
		List events = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			/*hql = "from SbiEventsLog as eventlog " + 
	         "where eventlog.user = '" + user + "'";*/

			hql = "from SbiEventsLog as eventlog " + 
			"where eventlog.user = ?";

			hqlQuery = aSession.createQuery(hql);
			hqlQuery.setString(0, user);
			events = hqlQuery.list();

			Iterator it = events.iterator();
			while (it.hasNext()) {
				SbiEventsLog aSbiEventsLog = (SbiEventsLog) it.next();
				Set roles = aSbiEventsLog.getRoles();
				Iterator rolesIt = roles.iterator();
				while (rolesIt.hasNext()) {
					SbiEventRole aSbiEventRole = (SbiEventRole) rolesIt.next();
					aSession.delete(aSbiEventRole);
				}
				aSession.delete((SbiEventsLog) it.next());
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
		} logger.debug("OUT");		
	}

	private EventLog toEventsLog(SbiEventsLog hibEventLog) {
		logger.debug("IN");
		EventLog eventLog = new EventLog();
		eventLog.setId(hibEventLog.getId());
		eventLog.setUser(hibEventLog.getUser());
		eventLog.setDate(hibEventLog.getDate());
		eventLog.setDesc(hibEventLog.getDesc());
		eventLog.setParams(hibEventLog.getParams());
		eventLog.setHandler(hibEventLog.getHandlerClass());
		List roles = new ArrayList();
		Set rolesSet = hibEventLog.getRoles();
		Iterator rolesIt = rolesSet.iterator();
		while (rolesIt.hasNext()) {
			SbiEventRole hibEventRole = (SbiEventRole) rolesIt.next();
			SbiExtRoles hibRole = hibEventRole.getId().getRole();
			roles.add(hibRole.getName());
		}
		eventLog.setRoles(roles);
		logger.debug("OUT");
		return eventLog;
	}

}
