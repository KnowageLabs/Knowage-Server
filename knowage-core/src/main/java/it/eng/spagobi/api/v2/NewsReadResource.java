package it.eng.spagobi.api.v2;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.news.dao.ISbiNewsReadDAO;
import it.eng.spagobi.tools.news.manager.INewsManager;
import it.eng.spagobi.tools.news.manager.NewsManagerImpl;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;

@Path("/2.0/newsRead")
public class NewsReadResource extends AbstractSpagoBIResource {

	@POST
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response insert(@PathParam("id") Integer id) {

		ISbiNewsReadDAO newsReadDao = null;

		logger.debug("IN");

		try {

			newsReadDao = DAOFactory.getSbiNewsReadDAO();
			newsReadDao.insertNewsRead(id, getUserProfile());

			return Response.ok(id).build();

		} catch (Exception e) {
			throw new SpagoBIRestServiceException("An error occured while inserting", buildLocaleFromSession(), e);

		} finally {
			logger.debug("OUT");
		}

	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/")
	public List<Integer> getRead() {

		ISbiNewsReadDAO newsReadDao = null;

		logger.debug("IN");

		try {

			newsReadDao = DAOFactory.getSbiNewsReadDAO();
			List<Integer> listOfReads = newsReadDao.getReadNews(getUserProfile());

			return listOfReads;

		} catch (Exception e) {
			throw new SpagoBIRestServiceException("An error occured while inserting", buildLocaleFromSession(), e);

		} finally {
			logger.debug("OUT");
		}

	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/unread")
	public Integer getNumberOfUnreadNews() {

		logger.debug("IN");

		try {

			INewsManager newsManager = new NewsManagerImpl();
			return newsManager.getAllNews(getUserProfile()).size() - getRead().size();

		} catch (Exception e) {
			throw new SpagoBIRestServiceException("An error occured while inserting", buildLocaleFromSession(), e);

		} finally {
			logger.debug("OUT");
		}

	}

}
