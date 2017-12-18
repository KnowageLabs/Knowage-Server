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

package it.eng.spagobi.security;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.SingletonConfig;

import org.apache.log4j.Logger;

/**
 * @author Zerbetto
 */
public class SecurityInfoProviderFactory {
		
		static private Logger logger = Logger.getLogger(SecurityInfoProviderFactory.class);
		
		/**
		 * Reads the security provider class from the spagobi.xml file
		 * 
		 * @return the instance of ISecurityInfoProvider
		 */
		public static synchronized ISecurityInfoProvider getPortalSecurityProvider() throws Exception {
			logger.debug("IN");
			SingletonConfig configSingleton = SingletonConfig.getInstance();
			String portalSecurityClassName = configSingleton.getConfigValue("SPAGOBI.SECURITY.PORTAL-SECURITY-CLASS.className");
			logger.debug(" Portal security class name: " + portalSecurityClassName);
			if (portalSecurityClassName == null || portalSecurityClassName.trim().equals("")) {
				logger.error(" Portal security class name not set!!!!");
				throw new Exception("Portal security class name not set");
			}
			portalSecurityClassName = portalSecurityClassName.trim();
			ISecurityInfoProvider portalSecurityProvider = null;
			try {
				portalSecurityProvider = (ISecurityInfoProvider)Class.forName(portalSecurityClassName).newInstance();
			} catch (Exception e) {
				logger.error(" Error while istantiating portal security class '" + portalSecurityClassName + "'.", e);
				throw e;
			}
			return portalSecurityProvider;
		}
	}

