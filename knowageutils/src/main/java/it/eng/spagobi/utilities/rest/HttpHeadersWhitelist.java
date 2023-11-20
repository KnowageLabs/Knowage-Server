package it.eng.spagobi.utilities.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class HttpHeadersWhitelist {
	private static final HttpHeadersWhitelist INSTANCE = new HttpHeadersWhitelist();
	private static final String PROPERTY_PATH = "headers.properties";
	private static final String PROPERTY_NAME_ALLOWED_HEADERS = "allowed.headers";

	private final List<String> whitelist = new ArrayList<>();

	private HttpHeadersWhitelist() {
		loadWhitelist();
	}

	public static HttpHeadersWhitelist getInstance() {
		return INSTANCE;
	}

	public List<String> getWhitelist() {
		return whitelist;
	}

	private void loadWhitelist() {
		if (whitelist.isEmpty()) {
			Properties properties = new Properties();

			try (InputStream inputStream = HttpHeadersWhitelist.class.getClassLoader().getResourceAsStream(PROPERTY_PATH)) {
				if (inputStream != null) {
					properties.load(inputStream);

					String allowedHeaders = properties.getProperty(PROPERTY_NAME_ALLOWED_HEADERS);
					if (allowedHeaders != null && !allowedHeaders.isEmpty()) {
						String[] splittedValue = allowedHeaders.split(",");
						List<String> splittedValueAsList = Arrays.asList(splittedValue);
						whitelist.addAll(splittedValueAsList);
					}
				} else {
					throw new SpagoBIRuntimeException("headers.properties file not found.");
				}
			} catch (IOException e) {
				throw new SpagoBIRuntimeException("Errors closing input stream for headers.properties file.");
			}
		}
	}
}
