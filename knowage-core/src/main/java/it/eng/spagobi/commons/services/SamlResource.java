package it.eng.spagobi.commons.services;

import java.net.URI;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import it.eng.spagobi.commons.bo.SessionUserProfile;
import it.eng.spagobi.commons.bo.SessionUserProfileBuilder;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.security.saml.FederatedSessionStore;
import it.eng.spagobi.security.saml.FederatedTokenGuard;
import it.eng.spagobi.security.saml.LocalUserProfileLoader;
import it.eng.spagobi.security.saml.PmApiProfilingService;
import it.eng.spagobi.security.saml.SamlConfiguration;
import it.eng.spagobi.security.saml.SamlIdentity;
import it.eng.spagobi.security.saml.SamlProtocolService;
import it.eng.spagobi.services.common.JWTSsoService;
import it.eng.spagobi.services.rest.annotations.PublicService;
import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;

@Path("/saml")
public class SamlResource {
	private static final Logger LOGGER = Logger.getLogger(SamlResource.class);
	private static final ObjectMapper JSON = new ObjectMapper();

	@GET
	@Path("/metadata")
	@PublicService
	public Response metadata() throws Exception {
		if (!SamlConfiguration.enabled()) {
			return Response.status(404).build();
		}
		SamlConfiguration config = SamlConfiguration.load();
		return Response.ok(new SamlProtocolService(config, FederatedSessionStore.configured()).metadata(), MediaType.APPLICATION_XML).build();
	}

	@GET
	@Path("/login")
	@PublicService
	public Response login(@QueryParam("challenge") String challenge) throws Exception {
		if (!SamlConfiguration.enabled()) {
			return Response.status(404).build();
		}
		SamlConfiguration config = SamlConfiguration.load();
		return noCache(Response.seeOther(new SamlProtocolService(config, FederatedSessionStore.configured()).start(challenge))).build();
	}

	@POST
	@Path("/acs")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@PublicService
	public Response acs(@FormParam("SAMLResponse") String response, @FormParam("RelayState") String relay) throws Exception {
		if (!SamlConfiguration.enabled()) {
			return Response.status(404).build();
		}
		SamlConfiguration config = SamlConfiguration.load();
		String sid = null;
		FederatedSessionStore state = FederatedSessionStore.configured();
		try {
			JsonNode pending = state.get("login:" + relay, "LOGIN");
			SamlIdentity identity = new SamlProtocolService(config, state).authenticate(response, relay);
			String profilingMode = config.profilingMode();
			SpagoBIUserProfile profile = "local".equals(profilingMode)
					? LocalUserProfileLoader.load(identity.identifier(config))
					: new PmApiProfilingService(config).resolve(identity);
			long expiresAt = identity.expiresAt;
			if ("pm".equals(profilingMode)) {
				// PM grants are refreshed on every login and never carried beyond the Rome calendar day.
				long midnight = LocalDate.now(ZoneId.of("Europe/Rome")).plusDays(1).atStartOfDay(ZoneId.of("Europe/Rome")).toInstant().toEpochMilli();
				expiresAt = Math.min(expiresAt, midnight);
			}
			sid = FederatedSessionStore.randomId();
			Map<String, String> attributes = new HashMap<>();
			profile.getAttributes().forEach((key, value) -> attributes.put(key, value.toString()));
			attributes.put(FederatedTokenGuard.SESSION_CLAIM, sid);
			attributes.put(FederatedTokenGuard.TENANT_CLAIM, profile.getOrganization());
			attributes.put(FederatedTokenGuard.PROFILE_CLAIM, profilingMode);
			String token = JWTSsoService.getFullJWTToken(profile.getUserId(), profile.getUserName(), profile.getRoles(),
					attributes, Boolean.TRUE.equals(profile.getIsSuperadmin()), new Date(expiresAt));
			ObjectNode context = JSON.createObjectNode().put("issuer", identity.issuer).put("sessionIndex", identity.sessionIndex)
					.put("authnInstant", identity.authnInstant).put("tokenHash", FederatedSessionStore.digest(token));
			context.set("nameId", identity.nameId);
			state.register(sid, FederatedSessionStore.subjectKey(identity.issuer, identity.nameId, config.required("sp.entity-id")),
					context, expiresAt);
			// Validate local tenant/role mappings before handing an application credential to the browser.
			UserUtilities.getUserProfile(token);
			String code = FederatedSessionStore.randomId();
			state.put("code:" + FederatedSessionStore.digest(code), "CODE", null,
					JSON.createObjectNode().put("binding", pending.path("binding").asText()).put("token", token),
					System.currentTimeMillis() + 60000);
			return noCache(Response.seeOther(URI.create(config.url("frontend.url") + "#samlCode=" + code))).build();
		} catch (Exception e) {
			if (sid != null) {
				state.revoke(sid);
			}
			// Toolkit exceptions may contain response content. Never log their message or stack.
			LOGGER.warn("SAML login or application profiling rejected");
			return noCache(Response.seeOther(URI.create(config.url("frontend.url") + "#samlError=authentication"))).build();
		}
	}

