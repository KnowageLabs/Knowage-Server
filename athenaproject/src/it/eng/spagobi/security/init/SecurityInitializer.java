/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.security.init;

import org.apache.log4j.Logger;

import it.eng.spago.base.Constants;
import it.eng.spago.base.SourceBean;
import it.eng.spago.init.InitializerIFace;
import it.eng.spago.tracing.TracerSingleton;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.security.RoleSynchronizer;

public class SecurityInitializer implements InitializerIFace {
	
	static private Logger logger = Logger.getLogger(SecurityInitializer.class);
	private SourceBean _config = null;
	
	/* (non-Javadoc)
	 * @see it.eng.spago.init.InitializerIFace#getConfig()
	 */
	public SourceBean getConfig() {
		return _config;
	}

	/* (non-Javadoc)
	 * @see it.eng.spago.init.InitializerIFace#init(it.eng.spago.base.SourceBean)
	 */
	public void init(SourceBean config) {
		logger.debug("IN");

		logger.debug("SecurityInitializer::init: roles synchronization ended.");
		_config = config;
		SingletonConfig configSingleton = SingletonConfig.getInstance();
		String portalSecurityInitClassName = (configSingleton.getConfigValue("SPAGOBI.SECURITY.PORTAL-SECURITY-INIT-CLASS.className"));
		logger.debug("SecurityInitializer::init: Portal security initialization class name: '" + portalSecurityInitClassName + "'");
		if (portalSecurityInitClassName == null || portalSecurityInitClassName.trim().equals("")) return;
		portalSecurityInitClassName = portalSecurityInitClassName.trim();
		InitializerIFace portalSecurityInit = null;
		try {
			portalSecurityInit = (InitializerIFace)Class.forName(portalSecurityInitClassName).newInstance();
		} catch (Exception e) {
			logger.error("SecurityInitializer::init: error while instantiating portal security initialization class name: '" + portalSecurityInitClassName + "'", e);
			return;
		}
		logger.debug("SecurityInitializer::init: invoking init method of the portal security initialization class name: '" + portalSecurityInitClassName + "'");
		portalSecurityInit.init(config);
		/*roles syncronizing after tables initialization*/
		logger.debug("SecurityInitializer::init: starting synchronizing roles...");
		RoleSynchronizer synch = new RoleSynchronizer();
		synch.synchronize();
		
		logger.debug("OUT");
	}

}
