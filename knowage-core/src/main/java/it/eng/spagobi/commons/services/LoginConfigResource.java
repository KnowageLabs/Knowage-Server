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
			item.put("ssoActive", config.getConfigValue("SPAGOBI_SSO.ACTIVE"));
			item.put("defaultLanguage", config.getConfigValue("SPAGOBI.LANGUAGE_SUPPORTED.LANGUAGE.default"));
			item.put("oauth2FlowType", Optional.ofNullable(System.getProperty("oauth2_flow_type", System.getenv("OAUTH2_FLOW_TYPE"))).orElse(""));

			item.put("JWT_LABEL", System.getProperty("JWT_LABEL", System.getenv("JWT_LABEL")));
			item.put("JWT_SESSION_STORAGE", System.getProperty("JWT_SESSION_STORAGE", System.getenv("JWT_SESSION_STORAGE")));
			item.put("JWT_SERVICE_LOGIN_URL", System.getProperty("JWT_SERVICE_LOGIN_URL", System.getenv("JWT_SERVICE_LOGIN_URL")));

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