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
package it.eng.spagobi.events;

import java.util.Date;

import org.apache.log4j.Logger;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.dao.PagedList;
import it.eng.spagobi.events.bo.EventLog;
import it.eng.spagobi.events.dao.EventLogDAOHibImpl;

public class EventsAccessController {

	static private Logger logger = Logger.getLogger(EventsAccessController.class);

	private UserProfile userProfile;

	public EventsAccessController(UserProfile userProfile) {
		this.userProfile = userProfile;
	}

	public PagedList<EventLog> loadEventsLogList(int offset, int fetchsize, Date startDate, Date endDate, String creationUser, String type,
			String sortingColumn, boolean sortingAscending) {
		logger.debug("IN");
		try {
			EventLogDAOHibImpl eventLogDAO = new EventLogDAOHibImpl();
			if (UserUtilities.isAdministrator(userProfile)) {
				logger.debug("User is admin, loading all events list ...");
				return eventLogDAO.loadAllEventsLog(offset, fetchsize, startDate, endDate, creationUser, type, sortingColumn, sortingAscending);
			} else {
				logger.debug("User is not admin, loading events that are visible by his roles ...");
				return eventLogDAO.loadEventsLogByUser(userProfile, offset, fetchsize, startDate, endDate, creationUser, type, sortingColumn, sortingAscending);
			}
		} finally {
			logger.debug("OUT");
		}
	}

}
