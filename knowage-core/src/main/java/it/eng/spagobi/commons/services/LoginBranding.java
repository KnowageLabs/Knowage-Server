package it.eng.spagobi.commons.services;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

final class LoginBranding {

	static final String CONFIG_LABEL = "KNOWAGE.LOGIN.BRANDING";
	private static final String[] FIELDS = { "backgroundUrl", "logoUrl", "faviconUrl" };
	private static final Logger LOGGER = Logger.getLogger(LoginBranding.class);

	private LoginBranding() {
	}

	static Map<String, String> read(String value) throws JSONException {
		try {
			Map<String, String> branding = new LinkedHashMap<>();
			JSONObject json = new JSONObject(value == null || value.trim().isEmpty() ? "{}" : value);
			for (String field : FIELDS) {
				branding.put(field, json.optString(field, ""));
			}
			return normalize(branding);
		} catch (Exception e) {
			LOGGER.warn("Invalid login branding configuration, using defaults");
			return normalize(new LinkedHashMap<>());
		}
	}

	static Map<String, String> normalize(Map<String, String> body) throws JSONException {
		if (body == null) {
			throw new IllegalArgumentException("A login branding configuration is required");
		}
		Map<String, String> branding = new LinkedHashMap<>();
		for (String field : FIELDS) {
			String value = body.get(field);
			value = value == null ? "" : value.trim();
			if (!value.isEmpty() && !isImageUrl(value)) {
				throw new IllegalArgumentException(field + " must be an HTTP(S) URL or a root-relative path");
			}
			branding.put(field, value);
		}
		// SBI_CONFIG.VALUE_CHECK already supports 1000 characters; no schema change is needed.
		if (new JSONObject(branding).toString().length() > 1000) {
			throw new IllegalArgumentException("Login branding configuration exceeds 1000 characters");
		}
		return branding;
	}

	private static boolean isImageUrl(String value) {
		if (value.matches(".*[\\p{Cntrl}\\s\\\\<>\"'].*")) {
			return false;
		}
		try {
			URI uri = new URI(value);
			if (value.startsWith("/") && !value.startsWith("//")) {
				return uri.getScheme() == null && uri.getRawAuthority() == null;
			}
			return ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
					&& uri.getHost() != null && uri.getRawUserInfo() == null;
		} catch (URISyntaxException e) {
			return false;
		}
	}
}
