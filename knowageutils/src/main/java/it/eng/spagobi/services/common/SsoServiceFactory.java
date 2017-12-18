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
package it.eng.spagobi.services.common;

import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;

import org.apache.log4j.Logger;

/**
 * Factory Class
 */
public abstract class SsoServiceFactory {

	static private Logger logger = Logger.getLogger(SsoServiceFactory.class);

	private SsoServiceFactory() {

	}

	/**
	 * Creates the proxy service.
	 *
	 * @return IProxyService
	 */
	public static final SsoServiceInterface createProxyService() {

		logger.debug("IN");
		SsoServiceInterface daoObject = null;
		try {
			String integrationClass = EnginConf.getInstance().getSpagoBiSsoClass();

			if (integrationClass == null) {
				// now we are in the core
				integrationClass = SpagoBIUtilities.readJndiResource(SingletonConfig.getInstance().getConfigValue("SPAGOBI_SSO.INTEGRATION_CLASS_JNDI"));
			}
			daoObject = (SsoServiceInterface) Class.forName(integrationClass).newInstance();
			logger.debug(" Instatiate successfully:" + integrationClass);
		} catch (Exception e) {
			logger.error("Error occurred", e);
		}
		return daoObject;
	}

}
