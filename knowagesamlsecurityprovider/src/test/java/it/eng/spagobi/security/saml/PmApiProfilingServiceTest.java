package it.eng.spagobi.security.saml;

import static org.junit.Assert.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;

public class PmApiProfilingServiceTest {
	private final ObjectMapper json = new ObjectMapper();
	private final LocalDate today = LocalDate.of(2026, 11, 4);

	@Test
	public void oversizedResponsesAreCancelledBeforeTheyCanBeBuffered() {
		java.util.concurrent.atomic.AtomicBoolean cancelled = new java.util.concurrent.atomic.AtomicBoolean();
		java.net.http.HttpResponse.BodySubscriber<byte[]> body = PmApiProfilingService.boundedBody();
		body.onSubscribe(new java.util.concurrent.Flow.Subscription() {
			@Override
			public void request(long count) {
			}

			@Override
			public void cancel() {
				cancelled.set(true);
			}
		});
		body.onNext(java.util.List.of(java.nio.ByteBuffer.allocate(1048577)));
		assertTrue(cancelled.get());
		assertTrue(body.getBody().toCompletableFuture().isCompletedExceptionally());
	}

	private JsonNode authorization(String grants) throws Exception {
		return json.readTree("{\"autorizzato\":true,\"accountUtente\":\"synthetic-account\",\"codiceProcedura\":\"test-procedure\","
				+ "\"autorizzazioni\":[" + grants + "]}");
	}

	private String grant(String role, String organization, String from, String to) {
		return "{\"codiceRuolo\":\"" + role + "\",\"idOrganizzazione\":\"" + organization + "\","
				+ "\"dataInizioValidita\":\"" + from + "\",\"dataFineValidita\":" + to + "}";
	}

	@Test
	public void mapsOnlyCurrentExplicitlyConfiguredGrants() throws Exception {
		JsonNode response = authorization(grant("reader", "42", "2026-11-04", "null") + ","
				+ grant("old-admin", "99", "2020-01-01", "\"2026-11-03\""));
		SpagoBIUserProfile profile = PmApiProfilingService.mapAuthorization(response, "synthetic-account", "test-procedure",
				Map.of("reader", "/test/reader"), Map.of("42", "test-tenant"), today);
		assertArrayEquals(new String[] { "/test/reader" }, profile.getRoles());
		assertEquals("test-tenant", profile.getOrganization());
		assertFalse(profile.getIsSuperadmin());
	}

	@Test
	public void rejectsWrongAccountProcedureUnmappedAndAmbiguousGrants() throws Exception {
		JsonNode response = authorization(grant("reader", "42", "2020-01-01", "null"));
		assertThrows(IllegalArgumentException.class, () -> PmApiProfilingService.mapAuthorization(response,
				"other-account", "test-procedure", Map.of("reader", "reader"), Map.of("42", "tenant"), today));
		assertThrows(IllegalArgumentException.class, () -> PmApiProfilingService.mapAuthorization(response,
				"synthetic-account", "other-procedure", Map.of("reader", "reader"), Map.of("42", "tenant"), today));
		assertThrows(IllegalArgumentException.class, () -> PmApiProfilingService.mapAuthorization(response,
				"synthetic-account", "test-procedure", Map.of(), Map.of("42", "tenant"), today));
		assertThrows(IllegalArgumentException.class, () -> PmApiProfilingService.mapAuthorization(response,
				"synthetic-account", "test-procedure", Map.of("reader", "reader"), Map.of("42", "tenant", "43", "tenant"), today));
		JsonNode ambiguous = authorization(grant("reader", "42", "2020-01-01", "null") + ","
				+ grant("reader", "43", "2020-01-01", "null"));
		assertThrows(IllegalArgumentException.class, () -> PmApiProfilingService.mapAuthorization(ambiguous,
				"synthetic-account", "test-procedure", Map.of("reader", "reader"), Map.of("42", "tenant", "43", "tenant"), today));
	}

