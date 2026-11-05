package it.eng.spagobi.security.saml;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.node.ObjectNode;

public final class SamlIdentity {
	public final String issuer;
	public final ObjectNode nameId;
	public final String sessionIndex;
	public final long authnInstant;
	public final long expiresAt;
	public final Map<String, List<String>> attributes;

	public SamlIdentity(String issuer, ObjectNode nameId, String sessionIndex, long authnInstant, long expiresAt,
			Map<String, List<String>> attributes) {
		this.issuer = issuer;
		this.nameId = nameId.deepCopy();
		this.sessionIndex = sessionIndex;
		this.authnInstant = authnInstant;
		this.expiresAt = expiresAt;
		this.attributes = Map.copyOf(attributes);
	}

	public String singleAttribute(String name) {
		List<String> values = attributes.get(name);
		if (values == null || values.size() != 1 || values.get(0).trim().isEmpty()) {
			throw new IllegalArgumentException("Missing or ambiguous SAML identity attribute");
		}
		return values.get(0).trim();
	}
}
