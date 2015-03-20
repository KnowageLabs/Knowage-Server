/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engine.cockpit.api;

import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.services.security.service.ISecurityServiceSupplier;

import org.apache.log4j.Logger;

/**
 * Factory class for the security supplier
 * 
 * @author Bernabei Angelo
 * 
 */
public class SecurityServiceSupplierFactory {

	static Logger logger = Logger.getLogger(SecurityServiceSupplierFactory.class);

	/**
	 * Creates a new SecurityServiceSupplier object.
	 * 
	 * @return the i security service supplier
	 */
	public static ISecurityServiceSupplier createISecurityServiceSupplier() {
		logger.debug("IN");
		SingletonConfig configSingleton = SingletonConfig.getInstance();
		String engUserProfileFactorySB = configSingleton.getConfigValue("SPAGOBI.SECURITY.USER-PROFILE-FACTORY-CLASS.className");
		if (engUserProfileFactorySB == null) {
			logger.warn("SPAGOBI.SECURITY.USER-PROFILE-FACTORY-CLASS ... NOT FOUND");
		}
		String engUserProfileFactoryClass = engUserProfileFactorySB;
		engUserProfileFactoryClass = engUserProfileFactoryClass.trim();
		try {
			return (ISecurityServiceSupplier) Class.forName(engUserProfileFactoryClass).newInstance();
		} catch (InstantiationException e) {
			logger.warn("InstantiationException", e);
		} catch (IllegalAccessException e) {
			logger.warn("IllegalAccessException", e);
		} catch (ClassNotFoundException e) {
			logger.warn("ClassNotFoundException", e);
		} finally {
			logger.debug("OUT");
		}
		return null;
	}
}
