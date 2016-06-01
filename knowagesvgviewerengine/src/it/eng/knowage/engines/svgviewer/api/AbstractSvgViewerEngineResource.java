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
package it.eng.knowage.engines.svgviewer.api;

import it.eng.knowage.engines.svgviewer.SvgViewerEngineInstance;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.EngineStartServletIOManager;
import it.eng.spagobi.utilities.engines.rest.AbstractRestService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;

/**
 *
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class AbstractSvgViewerEngineResource extends AbstractRestService {

	@Context
	protected HttpServletRequest request;
	@Context
	protected HttpServletResponse response;

	public static transient Logger logger = Logger.getLogger(AbstractSvgViewerEngineResource.class);

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
			throw new RuntimeException("An unexpected error occured while inizializing ioManager", e);
		}

		return ioManager;
	}

	/**
	 * Gets the svg viewer engine instance.
	 *
	 * @return the svg viewer engine instance
	 */
	@Override
	public SvgViewerEngineInstance getEngineInstance() {

		SvgViewerEngineInstance engineInstance = (SvgViewerEngineInstance) getIOManager().getHttpSession().getAttribute(EngineConstants.ENGINE_INSTANCE);
		// (GeoReportEngineInstance) req.getSession().getAttribute(EngineConstants.ENGINE_INSTANCE)
		return engineInstance;
		// ExecutionSession es = getExecutionSession();
		// return (GeoReportEngineInstance)es.getAttributeFromSession(
		// EngineConstants.ENGINE_INSTANCE );
		//
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.utilities.engines.rest.AbstractRestService#getServletRequest ()
	 */
	@Override
	public HttpServletRequest getServletRequest() {
		// TODO Auto-generated method stub
		return request;
	}

}
