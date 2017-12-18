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
