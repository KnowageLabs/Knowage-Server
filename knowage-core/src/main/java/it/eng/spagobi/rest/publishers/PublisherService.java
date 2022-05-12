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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;
import org.jboss.resteasy.plugins.providers.html.View;

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.ResourcePublisherMapping;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

/**
 * PublisherService Rest service can be used to display a jsp. It can be called passing the request parameter "PUBLISHER" containing the uri to the requested
 * resource. i.e. PUBLISHER=PUBLISHER=/WEB-INF/jsp/community/XXX.jsp
 *
 * @author franceschini
 *
 */

@Path("/publish")
public class PublisherService extends AbstractSpagoBIResource {

	@Context
	private HttpServletRequest request;

	@Context
	private HttpServletResponse response;

	private static Logger logger = Logger.getLogger(PublisherService.class);
	private static final String PUBLISHER = "PUBLISHER";

	@GET
	public View publish() {

		try {

			String publisher = request.getParameter(PUBLISHER);
			String logicKey = publisher.split("\\?")[0];

			if (logicKey != null) {
				String resourcePath = ResourcePublisherMapping.get(logicKey);
				if (resourcePath == null) {
					logger.error("The user " + getUserProfile().getUserId() + "is trying to read a secured file content using publisher");
					throw new IllegalAccessException("Unauthorized access to a system resource");
				}
				String fullPath = publisher.replaceFirst(logicKey, resourcePath);

				return new View(fullPath);
			}

		} catch (Exception e) {
			logger.error("Error forwarding request", e);
			throw new SpagoBIServiceException("publish", e);
		}

		return null;
	}

}
