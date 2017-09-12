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
