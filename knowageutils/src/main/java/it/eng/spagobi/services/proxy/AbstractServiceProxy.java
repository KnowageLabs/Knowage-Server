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

import it.eng.spago.base.SourceBean;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.services.common.EnginConf;
import it.eng.spagobi.services.common.SsoServiceFactory;
import it.eng.spagobi.services.common.SsoServiceInterface;
import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;
import it.eng.spagobi.services.security.exceptions.SecurityException;
import it.eng.spagobi.services.security.stub.SecurityService;
import it.eng.spagobi.services.security.stub.SecurityServiceServiceLocator;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

/**
 * Abstract Class of all Proxy 
 */
public abstract class AbstractServiceProxy {

    protected HttpSession session;

    static private final String SERVICE_NAME = "AbstractServiceProxy";
    
    protected URL serviceUrl = null;
    protected String userId = null;
    protected boolean isSecure = true; // if false don't sent a valid ticket
    
    protected String pass = null;    
    protected String secureAttributes = null;
    protected String serviceUrlStr = null;
    protected String spagoBiServerURL = null;
    protected String token = null;
    
    static final String IS_BACKEND = "isBackend"; // request came from spagobi server

    static private Logger logger = Logger.getLogger(AbstractServiceProxy.class);
    

  
    public AbstractServiceProxy(String user, HttpSession session) {
		this.session = session;
		this.userId = user;
		
		init();
    }
    
    public AbstractServiceProxy(String user,String secureAttributes,String serviceUrlStr,String spagoBiServerURL,String token, String pass) {
		this.userId = user;
		this.pass = pass;
		this.secureAttributes = secureAttributes;
		this.serviceUrlStr = serviceUrlStr;
		this.spagoBiServerURL = spagoBiServerURL;
		this.token = token;
		init();
    }

    protected AbstractServiceProxy() {
    	init();
    }
    
    private SecurityService lookUp() throws SecurityException {
    	SecurityService service;
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
     * Initialize the configuration
     */
    protected void init() {
    	if(secureAttributes==null){
    		secureAttributes = (String)session.getAttribute( IS_BACKEND );
    	}
		if (secureAttributes!=null && secureAttributes.equals("true")){
		    isSecure = false;
		}
		
		if(serviceUrlStr == null && spagoBiServerURL == null && pass == null){
			String className = this.getClass().getSimpleName();
			
			logger.debug("Initializing proxy [" + className + "]");
			
			SourceBean engineConfig = EnginConf.getInstance().getConfig();
		
			if (engineConfig != null) {
	
			    spagoBiServerURL = EnginConf.getInstance().getSpagoBiServerUrl();
			    
			    logger.debug("SpagoBI Service url is equal to [" + spagoBiServerURL + "]");  
			    
			    SourceBean sourceBeanConf = (SourceBean) engineConfig.getAttribute(className + "_URL");
			    if(sourceBeanConf == null) {
			    	throw new RuntimeException("Impossible to read the URL of service [" + className + "] from engine-config.xml");
			    }
			    serviceUrlStr = (String) sourceBeanConf.getCharacters();
			    logger.debug("Read serviceUrl=" + serviceUrlStr);
			    try {
			    	serviceUrl = new URL(spagoBiServerURL + serviceUrlStr);
			    } catch (MalformedURLException e) {
			    	logger.error("MalformedURLException:" + spagoBiServerURL + serviceUrlStr, e);
			    }
			    pass=EnginConf.getInstance().getPass();
			    if (pass==null) logger.warn("PassTicked don't set");
			} else {
				logger.warn("this proxy is used in core project.");
			}
			
			logger.debug("Proxy [" + className + "] succesfully initialized");
		} else {
			try {
					serviceUrl = new URL(spagoBiServerURL + serviceUrlStr);
			    } catch (MalformedURLException e) {
			    	logger.error("MalformedURLException:" + spagoBiServerURL + serviceUrlStr, e);
			    }
		}
    }
    
    /**
     * 
     * @return String Ticket for SSO control
     * @throws IOException 
     */
    protected String readTicket() throws IOException {
    	if(token!=null){
    		return token;
    	}else{
			if (!isSecure){
			    return pass;
			}
			if ( ! UserProfile.isSchedulerUser(userId) ) {
			    SsoServiceInterface proxyService = SsoServiceFactory.createProxyService();
			    return proxyService.readTicket(session);
			}else{
			    return "";
			}
    	}
    }
}
