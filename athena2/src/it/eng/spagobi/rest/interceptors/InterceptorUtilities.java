/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.rest.interceptors;

import it.eng.spagobi.commons.constants.SpagoBIConstants;
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
	 * @param req 
	 * @return the url of the service
	 */
	public static String getServiceUrl(HttpRequest req){
		String serviceUrl = req.getPreprocessedPath();
		UriInfo uri = req.getUri();
		//Remove the path parameters
		int pathParametersLength = uri.getPathParameters().size();
		for(int i=0; i<pathParametersLength;i++){
			int slahPosition = serviceUrl.lastIndexOf("/");
			serviceUrl = serviceUrl.substring(0,slahPosition);
		}
		return serviceUrl;
	}
	
	/**
	 * Get the url of the services. It removes the path parameters from the url
	 * @param req 
	 * @return the url of the service
	 */
	public static HashMap<String,String> getPathParameters(HttpRequest req){
		UriInfo uri = req.getUri();
		return fromMultivaluedMapToHashMap(uri.getPathParameters());
	}

	/**
	 * Trasforms a MultivaluedMap in a HashMap
	 * @param multiMap
	 * @return
	 */
	public static HashMap<String, String> fromMultivaluedMapToHashMap(MultivaluedMap<String, String> multiMap){
		HashMap<String, String> map = new HashMap<String, String>();
		String key, value;
		
		if(multiMap!=null){
			Iterator<String> it = multiMap.keySet().iterator();
			while (it.hasNext()) {
				key =  it.next();
				value = (multiMap.get(key)).toArray().toString();
				map.put(key, value);
			}
		}
	
		return map;
	}
	
	/**
	 * Get the content of a map of object and for every value apply the toString. 
	 * If the value is an array it iterate in all the entries
	 * @param stringMap
	 * @param genericMap
	 */
	public static void addGenericMap( Map<String, String> stringMap,Map genericMap){
		Object key, value;
		Object[] valueArray;
		StringBuilder valueString ;
		if(genericMap!=null){
			Iterator iter = genericMap.keySet().iterator();
			while (iter.hasNext()) {
				key = (Object) iter.next();
				value = genericMap.get(key);
				if(value instanceof Object[]){
					valueArray = (Object[])value;
					valueString = new StringBuilder("");
					valueString.append("[");
					for(int i=0; i<valueArray.length;i++){
						if(valueArray[i]!=null){
							valueString.append(valueArray[i].toString());
							if(i<valueArray.length-1){
								valueString.append(",");
							}
						}
					}
					valueString.append("]");
					stringMap.put(key.toString(),valueString.toString());
				}else{
					stringMap.put(key.toString(),value.toString());
				}
			}
		}
	}
	
	public static HashMap getRequestParameters(HttpRequest request, HttpServletRequest servletRequest){
		HashMap<String,String> parameters = new HashMap<String, String>();
		HashMap<String,String> pathParameters = InterceptorUtilities.getPathParameters(request);
		String strContentType = servletRequest.getContentType(); 
		if(strContentType!=null && strContentType.equals("application/x-www-form-urlencoded")){
			HashMap<String,String> formParameters = InterceptorUtilities.fromMultivaluedMapToHashMap(request.getDecodedFormParameters());
			parameters.putAll(formParameters);
		}
		
		Map requestParameters = servletRequest.getParameterMap();
		parameters.putAll(pathParameters);
		
		InterceptorUtilities.addGenericMap(parameters, requestParameters);
		return parameters;
	}
	
	public static String createUrlPrefix (HttpRequest request, HttpServletRequest servletRequest){
        String contextName = ChannelUtilities.getSpagoBIContextName(servletRequest);
        String addr= servletRequest.getServerName();
        Integer port=servletRequest.getServerPort();
        String proto =servletRequest.getScheme();
        String url= proto+"://"+addr+":"+port+""+contextName;
        return url;
		
	}
}
