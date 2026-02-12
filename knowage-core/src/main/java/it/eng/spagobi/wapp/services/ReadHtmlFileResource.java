package it.eng.spagobi.wapp.services;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import it.eng.knowage.menu.api.MenuManagementAPI;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.AuditLogUtilities;
import it.eng.spagobi.commons.utilities.HibernateSessionManager;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.wapp.bo.Menu;

@Path("/2.0/readHtmlFile")
public class ReadHtmlFileResource extends AbstractSpagoBIResource {

	private static final Logger LOGGER = LogManager.getLogger(ReadHtmlFileResource.class);

	@GET
	@Path("/")
	@Produces(MediaType.TEXT_HTML)
	public Response getHtmlFile(@Context HttpServletRequest req) throws Exception {

		IEngUserProfile profile = getUserProfile();

		// Start writing log in the DB
		Session aSession = null;
		try {
			aSession = HibernateSessionManager.getCurrentSession();
			AuditLogUtilities.updateAudit(req, profile, "HTML_MENU.OPEN_HTML_FILE", null, "OK");
		} catch (HibernateException he) {
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen()) {
					aSession.close();
				}
			}
		}
		// End writing log in the DB
		String menuId = req.getParameter("MENU_ID");

		Menu menu = DAOFactory.getMenuDAO().loadMenuByID(Integer.valueOf(menuId));
		boolean accessible = new MenuManagementAPI(profile).isAccessibleMenu(menu);
		if (!accessible) {
			LOGGER.error("No role found for menu with id = {}", menu.getMenuId());
			return Response.status(Response.Status.FORBIDDEN).entity("Not allowed menu").build();
		}

		String fileName = menu.getStaticPage();
		if (fileName == null || fileName.contains("\\") || fileName.contains("/") || fileName.contains("..")) {
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid file name").build();
		}

		String filePath = SpagoBIUtilities.getResourcePath() + "/static_menu/" + fileName;
		File file = new File(filePath);
		if (!file.exists()) {
			return Response.status(Response.Status.NOT_FOUND).entity("File not found").build();
		}

		String htmlContent = new String(java.nio.file.Files.readAllBytes(file.toPath()));
		LOGGER.debug("OUT");
		return Response.ok(htmlContent, MediaType.TEXT_HTML).build();

	}

}
