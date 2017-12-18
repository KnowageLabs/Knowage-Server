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

import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.Cas20ProxyTicketValidator;
import org.jasig.cas.client.validation.TicketValidationException;



/**
 * This class contain the specific code of CAS3
 * CAS Server 3.3.3
 * CAS Client 3.1.6 
 */
public class CasSsoService3 implements SsoServiceInterface {

    static private Logger logger = Logger.getLogger(CasSsoService3.class);
    
    /**
     * Read user id.
     * 
     * @param session HttpSession
     * 
     * @return String
     */
    public String readUserIdentifier(HttpServletRequest request){
    HttpSession session=request.getSession();
    Assertion assertion = (Assertion) session.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION);
    String userInSession=assertion.getPrincipal().getName();
	//String user=(String)request.getRemoteUser();
	//logger.debug("CAS user in HttpServletRequest:"+user);
	logger.debug("CAS user in HttpSession:"+userInSession);
	return userInSession;
    }
    
    /**
     * Read user id.
     * 
     * @param session PortletSession
     * 
     * @return String
     */
    public String readUserIdentifier(PortletSession session){
    Assertion assertion = (Assertion) session.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION);
    String user=assertion.getPrincipal().getName();
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
	    String ticket=null;
	    String spagoBiServerURL = EnginConf.getInstance().getSpagoBiServerUrl();
	    logger.debug("Read spagoBiServerURL=" + spagoBiServerURL);
	    SourceBean engineConfig = EnginConf.getInstance().getConfig();
	    SourceBean sourceBeanConf = (SourceBean) engineConfig.getAttribute("FILTER_RECEIPT");
	    String filterReceipt = (String) sourceBeanConf.getCharacters();
	    logger.debug("Read filterReceipt=" + filterReceipt);
	    filterReceipt = spagoBiServerURL + filterReceipt;
	    
	    Assertion assertion = (Assertion) session.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION);
	    ticket=assertion.getPrincipal().getProxyTicketFor(filterReceipt);

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
	ConfigSingleton config = ConfigSingleton.getInstance();
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
		AttributePrincipal principal = null;
		Cas20ProxyTicketValidator sv = new Cas20ProxyTicketValidator(validateUrl);
		sv.setAcceptAnyProxy(true);

    	Assertion a = sv.validate(ticket, validateService);
		principal = a.getPrincipal();
		logger.debug("Ticket is VALID, username=" + principal.getName());
			
	} catch (TicketValidationException e) {
		logger.error("An exception occured while validating the cas token");
		throw new SecurityException("An exception occured while validating the cas token", e);
	} catch (Throwable e) {
	    logger.error("An exception occured while validating the cas token");
	    throw new SecurityException("An exception occured while validating the cas token", e);
	} finally {
	    logger.debug("OUT");
	}

    }

}
