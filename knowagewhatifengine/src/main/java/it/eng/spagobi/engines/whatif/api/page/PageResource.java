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
package it.eng.spagobi.engines.whatif.api.page;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;
import org.jboss.resteasy.plugins.providers.html.View;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.engines.rest.AbstractRestService;

@Path("/pages")
@ManageAuthorization
public class PageResource extends AbstractRestService {

	static private Map<String, JSONObject> pages;
	static private Map<String, String> urls;
	@Context
	HttpServletRequest request;
	@Context
	HttpServletResponse response;

	static private Logger logger = Logger.getLogger(PageResource.class);

	/**
	 * TODO Tutte le pagine dell'engine
	 *
	 */
	static {
		pages = new HashMap<String, JSONObject>();
		urls = new HashMap<String, String>();

		try {
			pages.put("execute", new JSONObject("{name: 'execute', description: 'the Olap execution page', parameters: []}"));
			urls.put("execute", "/WEB-INF/jsp/whatIf2.jsp");

			pages.put("edit", new JSONObject("{name: 'execute', description: 'the Whatif execution page', parameters: []}"));
			urls.put("edit", "/WEB-INF/jsp/edit.jsp");

		} catch (JSONException t) {
			logger.error(t);
		}
	}

	@GET
	@Path("/{pagename}")
	@Produces("text/html")
	public View openPageGet(@PathParam("pagename") String pageName) {
		return openPage(pageName);
	}

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
		String dispatchUrl = urls.get(pageName);
		try {
			return new View(dispatchUrl);
		} catch (Exception e) {
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException("", getEngineInstance(), e);
		} finally {
			logger.debug("OUT");
		}
	}

	@Override
	public HttpServletRequest getServletRequest() {
		// TODO Auto-generated method stub
		return request;
	}

}
