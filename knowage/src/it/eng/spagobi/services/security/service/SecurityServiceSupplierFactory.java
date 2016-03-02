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
package it.eng.spagobi.services.security.service;

import it.eng.spagobi.commons.SingletonConfig;

import org.apache.log4j.Logger;

/**
 * Factory class for the security supplier 
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
    public static ISecurityServiceSupplier createISecurityServiceSupplier(){
	logger.debug("IN");
	SingletonConfig configSingleton = SingletonConfig.getInstance();
	String engUserProfileFactorySB = configSingleton.getConfigValue("SPAGOBI.SECURITY.USER-PROFILE-FACTORY-CLASS.className");
	if (engUserProfileFactorySB==null){
	    logger.warn("SPAGOBI.SECURITY.USER-PROFILE-FACTORY-CLASS ... NOT FOUND");
	}
	String engUserProfileFactoryClass = engUserProfileFactorySB;
	engUserProfileFactoryClass = engUserProfileFactoryClass.trim(); 
	try {
	    return  (ISecurityServiceSupplier)Class.forName(engUserProfileFactoryClass).newInstance();
	} catch (InstantiationException e) {
	    logger.warn("InstantiationException",e);
	} catch (IllegalAccessException e) {
	    logger.warn("IllegalAccessException",e);
	} catch (ClassNotFoundException e) {
	    logger.warn("ClassNotFoundException",e);
	}finally{
	    logger.debug("OUT");
	}
	return null;
    }
    
}
