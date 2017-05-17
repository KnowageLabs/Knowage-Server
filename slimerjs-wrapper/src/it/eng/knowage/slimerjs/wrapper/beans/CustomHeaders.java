package it.eng.knowage.slimerjs.wrapper.beans;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines a map of custom header defining additional HTTP headers that will be send with each HTTP request, both for pages and resources.
 */
public class CustomHeaders {

	public static final CustomHeaders EMPTY = new CustomHeaders(new HashMap<String, String>(0));

	private final Map<String, String> headers;

	public CustomHeaders(Map<String, String> headers) {
		if (headers == null) {
			throw new NullPointerException();
		}
		this.headers = headers;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

}
