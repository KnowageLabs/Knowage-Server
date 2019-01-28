/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2019 Engineering Ingegneria Informatica S.p.A.
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

package it.eng.spagobi.api.v2;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.news.dao.ISbiNewsDAO;
import it.eng.spagobi.tools.news.metadata.SbiNews;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;

@Path("/2.0/news")
public class NewsManagementResource extends AbstractSpagoBIResource {

	private final String charset = "; charset=UTF-8";
	static protected Logger logger = Logger.getLogger(NewsManagementResource.class);

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + charset)
	public Response getNews() {

		logger.debug("IN");

		try {
			UserProfile profile = getUserProfile();
			ISbiNewsDAO dao = DAOFactory.getSbiNewsDAO();
			dao.setUserProfile(profile);

			List<SbiNews> allNews = new ArrayList<>();
			allNews = dao.getAllNews();

			return Response.ok(allNews).build();

		} catch (Exception e) {
			logger.error("Error has occured while returing news", e);
			throw new SpagoBIRestServiceException("Cannot return news", buildLocaleFromSession(), e);

		} finally {
			logger.debug("OUT");
		}
	}

//	@POST
//	@Path("/")
//	@Produces(MediaType.APPLICATION_JSON)
//	public Response insertNews(HttpServletRequest req) {
//
//
//
//
//		return null;
//	}

	@DELETE
	@Path("/delete/{id}")
	public Response deleteNews(@PathParam("id") Integer id) {

		ISbiNewsDAO iNewsDao = null;

		try {
			iNewsDao = DAOFactory.getSbiNewsDAO();
			iNewsDao.setUserProfile(getUserProfile());
			iNewsDao.deleteNew(id);

			return Response.ok().build();

		} catch (Exception e) {
			logger.error("The error has occured while deleting news", e);
			throw new SpagoBIRestServiceException("Cannon delete news", buildLocaleFromSession(), e);
		}
	}

}
