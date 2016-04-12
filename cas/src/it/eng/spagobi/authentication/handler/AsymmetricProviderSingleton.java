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


import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


import sun.misc.BASE64Encoder;

/**
 * @author Franco vuoto (franco.vuoto@eng.it)
 * @author Alessandro Pegoraro (alessandro.pegoraro@eng.it)
 * 
 */
public class AsymmetricProviderSingleton {
	private static final String PROVIDER = "HmacSHA1";

	
	private static AsymmetricProviderSingleton _instance = null;
	private Mac mac = null;

	public static AsymmetricProviderSingleton getInstance() throws InvalidKeyException, NoSuchAlgorithmException  {
		if (_instance == null) {
			synchronized (AsymmetricProviderSingleton.class) {
				if (_instance == null)
					_instance = new AsymmetricProviderSingleton();
			}
		}
		return _instance;
	}

	private AsymmetricProviderSingleton() throws InvalidKeyException,NoSuchAlgorithmException {
		Provider sunJce = new com.sun.crypto.provider.SunJCE();
		Security.addProvider(sunJce);

		SecretKey key = new SecretKeySpec(keyBytes, PROVIDER);
		
			mac = Mac.getInstance(PROVIDER);
			mac.init(key);
		
	}

	public String enCrypt(String value) {
		byte[] result = mac.doFinal(value.getBytes());

		BASE64Encoder encoder = new BASE64Encoder();
		String encoded = encoder.encode(result);

		return encoded;
	}

	private static byte[] keyBytes =
		{
			(byte) 0x06,
			(byte) 0xAB,
			(byte) 0x12,
			(byte) 0xE4,
			(byte) 0xE4,
			(byte) 0xE4,
			(byte) 0xE4,
			(byte) 0x12,
			(byte) 0x13,
			(byte) 0xE4,
			(byte) 0x12,
			(byte) 0xCC,
			(byte) 0xEF,
			(byte) 0xE4,
			(byte) 0x06,
			(byte) 0x07,
			(byte) 0xE4,
			(byte) 0x07,
			(byte) 0x12,
			(byte) 0xCD,
			(byte) 0xE4,
			(byte) 0x07,
			(byte) 0xFE,
			(byte) 0xFF,
			(byte) 0x07,
			(byte) 0xE4,
			(byte) 0x08 };

	
}