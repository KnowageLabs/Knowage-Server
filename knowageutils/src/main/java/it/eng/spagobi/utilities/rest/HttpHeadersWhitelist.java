package it.eng.spagobi.utilities.rest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class HttpHeadersWhitelist {

	private static final Logger LOGGER = LogManager.getLogger(HttpHeadersWhitelist.class);

	private static final HttpHeadersWhitelist INSTANCE = new HttpHeadersWhitelist();
	private static final String PROPERTY_PATH = "headers.properties";
	private static final String PROPERTY_NAME_ALLOWED_HEADERS = "allowed.headers";

	private final Set<String> whitelist = new TreeSet<>();

	private HttpHeadersWhitelist() {
	}

	public static HttpHeadersWhitelist getInstance() {
		return INSTANCE;
	}

	public List<String> getWhitelist() {
		loadWhitelist();
		return new ArrayList<>(whitelist);
	}

	private void loadWhitelist() {
		if (whitelist.isEmpty()) {
			loadEntriesFromClasspath();
		}
	}

	private void loadEntriesFromClasspath() {
		ClassLoader classLoader = this.getClass().getClassLoader();

		try {
			Enumeration<URL> resources = classLoader.getResources(PROPERTY_PATH);
			List<URL> resourcesAslist = Collections.list(resources);

			for (URL url : resourcesAslist) {
				manageClasspathEntry(url);
			}
		} catch (IOException e) {
			throw new SpagoBIRuntimeException("Cannot list " + PROPERTY_PATH + " from classpat", e);
		}
	}

	private void manageClasspathEntry(URL url) {
		try (InputStream inputStream = url.openStream()) {
			loadHeadersOntoWhitelist(inputStream);
		} catch (IOException e) {
			LOGGER.warn("Non-fatal error loading {} from classpath. Skipping!", url);
		}
	}

	private void loadHeadersOntoWhitelist(InputStream inputStream) {
		try {
			Properties properties = new Properties();
			properties.load(inputStream);

			String allowedHeaders = properties.getProperty(PROPERTY_NAME_ALLOWED_HEADERS);
			if (allowedHeaders != null && !allowedHeaders.isEmpty()) {
				String[] splittedValue = allowedHeaders.split(",");
				List<String> splittedValueAsList = Arrays.asList(splittedValue);
				whitelist.addAll(splittedValueAsList);
			}
		} catch (IOException e) {
			throw new SpagoBIRuntimeException("Errors closing input stream for headers.properties file.");
		}
	}

}