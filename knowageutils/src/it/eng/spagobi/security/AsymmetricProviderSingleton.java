/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.security;


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