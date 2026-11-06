package it.eng.spagobi.security.saml;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SamlProfilingConfigurationTest {
	@Test
	public void normalizesLocalIdentityWithoutPmConfiguration() {
		Properties values = new Properties();
		values.setProperty("profiling.mode", "local");
		values.setProperty("identity.attribute", "username");
		values.setProperty("identity.prefix", "id:");
		SamlConfiguration config = new SamlConfiguration(values);
		SamlIdentity identity = identity(Map.of("username", List.of(" id:reader "), "roles", List.of("admin")));
		assertEquals("local", config.profilingMode());
		assertEquals("reader", identity.identifier(config));
	}

	@Test
	public void missingProfilingModeRetainsPmAndInvalidModesFailClosed() {
		Properties values = new Properties();
		assertEquals("pm", new SamlConfiguration(values).profilingMode());
		for (String mode : new String[] { "", "LOCAL", "other" }) {
			values.setProperty("profiling.mode", mode);
			assertThrows(IllegalStateException.class, () -> new SamlConfiguration(values).profilingMode());
		}
	}

	@Test
	public void localIdentityRejectsMissingAmbiguousAndEmptyNormalizedAttributes() {
		Properties values = new Properties();
		values.setProperty("identity.attribute", "username");
		values.setProperty("identity.prefix", "id:");
		SamlConfiguration config = new SamlConfiguration(values);
		for (Map<String, List<String>> attributes : List.of(
				Map.of("SubjectId", List.of("reader")),
				Map.of("username", List.of("reader", "admin")),
				Map.of("username", List.of("id:")))) {
			assertThrows(IllegalArgumentException.class, () -> identity(attributes).identifier(config));
		}
	}

	private SamlIdentity identity(Map<String, List<String>> attributes) {
		return new SamlIdentity("issuer", new ObjectMapper().createObjectNode().put("value", "transient"),
				"session", 1, Long.MAX_VALUE, attributes);
	}
}
