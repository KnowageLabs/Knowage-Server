package it.eng.spagobi.security.saml;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.onelogin.saml2.authn.AuthnRequest;
import com.onelogin.saml2.authn.AuthnRequestParams;
import com.onelogin.saml2.authn.SamlResponse;
import com.onelogin.saml2.logout.LogoutRequest;
import com.onelogin.saml2.logout.LogoutRequestParams;
import com.onelogin.saml2.logout.LogoutResponse;
import com.onelogin.saml2.logout.LogoutResponseParams;
import com.onelogin.saml2.model.SamlResponseStatus;
import com.onelogin.saml2.settings.Saml2Settings;
import com.onelogin.saml2.util.Constants;
import com.onelogin.saml2.util.SchemaFactory;
import com.onelogin.saml2.util.Util;

public final class SamlProtocolService {
	private static final ObjectMapper JSON = new ObjectMapper();
	private final SamlConfiguration config;
	private final Saml2Settings settings;
	private final FederatedSessionStore state;

	public SamlProtocolService(SamlConfiguration config, FederatedSessionStore state) throws Exception {
		this.config = config;
		this.settings = config.settings();
		this.state = state;
	}

	public String metadata() throws Exception {
		return settings.getSPMetadata();
	}

	public URI start(String challenge) throws Exception {
		if (challenge == null || !challenge.matches("[A-Za-z0-9_-]{43}")) {
			throw new IllegalArgumentException("Invalid browser challenge");
		}
		AuthnRequest request = new AuthnRequest(settings, new AuthnRequestParams(false, false, true, false));
		String xml = request.getAuthnRequestXml();
		if (config.myId()) {
			Document document = Util.loadXML(xml);
			Element scoping = document.createElementNS(Constants.NS_SAMLP, "samlp:Scoping");
			scoping.setAttribute("ProxyCount", "2");
			Element requester = document.createElementNS(Constants.NS_SAMLP, "samlp:RequesterID");
			requester.setTextContent(settings.getSpEntityId());
			scoping.appendChild(requester);
			document.getDocumentElement().appendChild(scoping);
			xml = Util.convertDocumentToString(document);
		}
		String relay = FederatedSessionStore.randomId();
		ObjectNode pending = JSON.createObjectNode().put("requestId", request.getId()).put("binding", challenge);
		state.cleanup();
		state.put("login:" + relay, "LOGIN", null, pending, System.currentTimeMillis() + 300000);
		String query = "SAMLRequest=" + encode(Util.deflatedBase64encoded(xml)) + "&RelayState=" + encode(relay)
				+ "&SigAlg=" + encode(settings.getSignatureAlgorithm());
		return URI.create(settings.getIdpSingleSignOnServiceUrl()
				+ (settings.getIdpSingleSignOnServiceUrl().getQuery() == null ? "?" : "&") + query + "&Signature="
				+ encode(Base64.getEncoder().encodeToString(Util.sign(query, settings.getSPkey(), settings.getSignatureAlgorithm()))));
	}

	public SamlIdentity authenticate(String encoded, String relay) throws Exception {
		if (relay == null || !relay.matches("[A-Za-z0-9_-]{43}")) {
			throw new IllegalArgumentException("Missing SAML correlation");
		}
		com.fasterxml.jackson.databind.JsonNode pending = state.get("login:" + relay, "LOGIN");
		if (pending == null) {
			throw new IllegalArgumentException("Unknown or expired SAML request");
		}
		Document document = decode(encoded, "Response");
		ValidatedResponse response = new ValidatedResponse(settings, config.url("sp.acs-url").toString(), encoded, config.myId());
		if (!config.url("sp.acs-url").toString().equals(document.getDocumentElement().getAttribute("Destination"))
				|| !response.isValid(pending.path("requestId").asText())) {
			throw new IllegalArgumentException("SAML authentication rejected");
		}
		if (state.consume("login:" + relay, "LOGIN", pending.path("binding").asText()) == null) {
			throw new IllegalArgumentException("SAML request already consumed");
		}
		long now = System.currentTimeMillis();
		state.put("replay:" + FederatedSessionStore.digest(settings.getIdpEntityId() + "|" + response.getId()),
				"REPLAY", null, JSON.createObjectNode(), now + 86400000);
		state.put("replay:" + FederatedSessionStore.digest(settings.getIdpEntityId() + "|" + response.getAssertionId()),
				"REPLAY", null, JSON.createObjectNode(), now + 86400000);
		Node statement = response.assertionNode("/saml:AuthnStatement");
		String context = response.assertionNode("/saml:AuthnStatement/saml:AuthnContext/saml:AuthnContextClassRef").getTextContent();
		if (!settings.getRequestedAuthnContext().contains(context)) {
			throw new IllegalArgumentException("Unexpected SAML authentication context");
		}
		long expiration = now + config.seconds("session.seconds", 36000) * 1000;
		if (response.getSessionNotOnOrAfter() != null) {
			expiration = Math.min(expiration, response.getSessionNotOnOrAfter().getMillis());
		}
		ObjectNode name = JSON.valueToTree(response.getNameIdData());
		return new SamlIdentity(settings.getIdpEntityId(), name, response.getSessionIndex(),
				Instant.parse(((Element) statement).getAttribute("AuthnInstant")).toEpochMilli(), expiration, response.getAttributes());
	}

