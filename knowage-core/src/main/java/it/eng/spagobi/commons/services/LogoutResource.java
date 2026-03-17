package it.eng.spagobi.commons.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import it.eng.knowage.commons.security.KnowageSystemConfiguration;
import it.eng.knowage.pm.dto.PrivacyDTO;
import it.eng.knowage.privacymanager.LoginEventBuilder;
import it.eng.knowage.privacymanager.PrivacyManagerClient;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.AuditLogUtilities;
import it.eng.spagobi.services.rest.annotations.PublicService;

@Path("/logout")
public class LogoutResource extends AbstractSpagoBIResource {

	private static Logger logger = Logger.getLogger(LogoutResource.class);

	private static String INVALIDATE_JSP = "/invalidateSession.jsp";

	/**
	 * Logs out the current authenticated user by updating the audit log, sending
	 * the related privacy event, invalidating the current HTTP session, and
	 * returning the configured logout redirect URL together with the list of
	 * engine session invalidation endpoints.
	 */
	@POST
	@Path("/")
	public Response logout(@Context HttpServletRequest request, @Context HttpServletResponse response) {
		logger.debug("IN");

		// Recupero profilo utente
		UserProfile up = getUserProfile();

		HashMap<String, String> logParam = new HashMap<>();
		logParam.put("USER", up.toString());
		AuditLogUtilities.updateAudit(request, up, "SPAGOBI.Logout", logParam, "OK");

		// Costruzione evento Privacy
		LoginEventBuilder eventBuilder = new LoginEventBuilder();
		eventBuilder.appendSession("knowage", up.getSourceIpAddress(), up.getSessionId(), up.getSessionStart(), up.getUserId().toString());
		eventBuilder.appendUserAgent(up.getOs(), up.getSourceIpAddress(), up.getSourceSocketEnabled(), up.getUserAgent());
		PrivacyDTO dto = eventBuilder.getDTO();
		dto.setDescription("Logout");
		PrivacyManagerClient.getInstance().sendMessage(dto);

		Map<String, Object> responseBody = buildLogoutResponseBody(request);

		logger.debug("OUT");
		return Response.ok(responseBody).build();
	}

	/**
	 * Performs an automatic logout by invalidating the current HTTP session and
	 * returning the configured logout redirect URL together with the list of
	 * engine session invalidation endpoints. This endpoint is exposed as a public
	 * service.
	 */
	@POST
	@Path("/auto")
	@PublicService
	public Response autoLogout(@Context HttpServletRequest request) {
		logger.debug("IN");

		Map<String, Object> responseBody = buildLogoutResponseBody(request);

		logger.debug("OUT");

		return Response.ok(responseBody).build();
	}

	private Map<String, Object> buildLogoutResponseBody(HttpServletRequest request) {
		request.getSession().invalidate();

		SingletonConfig config = SingletonConfig.getInstance();
		Object ssoActiveValue = config.getConfigValue("SPAGOBI_SSO.ACTIVE");
		boolean ssoActive = Boolean.parseBoolean(String.valueOf(ssoActiveValue));

		Map<String, Object> responseBody = new HashMap<>();
		responseBody.put("redirectUrl", ssoActive ? SingletonConfig.getInstance().getConfigValue("SPAGOBI_SSO.SECURITY_LOGOUT_URL") : null);
		responseBody.put("urlEnginesInvalidate", getUrlEnginesInvalidate());
		return responseBody;
	}

	private List<String> getUrlEnginesInvalidate() {
		List<String> urlEnginesInvalidate = new ArrayList<>();
		urlEnginesInvalidate.add(KnowageSystemConfiguration.getKnowageBirtReportEngineContext() + INVALIDATE_JSP);
		urlEnginesInvalidate.add(KnowageSystemConfiguration.getKnowageCockpitEngineContext() + INVALIDATE_JSP);
		urlEnginesInvalidate.add(KnowageSystemConfiguration.getKnowageDossierEngineContext() + INVALIDATE_JSP);
		urlEnginesInvalidate.add(KnowageSystemConfiguration.getKnowageJasperReportEngineContext() + INVALIDATE_JSP);
		urlEnginesInvalidate.add(KnowageSystemConfiguration.getKnowageKpiEngineContext() + INVALIDATE_JSP);
		urlEnginesInvalidate.add(KnowageSystemConfiguration.getKnowageMetaContext() + INVALIDATE_JSP);
		urlEnginesInvalidate.add(KnowageSystemConfiguration.getKnowageQbeEngineContext() + INVALIDATE_JSP);
		urlEnginesInvalidate.add(KnowageSystemConfiguration.getKnowageTalendEngineContext() + INVALIDATE_JSP);
		urlEnginesInvalidate.add(KnowageSystemConfiguration.getKnowageWhatifEngineContext() + INVALIDATE_JSP);
		return urlEnginesInvalidate;
	}

}