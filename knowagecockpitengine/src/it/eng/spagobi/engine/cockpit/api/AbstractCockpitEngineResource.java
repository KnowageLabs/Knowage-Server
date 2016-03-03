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
package it.eng.spagobi.engine.cockpit.api;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.engine.cockpit.CockpitEngineInstance;
import it.eng.spagobi.engine.cockpit.CockpitEngineRuntimeException;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.EngineStartServletIOManager;
import it.eng.spagobi.utilities.engines.rest.AbstractRestService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;

/**
 * 
 * @author Zerbetto Davide (davide.zerbetto@eng.it), Alberto Ghedin
 *         (alberto.ghedin@eng.it)
 * 
 */
public class AbstractCockpitEngineResource extends AbstractRestService {

	@Context
	protected HttpServletRequest request;
	@Context
	protected HttpServletResponse response;

	public static transient Logger logger = Logger.getLogger(AbstractCockpitEngineResource.class);

	public EngineStartServletIOManager getIOManager() {
		EngineStartServletIOManager ioManager = null;

		try {
			ioManager = new EngineStartServletIOManager(request, response);
			UserProfile userProfile = (UserProfile) ioManager.getParameterFromSession(IEngUserProfile.ENG_USER_PROFILE);
			if (userProfile == null) {
				String userId = request.getHeader("user");
				userProfile = (UserProfile) UserUtilities.getUserProfile(userId);
				ioManager.setUserProfile(userProfile);
			}
		} catch (Exception e) {
			throw new CockpitEngineRuntimeException("An unexpected error occured while inizializing ioManager", e);
		}

		return ioManager;
	}

	/**
	 * Gets the cockpit engine instance.
	 * 
	 * @return the console engine instance
	 */
	@Override
	public CockpitEngineInstance getEngineInstance() {

		CockpitEngineInstance engineInstance = (CockpitEngineInstance) getIOManager().getHttpSession().getAttribute(EngineConstants.ENGINE_INSTANCE);
		return engineInstance;
		// ExecutionSession es = getExecutionSession();
		// return (CockpitEngineInstance)es.getAttributeFromSession(
		// EngineConstants.ENGINE_INSTANCE );
		//
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.eng.spagobi.utilities.engines.rest.AbstractRestService#getServletRequest
	 * ()
	 */
	@Override
	public HttpServletRequest getServletRequest() {
		// TODO Auto-generated method stub
		return request;
	}

}
