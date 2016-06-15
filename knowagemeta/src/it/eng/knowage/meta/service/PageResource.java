/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.knowage.meta.service;

import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.engines.rest.AbstractRestService;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @authors
 * 
 */

@Path("/1.0/pages")
public class PageResource extends AbstractRestService {

	static private Map<String, JSONObject> pages;
	static private Map<String, String> urls;

	static private Logger logger = Logger.getLogger(PageResource.class);

	static {
		urls = new HashMap<String, String>();
		urls.put("edit", "/WEB-INF/jsp/test.jsp");

	}

	@Context
	protected HttpServletRequest request;
	@Context
	protected HttpServletResponse response;

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

	@GET
	@Path("/{pagename}")
	@Produces("text/html")
	public void getPage(@PathParam("pagename") String pageName) {
		String dispatchUrl = urls.get(pageName);

		try {

			// To deploy into JBOSSEAP64 is needed a StandardWrapper, instead of RestEasy Wrapper
			HttpServletRequest request = ResteasyProviderFactory.getContextData(HttpServletRequest.class);
			HttpServletResponse response = ResteasyProviderFactory.getContextData(HttpServletResponse.class);

			response.setContentType("text/html");
			response.setCharacterEncoding("UTF-8");

			if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
				request.getRequestDispatcher(dispatchUrl).include(request, response);
			} else {
				request.getRequestDispatcher(dispatchUrl).forward(request, response);
			}

		} catch (Exception e) {
			logger.error(e);
		} finally {
			logger.debug("OUT");
		}
	}

	@Override
	public HttpServletRequest getServletRequest() {
		return this.request;
	}
}
