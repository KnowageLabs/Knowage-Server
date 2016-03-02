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
