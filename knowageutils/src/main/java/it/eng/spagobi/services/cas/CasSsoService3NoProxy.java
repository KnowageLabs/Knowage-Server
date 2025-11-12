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

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.validation.Assertion;

import it.eng.spagobi.services.common.SsoServiceInterface;
import it.eng.spagobi.services.security.exceptions.SecurityException;



/**
 * This class contain the specific code of CAS 
 */
public class CasSsoService3NoProxy implements SsoServiceInterface {

    private static Logger logger = Logger.getLogger(CasSsoService3NoProxy.class);
    
    /**
     * Read user id.
     * 
     * @param session HttpSession
     * 
     * @return String
     */
    @Override
	public String readUserIdentifier(HttpServletRequest request){
    HttpSession session=request.getSession();
    Assertion assertion = (Assertion) session.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION);
    String userInSession = null;
    if(assertion != null) {
    	userInSession = assertion.getPrincipal() != null ? assertion.getPrincipal().getName() : null;    	
    }
	String user = request.getRemoteUser();
	String userId = request.getParameter(SsoServiceInterface.USER_ID);
	logger.debug("CAS user in HttpServletRequest:" + user);
	logger.debug("CAS user in HttpSession:" + userInSession);
	logger.debug("CAS user in user_id:" + userId);
	return user != null ? user : 
		userInSession != null ? userInSession : userId;
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
    @Override
	public String readTicket(HttpSession session) throws IOException{
	    logger.debug("IN");
	    logger.debug("OUT. No ticket ");
	    return "";
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
    @Override
	public void validateTicket(String ticket, String userId)throws SecurityException {
	logger.debug("IN");


    }

}
