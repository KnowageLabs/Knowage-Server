package it.eng.spagobi.rest.oauth2;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

@Path("/oauth2ssoservice")
public class OAuth2SsoService {

	@Context
	private HttpServletRequest request;

	@Context
	private HttpServletResponse response;
	
	private static Logger logger = Logger.getLogger(OAuth2SsoService.class);
	
	@GET
	public String getNonce() {
		
		String nonce = (String)request.getSession().getAttribute(it.eng.spagobi.services.oauth2.Oauth2SsoService.NONCE);	
		nonce = StringEscapeUtils.escapeJavaScript(nonce);
		return nonce;
	}

}
