/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.
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

/**
 *
 */
package it.eng.knowage.boot.utils;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import it.eng.knowage.boot.error.KnowageRuntimeException;

/**
 * @author albnale
 *
 */
@Component
public class HMACUtilities {

	private static final Logger LOGGER = Logger.getLogger(HMACUtilities.class);

	@Autowired
	private Context ctx;

	@Value("${jndi.lookup.hmackey}")
	private String hmacPropKey;

	public String getKeyHashedValue(String input) {
		String keyHashedValue = null;

		Mac sha256_HMAC;
		try {
			String key = getHmacKey();
			sha256_HMAC = Mac.getInstance("HmacSHA256");
			sha256_HMAC.init(new SecretKeySpec(key.getBytes(), "HmacSHA256"));
			byte[] result = sha256_HMAC.doFinal(input.getBytes());
			keyHashedValue = DatatypeConverter.printHexBinary(result);
		} catch (NoSuchAlgorithmException e1) {
			String message = "No algorithm found during key hashing with HMAC";
			LOGGER.error(message);
			throw new KnowageRuntimeException(message, e1);
		} catch (InvalidKeyException e) {
			String message = "No valid key for hashing with HMAC";
			LOGGER.error(message);
			throw new KnowageRuntimeException(message, e);
		} catch (NamingException e) {
			String message = "No valid name for lookup HMACKey";
			LOGGER.error(message);
			throw new KnowageRuntimeException(message, e);
		}
		return keyHashedValue;
	}

	private String getHmacKey() throws NamingException {
		return (String) ctx.lookup(hmacPropKey);
	}
}
