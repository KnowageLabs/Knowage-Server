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
package it.eng.knowage.engine.kpi.api.page;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.jboss.resteasy.plugins.providers.html.View;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.knowage.engine.kpi.KpiEngine;
import it.eng.knowage.engine.kpi.KpiEngineInstance;
import it.eng.knowage.engine.kpi.KpiEngineRuntimeException;
import it.eng.knowage.engine.kpi.api.AbstractFullKpiEngineResource;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.EngineStartServletIOManager;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @authors
 *
 */

@Path("/1.0/pages")
public class PageResource extends AbstractFullKpiEngineResource {

	static private Map<String, JSONObject> pages;
	static private Map<String, String> urls;

	static private Logger logger = Logger.getLogger(PageResource.class);

	/**
	 * TODO Tutte le pagine dell'engine
	 *
	 */
	static {
		pages = new HashMap<String, JSONObject>();
		urls = new HashMap<String, String>();

		try {
			pages.put("edit", new JSONObject("{name: 'edit', description: 'the kpi edit page', parameters: []}"));
			urls.put("edit", "/WEB-INF/jsp/kpiEdit.jsp");
			pages.put("execute", new JSONObject("{name: 'execute', description: 'the kpi execution page', parameters: ['template']}"));
			urls.put("execute", "/WEB-INF/jsp/kpiVisualization.jsp");
			// pages.put("test", new JSONObject("{name: 'test', description: 'the chart test page', parameters: ['template']}"));
			// urls.put("test", "/WEB-INF/jsp/test4.jsp");
			// pages.put("edit_cockpit", new JSONObject("{name: 'edit_cockpit', description: 'the chart edit page from cockpit', parameters: []}"));
			// urls.put("edit_cockpit", "/WEB-INF/jsp/chart_edit.jsp");
			pages.put("execute_cockpit",
					new JSONObject("{name: 'execute_cockpit', description: 'the chart execution page from cockpit', parameters: ['template']}"));
			urls.put("execute_cockpit", "/WEB-INF/jsp/kpiVisualization.jsp");
		} catch (JSONException t) {
			logger.error(t);
		}
	}

	/**
	 * TODO COMMENTARE
	 *
	 */
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public String getDataSets() {
		try {
			JSONArray resultsJSON = new JSONArray();
			Iterator<String> it = pages.keySet().iterator();
			while (it.hasNext()) {
				String pageName = it.next();
				resultsJSON.put(pages.get(pageName));
			}

			return resultsJSON.toString();
		} catch (Exception e) {
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException("", getEngineInstance(), e);
		} finally {
			logger.debug("OUT");
		}
	}

	@Override
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
			throw new KpiEngineRuntimeException("An unexpected error occured while inizializing ioManager", e);
		}

		return ioManager;
	}

	@SuppressWarnings("unchecked")
	@GET
	@Path("/{pagename}")
	@Produces("text/html")
	public View openPageGet(@PathParam("pagename") String pageName) {
		return openPage(pageName);
	}

	@SuppressWarnings("unchecked")
	@POST
	@Path("/{pagename}")
	@Produces("text/html")
	public View openPagePost(@PathParam("pagename") String pageName) {
		return openPage(pageName);
	}

	/**
	 * @param pageName
	 * @return
	 */
	private View openPage(String pageName) {
		KpiEngineInstance engineInstance;
		String dispatchUrl = urls.get(pageName);

		try {
			EngineStartServletIOManager ioManager = getIOManager();
			String savedTemplate = ioManager.getTemplateAsString();
			switch (pageName) {

			case "execute":
				// // TODO put this not in session but in context
				engineInstance = KpiEngine.createInstance(savedTemplate, getIOManager().getEnv());
				getIOManager().getHttpSession().setAttribute(EngineConstants.ENGINE_INSTANCE, engineInstance);
				break;

			case "edit":

				String templateString = savedTemplate != null ? savedTemplate : buildBaseTemplate().toString();

				// create a new engine instance
				engineInstance = KpiEngine.createInstance(templateString, getIOManager().getEnv());

				engineInstance.getEnv().put(EngineConstants.ENV_DOCUMENT_LABEL, getIOManager().getRequest().getParameter("document"));
				// TODO put this not in session but in context
				getIOManager().getHttpSession().setAttribute(EngineConstants.ENGINE_INSTANCE, engineInstance);

				break;

			case "test":
				break;

			default:
				dispatchUrl = "/WEB-INF/jsp/error.jsp";
				break;
			}

			return new View(dispatchUrl);
		} catch (Exception e) {
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException("", getEngineInstance(), e);
		} finally {
			logger.debug("OUT");
		}
	}

	@GET
	@Path("/executeTest")
	@Produces(MediaType.APPLICATION_JSON)
	public String testAction(@Context HttpServletResponse response) {

		logger.debug("IN");

		try {
			JSONObject obj = new JSONObject();
			try {
				obj.put("result", "ok");
			} catch (JSONException e) {
				logger.error("Error building the success string");
				throw new SpagoBIRuntimeException("Error building the success string");
			}
			String successString = obj.toString();
			return successString;
		} finally {
			logger.debug("OUT");
		}
	}

	private JSONObject buildBaseTemplate() {
		JSONObject template;

		logger.debug("IN");
		template = new JSONObject();
		logger.debug("OUT");

		return template;
	}
}