	/**
	 * POST logout uses the toolkit's XML signature validator, not its Redirect-only logout validator.
	 */
	public String receiveLogout(String encoded, boolean isRequest) throws Exception {
		String type = isRequest ? "LogoutRequest" : "LogoutResponse";
		Document document = decode(encoded, type);
		String base = "/samlp:" + type;
		Element root = document.getDocumentElement();
		if (!config.url("sp.slo-url").toString().equals(root.getAttribute("Destination"))
				|| !settings.getIdpEntityId().equals(one(document, base + "/saml:Issuer").getTextContent())
				|| !Util.validateXML(document, SchemaFactory.SAML_SCHEMA_PROTOCOL_2_0)) {
			throw new IllegalArgumentException("Invalid SAML logout envelope");
		}
		Node reference = one(document, base + "/ds:Signature/ds:SignedInfo/ds:Reference");
		if (!("#" + root.getAttribute("ID")).equals(((Element) reference).getAttribute("URI"))
				|| !Util.validateSign(document, signingCertificates(), null, "sha256", base + "/ds:Signature", !config.myId())) {
			throw new IllegalArgumentException("Invalid SAML logout signature");
		}
		String replayKey = "logout:" + FederatedSessionStore.digest(settings.getIdpEntityId() + "|" + root.getAttribute("ID"));
		if (isRequest) {
			com.fasterxml.jackson.databind.JsonNode previous = state.get(replayKey, "REPLAY");
			if (previous != null) {
				if (!FederatedSessionStore.digest(encoded).equals(previous.path("messageHash").asText())) {
					throw new IllegalArgumentException("Reused logout ID with different content");
				}
				return previous.path("reply").asText();
			}
		}
		if (!isRequest) {
			String requestId = root.getAttribute("InResponseTo");
			if (state.consume("slo:" + requestId, "SLO", null) == null
					|| !Constants.STATUS_SUCCESS.equals(((Element) one(document, base + "/samlp:Status/samlp:StatusCode")).getAttribute("Value"))) {
				throw new IllegalArgumentException("Unexpected SAML logout response");
			}
			state.put(replayKey, "REPLAY", null, JSON.createObjectNode(), System.currentTimeMillis() + 86400000);
			return null;
		}
		Element name = (Element) one(document, base + "/saml:NameID");
		ObjectNode nameId = JSON.createObjectNode().put("Value", name.getTextContent());
		for (String attribute : List.of("Format", "NameQualifier", "SPNameQualifier")) {
			if (name.hasAttribute(attribute)) {
				nameId.put(attribute, name.getAttribute(attribute));
			}
		}
		List<String> indexes = new ArrayList<>();
		NodeList indexNodes = Util.query(document, base + "/samlp:SessionIndex");
		for (int i = 0; i < indexNodes.getLength(); i++) {
			if (indexNodes.item(i).getTextContent().isEmpty()) {
				throw new IllegalArgumentException("Empty SAML session index");
			}
			indexes.add(indexNodes.item(i).getTextContent());
		}
		// Duplicate notifications are valid and idempotent; they never re-enable a session.
		state.revokeSubject(FederatedSessionStore.subjectKey(settings.getIdpEntityId(), nameId, settings.getSpEntityId()),
				indexes, Instant.parse(root.getAttribute("IssueInstant")).toEpochMilli());
		LogoutResponse response = new LogoutResponse(settings,
				new LogoutResponseParams(root.getAttribute("ID"), new SamlResponseStatus(Constants.STATUS_SUCCESS)));
		String reply = sign(response.getLogoutResponseXml());
		try {
			state.put(replayKey, "REPLAY", null, JSON.createObjectNode().put("messageHash", FederatedSessionStore.digest(encoded))
					.put("reply", reply), System.currentTimeMillis() + 86400000);
		} catch (IllegalStateException e) {
			com.fasterxml.jackson.databind.JsonNode previous = state.get(replayKey, "REPLAY");
			if (previous == null || !FederatedSessionStore.digest(encoded).equals(previous.path("messageHash").asText())) {
				throw e;
			}
			return previous.path("reply").asText();
		}
		return reply;
	}

