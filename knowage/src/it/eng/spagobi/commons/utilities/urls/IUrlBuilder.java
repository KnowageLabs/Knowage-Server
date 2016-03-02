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
