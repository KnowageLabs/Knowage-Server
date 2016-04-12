/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
  
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
  
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
package it.eng.spagobi.authentication.handler;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spagobi.authentication.utility.AuthenticationUtility;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.BadCredentialsAuthenticationException;
import org.jasig.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;



/**
 * @author 
 * 	Giachino (antonella.giachino@eng.it)
 *  Davide Zerbetto (davide.zerbetto@eng.it)
 **/

/**
 * Authenticates where the presented password is valid. 
 */
public class BaseAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {
	protected static Logger logger = Logger.getLogger(BaseAuthenticationHandler.class);
	
    protected boolean authenticateUsernamePasswordInternal(UsernamePasswordCredentials credentials) 
    	throws AuthenticationException {
		logger.debug("IN");

		String username = credentials.getUsername();
		String password = credentials.getPassword();
		logger.debug("user : " + username);
		logger.debug("psw : " + password);

		String correctPassword = null;
		String encrPass = null;
		logger.debug("Start validating password for the user " + username);
		List lstResult = null;
		// define query to get pwd from database
		try {
			encrPass = Password.encriptPassword(password);
			AuthenticationUtility utility = new AuthenticationUtility();
			List pars = new LinkedList();
			// CASE INSENSITVE SEARCH ON USER ID
			pars.add(username.toUpperCase());
			lstResult = utility.executeQuery(
					"SELECT PASSWORD FROM SBI_USER WHERE UPPER(USER_ID) = ?",
					pars);
		} catch (Exception e) {
			logger.error("Error while check pwd: " + e);
			throw new RuntimeException("Cannot authenticate user", e);
		}

		if (lstResult == null || lstResult.size() == 0) {
			logger.error("No user with the specified user identifier : [" + username + "]");
			throw new BadCredentialsAuthenticationException();
		}
		
		if (lstResult.size() > 1) {
			logger.error("There are different users with the same user identifier : " + username + ". " +
					"Remember that the check is case INSENSITIVE");
			throw new RuntimeException("There are different users with the same user identifier : " + username + ". " +
					"Remember that the check is case INSENSITIVE");
		}
		
		// gets the pwd presents in db
		SourceBeanAttribute sbAttribute = (SourceBeanAttribute) lstResult.get(0);
		SourceBean value = (SourceBean) sbAttribute.getValue();
		correctPassword = (String) value.getAttribute("PASSWORD");

		logger.debug("OUT");
		return encrPass.equals(correctPassword);
    }
    
    

}