	public URI logoutTarget() {
		if (!config.myId()) {
			throw new IllegalStateException("Standard SAML logout requires its session context");
		}
		URI target = config.url("myid.logout-url");
		return URI.create(target + (target.getQuery() == null ? "?" : "&") + "spid=" + encode(settings.getSpEntityId()));
	}

	public String standardLogout(com.fasterxml.jackson.databind.JsonNode context) throws Exception {
		com.fasterxml.jackson.databind.JsonNode name = context.path("nameId");
		LogoutRequest request = new LogoutRequest(settings, new LogoutRequestParams(context.path("sessionIndex").asText(null),
				name.path("Value").asText(), name.path("Format").asText(null), name.path("NameQualifier").asText(null),
				name.path("SPNameQualifier").asText(null)));
		state.put("slo:" + request.id, "SLO", null, JSON.createObjectNode(), System.currentTimeMillis() + 300000);
		return sign(request.getLogoutRequestXml());
	}

	public String logoutResponseUrl() {
		return config.url("idp.slo-response-url").toString();
	}

	public String logoutRequestUrl() {
		return config.url("idp.slo-url").toString();
	}

	private List<java.security.cert.X509Certificate> signingCertificates() {
		List<java.security.cert.X509Certificate> certificates = new ArrayList<>();
		certificates.add(settings.getIdpx509cert());
		if (settings.getIdpx509certMulti() != null) {
			certificates.addAll(settings.getIdpx509certMulti());
		}
		return certificates;
	}

	private String sign(String xml) throws Exception {
		return Base64.getEncoder().encodeToString(Util.addSign(Util.loadXML(xml), settings.getSPkey(), settings.getSPcert(),
				settings.getSignatureAlgorithm(), settings.getDigestAlgorithm()).getBytes(StandardCharsets.UTF_8));
	}

	private static Document decode(String encoded, String type) throws Exception {
		if (encoded == null || encoded.length() > 1048576) {
			throw new IllegalArgumentException("Invalid SAML message size");
		}
		Document document = Util.loadXML(new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8));
		if (document == null || !Constants.NS_SAMLP.equals(document.getDocumentElement().getNamespaceURI())
				|| !type.equals(document.getDocumentElement().getLocalName())) {
			throw new IllegalArgumentException("Unexpected SAML message");
		}
		Element root = document.getDocumentElement();
		long issue = Instant.parse(root.getAttribute("IssueInstant")).toEpochMilli();
		long now = System.currentTimeMillis();
		if (!"2.0".equals(root.getAttribute("Version")) || root.getAttribute("ID").isEmpty()
				|| issue < now - 300000 || issue > now + 120000
				|| (root.hasAttribute("NotOnOrAfter") && Instant.parse(root.getAttribute("NotOnOrAfter")).toEpochMilli() <= now)) {
			throw new IllegalArgumentException("Invalid or expired SAML envelope");
		}
		Set<String> ids = new HashSet<>();
		NodeList nodes = document.getElementsByTagName("*");
		for (int i = 0; i < nodes.getLength(); i++) {
			Element node = (Element) nodes.item(i);
			if (node.hasAttribute("ID") && (node.getAttribute("ID").isEmpty() || !ids.add(node.getAttribute("ID")))) {
				throw new IllegalArgumentException("Duplicate SAML ID");
			}
		}
		return document;
	}

	private static Node one(Document document, String xpath) throws Exception {
		NodeList nodes = Util.query(document, xpath);
		if (nodes.getLength() != 1) {
			throw new IllegalArgumentException("Missing or ambiguous SAML element");
		}
		return nodes.item(0);
	}

	private static String encode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}

	private static final class ValidatedResponse extends SamlResponse {
		private final boolean myId;

		private ValidatedResponse(Saml2Settings settings, String url, String encoded, boolean myId) throws Exception {
			super(settings, url, encoded);
			this.myId = myId;
		}

		@Override
		public Boolean validateNumAssertions() {
			if (!myId) {
				return super.validateNumAssertions();
			}
			try {
				// MyID embeds unsigned mapping assertions inside Advice. Never consume them.
				return query("/samlp:Response/saml:Assertion", null).getLength() == 1
						&& query("//saml:EncryptedAssertion", null).getLength() == 0;
			} catch (Exception e) {
				return false;
			}
		}

		private Node assertionNode(String suffix) throws Exception {
			NodeList nodes = queryAssertion(suffix);
			if (nodes.getLength() != 1) {
				throw new IllegalArgumentException("Ambiguous SAML assertion data");
			}
			return nodes.item(0);
		}
	}
}
