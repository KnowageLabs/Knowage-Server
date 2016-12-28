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

import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.dao.SubreportDAOHibImpl;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.bo.Subreport;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.events.EventsManager;
import it.eng.spagobi.events.bo.EventLog;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;


@Path("/2.0/events")
@ManageAuthorization
public class EventResource extends AbstractSpagoBIResource {

	static protected Logger logger = Logger.getLogger(EventResource.class);

	@SuppressWarnings("unchecked")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.EVENTS_MANAGEMENT })
	public List<EventLog> getAllEvents(@QueryParam("newPageNumber") Integer page, @QueryParam("itemsPerPage") Integer items
			, @QueryParam("searchValue") String searchValue, @QueryParam("columnOrdering") String columnOrdering, @QueryParam("reverseOrdering") String reverseOrdering) {

		
		EventsManager eventsManager = EventsManager.getInstance();	
		
		//the columns in the HANDLER that have value like talend, commonj and default. Generic is mapped in default
		if(searchValue!=null && "generic".contains(searchValue.toLowerCase())){
			searchValue = "default";
		}
		
		Map<String, Object> filters = new HashMap<String, Object>();
		filters.put("page", page);
		filters.put("ItemPerPage", items);
		filters.put("searchValue", (searchValue));
		filters.put("columnOrdering", purgeNames(columnOrdering));
		filters.put("reverseOrdering", reverseOrdering);
		
		List<EventLog> firedEventsList = eventsManager.getRegisteredEvents(getUserProfile(),filters);
        
	
		
		return firedEventsList;
	}

	public String purgeNames(String value){
		if(value==null || (!value.equals("formattedDescription") && !value.equals("formattedDate"))){
			return null;
		}
		if(value.equals("formattedDescription")){
			return "desc";
		}
		if(value.equals("formattedDate")){
			return "date";
		}
		return value;
	}
	
	
	@SuppressWarnings("unchecked")
	@GET
	@Path("/size")
	@Produces(MediaType.TEXT_PLAIN)
	@UserConstraint(functionalities = { SpagoBIConstants.EVENTS_MANAGEMENT })
	public int getEventSize( ) {

		
		EventsManager eventsManager = EventsManager.getInstance();		
		int firedEventsList = eventsManager.getRegisteredEventsSize(getUserProfile());
             
		return firedEventsList;
	}


}
