/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.georeport.api;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.engines.georeport.GeoReportEngineInstance;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.EngineStartServletIOManager;
import it.eng.spagobi.utilities.engines.rest.AbstractRestService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;

/**
 *
 * @author
 *
 */
public class AbstractChartEngineResource extends AbstractRestService {

	@Context
	protected HttpServletRequest request;
	@Context
	protected HttpServletResponse response;

	public static transient Logger logger = Logger.getLogger(AbstractChartEngineResource.class);

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
	 * Gets the cockpit engine instance.
	 *
	 * @return the console engine instance
	 */
	@Override
	public GeoReportEngineInstance getEngineInstance() {

		GeoReportEngineInstance engineInstance = (GeoReportEngineInstance) getIOManager().getHttpSession().getAttribute(EngineConstants.ENGINE_INSTANCE);
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
