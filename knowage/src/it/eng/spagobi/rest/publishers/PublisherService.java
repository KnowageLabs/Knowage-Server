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

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

/**
 * PublisherService Rest service can be used to display a jsp. It can be called passing the
 * request parameter "PUBLISHER" containing the uri to the requested resource.
 * i.e. PUBLISHER=PUBLISHER=/WEB-INF/jsp/community/XXX.jsp 
 * 
 * @author franceschini
 *
 */

@Path("/publish")
public class PublisherService extends AbstractSpagoBIResource {

	@Context
	private HttpServletResponse servletResponse;

	private static Logger logger = Logger.getLogger(PublisherService.class);
	private static final String PUBLISHER ="PUBLISHER";
	private static final String JSP_PATH = "/WEB-INF/jsp/";

	@GET
	public void publish(@Context HttpServletRequest req) {

		try {
			
			String publisher = request.getParameter(PUBLISHER);
			
			if(!isPublisherValid(publisher)  ) {
				logger.error("The user " + getUserProfile().getUserUniqueIdentifier() + " is trying to read a secured file content using publisher");
				throw new IllegalAccessException("Unauthorized access to a system resource.");
			}
			
			if (publisher != null) {
				request.getRequestDispatcher(publisher).forward(request, response);
			}

		} catch (Exception e) {
			logger.error("Error forwarding request", e);
			throw new SpagoBIServiceException("publish", e.getMessage());
		}
	}

	/* Vulnerability patch: allows only jsp publishing */
	private boolean isPublisherValid(String publisher) {
		boolean isValid = true;
		
		if(isValid && publisher == null) {
			isValid = false;
		}

		if(isValid && !publisher.startsWith(JSP_PATH)) {
				isValid = false;
		}
		String publisherNoParameters = publisher.indexOf("?") < 0 ? publisher : publisher.substring(0, publisher.indexOf("?")); 
		if(isValid && !publisherNoParameters.toLowerCase().endsWith("jsp")){
			isValid = false;
		}
		if(isValid && publisherNoParameters.contains("../")) {
			isValid = false;
		}
		
		return isValid;
	}

}
