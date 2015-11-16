/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.services.cas;

import it.eng.spago.base.SourceBean;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.services.common.EnginConf;
import it.eng.spagobi.services.common.SsoServiceInterface;
import it.eng.spagobi.services.security.exceptions.SecurityException;

import java.io.IOException;

import javax.portlet.PortletSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import edu.yale.its.tp.cas.client.CASReceipt;
import edu.yale.its.tp.cas.client.ProxyTicketValidator;
import edu.yale.its.tp.cas.client.filter.CASFilter;
import edu.yale.its.tp.cas.proxy.ProxyTicketReceptor;

/**
 * This class contain the specific code of CAS 
 */
public class CasSsoService implements SsoServiceInterface {

    static private Logger logger = Logger.getLogger(CasSsoService.class);
    
    /**
     * Read user id.
     * 
     * @param session HttpSession
     * 
     * @return String
     */
    public String readUserIdentifier(HttpServletRequest request){
    HttpSession session=request.getSession();
	String user=(String)session.getAttribute(CASFilter.CAS_FILTER_USER);
	logger.debug("CAS user in HttpSession:"+user);
	return user;
    }
    
    /**
     * Read user id.
     * 
     * @param session PortletSession
     * 
     * @return String
     */
    public String readUserIdentifier(PortletSession session){
	String user=(String)session.getAttribute(CASFilter.CAS_FILTER_USER);
	logger.debug("CAS user in PortletSession:"+user);
	return user;
    }
    
    /**
     * Get a new ticket.
     * 
     * @param session HttpSession
     * 
     * @return String
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public String readTicket(HttpSession session) throws IOException{
	    logger.debug("IN");
	    String spagoBiServerURL = EnginConf.getInstance().getSpagoBiServerUrl();
	    logger.debug("Read spagoBiServerURL=" + spagoBiServerURL);
	    SourceBean engineConfig = EnginConf.getInstance().getConfig();
	    SourceBean sourceBeanConf = (SourceBean) engineConfig.getAttribute("FILTER_RECEIPT");
	    String filterReceipt = (String) sourceBeanConf.getCharacters();
	    logger.debug("Read filterReceipt=" + filterReceipt);
	    filterReceipt = spagoBiServerURL + filterReceipt;
	    CASReceipt cr = (CASReceipt) session.getAttribute(CASFilter.CAS_FILTER_RECEIPT);
	    logger.debug("Read cr=" + cr);
	    if (cr==null) logger.warn("CASReceipt in session is NULL");
	    String ticket=ProxyTicketReceptor.getProxyTicket(cr.getPgtIou(), filterReceipt);
	    logger.debug("OUT.ticket="+ticket);
	    return ticket;
    }

    /**
     * This method verify the ticket.
     * 
     * @param ticket String, ticket to validate
     * @param userId String, user id
     * 
     * @return String
     * 
     * @throws SecurityException the security exception
     */
    public void validateTicket(String ticket, String userId)throws SecurityException {
	logger.debug("IN");
	SingletonConfig config = SingletonConfig.getInstance();
	String validateUrl=null;
	String validateService=null;
	if (config!=null){
		// only server side...
    	validateUrl = SingletonConfig.getInstance().getConfigValue("CAS_SSO.VALIDATE-USER.URL");
    	logger.debug("Read validateUrl=" + validateUrl);
    	validateService = SingletonConfig.getInstance().getConfigValue("CAS_SSO.VALIDATE-USER.SERVICE");
    	logger.debug("Read validateService=" + validateService);
	}
	logger.debug("userId:"+userId);
	try {
	    ProxyTicketValidator pv = null;
	    pv = new ProxyTicketValidator();
	    pv.setCasValidateUrl(validateUrl);
	    pv.setServiceTicket(ticket);
	    pv.setService(validateService);
	    pv.setRenew(false);
	    pv.validate();
	    if (pv.isAuthenticationSuccesful()) {
			String tmpUserId = pv.getUser();
			logger.debug("CAS User:" + tmpUserId);
			if (  userId==null || !userId.equals(tmpUserId)) {
			    logger.warn("Proxy and application users are not the same [" + userId + "-"
				    + tmpUserId + "]");
			    throw new SecurityException("Proxy and application users are not the same [" + userId + "-"
					    + tmpUserId + "]");
			}
			
			
	    } else {
		logger.error("Token NOT VALID");
		throw new SecurityException("Token NOT VALID");
	    }
	} catch (Throwable e) {
	    logger.error("An exception occured while validating the cas token");
	    throw new SecurityException("An exception occured while validating the cas token", e);
	} finally {
	    logger.debug("OUT");
	}

    }

}
