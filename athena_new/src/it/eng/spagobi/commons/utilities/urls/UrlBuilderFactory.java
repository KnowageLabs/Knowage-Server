/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.utilities.urls;

import org.apache.log4j.Logger;

import it.eng.spago.base.ApplicationContainer;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.constants.SpagoBIConstants;

public class UrlBuilderFactory {

	private static transient Logger logger = Logger.getLogger(UrlBuilderFactory.class);
	
	/**
	 * Gets the url builder.
	 * 
	 * @return the url builder
	 */
	public static IUrlBuilder getUrlBuilder() {
		ApplicationContainer spagoContext = ApplicationContainer.getInstance();
		IUrlBuilder urlBuilder = (IUrlBuilder)spagoContext.getAttribute(SpagoBIConstants.URL_BUILDER);
		if(urlBuilder==null) {
			SingletonConfig spagoconfig = SingletonConfig.getInstance();
			// get mode of execution
			String sbiMode = (String)spagoconfig.getConfigValue("SPAGOBI.SPAGOBI-MODE.mode");   
			if (sbiMode==null) {
				logger.error("SPAGOBI.SPAGOBI-MODE.mode IS NULL");
				sbiMode="WEB";
			}
			// based on mode get spago object and url builder
			if (sbiMode.equalsIgnoreCase("WEB")) {
				urlBuilder = new WebUrlBuilder();		
			} else if  (sbiMode.equalsIgnoreCase("PORTLET")){
				urlBuilder = new PortletUrlBuilder();
			}
			spagoContext.setAttribute(SpagoBIConstants.URL_BUILDER, urlBuilder);
		}	
		return urlBuilder;
	}
	
	/**
	 * Gets the url builder.
	 * 
	 * @param channelType the channel type
	 * 
	 * @return the url builder
	 */
	public static IUrlBuilder getUrlBuilder(String channelType) {
		IUrlBuilder urlBuilder = null;
		// based on mode get spago object and url builder
		if (channelType.equalsIgnoreCase("WEB") || channelType.equalsIgnoreCase("HTTP")) {
			urlBuilder = new WebUrlBuilder();		
		} else if  (channelType.equalsIgnoreCase("PORTLET")){
			urlBuilder = new PortletUrlBuilder();
		}
		return urlBuilder;
	}	
		
	
}
