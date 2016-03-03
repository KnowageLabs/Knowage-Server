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
package it.eng.spagobi.services.proxy;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.container.SpagoBIHttpSessionContainer;
import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;
import it.eng.spagobi.services.security.exceptions.SecurityException;
import it.eng.spagobi.services.security.stub.SecurityServiceServiceLocator;

import java.security.Principal;

import javax.servlet.http.HttpSession;
import javax.xml.rpc.ServiceException;

import org.apache.log4j.Logger;

/**
 * 
 * Security Service Proxy. 
 * Use in engine component only
 *
 */
public final class SecurityServiceProxy extends AbstractServiceProxy{

	static private final String SERVICE_NAME = "Security Service";
	
    static private Logger logger = Logger.getLogger(SecurityServiceProxy.class);
    
/**
 * Use this constructor.
 * 
 * @param user user ID
 * @param session HttpSession
 */
    public SecurityServiceProxy(String user, HttpSession session){
    	super(user,session);
    }    
 
    /**
     * Don't use it.
     */
    private SecurityServiceProxy() {
    	super();
    }
    
    /**
     * @return Object used
     * @throws SecurityException catch this if exist error
     */
    private it.eng.spagobi.services.security.stub.SecurityService lookUp() throws SecurityException {
    	it.eng.spagobi.services.security.stub.SecurityService service;
    	SecurityServiceServiceLocator locator;
    	
    	service = null;
    	try {
		    locator = new SecurityServiceServiceLocator();
		   
		    if (serviceUrl != null) {
		    	service = locator.getSecurityService( serviceUrl );
		    } else {
		    	service = locator.getSecurityService();
		    }
		} catch (Throwable e) {
			logger.error("Impossible to locate [" + SERVICE_NAME + "] at [" + serviceUrl + "]");
		    throw new SecurityException("Impossible to locate [" + SERVICE_NAME + "] at [" + serviceUrl + "]", e);
		}
		
		return service;
    }
    
    /**
     * Gets the user profile.
     * 
     * @return IEngUserProfile with user profile
     * 
     * @throws SecurityException if the process has generated an error
     */
    public IEngUserProfile getUserProfile() throws SecurityException{
    	UserProfile userProfile;
    	
    	logger.debug("IN");
    	
    	userProfile = null;
		try {
            SpagoBIUserProfile user = lookUp().getUserProfile(readTicket(), userId);
            if (user!=null) userProfile = new UserProfile(user);
            else logger.error("Error occured while retrieving user profile of user [" + userId + "] from service [" + SERVICE_NAME + "] at endpoint [" + serviceUrl + "]. user is null!");
        } catch (Throwable e) {
            logger.error("Error occured while retrieving user profile of user [" + userId + "] from service [" + SERVICE_NAME + "] at endpoint [" + serviceUrl + "]");
            throw new SecurityException("Error occured while retrieving user profile of user [" + userId + "] from service [" + SERVICE_NAME + "] at endpoint [" + serviceUrl + "]", e);
        }finally{
            logger.debug("OUT");
        }
        
        return userProfile;
    }

    /**
     * Check if the user is authorized to access the folder.
     * 
     * @param folderId folder id
     * @param mode mode
     * 
     * @return true/false
     */    
    public boolean isAuthorized(String folderId,String mode) {
    	
    	logger.debug("IN");
        try {
            return lookUp().isAuthorized(readTicket(), userId, folderId, mode);
        } catch (Throwable e) {
        	logger.error("Error occured while retrieving access right to folder [" + folderId + "] for user [" + folderId+ "] in modality [" + mode + "]");
        } finally{
            logger.debug("OUT");
        }
        return false;
    } 
    

    /**
     * Check if the user can execute the function ( user function ).
     * 
     * @param function function id
     * 
     * @return true/false
     */
    public boolean checkAuthorization(String function) {
	   	return false;
    }
    
    /**
     * Check if the user can execute the function ( user function ).
     * 
     * @param function function
     * @param principal user principal
     * 
     * @return true / false
     */    
    public boolean checkAuthorization(Principal principal,String function){
    	return false;
    }    
}
