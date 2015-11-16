/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.sdk.callbacks;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.ws.security.WSPasswordCallback;

/**
 * 
 * @author zerbetto
 *
 */
public class ClientCredentialsHolder implements CallbackHandler {

	private String username = null;
	private String password = null;
	
	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
	
	public ClientCredentialsHolder(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	public void handle(Callback[] callbacks) throws IOException,
			UnsupportedCallbackException {
		for (int i = 0; i < callbacks.length; i++) {
			if (callbacks[i] instanceof WSPasswordCallback) {
                WSPasswordCallback pc = (WSPasswordCallback) callbacks[i];
                // We need the password to fill in, so the usage code must
                // match the WSPasswordCallback.USERNAME_TOKEN value
                // i.e. "2"
                if (pc.getUsage() != WSPasswordCallback.USERNAME_TOKEN) {
                    throw new UnsupportedCallbackException(callbacks[i],
                        "Usage code was not USERNAME_TOKEN - value was "
                        + pc.getUsage());
                }
                pc.setPassword(password); 
			} else {
				throw new UnsupportedCallbackException(callbacks[i],
						"Unrecognized Callback");
			}
		}
	}
	
}
