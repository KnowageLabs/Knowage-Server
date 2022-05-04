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
package it.eng.knowage.engines.svgviewer.api.page;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.jboss.resteasy.plugins.providers.html.View;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import it.eng.knowage.engines.svgviewer.SvgViewerEngine;
import it.eng.knowage.engines.svgviewer.SvgViewerEngineInstance;
import it.eng.knowage.engines.svgviewer.api.AbstractSvgViewerEngineResource;
import it.eng.spago.base.SourceBean;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;

@Path("/1.0/pages")
@ManageAuthorization
public class PageResource extends AbstractSvgViewerEngineResource {

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
			pages.put("execute", new JSONObject("{name: 'execute', description: 'the svg viewer execution page', parameters: []}"));
			urls.put("execute", "/WEB-INF/jsp/svgViewer.jsp");

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
		SvgViewerEngineInstance engineInstance;
		String dispatchUrl = urls.get(pageName);

		try {
			Monitor getTemplateAsSourceBeanMonitor = MonitorFactory.start("GeoEngine.openPage.getTemplateAsSourceBean");
			// SourceBean savedTemplate = getIOManager().getTemplateAsSourceBean();
			SourceBean savedTemplate = getTemplate();
			getTemplateAsSourceBeanMonitor.stop();

			switch (pageName) {

			case "execute":
				Monitor getEngineEnvMonitor = MonitorFactory.start("GeoEngine.openPage.getEngineEnv");
				Map env = getEngineEnv();
				getEngineEnvMonitor.stop();

				engineInstance = SvgViewerEngine.createInstance(savedTemplate, env);

				// TODO put this not in session but in context
				Monitor setAttributeMonitor = MonitorFactory.start("GeoEngine.openPage.setAttribute");
				getIOManager().getHttpSession().setAttribute(EngineConstants.ENGINE_INSTANCE, engineInstance);
				setAttributeMonitor.stop();
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
}
