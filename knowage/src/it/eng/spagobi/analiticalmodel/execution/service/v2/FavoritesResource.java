package it.eng.spagobi.analiticalmodel.execution.service.v2;

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.hotlink.rememberme.bo.RememberMe;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

@Path("/2.0/favorites")
@ManageAuthorization
public class FavoritesResource extends AbstractSpagoBIResource {

	static protected Logger logger = Logger.getLogger(FavoritesResource.class);

	RememberMe rememberMe = null;
	List<RememberMe> rememberMeList;

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public List<RememberMe> getFavouritesOfUserByUserId() {
		logger.debug("IN");
		try {
			rememberMeList = DAOFactory.getRememberMeDAO().getMyRememberMe(getUserProfile().getUserId().toString());
		} catch (Exception e) {
			logger.error("Error while recovering favourites of user [" + getUserProfile() + "]", e);
		} finally {
			logger.debug("OUT");
		}
		return rememberMeList;
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public RememberMe getFavouritesById(@PathParam("id") Integer id) {
		logger.debug("IN");
		try {
			rememberMe = DAOFactory.getRememberMeDAO().getRememberMe(id);
		} catch (Exception e) {
			logger.error("Error while recovering favourites with id [" + id + "]", e);
		} finally {
			logger.debug("OUT");
		}
		return rememberMe;
	}

	@DELETE
	@Path("/{id}")
	public void deleteFavoriteById(@PathParam("id") Integer id) {
		logger.debug("IN");
		try {
			DAOFactory.getRememberMeDAO().delete(id);
		} catch (Exception e) {
			logger.error("Error deleting a favorite documetn execution", e);
		} finally {
			logger.debug("OUT");
		}
	}

}
