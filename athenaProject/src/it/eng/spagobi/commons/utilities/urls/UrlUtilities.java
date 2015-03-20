/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.utilities.urls;

import it.eng.spago.navigation.LightNavigationManager;

public class UrlUtilities {

	/**
	 * Adds the navigator disabled parameter.
	 * 
	 * @param url the url
	 * 
	 * @return the string
	 */
	public static String addNavigatorDisabledParameter(String url) {
		String urltoreturn = url;
		if(url.indexOf("?")==-1) {
			urltoreturn = url + "?" + LightNavigationManager.LIGHT_NAVIGATOR_DISABLED + "=TRUE";
		} else {
			if(url.indexOf(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED)==-1) {
				if(url.endsWith("&") || url.endsWith("&amp;") || url.endsWith("?")) {
					urltoreturn = url + LightNavigationManager.LIGHT_NAVIGATOR_DISABLED + "=TRUE";
				} else {
					urltoreturn = url + "&" + LightNavigationManager.LIGHT_NAVIGATOR_DISABLED + "=TRUE";
				}
			}
		} 
		return urltoreturn;
	}
	
}
