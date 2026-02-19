package it.eng.spagobi.commons.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import it.eng.knowage.monitor.IKnowageMonitor;
import it.eng.knowage.monitor.KnowageMonitorFactory;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.security.OAuth2.OAuth2Config;
import it.eng.spagobi.services.rest.annotations.PublicService;

@Path("/loginconfig")
public class LoginConfigResource extends AbstractSpagoBIResource {

    private static final Logger logger = Logger.getLogger(LoginConfigResource.class);

    @GET
    @Path("/")
    @PublicService
    public Response getLoginConfig(@Context HttpServletRequest req) {
        IKnowageMonitor monitor = KnowageMonitorFactory.getInstance().start("knowage.login.config.load");
        try {

			SingletonConfig config = SingletonConfig.getInstance();

			Map<String, Object> item = new HashMap<>();
			Object ssoActiveValue = config.getConfigValue("SPAGOBI_SSO.ACTIVE");
			boolean ssoActive = Boolean.parseBoolean(String.valueOf(ssoActiveValue));
			String oauth2FlowType = Optional.ofNullable(System.getProperty("oauth2_flow_type", System.getenv("OAUTH2_FLOW_TYPE"))).orElse("");
			item.put("ssoActive", ssoActive);
			item.put("defaultLanguage", config.getConfigValue("SPAGOBI.LANGUAGE_SUPPORTED.LANGUAGE.default"));
			item.put("oauth2FlowType", oauth2FlowType);

			item.put("JWT_LABEL", System.getProperty("JWT_LABEL", System.getenv("JWT_LABEL")));
			item.put("JWT_SESSION_STORAGE", System.getProperty("JWT_SESSION_STORAGE", System.getenv("JWT_SESSION_STORAGE")));

			if (oauth2FlowType != null && !oauth2FlowType.equalsIgnoreCase("NONE")) {
				OAuth2Config oauth2Config = OAuth2Config.getInstance();
				item.put("authorizeUrl", oauth2Config.getAuthorizeUrl());
				item.put("clientId", oauth2Config.getClientId());
				item.put("redirectUrl", oauth2Config.getRedirectUrl());
				item.put("scopes", oauth2Config.getScopes());

			}

            monitor.stop();
            return Response.ok(Map.of("items", List.of(item))).build();
        } catch (Exception e) {
            logger.error("Unexpected error loading login configuration", e);
            monitor.stop(e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Unable to load login configuration"))
                    .build();
        }
    }
}