/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2019 Engineering Ingegneria Informatica S.p.A.
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

package it.eng.knowage.backendservices.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.user.UserProfileManager;

@Path("/2.0/backendservices/userprofile")
public class UserProfileResource {

	static protected Logger logger = Logger.getLogger(UserProfileResource.class);

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public UserProfile getUserProfile() {
		logger.debug("IN");
		UserProfile userProfile = UserProfileManager.getProfile();
		logger.debug("OUT");
		return userProfile;
	}

}
