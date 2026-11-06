package it.eng.spagobi.security.saml;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;

/**
 * Read-only PM-API v1.2 adapter; never provisions PM or Knowage accounts.
 */
public class PmApiProfilingService implements ProjectProfilingService {
	private static final ObjectMapper JSON = new ObjectMapper();
	private final SamlConfiguration config;
	private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5))
			.followRedirects(HttpClient.Redirect.NEVER).build();
	private final HttpClient http;
	private final String identityParameter;
	private final String identityField;

	public PmApiProfilingService(SamlConfiguration config) {
		this(config, HTTP);
	}

	PmApiProfilingService(SamlConfiguration config, HttpClient http) {
		this.config = config;
		this.http = http;
		config.required("identity.attribute", "identity.fiscal-number-attribute");
		if (config.optional("identity.prefix", "identity.fiscal-number-prefix", "") == null) {
			throw new IllegalStateException("Missing SAML identity prefix configuration");
		}
		identityParameter = identityName("pm.identity-parameter");
		identityField = identityName("pm.identity-field");
		if ("codiceProcedura".equals(identityParameter)) {
			throw new IllegalStateException("PM identity parameter conflicts with codiceProcedura");
		}
	}

	private String identityName(String key) {
		String value = config.optional(key, "codiceFiscale");
		if (value == null || !value.matches("[A-Za-z_][A-Za-z0-9_.-]*") || value.contains("REPLACE_")) {
			throw new IllegalStateException("Invalid PM identity configuration: " + key);
		}
		return value;
	}

	@Override
	public SpagoBIUserProfile resolve(SamlIdentity identity) throws Exception {
		String identifier = identity.identifier(config);
		String accessToken = token();
		String query = encode(identityParameter) + "=" + encode(identifier)
				+ "&codiceProcedura=" + encode(config.required("pm.procedure"));
		JsonNode profile = get("pm.profile-url", query, accessToken).path("profili");
		if (!profile.isArray() || profile.size() != 1 || !profile.get(0).path("procedura").isArray()
				|| profile.get(0).path("procedura").size() != 1) {
			throw new IllegalArgumentException("Missing or ambiguous PM profile");
		}
		String account = text(profile.get(0), "account");
		JsonNode users = get("pm.user-url", "account=" + encode(account) + "&tipoUtente=" + encode(config.required("pm.user-type")),
				accessToken).path("utenti");
		if (!users.isArray() || users.size() != 1 || !account.equals(text(users.get(0), "account"))
				|| !identifier.equals(text(users.get(0), identityField))
				|| !users.get(0).path("flgBloccoUtente").isBoolean() || users.get(0).path("flgBloccoUtente").asBoolean()) {
			throw new IllegalArgumentException("PM identity is blocked or inconsistent");
		}
		JsonNode authorization = get("pm.authorization-url", query, accessToken);
		LocalDate today = LocalDate.now(ZoneId.of("Europe/Rome"));
		SpagoBIUserProfile result = mapAuthorization(authorization, account, config.required("pm.procedure"),
				config.mapping("pm.role-map."), config.mapping("pm.tenant-map."), today);
		Set<String> functions = mapFunctions(profile.get(0).path("procedura").get(0).path("funzioni"),
				result.getAttributes().get("pm_organization").toString(), config.mapping("pm.function-map."),
				config.mapping("pm.access-type-map."));
		result.getAttributes().put("pm_functions", JSON.writeValueAsString(functions));
		result.setUserName((users.get(0).path("nome").asText("") + " " + users.get(0).path("cognome").asText("")).trim());
		if (result.getUserName().isEmpty()) {
			result.setUserName(account);
		}
		result.getAttributes().put("pm_account", account);
		result.getAttributes().put("pm_procedure", config.required("pm.procedure"));
		String email = profile.get(0).path("email").asText("");
		if (!email.isEmpty()) {
			result.getAttributes().put("email", email);
		}
		result.getAttributes().put("pm_identity", identifier);
		if ("codiceFiscale".equals(identityField)) {
			result.getAttributes().put("codiceFiscale", identifier);
		}
		return result;
	}

	public static Set<String> mapFunctions(JsonNode functions, String organization, Map<String, String> functionMapping,
			Map<String, String> accessTypeMapping) {
		if (!functions.isArray()) {
			throw new IllegalArgumentException("Missing PM functions");
		}
		Set<String> allowed = new LinkedHashSet<>();
		for (JsonNode function : functions) {
			String mapped = functionMapping.get(text(function, "codice"));
			if (mapped == null || !function.path("accesso").isArray()) {
				throw new IllegalArgumentException("Unmapped PM function");
			}
			for (JsonNode access : function.path("accesso")) {
				String accessFunctions = accessTypeMapping.get(text(access, "tipoAccesso"));
				if (accessFunctions == null || !access.path("organizzazione").isArray()) {
					throw new IllegalArgumentException("Unmapped PM data access restriction");
				}
				boolean matching = "*".equals(organization) && access.path("organizzazione").isEmpty();
				for (JsonNode org : access.path("organizzazione")) {
					matching |= organization.equals(text(org, "idOrganizzazione"));
				}
				if (matching) {
					Set<String> accessAllowed = Set.of(accessFunctions.split(","));
					for (String name : mapped.split(",")) {
						if (accessAllowed.contains(name)) {
							allowed.add(name);
						}
					}
				}
			}
		}
		if (allowed.isEmpty()) {
			throw new IllegalArgumentException("No mapped PM functionality is enabled");
		}
		return allowed;
	}

	public static SpagoBIUserProfile mapAuthorization(JsonNode response, String account, String procedure,
			Map<String, String> roleMapping, Map<String, String> tenantMapping, LocalDate today) {
		if (!response.path("autorizzato").isBoolean() || !response.path("autorizzato").asBoolean()
				|| !account.equals(text(response, "accountUtente")) || !procedure.equals(text(response, "codiceProcedura"))
				|| !response.path("autorizzazioni").isArray()) {
			throw new IllegalArgumentException("PM access denied");
		}
		Set<String> roles = new LinkedHashSet<>();
		Set<String> organizations = new LinkedHashSet<>();
		String tenant = null;
		if (new java.util.HashSet<>(tenantMapping.values()).size() != tenantMapping.size()) {
			throw new IllegalArgumentException("PM organizations require distinct dedicated tenants");
		}
		for (JsonNode grant : response.path("autorizzazioni")) {
			LocalDate start = LocalDate.parse(text(grant, "dataInizioValidita"));
			LocalDate end = grant.path("dataFineValidita").isNull() ? null : LocalDate.parse(text(grant, "dataFineValidita"));
			if (today.isBefore(start) || (end != null && today.isAfter(end))) {
				continue;
			}
			String role = roleMapping.get(text(grant, "codiceRuolo"));
			String organization = grant.path("idOrganizzazione").isNull() ? "*" : text(grant, "idOrganizzazione");
			String mappedTenant = tenantMapping.get(organization);
			if (role == null || mappedTenant == null || (tenant != null && !tenant.equals(mappedTenant))) {
				throw new IllegalArgumentException("Unmapped or ambiguous PM authorization");
			}
			roles.add(role);
			organizations.add(organization);
			tenant = mappedTenant;
		}
		// Distinct data restrictions cannot silently be flattened into one tenant.
		if (roles.isEmpty() || organizations.size() != 1) {
			throw new IllegalArgumentException("No unambiguous active PM authorization");
		}
		SpagoBIUserProfile result = new SpagoBIUserProfile();
		result.setUserId(account);
		result.setOrganization(tenant);
		result.setRoles(roles.toArray(new String[0]));
		result.setIsSuperadmin(false);
		result.getAttributes().put("pm_organization", organizations.iterator().next());
		return result;
	}

	private String token() throws Exception {
		String credentials = config.required("pm.client-id") + ":" + config.required("pm.client-secret");
		HttpRequest request = HttpRequest.newBuilder(config.url("pm.token-url")).timeout(Duration.ofSeconds(10))
				.header("Authorization", "Basic " + java.util.Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8)))
				.header("Content-Type", "application/x-www-form-urlencoded")
				.POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials")).build();
		JsonNode response = send(request);
		if (!"Bearer".equalsIgnoreCase(text(response, "token_type"))) {
			throw new IllegalArgumentException("Unsupported VAM token type");
		}
		return text(response, "access_token");
	}

	private JsonNode get(String key, String query, String token) throws Exception {
		URI endpoint = config.url(key);
		String url = endpoint + (endpoint.getQuery() == null ? "?" : "&") + query;
		return send(HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(10))
				.header("Accept", "application/json").header("Authorization", "Bearer " + token)
				.header("X-PM-User", config.required("pm.service-user"))
				.header("X-PM-PW", config.required("pm.service-password")).GET().build());
	}

	private JsonNode send(HttpRequest request) throws Exception {
		CompletableFuture<HttpResponse<byte[]>> pending = http.sendAsync(request, info -> boundedBody());
		try {
			HttpResponse<byte[]> response = pending.get(10, TimeUnit.SECONDS);
			if (response.statusCode() != 200) {
				throw new IllegalStateException("PM/VAM request failed (HTTP " + response.statusCode() + ")");
			}
			return JSON.readTree(response.body());
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw e;
		} finally {
			pending.cancel(true);
		}
	}

	static HttpResponse.BodySubscriber<byte[]> boundedBody() {
		HttpResponse.BodySubscriber<byte[]> delegate = HttpResponse.BodySubscribers.ofByteArray();
		return new HttpResponse.BodySubscriber<byte[]>() {
			private Flow.Subscription subscription;
			private long size;
			private boolean rejected;

			@Override
			public CompletionStage<byte[]> getBody() {
				return delegate.getBody();
			}

			@Override
			public void onSubscribe(Flow.Subscription value) {
				subscription = value;
				delegate.onSubscribe(value);
			}

			@Override
			public void onNext(List<ByteBuffer> buffers) {
				if (rejected) {
					return;
				}
				for (ByteBuffer buffer : buffers) {
					size += buffer.remaining();
				}
				if (size > 1048576) {
					rejected = true;
					subscription.cancel();
					delegate.onError(new IllegalArgumentException("PM response too large"));
				} else {
					delegate.onNext(buffers);
				}
			}

			@Override
			public void onError(Throwable error) {
				if (!rejected) {
					delegate.onError(error);
				}
			}

			@Override
			public void onComplete() {
				if (!rejected) {
					delegate.onComplete();
				}
			}
		};
	}

	private static String text(JsonNode value, String name) {
		JsonNode field = value.path(name);
		if ((!field.isTextual() && !field.isIntegralNumber()) || field.asText().trim().isEmpty()) {
			throw new IllegalArgumentException("Missing PM field: " + name);
		}
		return field.asText().trim();
	}

	private static String encode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}
}
