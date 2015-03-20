/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.utilities.urls;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * This is the interface for classes that implements logic
 * to generate URLS 
 * This is because we want reuse JSP in Standalone Web applications and Portlet Enviroments
 */
public interface IUrlBuilder {

	/**
	 * Gets the url.
	 * 
	 * @param aHttpServletRequest the http servlet request
	 * @param parameters an HashMap of parameters
	 * 
	 * @return a URL given the Map parameters
	 */
	public String getUrl(HttpServletRequest aHttpServletRequest, Map parameters);
	
	/**
	 * Gets the resource link.
	 * 
	 * @param aHttpServletRequest the http servlet request
	 * @param originalUrl a String representic a link to static resource img, css, js and so on
	 * 
	 * @return the resource link
	 */
	public String getResourceLink(HttpServletRequest aHttpServletRequest, String originalUrl);

	
	/**
	 * Gets the resource link.
	 * 
	 * @param aHttpServletRequest the http servlet request
	 * @param originalUrl a String representic a link to static resource img, css, js and so on
	 * 
	 * @return the resource link
	 */
	public String getResourceLinkByTheme(HttpServletRequest aHttpServletRequest, String originalUrl, String theme);


}
