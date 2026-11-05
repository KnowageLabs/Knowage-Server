package it.eng.spagobi.security.saml;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.sql.Connection;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onelogin.saml2.settings.Saml2Settings;
import com.onelogin.saml2.util.Constants;
import com.onelogin.saml2.util.Util;

public class SamlProtocolServiceTest {
	private static final String CONTEXT = "urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport";
	@Rule
	public TemporaryFolder temporary = new TemporaryFolder();
	private SamlConfiguration config;
	private SamlProtocolService protocol;
	private Saml2Settings settings;
	private FederatedSessionStore state;
	private PrivateKey key;
	private X509Certificate certificate;
	private Properties properties;

	@Before
	public void setup() throws Exception {
		// Public, synthetic test material: never use these keys in a deployment.
		try (InputStream in = getClass().getResourceAsStream("/synthetic-saml-cert.pem")) {
			certificate = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(in);
		}
		String pem;
		try (InputStream in = getClass().getResourceAsStream("/synthetic-saml-key.pem")) {
			pem = new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
		key = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(
				pem.replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "").replaceAll("\\s", ""))));
		KeyStore store = KeyStore.getInstance("PKCS12");
		store.load(null, null);
		store.setKeyEntry("synthetic", key, "test-only".toCharArray(), new java.security.cert.Certificate[] { certificate });
		java.io.File keyFile = temporary.newFile("test.p12");
		try (java.io.OutputStream out = Files.newOutputStream(keyFile.toPath())) {
			store.store(out, "test-only".toCharArray());
		}
		String cert = Base64.getEncoder().encodeToString(certificate.getEncoded());
		String metadata = "<md:EntityDescriptor xmlns:md=\"urn:oasis:names:tc:SAML:2.0:metadata\""
				+ " xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\" entityID=\"https://idp.invalid/metadata\">"
				+ "<md:IDPSSODescriptor protocolSupportEnumeration=\"urn:oasis:names:tc:SAML:2.0:protocol\">"
				+ "<md:KeyDescriptor use=\"signing\"><ds:KeyInfo><ds:X509Data><ds:X509Certificate>" + cert
				+ "</ds:X509Certificate></ds:X509Data></ds:KeyInfo></md:KeyDescriptor>"
				+ "<md:SingleSignOnService Binding=\"" + Constants.BINDING_HTTP_REDIRECT + "\" Location=\"https://idp.invalid/sso\"/>"
				+ "</md:IDPSSODescriptor></md:EntityDescriptor>";
		java.io.File metadataFile = temporary.newFile("metadata.xml");
		Files.writeString(metadataFile.toPath(), metadata);
		Properties p = new Properties();
		properties = p;
		p.setProperty("provider", "myid");
		p.setProperty("sp.entity-id", "https://sp.invalid/metadata");
		p.setProperty("sp.acs-url", "https://sp.invalid/acs");
		p.setProperty("sp.slo-url", "https://sp.invalid/slo");
		p.setProperty("frontend.url", "https://sp.invalid/saml-login");
		p.setProperty("idp.entity-id", "https://idp.invalid/metadata");
		p.setProperty("idp.metadata-file", metadataFile.toString());
		p.setProperty("idp.slo-url", "https://idp.invalid/slo");
		p.setProperty("idp.slo-response-url", "https://idp.invalid/slo");
		p.setProperty("authn.contexts", CONTEXT);
		p.setProperty("sp.keystore-file", keyFile.toString());
		p.setProperty("sp.keystore-password", "test-only");
		p.setProperty("sp.key-password", "test-only");
		p.setProperty("sp.key-alias", "synthetic");
		config = new SamlConfiguration(p);
		settings = config.settings();
		JDBCDataSource source = new JDBCDataSource();
		source.setUrl("jdbc:hsqldb:mem:" + FederatedSessionStore.randomId());
		source.setUser("SA");
		try (Connection c = source.getConnection()) {
			c.createStatement().execute("CREATE TABLE SBI_SAML_STATE (STATE_ID VARCHAR(128) PRIMARY KEY, STATE_KIND VARCHAR(16),"
					+ " SUBJECT_KEY VARCHAR(64), PAYLOAD CLOB, EXPIRES_AT BIGINT, TOKEN_HASH VARCHAR(64), ACTIVE INTEGER)");
		}
		state = new FederatedSessionStore(source);
		protocol = new SamlProtocolService(config, state);
	}

	@Test
	public void rsaSha1RedirectAndMyIdScopingAreSigned() throws Exception {
		URI target = protocol.start(FederatedSessionStore.digest("browser-verifier"));
		Map<String, String> params = query(target);
		assertEquals(Constants.RSA_SHA1, params.get("SigAlg"));
		String encodedQuery = target.getRawQuery().substring(0, target.getRawQuery().indexOf("&Signature="));
		assertTrue(Util.validateBinarySignature(encodedQuery, Base64.getDecoder().decode(params.get("Signature")),
				java.util.List.of(certificate), Constants.RSA_SHA1));
		Document request = Util.loadXML(Util.base64decodedInflated(params.get("SAMLRequest")));
		assertEquals("https://sp.invalid/metadata", Util.query(request, "/samlp:AuthnRequest/samlp:Scoping/samlp:RequesterID").item(0).getTextContent());
		assertEquals(1, Util.query(Util.loadXML(protocol.metadata()), "//md:SingleLogoutService[@Binding='" + Constants.BINDING_HTTP_POST + "']").getLength());
	}

	@Test
	public void acceptsSignedCorrelatedResponseIncludingMyIdAdviceAndRejectsReplay() throws Exception {
		Map<String, String> params = query(protocol.start(FederatedSessionStore.digest("browser")));
		String id = ((Element) Util.loadXML(Util.base64decodedInflated(params.get("SAMLRequest"))).getDocumentElement()).getAttribute("ID");
		String response = response(id, "https://sp.invalid/metadata", "https://sp.invalid/acs", Instant.now(), true, "https://idp.invalid/metadata");
		SamlIdentity identity = protocol.authenticate(response, params.get("RelayState"));
		assertEquals("SYNTHETIC-IDENTITY", identity.singleAttribute("CodiceFiscale"));
		assertEquals("synthetic-session", identity.sessionIndex);
		assertThrows(Exception.class, () -> protocol.authenticate(response, params.get("RelayState")));
	}

	@Test
	public void rejectsInvalidSignatureAudienceRecipientExpirationAndCorrelation() throws Exception {
		for (String failure : new String[] { "signature", "issuer", "audience", "recipient", "expiration", "correlation" }) {
			Map<String, String> params = query(protocol.start(FederatedSessionStore.digest("browser")));
			String id = Util.loadXML(Util.base64decodedInflated(params.get("SAMLRequest"))).getDocumentElement().getAttribute("ID");
			String encoded = response("correlation".equals(failure) ? "_unknown" : id,
					"audience".equals(failure) ? "https://other.invalid" : "https://sp.invalid/metadata",
					"recipient".equals(failure) ? "https://other.invalid" : "https://sp.invalid/acs",
					"expiration".equals(failure) ? Instant.now().minusSeconds(600) : Instant.now(), false,
					"issuer".equals(failure) ? "https://other.invalid/metadata" : "https://idp.invalid/metadata");
			if ("signature".equals(failure)) {
				String xml = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8).replace("SYNTHETIC-IDENTITY", "ALTERED-IDENTITY");
				encoded = Base64.getEncoder().encodeToString(xml.getBytes(StandardCharsets.UTF_8));
			}
			final String input = encoded;
			assertThrows(Exception.class, () -> protocol.authenticate(input, params.get("RelayState")));
		}
	}

	@Test
	public void standardProviderUsesSha256AndRequiresCorrelatedPostLogout() throws Exception {
		properties.setProperty("provider", "standard");
		config = new SamlConfiguration(properties);
		settings = config.settings();
		protocol = new SamlProtocolService(config, state);
		Map<String, String> params = query(protocol.start(FederatedSessionStore.digest("browser")));
		assertEquals(Constants.RSA_SHA256, params.get("SigAlg"));
		Document request = Util.loadXML(Util.base64decodedInflated(params.get("SAMLRequest")));
		assertEquals(0, Util.query(request, "/samlp:AuthnRequest/samlp:Scoping").getLength());
		SamlIdentity identity = protocol.authenticate(response(request.getDocumentElement().getAttribute("ID"),
				"https://sp.invalid/metadata", "https://sp.invalid/acs", Instant.now(), false, "https://idp.invalid/metadata"),
				params.get("RelayState"));
		com.fasterxml.jackson.databind.node.ObjectNode context = new ObjectMapper().createObjectNode().put("sessionIndex", identity.sessionIndex);
		context.set("nameId", identity.nameId);
		String logout = protocol.standardLogout(context);
		Document logoutRequest = Util.loadXML(new String(Base64.getDecoder().decode(logout), StandardCharsets.UTF_8));
		String xml = "<samlp:LogoutResponse xmlns:samlp=\"" + Constants.NS_SAMLP + "\" xmlns:saml=\"" + Constants.NS_SAML
				+ "\" ID=\"_logoutResponse\" Version=\"2.0\" IssueInstant=\"" + Instant.now()
				+ "\" Destination=\"https://sp.invalid/slo\" InResponseTo=\"" + logoutRequest.getDocumentElement().getAttribute("ID")
				+ "\"><saml:Issuer>https://idp.invalid/metadata</saml:Issuer><samlp:Status><samlp:StatusCode Value=\""
				+ Constants.STATUS_SUCCESS + "\"/></samlp:Status></samlp:LogoutResponse>";
		String encoded = Base64.getEncoder().encodeToString(Util.addSign(Util.loadXML(xml), key, certificate,
				Constants.RSA_SHA256, Constants.SHA256).getBytes(StandardCharsets.UTF_8));
		assertNull(protocol.receiveLogout(encoded, false));
		assertThrows(Exception.class, () -> protocol.receiveLogout(encoded, false));
	}

	@Test
	public void signedLogoutWithoutCookiesRevokesSessionAndRepliesIdempotently() throws Exception {
		long now = System.currentTimeMillis();
		ObjectMapper json = new ObjectMapper();
		com.fasterxml.jackson.databind.node.ObjectNode name = json.createObjectNode().put("Value", "synthetic-name")
				.put("Format", Constants.NAMEID_TRANSIENT);
		String subject = FederatedSessionStore.subjectKey(settings.getIdpEntityId(), name, settings.getSpEntityId());
		state.register("session", subject, json.createObjectNode().put("authnInstant", now - 1000)
				.put("sessionIndex", "synthetic-session").put("tokenHash", "synthetic"), now + 60000);
		String xml = "<samlp:LogoutRequest xmlns:samlp=\"" + Constants.NS_SAMLP + "\" xmlns:saml=\"" + Constants.NS_SAML
				+ "\" ID=\"_logout\" Version=\"2.0\" IssueInstant=\"" + Instant.now() + "\" Destination=\"https://sp.invalid/slo\">"
				+ "<saml:Issuer>https://idp.invalid/metadata</saml:Issuer><saml:NameID Format=\"" + Constants.NAMEID_TRANSIENT
				+ "\">synthetic-name</saml:NameID><samlp:SessionIndex>synthetic-session</samlp:SessionIndex></samlp:LogoutRequest>";
		String encoded = Base64.getEncoder().encodeToString(Util.addSign(Util.loadXML(xml), key, certificate,
				Constants.RSA_SHA1, Constants.SHA1).getBytes(StandardCharsets.UTF_8));
		String reply = protocol.receiveLogout(encoded, true);
		assertNull(state.get("session", "SESSION"));
		assertEquals(reply, protocol.receiveLogout(encoded, true));
		Document response = Util.loadXML(new String(Base64.getDecoder().decode(reply), StandardCharsets.UTF_8));
		assertTrue(Util.validateSign(response, java.util.List.of(certificate), null, "sha256", "/samlp:LogoutResponse/ds:Signature", false));
	}

	private String response(String requestId, String audience, String recipient, Instant issue, boolean advice, String issuer) throws Exception {
		String assertion = "<saml:Assertion xmlns:saml=\"" + Constants.NS_SAML + "\" xmlns:samlp=\"" + Constants.NS_SAMLP
				+ "\" ID=\"_" + FederatedSessionStore.randomId() + "\" Version=\"2.0\" IssueInstant=\"" + issue + "\">"
				+ "<saml:Issuer>" + issuer + "</saml:Issuer>"
				+ "<saml:Subject><saml:NameID Format=\"" + Constants.NAMEID_TRANSIENT + "\">synthetic-name</saml:NameID>"
				+ "<saml:SubjectConfirmation Method=\"urn:oasis:names:tc:SAML:2.0:cm:bearer\"><saml:SubjectConfirmationData InResponseTo=\""
				+ requestId + "\" Recipient=\"" + recipient + "\" NotOnOrAfter=\"" + issue.plusSeconds(180) + "\"/></saml:SubjectConfirmation></saml:Subject>"
				+ "<saml:Conditions NotBefore=\"" + issue.minusSeconds(60) + "\" NotOnOrAfter=\"" + issue.plusSeconds(180)
				+ "\"><saml:AudienceRestriction><saml:Audience>" + audience + "</saml:Audience></saml:AudienceRestriction></saml:Conditions>"
				+ (advice ? "<saml:Advice><saml:Assertion ID=\"_mapping\" Version=\"2.0\" IssueInstant=\"" + issue
						+ "\"><saml:Issuer>https://idp.invalid/metadata</saml:Issuer></saml:Assertion></saml:Advice>" : "")
				+ "<saml:AuthnStatement AuthnInstant=\"" + issue + "\" SessionIndex=\"synthetic-session\"><saml:AuthnContext>"
				+ "<saml:AuthnContextClassRef>" + CONTEXT
				+ "</saml:AuthnContextClassRef></saml:AuthnContext></saml:AuthnStatement>"
				+ "<saml:AttributeStatement><saml:Attribute Name=\"CodiceFiscale\"><saml:AttributeValue>SYNTHETIC-IDENTITY</saml:AttributeValue>"
				+ "</saml:Attribute></saml:AttributeStatement></saml:Assertion>";
		String signed = Util.addSign(Util.loadXML(assertion), key, certificate, settings.getSignatureAlgorithm(), settings.getDigestAlgorithm());
		String xml = "<samlp:Response xmlns:samlp=\"" + Constants.NS_SAMLP + "\" xmlns:saml=\"" + Constants.NS_SAML
				+ "\" ID=\"_" + FederatedSessionStore.randomId() + "\" Version=\"2.0\" IssueInstant=\"" + issue
				+ "\" InResponseTo=\"" + requestId + "\" Destination=\"https://sp.invalid/acs\"><saml:Issuer>" + issuer + "</saml:Issuer>"
				+ "<samlp:Status><samlp:StatusCode Value=\"" + Constants.STATUS_SUCCESS + "\"/></samlp:Status>"
				+ signed.replaceFirst("<\\?xml[^>]*\\?>", "") + "</samlp:Response>";
		return Base64.getEncoder().encodeToString(Util.addSign(Util.loadXML(xml), key, certificate,
				settings.getSignatureAlgorithm(), settings.getDigestAlgorithm()).getBytes(StandardCharsets.UTF_8));
	}

	private Map<String, String> query(URI uri) {
		Map<String, String> result = new HashMap<>();
		for (String part : uri.getRawQuery().split("&")) {
			String[] pair = part.split("=", 2);
			result.put(URLDecoder.decode(pair[0], StandardCharsets.UTF_8), URLDecoder.decode(pair[1], StandardCharsets.UTF_8));
		}
		return result;
	}
}
