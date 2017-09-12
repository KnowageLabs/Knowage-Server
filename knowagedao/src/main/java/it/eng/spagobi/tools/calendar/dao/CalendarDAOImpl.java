package it.eng.spagobi.tools.calendar.dao;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.tools.calendar.metadata.Calendar;
import it.eng.spagobi.tools.calendar.metadata.CalendarAttribute;
import it.eng.spagobi.tools.calendar.metadata.CalendarAttributeDomain;
import it.eng.spagobi.tools.calendar.metadata.CalendarConfiguration;
import it.eng.spagobi.tools.calendar.metadata.TimeByDay;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CalendarDAOImpl implements ICalendarDAO {

	@Override
	public Calendar loadCalendarById(Integer id, Session session) throws EMFUserError {
		Calendar cal;

		try {
			cal = (Calendar) session.get(Calendar.class, id);

		} catch (HibernateException he) {
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		}

		return cal;
	}

	@Override
	public List<Calendar> loadCalendarList(final Session session) {
		return session.createCriteria(Calendar.class).list();

	}

	@Override
	public List generateCalendarDays(Integer id, Session session) throws EMFUserError {
		Calendar cal = loadCalendarById(id, session);
		java.util.Calendar c1 = java.util.Calendar.getInstance();
		java.util.Calendar c2 = java.util.Calendar.getInstance();
		c2.setTime(cal.getCalEndDay());
		c1.setTime(cal.getCalStartDay());
		TimeByDay time = loadCalendarConfigurationByDate(c1, session);

		Transaction tx2 = session.beginTransaction();
		while (c1.before(c2) || c1.equals(c2)) {
			Date date = c1.getTime();
			int year = date.getYear() + 1900;
			int month = date.getMonth() + 1;
			int day = date.getDate();
			Integer timeId = new Integer(year * 10000 + month * 100 + day);

			CalendarConfiguration calConf = new CalendarConfiguration();
			calConf.setTimeId(timeId);
			calConf.setCalendarId(cal.getCalendarId());
			calConf.setRecStatus("A");
			session.save(calConf);

			c1.add(java.util.Calendar.DAY_OF_MONTH, 1);
		}
		tx2.commit();
		List daysGenerated = loadCalendarDays(cal.getCalendarId(), session);
		return daysGenerated;

	}

	@Override
	public List<CalendarConfiguration> loadCalendarDays(Integer calId, Session session) {
		return session.createCriteria(CalendarConfiguration.class).add(Restrictions.eq("calendarId", calId)).addOrder(Order.asc("timeId")).list();
	}

	@Override
	public TimeByDay loadCalendarConfigurationByDate(java.util.Calendar date, Session session) throws EMFUserError {

		return (TimeByDay) session.createCriteria(TimeByDay.class).add(Restrictions.eq("dayOfMonth", date.get(java.util.Calendar.DAY_OF_MONTH)))
				.add(Restrictions.eq("monthOfYear", date.get(java.util.Calendar.MONTH) + 1)).add(Restrictions.eq("yearId", date.get(java.util.Calendar.YEAR)))
				.setMaxResults(1).uniqueResult();
	}

	@Override
	public Integer saveCalendar(JSONObject requestBodyJSON, Session session) {
		Calendar cal = new Calendar();
		Transaction tx = session.beginTransaction();

		try {
			cal.setCalendar(requestBodyJSON.getString("calendar"));
			Long dateOfCal = new Long(requestBodyJSON.getString("calEndDay"));
			Date date = new Date(dateOfCal);
			cal.setCalEndDay(date);
			dateOfCal = new Long(requestBodyJSON.getString("calStartDay"));
			date = new Date(dateOfCal);
			cal.setCalStartDay(date);
			cal.setCalType(requestBodyJSON.optString("calType"));
			cal.setRecStatus("A");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		session.save(cal);
		Integer idInserted = cal.getCalendarId();
		tx.commit();
		return idInserted;

	}

	@Override
	public void updateDaysGenerated(JSONArray requestBodyJSON, Session session, Integer idCalendar) {

		Transaction tx = session.beginTransaction();

		try {
			Calendar cal = loadCalendarById(idCalendar, session);
			for (int i = 0; i < requestBodyJSON.length(); i++) {
				JSONObject object = requestBodyJSON.getJSONObject(i);
				CalendarConfiguration conf = loadCalendarConfigurationById(object.getInt("idCalComposition"), session);
				conf.setIsHoliday(object.optInt("isHoliday"));
				conf.setPubHoliday(object.optString("pubHoliday"));
				if (object.optJSONArray("listOfAttributes") != null) {
					Set<CalendarAttribute> listOfAttributes = new HashSet();

					for (int j = 0; j < object.optJSONArray("listOfAttributes").length(); j++) {
						String value;
						Object attObj = object.optJSONArray("listOfAttributes").get(j);
						if (attObj instanceof JSONObject) {
							value = object.optJSONArray("listOfAttributes").getJSONObject(j).getJSONObject("calendarAttributeDomain")
									.getString("attributeDomainDescr");
						} else {
							value = attObj.toString();
						}
						CalendarAttributeDomain domain = loadDomainbyDescr(session, value);
						CalendarAttribute attribute = (CalendarAttribute) session.createCriteria(CalendarAttribute.class)
								.add(Restrictions.eq("domainId", domain.getDomainId())).add(Restrictions.eq("calendarId", cal.getCalendarId()))
								.setMaxResults(1).uniqueResult();
						if (attribute == null) {
							attribute = new CalendarAttribute();
							attribute.setCalendar(cal);
							attribute.setCalendarAttributeDomain(domain);
							attribute.setCalendarId(cal.getCalendarId());
							attribute.setDomainId(domain.getDomainId());
							attribute.setRecStatus("A");
							session.save(attribute);

						}
						listOfAttributes.add(attribute);

					}
					conf.setListOfAttributes(listOfAttributes);
				}

				session.saveOrUpdate(conf);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EMFUserError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		tx.commit();
	}

	@Override
	public List<CalendarAttributeDomain> loadCalendarDomains(Session session) {
		return session.createCriteria(CalendarAttributeDomain.class).add(Restrictions.eq("attributeDomain", "CALENDARDOMAIN"))
				.add(Restrictions.eq("recStatus", "A")).list();

	}

	@Override
	public CalendarAttribute loadAttribute(Integer id, Session session) {
		return (CalendarAttribute) session.createCriteria(CalendarAttribute.class).add(Restrictions.eq("attributeId", id)).uniqueResult();

	}

	private CalendarAttribute loadCalendarAttributebyCalId(Integer calId, Session session) {
		return (CalendarAttribute) session.createCriteria(CalendarAttribute.class).add(Restrictions.eq("calendarId", calId))
				.add(Restrictions.eq("domainId", 0)).uniqueResult();
	}

	@Override
	public void deleteCalendar(Integer id, Session session) throws EMFUserError {
		Calendar calToDelete = loadCalendarById(id, session);
		List<CalendarConfiguration> listDays = loadCalendarDays(id, session);
		Transaction tx = session.beginTransaction();

		for (CalendarConfiguration conf : listDays) {
			for (CalendarAttribute attr : conf.getListOfAttributes()) {
				if (attr != null) {
					session.delete(attr);
				}

			}
			session.delete(conf);
		}

		session.delete(calToDelete);
		tx.commit();
	}

	@Override
	public void deleteDayofCalendar(Integer id, Session session) throws EMFUserError {

		Transaction tx = session.beginTransaction();
		CalendarConfiguration conf = loadCalendarConfigurationById(id, session);

		session.delete(conf);

		for (CalendarAttribute attr : conf.getListOfAttributes()) {

			if (attr.getListOfConfiguration().size() == 1) {
				Iterator<CalendarConfiguration> it = attr.getListOfConfiguration().iterator();
				session.delete(attr);
			}
		}
		tx.commit();

	}

	private CalendarConfiguration loadCalendarConfigurationById(Integer id, Session session) throws EMFUserError {

		return (CalendarConfiguration) session.createCriteria(CalendarConfiguration.class).add(Restrictions.eq("idCalComposition", id)).uniqueResult();
	}

	private CalendarAttributeDomain loadDomainbyDescr(Session session, String descr) {
		return (CalendarAttributeDomain) session.createCriteria(CalendarAttributeDomain.class).add(Restrictions.eq("attributeDomain", "CALENDARDOMAIN"))
				.add(Restrictions.eq("attributeDomainDescr", descr)).uniqueResult();

	}
}
