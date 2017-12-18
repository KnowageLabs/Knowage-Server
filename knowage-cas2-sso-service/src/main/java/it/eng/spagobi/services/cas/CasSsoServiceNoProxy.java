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
public class CasSsoServiceNoProxy implements SsoServiceInterface {

    static private Logger logger = Logger.getLogger(CasSsoServiceNoProxy.class);
    
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
     * @param filterReceipt String
     * 
     * @return String
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public String readTicket(HttpSession session) throws IOException{
	    logger.debug("Not implemented");
	    return "";
    }

    /**
     * This method verify the ticket.
     * 
     * @param ticket String, ticket to validate
     * @param userId String, user id
     * @param validateUrl String
     * @param validateService String
     * 
     * @return String
     * 
     * @throws SecurityException the security exception
     */
    public void validateTicket(String ticket, String userId)throws SecurityException {
	logger.debug("Not implemented");

    }

}