	@Test
	public void expiredFutureOrDeniedUsersHaveNoFallbackRole() throws Exception {
		for (JsonNode response : new JsonNode[] {
				authorization(grant("reader", "42", "2020-01-01", "\"2026-11-03\"")),
				authorization(grant("reader", "42", "2026-11-05", "null")),
				json.readTree("{\"autorizzato\":false}") }) {
			assertThrows(IllegalArgumentException.class, () -> PmApiProfilingService.mapAuthorization(response,
					"synthetic-account", "test-procedure", Map.of("reader", "reader"), Map.of("42", "tenant"), today));
		}
	}

	@Test
	public void intersectsFunctionAccessTypeAndOrganizationRestrictions() throws Exception {
		JsonNode functions = json.readTree("[{\"codice\":\"reports\",\"accesso\":[{\"tipoAccesso\":\"read\","
				+ "\"organizzazione\":[{\"idOrganizzazione\":\"42\"}]}]}]");
		assertEquals(Set.of("ViewReports"), PmApiProfilingService.mapFunctions(functions, "42",
				Map.of("reports", "ViewReports,DeleteReports"), Map.of("read", "ViewReports")));
		assertThrows(IllegalArgumentException.class, () -> PmApiProfilingService.mapFunctions(functions, "other",
				Map.of("reports", "ViewReports"), Map.of("read", "ViewReports")));
		assertThrows(IllegalArgumentException.class, () -> PmApiProfilingService.mapFunctions(functions, "42",
				Map.of("reports", "ViewReports"), Map.of()));
	}

	@Test
	public void legacyFiscalIdentityStillResolvesAndPreservesItsAttribute() throws Exception {
		Properties properties = profilingProperties();
		properties.setProperty("identity.fiscal-number-prefix", "TINIT-");
		StubHttpClient http = http(user("codiceFiscale", "TEST-CF-001"));
		SpagoBIUserProfile profile = new PmApiProfilingService(new SamlConfiguration(properties), http)
				.resolve(identity(Map.of("CodiceFiscale", List.of("TINIT-TEST-CF-001"))));
		assertEquals("synthetic-account", profile.getUserId());
		assertEquals("TEST-CF-001", profile.getAttributes().get("codiceFiscale"));
		assertEquals("TEST-CF-001", profile.getAttributes().get("pm_identity"));
		assertIdentityQueries(http, "codiceFiscale=TEST-CF-001&codiceProcedura=test-procedure");
	}

	@Test
	public void alternativeIdentityResolvesWithoutAnyFiscalNumber() throws Exception {
		Properties properties = profilingProperties();
		properties.setProperty("identity.attribute", "SubjectId");
		properties.setProperty("identity.fiscal-number-prefix", "opaque+");
		properties.setProperty("pm.identity-parameter", "subjectId");
		properties.setProperty("pm.identity-field", "externalId");
		for (String prefix : List.of("", "external:")) {
			properties.setProperty("identity.prefix", prefix);
			StubHttpClient http = http(user("externalId", "opaque+id&42"));
			SpagoBIUserProfile profile = new PmApiProfilingService(new SamlConfiguration(properties), http)
					.resolve(identity(Map.of("SubjectId", List.of(prefix + "opaque+id&42"))));
			assertEquals("synthetic-account", profile.getUserId());
			assertEquals("opaque+id&42", profile.getAttributes().get("pm_identity"));
			assertFalse(profile.getAttributes().containsKey("codiceFiscale"));
			assertArrayEquals(new String[] { "/test/reader" }, profile.getRoles());
			assertIdentityQueries(http, "subjectId=opaque%2Bid%2642&codiceProcedura=test-procedure");
		}
	}

	@Test
	public void configuredIdentityNeverFallsBackToAnotherAttribute() throws Exception {
		Properties properties = profilingProperties();
		properties.setProperty("identity.attribute", "SubjectId");
		properties.setProperty("identity.prefix", "external:");
		StubHttpClient http = http(user("codiceFiscale", "TEST-CF-001"));
		PmApiProfilingService service = new PmApiProfilingService(new SamlConfiguration(properties), http);
		for (Map<String, List<String>> attributes : List.of(
				Map.of("CodiceFiscale", List.of("TEST-CF-001")),
				Map.of("SubjectId", List.of("first", "second")),
				Map.of("SubjectId", List.of("external:")))) {
			assertThrows(IllegalArgumentException.class, () -> service.resolve(identity(attributes)));
		}
		assertTrue(http.requests.isEmpty());
	}

