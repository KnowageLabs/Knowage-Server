/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.services.rest;

import it.eng.spagobi.commons.utilities.ChannelUtilities;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.spi.HttpRequest;

/**
 * 
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 * 
 */
public class InterceptorUtilities {

	/**
	 * Get the url of the services. It removes the path parameters from the url
	 * 
	 * @param req
	 * @return the url of the service
	 */
	public static String getServiceUrl(HttpRequest req) {
		String serviceUrl = req.getPreprocessedPath();
		UriInfo uri = req.getUri();
		// Remove the path parameters
		int pathParametersLength = uri.getPathParameters().size();
		for (int i = 0; i < pathParametersLength; i++) {
			int slahPosition = serviceUrl.lastIndexOf("/");
			serviceUrl = serviceUrl.substring(0, slahPosition);
		}
		return serviceUrl;
	}

	/**
	 * Get the url of the services. It removes the path parameters from the url
	 * 
	 * @param req
	 * @return the url of the service
	 */
	public static HashMap<String, String> getPathParameters(HttpRequest req) {
		UriInfo uri = req.getUri();
		return fromMultivaluedMapToHashMap(uri.getPathParameters());
	}

	/**
	 * Trasforms a MultivaluedMap in a HashMap
	 * 
	 * @param multiMap
	 * @return
	 */
	public static HashMap<String, String> fromMultivaluedMapToHashMap(MultivaluedMap<String, String> multiMap) {
		HashMap<String, String> map = new HashMap<String, String>();
		String key, value;

		if (multiMap != null) {
			Iterator<String> it = multiMap.keySet().iterator();
			while (it.hasNext()) {
				key = it.next();
				value = (multiMap.get(key)).toArray().toString();
				map.put(key, value);
			}
		}

		return map;
	}

	/**
	 * Get the content of a map of object and for every value apply the
	 * toString. If the value is an array it iterate in all the entries
	 * 
	 * @param stringMap
	 * @param genericMap
	 */
	public static void addGenericMap(Map<String, String> stringMap, Map genericMap) {
		Object key, value;
		Object[] valueArray;
		StringBuilder valueString;
		if (genericMap != null) {
			Iterator iter = genericMap.keySet().iterator();
			while (iter.hasNext()) {
				key = iter.next();
				value = genericMap.get(key);
				if (value instanceof Object[]) {
					valueArray = (Object[]) value;
					valueString = new StringBuilder("");
					valueString.append("[");
					for (int i = 0; i < valueArray.length; i++) {
						if (valueArray[i] != null) {
							valueString.append(valueArray[i].toString());
							if (i < valueArray.length - 1) {
								valueString.append(",");
							}
						}
					}
					valueString.append("]");
					stringMap.put(key.toString(), valueString.toString());
				} else {
					stringMap.put(key.toString(), value.toString());
				}
			}
		}
	}

	public static HashMap getRequestParameters(HttpRequest request, HttpServletRequest servletRequest) {
		HashMap<String, String> parameters = new HashMap<String, String>();
		HashMap<String, String> pathParameters = InterceptorUtilities.getPathParameters(request);
		String strContentType = servletRequest.getContentType();
		if (strContentType != null && strContentType.equals("application/x-www-form-urlencoded")) {
			HashMap<String, String> formParameters = InterceptorUtilities.fromMultivaluedMapToHashMap(request.getDecodedFormParameters());
			parameters.putAll(formParameters);
		}

		Map requestParameters = servletRequest.getParameterMap();
		parameters.putAll(pathParameters);

		InterceptorUtilities.addGenericMap(parameters, requestParameters);
		return parameters;
	}

	public static String createUrlPrefix(HttpRequest request, HttpServletRequest servletRequest) {
		String contextName = ChannelUtilities.getSpagoBIContextName(servletRequest);
		String addr = servletRequest.getServerName();
		Integer port = servletRequest.getServerPort();
		String proto = servletRequest.getScheme();
		String url = proto + "://" + addr + ":" + port + "" + contextName;
		return url;

	}
}
