package it.eng.spagobi.rest.oauth2;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.rest.oauth2.dto.OAuth2ConfigDTO;
import it.eng.spagobi.security.OAuth2.OAuth2Config;
import it.eng.spagobi.services.rest.annotations.PublicService;


@Path("/oauth2configservice")
public class OAuth2ConfigService extends AbstractSpagoBIResource {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@PublicService
	public Response getOAuth2Config() {
		OAuth2Config oauth2Config = OAuth2Config.getInstance();
		String authorizeUrl = oauth2Config.getAuthorizeUrl();
		String accessTokenUrl = oauth2Config.getAccessTokenUrl();
		String clientId = oauth2Config.getClientId();
		String redirectUrl = oauth2Config.getRedirectUrl();
		String scopes = oauth2Config.getScopes();

		OAuth2ConfigDTO dto = new OAuth2ConfigDTO();
		dto.setAccessTokenUrl(accessTokenUrl);
		dto.setAuthorizeUrl(authorizeUrl);
		dto.setClientId(clientId);
		dto.setRedirectUrl(redirectUrl);
		dto.setScopes(scopes);
		return Response.ok(dto).build();
	}



}