	@Test
	public void alternativeIdentityMustMatchThePmUserBeforeAuthorization() throws Exception {
		Properties properties = profilingProperties();
		properties.setProperty("identity.attribute", "SubjectId");
		properties.setProperty("pm.identity-parameter", "subjectId");
		properties.setProperty("pm.identity-field", "externalId");
		ObjectNode missing = user("codiceFiscale", "TEST-CF-001");
		ObjectNode blocked = user("externalId", "expected");
		blocked.put("flgBloccoUtente", true);
		ObjectNode otherAccount = user("externalId", "expected");
		otherAccount.put("account", "other-account");
		for (ObjectNode user : List.of(missing, user("externalId", "different"), blocked, otherAccount)) {
			StubHttpClient http = http(user);
			PmApiProfilingService service = new PmApiProfilingService(new SamlConfiguration(properties), http);
			assertThrows(IllegalArgumentException.class,
					() -> service.resolve(identity(Map.of("SubjectId", List.of("expected")))));
			assertEquals(3, http.requests.size());
			assertFalse(http.requests.stream().anyMatch(request -> "/authorization".equals(request.uri().getPath())));
		}
	}

	@Test
	public void invalidIdentityParametersAreRejectedBeforeCallingPm() throws Exception {
		for (String parameter : List.of("", "codiceProcedura", "subjectId&account", "REPLACE_IDENTITY_PARAMETER")) {
			Properties properties = profilingProperties();
			properties.setProperty("pm.identity-parameter", parameter);
			StubHttpClient http = http(user("codiceFiscale", "TEST-CF-001"));
			assertThrows(IllegalStateException.class,
					() -> new PmApiProfilingService(new SamlConfiguration(properties), http));
			assertTrue(http.requests.isEmpty());
		}
	}

	private Properties profilingProperties() {
		Properties properties = new Properties();
		properties.setProperty("identity.fiscal-number-attribute", "CodiceFiscale");
		properties.setProperty("pm.procedure", "test-procedure");
		properties.setProperty("pm.user-type", "SPID");
		for (String endpoint : List.of("token", "profile", "user", "authorization")) {
			properties.setProperty("pm." + endpoint + "-url", "https://pm.example.invalid/" + endpoint);
		}
		properties.setProperty("pm.client-id", "synthetic-client");
		properties.setProperty("pm.client-secret", "synthetic-secret");
		properties.setProperty("pm.service-user", "synthetic-service");
		properties.setProperty("pm.service-password", "synthetic-password");
		properties.setProperty("pm.role-map.reader", "/test/reader");
		properties.setProperty("pm.tenant-map.42", "test-tenant");
		properties.setProperty("pm.function-map.reports", "ViewReports");
		properties.setProperty("pm.access-type-map.read", "ViewReports");
		return properties;
	}

	private SamlIdentity identity(Map<String, List<String>> attributes) {
		return new SamlIdentity("https://idp.example.invalid", json.createObjectNode(), "synthetic-session",
				0, Long.MAX_VALUE, attributes);
	}

	private ObjectNode user(String identityField, String identifier) {
		ObjectNode user = json.createObjectNode();
		user.put("account", "synthetic-account");
		user.put(identityField, identifier);
		user.put("flgBloccoUtente", false);
		return user;
	}

	private StubHttpClient http(ObjectNode user) throws Exception {
		String profile = "{\"profili\":[{\"account\":\"synthetic-account\",\"procedura\":[{\"funzioni\":["
				+ "{\"codice\":\"reports\",\"accesso\":[{\"tipoAccesso\":\"read\",\"organizzazione\":["
				+ "{\"idOrganizzazione\":\"42\"}]}]}]}]}]}";
		return new StubHttpClient(Map.of(
				"/token", "{\"token_type\":\"Bearer\",\"access_token\":\"synthetic-token\"}",
				"/profile", profile,
				"/user", json.createObjectNode().set("utenti", json.createArrayNode().add(user)).toString(),
				"/authorization", authorization(grant("reader", "42", "2020-01-01", "null")).toString()));
	}

