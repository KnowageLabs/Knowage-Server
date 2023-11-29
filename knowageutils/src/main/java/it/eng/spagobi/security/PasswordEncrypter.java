/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2023 Engineering Ingegneria Informatica S.p.A.
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
package it.eng.spagobi.security;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class PasswordEncrypter {
	private Mac mac = null;
	private static final String PROVIDER = "HmacSHA1";

	public PasswordEncrypter(byte[] keyBytes) {
		SecretKey key = new SecretKeySpec(keyBytes, PROVIDER);

		try {
			mac = Mac.getInstance(PROVIDER);
			mac.init(key);
		} catch (NoSuchAlgorithmException e) {
			throw new Error("Unable to find algorithm for security initialization", e);
		} catch (InvalidKeyException e) {
			throw new Error("Unable to find a valid key for security initialization", e);
		}
	}

	public String encrypt(String value) {
		byte[] result = mac.doFinal(value.getBytes());

		Base64.Encoder encoder = Base64.getEncoder();
		return encoder.encodeToString(result);
	}
}
