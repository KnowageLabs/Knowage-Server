package it.eng.spagobi.utilities.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class WhitelistCache {
	private final List<String> whitelist;
	private static WhitelistCache instance;

	private WhitelistCache() {
		whitelist = loadWhitelist();
	}

	public static WhitelistCache getInstance() {
		if (instance == null) {
			instance = new WhitelistCache();
		}
		return instance;
	}

	public List<String> getWhitelist() {
		return whitelist;
	}

	private List<String> loadWhitelist() {
		if (whitelist != null && !whitelist.isEmpty())
			return whitelist;

		Properties properties = new Properties();

		try (InputStream inputStream = WhitelistCache.class.getClassLoader().getResourceAsStream("headers.properties")) {
			if (inputStream != null) {
				properties.load(inputStream);

				String allowedHeaders = properties.getProperty("allowed.headers");
				if (allowedHeaders != null && !allowedHeaders.isEmpty()) {
					return Arrays.asList(allowedHeaders.split(","));
				}
			} else {
				throw new SpagoBIRuntimeException("headers.properties file not found.");
			}
		} catch (IOException e) {
			throw new SpagoBIRuntimeException("Errors closing input stream for headers.properties file.");
		}

		return new ArrayList<>();
	}
}
