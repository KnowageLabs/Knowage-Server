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
import it.eng.spagobi.security.hmacfilter.HMACSecurityException;
import it.eng.spagobi.security.hmacfilter.HMACUtils;
import it.eng.spagobi.security.hmacfilter.SystemTimeHMACTokenValidator;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.BadCredentialsAuthenticationException;
import org.jasig.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.springframework.beans.factory.annotation.Autowired;

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

	private final static long MAX_TIME_DELTA_DEFAULT_MS = 30000;

	@Autowired
	private String hmacKey;

	@Override
	protected boolean authenticateUsernamePasswordInternal(UsernamePasswordCredentials credentials) throws AuthenticationException {
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
			lstResult = utility.executeQuery("SELECT PASSWORD FROM SBI_USER WHERE UPPER(USER_ID) = ?", pars);
		} catch (Exception e) {
			logger.error("Error while check pwd: " + e);
			throw new RuntimeException("Cannot authenticate user", e);
		}

		if (lstResult == null || lstResult.size() == 0) {
			logger.error("No user with the specified user identifier : [" + username + "]");
			throw new BadCredentialsAuthenticationException();
		}

		if (lstResult.size() > 1) {
			logger.error("There are different users with the same user identifier : " + username + ". " + "Remember that the check is case INSENSITIVE");
			throw new RuntimeException("There are different users with the same user identifier : " + username + ". "
					+ "Remember that the check is case INSENSITIVE");
		}

		// gets the pwd presents in db
		SourceBeanAttribute sbAttribute = (SourceBeanAttribute) lstResult.get(0);
		SourceBean value = (SourceBean) sbAttribute.getValue();
		correctPassword = (String) value.getAttribute("PASSWORD");

		/**
		 * Handling authentication by hmac (used by phantomjs)
		 */
		if (!encrPass.equals(correctPassword)) {
			return testHmac(username, password);
		}
		logger.debug("OUT");
		return true;
	}

	/**
	 * @param username
	 * @param password
	 * @throws AuthenticationException
	 */
	private boolean testHmac(String username, String password) throws AuthenticationException {
		String[] ss = password.split("\\|");
		if (ss.length == 3) {
			String urlString = ss[0];
			String uniqueToken = ss[1];
			String hmacString = ss[2];
			try {
				URL url = new URL(urlString);
				HMACUtils.checkHMAC(url.getPath() + url.getQuery(), "" + uniqueToken, hmacString, new SystemTimeHMACTokenValidator(MAX_TIME_DELTA_DEFAULT_MS),
						getHmacKey());
				return true;
			} catch (MalformedURLException e) {
				logger.error("CAS Authentication - not valid hmac");
				throw new BadCredentialsAuthenticationException();
			} catch (HMACSecurityException e) {
				logger.error("CAS Authentication - not valid hmac");
				throw new BadCredentialsAuthenticationException();
			} catch (IOException e) {
				logger.error("CAS Authentication - not valid hmac");
				throw new BadCredentialsAuthenticationException();
			}
		}
		return false;
	}

	/**
	 * @return the hmacKey
	 */
	public String getHmacKey() {
		return hmacKey;
	}

	/**
	 * @param hmacKey
	 *            the hmacKey to set
	 */
	public void setHmacKey(String hmacKey) {
		this.hmacKey = hmacKey;
	}

}