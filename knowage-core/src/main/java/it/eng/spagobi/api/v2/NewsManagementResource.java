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

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.tools.news.bo.News;
import it.eng.spagobi.tools.news.dao.ISbiNewsDAO;
import it.eng.spagobi.tools.news.manager.INewsManager;
import it.eng.spagobi.tools.news.manager.NewsManagerTechImpl;
import it.eng.spagobi.tools.news.manager.NewsManagerUserImpl;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

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
			INewsManager newsMan = null;

			if (UserUtilities.isTechnicalUser(profile)) {
				newsMan = new NewsManagerTechImpl();

			} else {
				newsMan = new NewsManagerUserImpl();
			}

			List<News> allNews = newsMan.getAllNews(profile);
			JSONArray jsonArray = new JSONArray();

			for (int i = 0; i < allNews.size(); i++) {
				JSONObject jsonObject = new JSONObject();
				News news = allNews.get(i);

				jsonObject.put("id", news.getId());
				jsonObject.put("title", news.getTitle());
				jsonObject.put("description", news.getDescription());

				jsonArray.put(jsonObject);

			}

			return Response.ok(jsonArray.toString()).build();

		} catch (Exception e) {
			logger.error("Error has occured while returing news", e);
			throw new SpagoBIRestServiceException(e.getMessage(), buildLocaleFromSession(), e);

		} finally {
			logger.debug("OUT");
		}
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON + charset)
	public Response getNewsById(@PathParam("id") Integer id) {

		logger.debug("IN");
		ISbiNewsDAO sbiNewsDAO = null;

		try {
			UserProfile profile = getUserProfile();
			sbiNewsDAO = DAOFactory.getSbiNewsDAO();
			sbiNewsDAO.setUserProfile(profile);
			News news = sbiNewsDAO.getNewsById(id);

			return Response.ok(news).build();

		} catch (Exception e) {
			logger.error("Error while geting news by id");
			throw new SpagoBIRestServiceException(e.getMessage(), buildLocaleFromSession(), e);
		}

	}

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response insertNews(@javax.ws.rs.core.Context HttpServletRequest req) {

		ISbiNewsDAO newsDao = null;
		IRoleDAO rolesDao = null;
		logger.debug("IN");

		try {
			JSONObject requestBodyJSON = RestUtilities.readBodyAsJSONObject(req);
			newsDao = DAOFactory.getSbiNewsDAO();
			rolesDao = DAOFactory.getRoleDAO();
			newsDao.setUserProfile(getUserProfile());

			News news = new News();
			news.setDescription(requestBodyJSON.getString("description"));

			Set<Role> listRoles = new HashSet<>();

			if (requestBodyJSON.getJSONArray("roles") != null) {
				JSONArray roles = requestBodyJSON.getJSONArray("roles");
				Role[] sequenceOfRoles = new Role[roles.length()];

				for (int i = 0; i < roles.length(); i++) {
					int roleID = roles.getJSONObject(i).getInt("id");
					Role r = rolesDao.loadByID(roleID);
					listRoles.add(r);
				}

				news.setRoles(listRoles);
			}

			news.setTitle(requestBodyJSON.getString("title"));
			news.setStatus(requestBodyJSON.optString("active"));
			news.setType(requestBodyJSON.optInt("type"));
			news.setHtml(requestBodyJSON.optString("html"));
			news.setId(!requestBodyJSON.optString("id").equals("") ? requestBodyJSON.optInt("id") : null);

			if (requestBodyJSON.optLong("expirationDate") != 0) {
				long miliSec = requestBodyJSON.optLong("expirationDate");
				Date result = new Date(miliSec);
				news.setExpirationDate(result);
			}

			news = newsDao.saveNews(news);

			return Response.ok(news.getId()).build();

		} catch (Exception e) {
			logger.error("Error while posting news");
			throw new SpagoBIRestServiceException(e.getMessage(), buildLocaleFromSession(), e);

		} finally {
			logger.debug("OUT");
		}
	}

	@DELETE
	@Path("/{id}")
	public Response deleteNews(@PathParam("id") Integer id) {

		ISbiNewsDAO iNewsDao = null;
		logger.debug("IN");

		try {
			iNewsDao = DAOFactory.getSbiNewsDAO();
			iNewsDao.setUserProfile(getUserProfile());
			iNewsDao.deleteNew(id);

			return Response.ok().build();

		} catch (Exception e) {
			logger.error("The error has occured while deleting news", e);
			throw new SpagoBIRestServiceException(e.getMessage(), buildLocaleFromSession(), e);

		} finally {
			logger.debug("OUT");
		}
	}

}
