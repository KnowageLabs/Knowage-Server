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
package it.eng.spagobi.engines.georeport.api.page;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
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

import it.eng.spagobi.engines.georeport.GeoReportEngine;
import it.eng.spagobi.engines.georeport.GeoReportEngineInstance;
import it.eng.spagobi.engines.georeport.api.AbstractChartEngineResource;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;

@Path("/1.0/pages")
@ManageAuthorization
public class PageResource extends AbstractChartEngineResource {

	@Context
	private HttpServletRequest request;

	@Context
	private HttpServletResponse response;

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
			pages.put("execute", new JSONObject("{name: 'execute', description: 'the georeport execution page', parameters: []}"));
			urls.put("execute", "/WEB-INF/jsp/geoReport.jsp");

			pages.put("edit_map", new JSONObject("{name: 'execute', description: 'the georeport execution page', parameters: []}"));
			urls.put("edit_map", "/WEB-INF/jsp/geoReport.jsp");

			pages.put("edit", new JSONObject("{name: 'edit', description: 'the geo edit page', parameters: []}"));
			urls.put("edit", "/WEB-INF/jsp/geoEdit.jsp");

		} catch (JSONException t) {
			logger.error(t);
		}
	}

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
		GeoReportEngineInstance engineInstance;
		String dispatchUrl = urls.get(pageName);

		try {

			String savedTemplate = getIOManager().getTemplateAsString();

			switch (pageName) {
			case "execute":
				if (savedTemplate == null) {
					dispatchUrl = "/WEB-INF/jsp/error.jsp";
				} else {
					engineInstance = GeoReportEngine.createInstance(savedTemplate, getIOManager().getEnv());
					getIOManager().getHttpSession().setAttribute(EngineConstants.ENGINE_INSTANCE, engineInstance);
				}
				break;
			case "edit":
				String saveTemplateForEdit = getIOManager().getTemplateAsString(true);
				String templateString = saveTemplateForEdit != null ? saveTemplateForEdit : buildBaseTemplate().toString();
				engineInstance = GeoReportEngine.createInstance(templateString, getIOManager().getEnv());
				engineInstance.getEnv().put(EngineConstants.ENV_DOCUMENT_LABEL, getIOManager().getRequest().getParameter("DOCUMENT_LABEL"));
				engineInstance.getEnv().put(EngineConstants.ENV_IS_TECHNICAL_USER, getIOManager().getRequest().getParameter("IS_TECHNICAL_USER"));
				getIOManager().getHttpSession().setAttribute(EngineConstants.ENGINE_INSTANCE, engineInstance);
				break;
			default:
				logger.error("pageName not defined");
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

	private JSONObject buildBaseTemplate() {
		JSONObject template;

		logger.debug("IN");
		template = new JSONObject();
		logger.debug("OUT");

		return template;
	}

}
