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
package it.eng.spagobi.community.service;

import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
@Path("/subscribe")
public class SubscribeAction {
	
	@Path("/getUserInfo")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public String getUserInfo(@Context HttpServletRequest req) {
		
		String firstName = (String)req.getParameter("firstname");
		String lastName = (String)req.getParameter("lastname");
		
		JSONObject result = null;
		try {
			result = new JSONObject();
			if (firstName != null){
				result.put("firstname", firstName);
			}
			if (lastName != null){
				result.put("lastname", lastName);
			}
		} catch (Throwable t) {
			throw new SpagoBIServiceException(
					"An unexpected error occured while executing the subscribe action", t);
		}

		return result.toString();
		
	}

}
