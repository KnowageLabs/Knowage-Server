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
package it.eng.spagobi.api.v2;

import java.util.Date;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.dao.PagedList;
import it.eng.spagobi.events.EventsAccessController;
import it.eng.spagobi.events.bo.EventLog;
import it.eng.spagobi.services.rest.annotations.DateFormat;
import it.eng.spagobi.services.rest.annotations.UserConstraint;

@Path("/2.0/events")
public class EventResource extends AbstractSpagoBIResource {

	static protected Logger logger = Logger.getLogger(EventResource.class);

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.EVENTS_MANAGEMENT })
	public PagedList<EventLog> getEventsLogList(
	// @formatter:off
			@DefaultValue("0") @QueryParam("offset") Integer offset,
			@DefaultValue("20") @QueryParam("fetchsize") Integer fetchsize,
			@QueryParam("startDate") @DateFormat("yyyy-MM-dd+HH:mm:ss") Date startDate,
			@QueryParam("endDate") @DateFormat("yyyy-MM-dd+HH:mm:ss") Date endDate,
			@QueryParam("creationUser") String creationUser,
			@QueryParam("type") String type,
			@DefaultValue("date") @QueryParam("sortingColumn") String sortingColumn,
			@DefaultValue("false") @QueryParam("sortingAscending") Boolean sortingAscending
			// @formatter:on
	) {
		EventsAccessController eventAccessController = new EventsAccessController(this.getUserProfile());
		PagedList<EventLog> toReturn = eventAccessController.loadEventsLogList(offset, fetchsize, startDate, endDate, creationUser, type, sortingColumn,
				sortingAscending);
		return toReturn;
	}

}
