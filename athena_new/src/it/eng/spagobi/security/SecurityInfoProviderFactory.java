/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

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

