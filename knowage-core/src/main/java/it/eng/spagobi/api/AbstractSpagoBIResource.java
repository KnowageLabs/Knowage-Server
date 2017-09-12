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

package it.eng.spagobi.api;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.engines.EngineStartServletIOManager;
import it.eng.spagobi.utilities.engines.rest.AbstractRestService;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;

/**
 *
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class AbstractSpagoBIResource extends AbstractRestService {

	@Context
	protected HttpServletRequest request;
	@Context
	protected HttpServletResponse response;

	public static transient Logger logger = Logger.getLogger(AbstractSpagoBIResource.class);

	public EngineStartServletIOManager getIOManager() {
		EngineStartServletIOManager ioManager = null;

		try {
			ioManager = new EngineStartServletIOManager(request, response);
			UserProfile userProfile = getUserProfile();
			if (userProfile == null) {
				String userId = request.getHeader("user");
				userProfile = (UserProfile) UserUtilities.getUserProfile(userId);
				ioManager.setUserProfile(userProfile);
			}
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("An unexpected error occured while inizializing ioManager", t);
		}

		return ioManager;
	}

	@Override
	public HttpServletRequest getServletRequest() {
		return request;
	}

	public HttpServletResponse getServletResponse() {
		return response;
	}

	public UserProfile getUserProfile() {
		return UserProfileManager.getProfile();
	}

}
