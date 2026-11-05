package it.eng.spagobi.security.saml;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.onelogin.saml2.settings.IdPMetadataParser;
import com.onelogin.saml2.settings.Saml2Settings;
import com.onelogin.saml2.settings.SettingsBuilder;
import com.onelogin.saml2.util.Constants;
import com.onelogin.saml2.util.Util;

public final class SamlConfiguration {
	private final Properties properties;

	public SamlConfiguration(Properties properties) {
		this.properties = new Properties();
		this.properties.putAll(properties);
	}

	public static boolean enabled() {
		return Boolean.parseBoolean(System.getProperty("knowage.saml.enabled", System.getenv("KNOWAGE_SAML_ENABLED")));
	}

	public static SamlConfiguration load() {
		if (!enabled()) {
			throw new IllegalStateException("SAML is disabled");
		}
		String file = System.getProperty("knowage.saml.config", System.getenv("KNOWAGE_SAML_CONFIG"));
		Properties values = new Properties();
		try (InputStream in = Files.newInputStream(Paths.get(file))) {
			values.load(in);
			return new SamlConfiguration(values);
		} catch (Exception e) {
			throw new IllegalStateException("Cannot load SAML configuration", e);
		}
	}

	public String optional(String key, String fallback) {
		String value = properties.getProperty(key, fallback);
		if (value != null && value.startsWith("env:")) {
			value = System.getenv(value.substring(4));
		}
		return value;
	}

	public String required(String key) {
		String value = optional(key, null);
		if (value == null || value.trim().isEmpty() || value.contains("REPLACE_")) {
			throw new IllegalStateException("Missing SAML configuration: " + key);
		}
		return value.trim();
	}

	public String required(String key, String legacyKey) {
		return required(properties.containsKey(key) ? key : legacyKey);
	}

	public String optional(String key, String legacyKey, String fallback) {
		return optional(properties.containsKey(key) ? key : legacyKey, fallback);
	}

	public URI url(String key) {
		URI uri = URI.create(required(key));
		if (!"https".equals(uri.getScheme()) || uri.getHost() == null || uri.getUserInfo() != null
				|| uri.getFragment() != null) {
			throw new IllegalStateException("An absolute HTTPS URL is required: " + key);
		}
		return uri;
	}

	public long seconds(String key, long fallback) {
		long value = Long.parseLong(optional(key, Long.toString(fallback)));
		if (value < 1 || value > 36000) {
			throw new IllegalStateException("Invalid SAML lifetime: " + key);
		}
		return value;
	}

	public Map<String, String> mapping(String prefix) {
		Map<String, String> result = new HashMap<>();
		for (String key : properties.stringPropertyNames()) {
			if (key.startsWith(prefix)) {
				result.put(key.substring(prefix.length()), required(key));
			}
		}
		return result;
	}

	public boolean myId() {
		return "myid".equals(required("provider"));
	}

