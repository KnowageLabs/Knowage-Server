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

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.calendar.dto.SaveCalendarRequestDTO;
import it.eng.spagobi.calendar.dto.UpdateDaysRequestDTO;
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

	public Integer saveCalendar(SaveCalendarRequestDTO requestDTO, Session session);

	public Calendar loadCalendarById(Session aidaSession, Integer id) throws EMFUserError;

	public List<Calendar> loadCalendarList(Session aidaSession);

	public TimeByDay loadCalendarConfigurationByDate(Session session, java.util.Calendar date) throws EMFUserError;

	public List generateCalendarDays(Session session, Integer id) throws EMFUserError;

	public List<CalendarConfiguration> loadCalendarDays(Session session, Integer calId);

	public List<CalendarAttributeDomain> loadCalendarDomains(Session session);

	public CalendarAttribute loadAttribute(Session session, Integer id);

	public void deleteCalendar(Session session, Integer id) throws EMFUserError;

	public void deleteDayofCalendar(Session session, Integer id) throws EMFUserError;

	public void updateDaysGenerated(Session session, Integer idCalendar, List<UpdateDaysRequestDTO> days);

}
