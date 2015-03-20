/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.sdk.callbacks;

import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;
import it.eng.spagobi.services.security.service.ISecurityServiceSupplier;
import it.eng.spagobi.services.security.service.SecurityServiceSupplierFactory;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.axis.MessageContext;
import org.apache.log4j.Logger;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.handler.WSHandlerConstants;

/**
 * 
 * @author zerbetto
 *
 */
public class ServerPWCallback implements CallbackHandler {

	static private Logger logger = Logger.getLogger(ServerPWCallback.class);
	
	public void handle(Callback[] callbacks) throws IOException,
			UnsupportedCallbackException {
		logger.debug("IN");
		for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof WSPasswordCallback) {
                WSPasswordCallback pc = (WSPasswordCallback) callbacks[i];
                String userId = pc.getIdentifier();
                logger.debug("UserId found from request: " + userId);
                if (pc.getUsage() == WSPasswordCallback.DECRYPT) {
                	logger.debug("WSPasswordCallback.DECRYPT=" + WSPasswordCallback.DECRYPT);
                	pc.setPassword("security");
//                } else if (pc.getUsage() == WSPasswordCallback.USERNAME_TOKEN) {
//					logger.debug("WSPasswordCallback.USERNAME_TOKEN = " + pc.getUsage() + " callback usage");
//					// for passwords sent in digest mode we need to provide the password,
//					// because the original one can't be un-digested from the message
//					String password = getPassword(userId);
//					// this will throw an exception if the passwords don't match
//					pc.setPassword(password);
				} else if (pc.getUsage() == WSPasswordCallback.USERNAME_TOKEN_UNKNOWN) {
					logger.debug("WSPasswordCallback.USERNAME_TOKEN_UNKNOWN = " + pc.getUsage() + " callback usage");
					// for passwords sent in clear-text mode we can compare passwords directly
	                // Get the password that was sent
	                String password = pc.getPassword();
	                // Now pass them to your authentication mechanism
	                authenticate(userId, password); // throws WSSecurityException.FAILED_AUTHENTICATION on failure
				} else {
					logger.error("WSPasswordCallback usage [" + pc.getUsage() + "] not treated.");
                    throw new UnsupportedCallbackException(callbacks[i], "WSPasswordCallback usage [" + pc.getUsage() + "] not treated.");
				}
                // Put userId into MessageContext (for services that depend on profiling)
        		MessageContext mc = MessageContext.getCurrentContext();
        		mc.setProperty(WSHandlerConstants.USER, userId);
            } else {
            	logger.error("Unrecognized Callback");
                throw new UnsupportedCallbackException(callbacks[i],
                        "Unrecognized Callback");
            }
		}
	}
	
	private void authenticate(String userId, String password) throws WSSecurityException {
		logger.debug("IN: userId = " + userId);
		try {
			ISecurityServiceSupplier supplier = SecurityServiceSupplierFactory.createISecurityServiceSupplier();
			SpagoBIUserProfile profile = supplier.checkAuthentication(userId, password);
			if (profile == null) {
				logger.error("Authentication failed for user " + userId);
				throw new WSSecurityException(WSSecurityException.FAILED_AUTHENTICATION);
			}
		} catch (WSSecurityException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Error while authenticating userId = " + userId, e);
			throw new RuntimeException("Error while authenticating userId = " + userId, e);
		} finally {
			logger.debug("OUT");
		}
	}
	
//	private String getPassword(String userId) {
//		logger.debug("IN: userId = " + userId);
//		String password = null;
//		try {
//			ISecurityServiceSupplier supplier = SecurityServiceSupplierFactory.createISecurityServiceSupplier();
//			password = supplier.getPassword(userId);
//		} catch (Throwable t) {
//			logger.error("Error while authenticating userId = " + userId, t);
//		} finally {
//			logger.debug("OUT");
//		}
//		return password;
//	}
	
}