	public Saml2Settings settings() throws Exception {
		if (!myId() && !"standard".equals(required("provider"))) {
			throw new IllegalStateException("Unknown SAML provider");
		}
		url("frontend.url");
		url("sp.acs-url");
		url("sp.slo-url");
		Document metadata = Util.loadXML(Files.readString(Paths.get(required("idp.metadata-file"))));
		Map<String, Object> values = IdPMetadataParser.parseXML(metadata, required("idp.entity-id"),
				Constants.NAMEID_TRANSIENT, Constants.BINDING_HTTP_REDIRECT, Constants.BINDING_HTTP_POST);
		if (!Constants.BINDING_HTTP_REDIRECT.equals(values.get("onelogin.saml2.idp.single_sign_on_service.binding"))) {
			throw new IllegalStateException("IdP has no HTTP-Redirect SSO endpoint");
		}
		// Trust only signing keys in IDPSSODescriptor, never the gateway's SP keys.
		values.keySet().removeIf(k -> k.startsWith("onelogin.saml2.idp.x509cert"));
		org.w3c.dom.Node descriptor = null;
		NodeList descriptors = Util.query(metadata, "//md:EntityDescriptor/md:IDPSSODescriptor");
		for (int i = 0; i < descriptors.getLength(); i++) {
			if (required("idp.entity-id").equals(((org.w3c.dom.Element) descriptors.item(i).getParentNode()).getAttribute("entityID"))) {
				if (descriptor != null) {
					throw new IllegalStateException("Duplicate IdP descriptor");
				}
				descriptor = descriptors.item(i);
			}
		}
		if (descriptor == null) {
			throw new IllegalStateException("IdP descriptor is missing");
		}
		NodeList certificates = Util.query(metadata,
				"./md:KeyDescriptor[not(@use) or @use='signing']/ds:KeyInfo/ds:X509Data/ds:X509Certificate", descriptor);
		if (certificates.getLength() == 0 || !required("idp.entity-id").equals(values.get("onelogin.saml2.idp.entityid"))) {
			throw new IllegalStateException("IdP signing metadata is missing");
		}
		for (int i = 0; i < certificates.getLength(); i++) {
			values.put(i == 0 ? "onelogin.saml2.idp.x509cert" : "onelogin.saml2.idp.x509certMulti." + (i - 1),
					certificates.item(i).getTextContent());
		}
		values.put("onelogin.saml2.sp.entityid", required("sp.entity-id"));
		values.put("onelogin.saml2.sp.assertion_consumer_service.url", url("sp.acs-url").toString());
		values.put("onelogin.saml2.sp.assertion_consumer_service.binding", Constants.BINDING_HTTP_POST);
		values.put("onelogin.saml2.sp.single_logout_service.url", url("sp.slo-url").toString());
		values.put("onelogin.saml2.sp.single_logout_service.binding", Constants.BINDING_HTTP_POST);
		values.put("onelogin.saml2.sp.nameidformat", optional("sp.name-id-format", Constants.NAMEID_TRANSIENT));
		// The public MyID metadata has no SLO endpoint; activation must supply it.
		values.put("onelogin.saml2.idp.single_logout_service.url", url("idp.slo-url").toString());
		values.put("onelogin.saml2.idp.single_logout_service.response.url", url("idp.slo-response-url").toString());
		values.put("onelogin.saml2.idp.single_logout_service.binding", Constants.BINDING_HTTP_POST);
		values.put("onelogin.saml2.security.authnrequest_signed", true);
		values.put("onelogin.saml2.security.want_messages_signed", true);
		values.put("onelogin.saml2.security.want_assertions_signed", true);
		values.put("onelogin.saml2.security.want_xml_validation", true);
		values.put("onelogin.saml2.security.want_nameid", true);
		values.put("onelogin.saml2.security.reject_unsolicited_responses_with_inresponseto", true);
		values.put("onelogin.saml2.security.requested_authncontext", Arrays.asList(required("authn.contexts").split(",")));
		values.put("onelogin.saml2.security.requested_authncontextcomparison", "exact");
		values.put("onelogin.saml2.security.signature_algorithm", myId() ? Constants.RSA_SHA1 : Constants.RSA_SHA256);
		values.put("onelogin.saml2.security.digest_algorithm", myId() ? Constants.SHA1 : Constants.SHA256);
		values.put("onelogin.saml2.security.reject_deprecated_alg", !myId());
		KeyStore store = KeyStore.getInstance(optional("sp.keystore-type", "PKCS12"));
		char[] password = required("sp.keystore-password").toCharArray();
		try (InputStream in = Files.newInputStream(Paths.get(required("sp.keystore-file")))) {
			store.load(in, password);
		} finally {
			Arrays.fill(password, '\0');
		}
		values.put(SettingsBuilder.KEYSTORE_KEY, store);
		values.put(SettingsBuilder.KEYSTORE_ALIAS, required("sp.key-alias"));
		values.put(SettingsBuilder.KEYSTORE_KEY_PASSWORD, required("sp.key-password"));
		Saml2Settings settings = new SettingsBuilder().fromValues(values).build();
		settings.setStrict(true);
		if (!settings.checkSettings().isEmpty() || settings.getSPkey() == null || settings.getSPcert() == null
				|| !"RSA".equals(settings.getSPkey().getAlgorithm())) {
			throw new IllegalStateException("Invalid SAML settings or signing key");
		}
		URI sso = settings.getIdpSingleSignOnServiceUrl().toURI();
		if (!"https".equals(sso.getScheme()) || sso.getHost() == null || sso.getUserInfo() != null || sso.getFragment() != null) {
			throw new IllegalStateException("IdP SSO must use an absolute HTTPS URL");
		}
		return settings;
	}
}
