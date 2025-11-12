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
package it.eng.spagobi.commons.utilities;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import it.eng.knowage.commons.security.KnowageSystemConfiguration;
import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.RequestContainerAccess;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.base.ResponseContainerAccess;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.SingletonConfig;

public class ChannelUtilities {
	private static transient Logger logger = Logger.getLogger(ChannelUtilities.class);

	/**
	 * Gets the request container.
	 *
	 * @param httpRequest the http request
	 *
	 * @return the request container
	 */
	public static RequestContainer getRequestContainer(HttpServletRequest httpRequest) {
		return RequestContainerAccess.getRequestContainer(httpRequest);
	}

	/**
	 * Gets the response container.
	 *
	 * @param httpRequest the http request
	 *
	 * @return the response container
	 */
	public static ResponseContainer getResponseContainer(HttpServletRequest httpRequest) {
		return ResponseContainerAccess.getResponseContainer(httpRequest);
	}


	/**
	 * Gets the spago bi context name.
	 *
	 * @param httpRequest the http request
	 *
	 * @return the spago bi context name
	 * @throws EMFUserError
	 */
	public static String getSpagoBIContextName(HttpServletRequest httpRequest) {
		String contextName = "Spagobi";
		SingletonConfig spagoconfig = SingletonConfig.getInstance();
		// get mode of execution
		String sbiMode = spagoconfig.getConfigValue("SPAGOBI.SPAGOBI-MODE.mode");
		if (sbiMode == null) {
			logger.error("SPAGOBI.SPAGOBI-MODE.mode IS NULL");
			sbiMode = "WEB";
		}
		SingletonConfig spagoConfig = SingletonConfig.getInstance();
		String path = KnowageSystemConfiguration.getKnowageContext(); // spagoConfig.getConfigValue("SPAGOBI.SPAGOBI_CONTEXT");
		if (path == null || path.length() == 0) {
			path = httpRequest.getContextPath();
		}
		// based on mode get spago object and url builder
		if (sbiMode.equalsIgnoreCase("WEB")) {
			contextName = path;
		}
		return contextName;
	}

	/**
	 * Checks if is web running.
	 *
	 * @return true, if is web running
	 */
	public static boolean isWebRunning() {
		SingletonConfig spagoconfig = SingletonConfig.getInstance();
		// get mode of execution
		String sbiMode = spagoconfig.getConfigValue("SPAGOBI.SPAGOBI-MODE.mode");
		if (sbiMode == null) {
			logger.error("SPAGOBI.SPAGOBI-MODE.mode IS NULL");
			return false;
		}
		if (sbiMode.equalsIgnoreCase("WEB")) {
			return true;
		} else {
			return false;
		}
	}

}
