package it.eng.spagobi.rest.oauth2;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;

import it.eng.spagobi.rest.oauth2.dto.OAuth2ConfigDTO;
import it.eng.spagobi.security.OAuth2.OAuth2Config;


//@PublicService
@Path("/oauth2configservice")
public class OAuth2ConfigService {
	@Context
	private HttpServletRequest request;

	@Context
	private HttpServletResponse response;
	
	private static Logger logger = Logger.getLogger(OAuth2ConfigService.class);
	
	@GET
	public OAuth2ConfigDTO getOAuth2Config() {
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
		return dto;
	}
	

}
