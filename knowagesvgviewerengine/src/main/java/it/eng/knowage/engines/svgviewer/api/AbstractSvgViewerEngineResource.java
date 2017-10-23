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

import it.eng.knowage.engines.svgviewer.SvgViewerEngineConstants;
import it.eng.knowage.engines.svgviewer.SvgViewerEngineInstance;
import it.eng.spago.base.SourceBean;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.ParametersDecoder;
import it.eng.spagobi.utilities.cache.CacheInterface;
import it.eng.spagobi.utilities.cache.TemplateCache;
import it.eng.spagobi.utilities.callbacks.mapcatalogue.MapCatalogueAccessUtils;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.EngineStartServletIOManager;
import it.eng.spagobi.utilities.engines.rest.AbstractEngineRestService;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 *
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class AbstractSvgViewerEngineResource extends AbstractEngineRestService {

	private static final String ENGINE_NAME = "SvgViewerEngine";

	@Context
	protected HttpServletRequest request;
	@Context
	protected HttpServletResponse response;

	public static transient Logger logger = Logger.getLogger(AbstractSvgViewerEngineResource.class);

	public EngineStartServletIOManager getIOManager() {
		Monitor getIOManagerMonitor = MonitorFactory.start("GeoEngine.AbstractSvgViewerEngineResource.getIOManager");

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
		getIOManagerMonitor.stop();

		return ioManager;
	}

	/**
	 * Retrieve the document template using the cache (if found)
	 *
	 * @return
	 */
	public SourceBean getTemplate() {
		HashMap requestParameters = ParametersDecoder.getDecodedRequestParameters(request);
		String document_version_id = (String) requestParameters.get("DOCUMENT_VERSION");

		CacheInterface cache = TemplateCache.getCache();
		boolean isCachedTemplate = cache.contains(document_version_id);
		if (isCachedTemplate) {
			logger.debug("Retrieving template from cache for document: " + document_version_id);
			return (SourceBean) cache.get(document_version_id);
		} else {
			logger.debug("Retrieving template from service for document: " + document_version_id);
			SourceBean template = getTemplateAsSourceBean();
			cache.put(document_version_id, template);
			return template;
		}

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

	public void setEngineInstance(SvgViewerEngineInstance engineInstance) {
		getIOManager().getHttpSession().setAttribute(EngineConstants.ENGINE_INSTANCE, engineInstance);
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

	@Override
	public UserProfile getUserProfile() {
		return UserProfileManager.getProfile();
	}

	protected Map getEngineEnv() throws Exception {

		UserProfile userProfile = (UserProfile) getIOManager().getParameterFromSession(IEngUserProfile.ENG_USER_PROFILE);
		String userUniqueIdentifier = (String) userProfile.getUserUniqueIdentifier();

		MapCatalogueAccessUtils mapCatalogueServiceProxy = new MapCatalogueAccessUtils(getHttpSession(), userUniqueIdentifier);
		// String standardHierarchy = mapCatalogueServiceProxy.getStandardHierarchy();

		Map env = getIOManager().getEnv();

		// Add extra Environment variables specific for this engine
		env.put(SvgViewerEngineConstants.ENV_MAPCATALOGUE_SERVICE_PROXY, mapCatalogueServiceProxy);

		env.put(SvgViewerEngineConstants.ENV_CONTEXT_URL, getContextUrl());

		env.put(SvgViewerEngineConstants.ENV_ABSOLUTE_CONTEXT_URL, getAbsoluteContextUrl());

		return env;
	}

	private String getContextUrl() {
		String contextUrl = null;

		contextUrl = request.getContextPath();
		logger.debug("Context path: " + contextUrl);

		return contextUrl;
	}

	private String getAbsoluteContextUrl() {
		String contextUrl = null;

		contextUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/" + getContextUrl();
		logger.debug("Context path: " + contextUrl);

		return contextUrl;
	}

	@Override
	public String getEngineName() {
		return ENGINE_NAME;
	}

}
