package it.eng.spagobi.api.v2;

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * @author Radmila Selakovic (radmila.selakovic@mht.net) service that
 *         authenticate user
 *
 */
@Path("/2.0/autenticateUser")
@ManageAuthorization
public class OAuth2Resource extends AbstractSpagoBIResource {
	static protected Logger logger = Logger.getLogger(OAuth2Resource.class);

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response authencticateUser() {
		try {
			return Response.ok((new JSONObject()).toString()).build();
		} catch (Exception e) {
			String errorString = "User cannot be authenticated";
			logger.error(errorString, e);
			throw new SpagoBIRuntimeException(errorString, e);
		} finally {
			logger.debug("OUT");
		}
	}
}