	@POST
	@Path("/exchange")
	@Consumes(MediaType.APPLICATION_JSON)
	@PublicService
	public Response exchange(@Context HttpServletRequest request, Map<String, String> payload) {
		if (!SamlConfiguration.enabled()) {
			return Response.status(404).build();
		}
		String token = null;
		try {
			String code = payload.get("code");
			String verifier = payload.get("verifier");
			if (code == null || !code.matches("[A-Za-z0-9_-]{43}") || verifier == null || !verifier.matches("[A-Za-z0-9_-]{43}")) {
				return noCache(Response.status(401)).build();
			}
			JsonNode result = FederatedSessionStore.configured().consume("code:" + FederatedSessionStore.digest(code),
					"CODE", FederatedSessionStore.digest(verifier));
			if (result == null) {
				return noCache(Response.status(401)).build();
			}
			token = result.path("token").asText();
			FederatedTokenGuard.check(token);
			UserProfile complete = (UserProfile) UserUtilities.getUserProfile(token);
			// An internal default role must never widen/rewrite an external PM grant.
			SessionUserProfile profile = FederatedTokenGuard.isLocalProfile(token)
					? SessionUserProfileBuilder.getDefaultUserProfile(complete)
					: new SessionUserProfile(UserUtilities.clone(complete.getSpagoBIUserProfile()));
			if (profile.getDefaultRole() != null && !complete.getRoles().contains(profile.getDefaultRole())) {
				throw new java.lang.SecurityException("Default role is not assigned to the local SAML account");
			}
			LoginCompletionService.completeFederated(profile, request);
			return noCache(Response.ok(Map.of("token", token))).build();
		} catch (Exception e) {
			if (token != null) {
				try {
					FederatedSessionStore.configured().revoke(com.auth0.jwt.JWT.decode(token).getClaim(FederatedTokenGuard.SESSION_CLAIM).asString());
				} catch (Exception cleanupFailure) {
					LOGGER.warn("Cannot revoke failed SAML exchange; shared state must be restored");
				}
			}
			if (request.getSession(false) != null) {
				request.getSession(false).invalidate();
			}
			LOGGER.warn("SAML login exchange rejected");
			return noCache(Response.status(401)).build();
		}
	}

	@POST
	@Path("/slo")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@PublicService
	public Response slo(@FormParam("SAMLRequest") String request, @FormParam("SAMLResponse") String response,
			@FormParam("RelayState") String relay) throws Exception {
		if (!SamlConfiguration.enabled()) {
			return Response.status(404).build();
		}
		try {
			if ((request == null) == (response == null) || (relay != null && relay.length() > 80)) {
				return Response.status(400).build();
			}
			SamlConfiguration config = SamlConfiguration.load();
			SamlProtocolService protocol = new SamlProtocolService(config, FederatedSessionStore.configured());
			String reply = protocol.receiveLogout(request == null ? response : request, request != null);
			if (reply == null) {
				return noCache(Response.seeOther(URI.create(config.url("frontend.url") + "#samlLoggedOut=true"))).build();
			}
			String nonce = FederatedSessionStore.randomId();
			return noCache(Response.ok(postForm(protocol.logoutResponseUrl(), "SAMLResponse", reply, relay, nonce), MediaType.TEXT_HTML))
					.header("Content-Security-Policy", "default-src 'none'; script-src 'nonce-" + nonce + "'; form-action " + origin(protocol.logoutResponseUrl()))
					.build();
		} catch (Exception e) {
			LOGGER.warn("SAML logout message rejected");
			return noCache(Response.status(400)).build();
		}
	}

	public static String postForm(String target, String parameter, String value, String relay, String nonce) {
		return "<!doctype html><html><body><form method=\"post\" action=\"" + escape(target) + "\">"
				+ "<input type=\"hidden\" name=\"" + parameter + "\" value=\"" + escape(value) + "\">"
				+ (relay == null ? "" : "<input type=\"hidden\" name=\"RelayState\" value=\"" + escape(relay) + "\">")
				+ "<button type=\"submit\">Continue logout</button></form><script nonce=\"" + nonce
				+ "\">document.forms[0].submit()</script></body></html>";
	}

	private static String origin(String url) {
		URI uri = URI.create(url);
		return uri.getScheme() + "://" + uri.getAuthority();
	}

	private static String escape(String value) {
		return value.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;");
	}

	private static Response.ResponseBuilder noCache(Response.ResponseBuilder response) {
		return response.header("Cache-Control", "no-store").header("Pragma", "no-cache").header("Referrer-Policy", "no-referrer");
	}
}
