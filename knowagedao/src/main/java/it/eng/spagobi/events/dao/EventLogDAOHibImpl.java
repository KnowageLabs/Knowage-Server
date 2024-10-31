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
package it.eng.spagobi.events.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.dao.PagedList;
import it.eng.spagobi.events.bo.EventLog;
import it.eng.spagobi.events.bo.EventType;
import it.eng.spagobi.events.metadata.SbiEventsLog;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Gioia
 *
 */
public class EventLogDAOHibImpl extends AbstractHibernateDAO implements IEventLogDAO {

	private static Logger logger = Logger.getLogger(EventLogDAOHibImpl.class);

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
	@Override
	public EventLog loadEventLogById(Integer id) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		EventLog realResult = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiEventsLog aSbiEventsLog = (SbiEventsLog) aSession.get(SbiEventsLog.class, id);

			if (aSbiEventsLog == null) {
				return null;
			}

			realResult = toEventsLog(aSbiEventsLog);

			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			rollbackIfActive(tx);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		logger.debug("OUT");
		return realResult;

	}

	public PagedList<EventLog> loadAllEventsLog(int offset, int fetchsize, Date startDate, Date endDate,
			String creationUser, String type, String sortingColumn, boolean sortingAscending) {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		PagedList<EventLog> toReturn = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Criteria criteria = getBaseFilteringCriteria(startDate, endDate, creationUser, type, aSession);
			int total = getTotalNumber(criteria);

			addOrderingCriteria(criteria, sortingColumn, sortingAscending);

			criteria.setFirstResult(offset);
			criteria.setMaxResults(fetchsize);

			List hibList = criteria.list();

			Iterator it = hibList.iterator();
			List<EventLog> results = new ArrayList<>();
			while (it.hasNext()) {
				results.add(toEventsLog((SbiEventsLog) it.next()));
			}
			int start = offset + 1;
			toReturn = new PagedList<>(results, total, start);
		} catch (HibernateException he) {
			logException(he);
			rollbackIfActive(tx);
			throw new SpagoBIRuntimeException("Error loading events", he);
		} finally {
			closeSessionIfOpen(aSession);
			logger.debug("OUT");
		}
		return toReturn;
	}

	private Criteria getBaseFilteringCriteria(Date startDate, Date endDate, String creationUser, String type,
			Session aSession) {
		Criteria criteria = aSession.createCriteria(SbiEventsLog.class);
		if (startDate != null) {
			criteria.add(Restrictions.ge("date", startDate));
		}
		if (endDate != null) {
			criteria.add(Restrictions.le("date", endDate));
		}
		if (!StringUtils.isEmpty(creationUser)) {
			criteria.add(Restrictions.eq("user", creationUser));
		}
		if (!StringUtils.isEmpty(type)) {
			EventType eventType = EventType.valueOf(type);
			criteria.add(Restrictions.eq("eventType", eventType));
		}
		return criteria;
	}

	private void addOrderingCriteria(Criteria criteria, String sortingColumn, boolean sortingAscending) {
		if (!StringUtils.isEmpty(sortingColumn)) {
			if (sortingAscending) {
				criteria.addOrder(Order.asc(sortingColumn));
			} else {
				criteria.addOrder(Order.desc(sortingColumn));
			}
		} else {
			// sorting descending by date as default
			criteria.addOrder(Order.desc("date"));
		}
	}

	/**
	 * Load events log by user.
	 *
	 * @see it.eng.spagobi.events.dao.IEventLogDAO#loadEventsLogByUser(it.eng.spago.security.IEngUserProfile)
	 */
	@Override
	public PagedList<EventLog> loadEventsLogByUser(UserProfile profile, int offset, int fetchsize, Date startDate,
			Date endDate, String creationUser, String type, String sortingColumn, boolean sortingAscending) {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		PagedList<EventLog> toReturn = null;
		// String hql = null;
		// Query hqlQuery = null;

		Collection<String> roles = getUserRoles(profile);
		if (roles == null || roles.size() == 0) {
			return PagedList.emptyList(EventLog.class);
		}

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Criteria criteria = getBaseFilteringCriteria(startDate, endDate, creationUser, type, aSession);
			// adding restrictions about user's role
			criteria.createAlias("roles", "role");
			criteria.add(Restrictions.in("role.name", roles));

			int total = getTotalNumber(criteria);

			addOrderingCriteria(criteria, sortingColumn, sortingAscending);

			criteria.setFirstResult(offset);
			criteria.setMaxResults(fetchsize);

			List hibList = criteria.list();

			Iterator it = hibList.iterator();
			List<EventLog> results = new ArrayList<>();
			while (it.hasNext()) {
				results.add(toEventsLog((SbiEventsLog) it.next()));
			}
			int start = offset + 1;
			toReturn = new PagedList<>(results, total, start);

		} catch (HibernateException he) {
			logException(he);
			rollbackIfActive(tx);
			throw new SpagoBIRuntimeException("Error while loading events for user " + profile.getUserId(), he);
		} finally {
			closeSessionIfOpen(aSession);
		}
		logger.debug("OUT");
		return toReturn;
	}

	private Collection<String> getUserRoles(UserProfile profile) {
		Collection<String> roles = null;
		try {
			roles = profile.getRolesForUse();
		} catch (EMFInternalError e) {
			logException(e);
			throw new SpagoBIRuntimeException("Error while getting user roles", e);
		}

		// TODO : do we really need the following code????
		List<String> roleNames = new ArrayList<>();
		Iterator<String> rolesIt = roles.iterator();
		while (rolesIt.hasNext()) {
			String roleName = rolesIt.next();
			if (!roleNames.contains(roleName)) {
				roleNames.add(roleName);
			}
		}

		return roleNames;
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
	@Override
	public Integer insertEventLog(EventLog eventLog) throws EMFUserError {
		logger.debug("IN");
		Session session = null;
		Transaction tx = null;
		try {
			session = getSession();
			tx = session.beginTransaction();

			SbiEventsLog hibEventLog = new SbiEventsLog();
			hibEventLog.setUser(eventLog.getUser());
			hibEventLog.setDate(eventLog.getDate());
			hibEventLog.setDesc(eventLog.getDesc());
			hibEventLog.setParams(eventLog.getParams());
			hibEventLog.setEventType(eventLog.getType());
			this.updateSbiCommonInfo4Insert(hibEventLog);
			session.save(hibEventLog);
			tx.commit();
			return hibEventLog.getId();
		} catch (HibernateException he) {
			logException(he);

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			closeSessionIfOpen(session);
			logger.debug("OUT");
		}
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
	@Override
	public void eraseEventLog(EventLog eventLog) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiEventsLog hibEventLog = (SbiEventsLog) aSession.load(SbiEventsLog.class, eventLog.getId());
			// Set roles = hibEventLog.getRoles();
			// Iterator rolesIt = roles.iterator();
			// while (rolesIt.hasNext()) {
			// SbiEventRole aSbiEventRole = (SbiEventRole) rolesIt.next();
			// aSession.delete(aSbiEventRole);
			// }
			aSession.delete(hibEventLog);
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			rollbackIfActive(tx);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} catch (Exception ex) {
			logException(ex);
			rollbackIfActive(tx);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		logger.debug("OUT");
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
	@Override
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

			/*
			 * hql = "from SbiEventsLog as eventlog " + "where eventlog.user = '" + user + "'";
			 */

			hql = "from SbiEventsLog as eventlog where eventlog.user = ?";

			hqlQuery = aSession.createQuery(hql);
			hqlQuery.setString(0, user);
			events = hqlQuery.list();

			Iterator it = events.iterator();
			while (it.hasNext()) {
				SbiEventsLog aSbiEventsLog = (SbiEventsLog) it.next();
				// Set roles = aSbiEventsLog.getRoles();
				// Iterator rolesIt = roles.iterator();
				// while (rolesIt.hasNext()) {
				// SbiEventRole aSbiEventRole = (SbiEventRole) rolesIt.next();
				// aSession.delete(aSbiEventRole);
				// }
				aSession.delete(it.next());
			}
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			rollbackIfActive(tx);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} catch (Exception ex) {
			logException(ex);
			rollbackIfActive(tx);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		logger.debug("OUT");
	}

	private EventLog toEventsLog(SbiEventsLog hibEventLog) {
		logger.debug("IN");
		EventLog eventLog = new EventLog();
		eventLog.setId(hibEventLog.getId());
		eventLog.setUser(hibEventLog.getUser());
		eventLog.setDate(hibEventLog.getDate());
		eventLog.setDesc(hibEventLog.getDesc());
		eventLog.setParams(hibEventLog.getParams());
		eventLog.setType(hibEventLog.getEventType());
		logger.debug("OUT");
		return eventLog;
	}

}
