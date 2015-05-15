/*
 * SpagoBI, the Open Source Business Intelligence suite
 * © 2005-2015 Engineering Group
 *
 * This file is part of SpagoBI. SpagoBI is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 2.1 of the License, or any later version. 
 * SpagoBI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You should have received
 * a copy of the GNU Lesser General Public License along with SpagoBI. If not, see: http://www.gnu.org/licenses/.
 * The complete text of SpagoBI license is included in the COPYING.LESSER file. 
 */
package it.eng.spagobi.engine.chart.api.page;

import it.eng.spagobi.engine.chart.ChartEngine;
import it.eng.spagobi.engine.chart.ChartEngineInstance;
import it.eng.spagobi.engine.chart.api.AbstractChartEngineResource;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @authors
 * 
 */

@Path("/1.0/pages")
public class PageResource extends AbstractChartEngineResource {

	static private Map<String, JSONObject> pages;
	static private Map<String, String> urls;

	static private Logger logger = Logger.getLogger(PageResource.class);

	/**
	 * TODO Tutte le pagine dell'engine
	 * 
	 * */
	static {
		pages = new HashMap<String, JSONObject>();
		urls = new HashMap<String, String>();

		try {
			pages.put("edit", new JSONObject("{name: 'edit', description: 'the chart edit page', parameters: []}"));
			urls.put("edit", "/WEB-INF/jsp/chart_edit.jsp");
			pages.put("execute", new JSONObject("{name: 'execute', description: 'the chart execution page', parameters: ['template']}"));
			urls.put("execute", "/WEB-INF/jsp/chart.jsp");
			pages.put("test", new JSONObject("{name: 'test', description: 'the chart test page', parameters: ['template']}"));
			urls.put("test", "/WEB-INF/jsp/test4.jsp");
		} catch (JSONException t) {
			logger.error(t);
		}
	}

	/**
	 * TODO COMMENTARE
	 * 
	 * */
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
	public void openPage(@PathParam("pagename") String pageName) {
		ChartEngineInstance engineInstance;
		String dispatchUrl = urls.get(pageName);

		try {

			String savedTemplate = getIOManager().getTemplateAsString();
			switch (pageName) {

			case "execute":
				engineInstance = ChartEngine.createInstance(savedTemplate, getIOManager().getEnv());
				// TODO put this not in session but in context
				getIOManager().getHttpSession().setAttribute(EngineConstants.ENGINE_INSTANCE, engineInstance);
				break;

			case "edit":

				String templateString = savedTemplate != null ? savedTemplate : buildBaseTemplate().toString();

				// create a new engine instance
				engineInstance = ChartEngine.createInstance(templateString, getIOManager().getEnv());

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

			request.getRequestDispatcher(dispatchUrl).forward(request, response);
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
