package it.eng.spagobi.rest.oauth2;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.rest.oauth2.dto.OAuth2TokenDTO;
import it.eng.spagobi.security.OAuth2.OAuth2Client;
import it.eng.spagobi.services.rest.annotations.PublicService;

@Path("/oauth2clientservice")
public class OAuth2ClientService extends AbstractSpagoBIResource {

	private static Logger logger = Logger.getLogger(OAuth2ClientService.class);

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@PublicService
	public Response getOAuth2Client(@QueryParam("code") @DefaultValue("") String code) {
		OAuth2Client client = new OAuth2Client();
		String genToken = client.getAccessToken(code);
		OAuth2TokenDTO dto = new OAuth2TokenDTO();
		if(genToken != null) {
			JSONObject jo;
			try {
				jo = new JSONObject(genToken);
  			    dto.setAccessToken(jo.getString("access_token"));
			    dto.setIdToken(jo.getString("id_token"));
			} catch (JSONException e) {
				logger.error("Cannot parse access token response", e);
			}
		}
		return Response.ok(dto).build();
	}


}
