/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.utilities.filters;

import it.eng.spagobi.services.common.EnginConf;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

/**
 * This filter is in charge of checking if the session has expired on external engines.
 * If the session has expired and there is no request to open a new session,
 * call is forwarded to configured (in engine-config.xml) session expired URL.
 * This filter is required when using CAS: if the engine session has expired, there is no actual need 
 * to check if the CAS ticket is still valid, since most likely the engine will not work anymore with a 
 * new clean session. Moreover, if the CAS ticket is not valid, the call is redirected to CAS login page 
 * and Ajax requests will not be able to handle the resulting HTML page.
 * Therefore this filter must be put just before the CAS filter.
 * 
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class EngineCheckSessionFilter implements Filter {

	public static final String NEW_SESSION = "NEW_SESSION";
	
	private static transient Logger logger = Logger.getLogger(EngineCheckSessionFilter.class);

    public void init(FilterConfig config) throws ServletException {
    	// do nothing
    }
	
    public void destroy() {
    	// do nothing
    }
    
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
    	throws IOException, ServletException {
    	
    	logger.debug("IN");
    	
    	try {
    		
			if (request instanceof HttpServletRequest) {
				HttpServletRequest httpRequest = (HttpServletRequest) request;
				HttpSession session = httpRequest.getSession(false);
				boolean isValidSession = session != null;
				boolean isRequiredNewSession = false; // for those requests that require a new session anyway, 
													  // do not forward to session expired url
				String newSessionRequestAttr = httpRequest.getParameter(NEW_SESSION);
	        	isRequiredNewSession = newSessionRequestAttr != null && newSessionRequestAttr.equalsIgnoreCase("TRUE");
	        	boolean isRequestedSessionIdValid = httpRequest.isRequestedSessionIdValid();
				if (!isValidSession && !isRequestedSessionIdValid && !isRequiredNewSession) {
					// session has expired
					logger.debug("Session has expired!!");
					String sessionExpiredUrl = EnginConf.getInstance().getSessionExpiredUrl();
					if (sessionExpiredUrl == null) {
						logger.warn("Session expired URL not set!!! check engine-config.xml configuration");
					} else {
						logger.debug("Forwarding to " + sessionExpiredUrl);
						httpRequest.getRequestDispatcher(sessionExpiredUrl).forward(request, response);
						return;
					}
				}
			}
			
			chain.doFilter(request, response);
			
	    } catch(Throwable t) {
	    	logger.error("--------------------------------------------------------------------------------");
		    logger.error("EngineCheckSessionFilter" + ":doFilter ServletException!!",t); 
			logger.error(" msg: [" + t.getMessage() + "]"); 
			Throwable z = t.getCause(); 
			if(z != null) {
				logger.error("-----------------------------");
				logger.error("ROOT CAUSE:");
				logger.error("-----------------------------"); 
				logger.error(" msg: ["+ z.getMessage() + "]"); 
				logger.error(" stacktrace:");
			}
			t.printStackTrace(); 
	    	throw new ServletException(t);
		} finally {
			logger.debug("OUT");
		}
	
    }
    
}
