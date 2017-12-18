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

import it.eng.spago.configuration.ConfigSingleton;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;


/**
 * The implementation of IUrlBuilder used when SpagoBI is used as a PORTLET
 */
public class PortletUrlBuilder implements IUrlBuilder{

	private static transient Logger logger = Logger.getLogger(PortletUrlBuilder.class);
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.commons.utilities.urls.IUrlBuilder#getUrl(javax.servlet.http.HttpServletRequest, java.util.Map)
	 */
	public String getUrl(HttpServletRequest aHttpServletRequest, Map parameters) {
		logger.debug("IN");
		String url = getActionUrl(aHttpServletRequest, parameters);
		logger.debug("OUT");
		return url;
	}



	public String getActionUrl(HttpServletRequest aHttpServletRequest, Map parameters) {
		logger.debug("IN");
		RenderResponse renderResponse =(RenderResponse)aHttpServletRequest.getAttribute("javax.portlet.response");
		PortletURL aPortletURL = renderResponse.createActionURL(); 
		if (parameters != null){
			Iterator keysIt = parameters.keySet().iterator();
			String paramName = null;
			Object paramValue = null;
			while (keysIt.hasNext()){
				paramName = (String)keysIt.next();
				paramValue = parameters.get(paramName);
				if (paramValue == null) {
					logger.warn("Parameter with name " + paramName + " has null value. This parameter will be not considered.");
					continue;
				}
				aPortletURL.setParameter(paramName, paramValue.toString());
			}
		}
		String url = aPortletURL.toString();
		logger.debug("OUT");
		return url;
	}

	public String getRenderUrl(HttpServletRequest aHttpServletRequest, Map parameters) {
		logger.debug("IN");
		RenderResponse renderResponse =(RenderResponse)aHttpServletRequest.getAttribute("javax.portlet.response");
		PortletURL aPortletURL = renderResponse.createRenderURL(); 
		if (parameters != null){
			Iterator keysIt = parameters.keySet().iterator();
			String paramName = null;
			Object paramValue = null;
			while (keysIt.hasNext()){
				paramName = (String)keysIt.next();
				paramValue = parameters.get(paramName); 
				if (paramValue == null) {
					logger.warn("Parameter with name " + paramName + " has null value. This parameter will be not considered.");
					continue;
				}
				aPortletURL.setParameter(paramName, paramValue.toString());
			}
		}
		String url = aPortletURL.toString();
		logger.debug("OUT");
		return url;
	}







	/* (non-Javadoc)
	 * @see it.eng.spagobi.commons.utilities.urls.IUrlBuilder#getResourceLink(javax.servlet.http.HttpServletRequest, java.lang.String)
	 */
	public String getResourceLink(HttpServletRequest aHttpServletRequest, String originalUrl){
		logger.debug("IN");
		RenderRequest renderRequest =(RenderRequest)aHttpServletRequest.getAttribute("javax.portlet.request");
		RenderResponse renderResponse =(RenderResponse)aHttpServletRequest.getAttribute("javax.portlet.response");
		String urlToConvert = null; 
		originalUrl = originalUrl.trim();
		if(originalUrl.startsWith("/")) {
			urlToConvert = originalUrl.substring(1);
		} else {
			urlToConvert = originalUrl;
		}
		String newUrl = renderResponse.encodeURL(renderRequest.getContextPath() + "/" + urlToConvert).toString();
		logger.debug("OUT");
		return newUrl;
	}
	
	
	
	public String getResourceLinkByTheme(HttpServletRequest aHttpServletRequest, String originalUrl, String theme){
		logger.debug("IN");
		ConfigSingleton config = ConfigSingleton.getInstance();
		String rootPath=config.getRootPath();
		String urlByTheme=originalUrl;
		originalUrl.trim();
		if(originalUrl.startsWith("/"))
			originalUrl=originalUrl.substring(1);

		if(theme!=null)
		{
			urlByTheme="/themes/"+theme+"/"+originalUrl;
		}

		String urlComplete=rootPath+urlByTheme;
		// check if object exists
		File check=new File(urlComplete);
		// if file
		if(!check.exists())
		{
			urlByTheme="/themes/sbi_default/"+originalUrl;

		// check if the default object exist
		urlComplete=rootPath+urlByTheme;
		File checkDef=new File(urlComplete);
		// if file
		if(!checkDef.exists())
		{
			urlByTheme=originalUrl;
		}
		}

		logger.debug("OUT");
		return getResourceLink(aHttpServletRequest, urlByTheme);
	}

}
