/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.rest.publishers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

/**
 * PublisherService Rest service can be used to display a jsp. It can be called passing the
 * request parameter "PUBLISHER" containing the uri to the requested resource.
 * i.e. PUBLISHER=PUBLISHER=/WEB-INF/jsp/community/XXX.jsp 
 * 
 * @author franceschini
 *
 */

@Path("/publish")
public class PublisherService {
	@Context
	private HttpServletResponse servletResponse;

	private static Logger logger = Logger.getLogger(PublisherService.class);
	private static String PUBLISHER ="PUBLISHER";

	@GET
	public void publish(@Context HttpServletRequest req) {

		try {
			
			HttpServletRequest request = ResteasyProviderFactory.getContextData(HttpServletRequest.class);
		    HttpServletResponse response = ResteasyProviderFactory.getContextData(HttpServletResponse.class);
			
			String publisher = request.getParameter(PUBLISHER);
			if (publisher != null) {
				request.getRequestDispatcher(publisher).forward(request,
						response);
			}

		} catch (Exception e) {
			logger.error("Error forwarding request", e);
		}
	}

}