	private void assertIdentityQueries(StubHttpClient http, String query) {
		assertEquals(4, http.requests.size());
		assertEquals(query, http.requests.get(1).uri().getRawQuery());
		assertEquals("account=synthetic-account&tipoUtente=SPID", http.requests.get(2).uri().getRawQuery());
		assertEquals(query, http.requests.get(3).uri().getRawQuery());
	}

	private static class StubHttpClient extends java.net.http.HttpClient {
		final List<java.net.http.HttpRequest> requests = new ArrayList<>();
		private final Map<String, String> responses;

		StubHttpClient(Map<String, String> responses) {
			this.responses = responses;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> java.util.concurrent.CompletableFuture<java.net.http.HttpResponse<T>> sendAsync(
				java.net.http.HttpRequest request, java.net.http.HttpResponse.BodyHandler<T> handler) {
			requests.add(request);
			byte[] bytes = responses.get(request.uri().getPath()).getBytes(java.nio.charset.StandardCharsets.UTF_8);
			java.net.http.HttpResponse<byte[]> response = new java.net.http.HttpResponse<byte[]>() {
				@Override
				public int statusCode() {
					return 200;
				}
				@Override
				public java.net.http.HttpRequest request() {
					return request;
				}
				@Override
				public java.util.Optional<java.net.http.HttpResponse<byte[]>> previousResponse() {
					return java.util.Optional.empty();
				}
				@Override
				public java.net.http.HttpHeaders headers() {
					return java.net.http.HttpHeaders.of(Map.of(), (key, value) -> true);
				}
				@Override
				public byte[] body() {
					return bytes;
				}
				@Override
				public java.util.Optional<javax.net.ssl.SSLSession> sslSession() {
					return java.util.Optional.empty();
				}
				@Override
				public java.net.URI uri() {
					return request.uri();
				}
				@Override
				public Version version() {
					return Version.HTTP_1_1;
				}
			};
			return java.util.concurrent.CompletableFuture.completedFuture((java.net.http.HttpResponse<T>) response);
		}

		@Override
		public <T> java.util.concurrent.CompletableFuture<java.net.http.HttpResponse<T>> sendAsync(
				java.net.http.HttpRequest request, java.net.http.HttpResponse.BodyHandler<T> handler,
				java.net.http.HttpResponse.PushPromiseHandler<T> pushHandler) {
			return sendAsync(request, handler);
		}

		@Override
		public <T> java.net.http.HttpResponse<T> send(java.net.http.HttpRequest request,
				java.net.http.HttpResponse.BodyHandler<T> handler) {
			throw new UnsupportedOperationException();
		}

		@Override
		public java.util.Optional<java.net.CookieHandler> cookieHandler() {
			return java.util.Optional.empty();
		}
		@Override
		public java.util.Optional<java.time.Duration> connectTimeout() {
			return java.util.Optional.empty();
		}
		@Override
		public Redirect followRedirects() {
			return Redirect.NEVER;
		}
		@Override
		public java.util.Optional<java.net.ProxySelector> proxy() {
			return java.util.Optional.empty();
		}
		@Override
		public javax.net.ssl.SSLContext sslContext() {
			throw new UnsupportedOperationException();
		}
		@Override
		public javax.net.ssl.SSLParameters sslParameters() {
			return new javax.net.ssl.SSLParameters();
		}
		@Override
		public java.util.Optional<java.net.Authenticator> authenticator() {
			return java.util.Optional.empty();
		}
		@Override
		public Version version() {
			return Version.HTTP_1_1;
		}
		@Override
		public java.util.Optional<java.util.concurrent.Executor> executor() {
			return java.util.Optional.empty();
		}
	}
}
