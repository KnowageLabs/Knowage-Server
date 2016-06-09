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
package it.eng.spagobi.tools.calendar.dao;

import java.util.List;

import org.hibernate.Session;
import org.json.JSONArray;
import org.json.JSONObject;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.tools.calendar.metadata.Calendar;
import it.eng.spagobi.tools.calendar.metadata.CalendarAttribute;
import it.eng.spagobi.tools.calendar.metadata.CalendarAttributeDomain;
import it.eng.spagobi.tools.calendar.metadata.CalendarConfiguration;
import it.eng.spagobi.tools.calendar.metadata.TimeByDay;

/**
 * CRUD operations
 *
 * @author salvo l.
 *
 */
public interface ICalendarDAO {
	public Calendar loadCalendarById(Integer id, Session aidaSession) throws EMFUserError;

	public List<Calendar> loadCalendarList(Session aidaSession);

	public TimeByDay loadCalendarConfigurationByDate(java.util.Calendar date, Session session) throws EMFUserError;

	public List generateCalendarDays(Integer id, Session session) throws EMFUserError;

	public List<CalendarConfiguration> loadCalendarDays(Integer calId, Session session);

	public Integer saveCalendar(JSONObject requestBodyJSON, Session session);

	public void updateDaysGenerated(JSONArray requestBodyJSON, Session session, Integer idCalendar);

	public List<CalendarAttributeDomain> loadCalendarDomains(Session session);

	public CalendarAttribute loadAttribute(Integer id, Session session);

	public void deleteCalendar(Integer id, Session session) throws EMFUserError;

	public void deleteDayofCalendar(Integer id, Session session) throws EMFUserError;
}
