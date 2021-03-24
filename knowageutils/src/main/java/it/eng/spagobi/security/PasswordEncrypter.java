package it.eng.spagobi.security;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class PasswordEncrypter {
	private Mac mac = null;
	private String PROVIDER = "HmacSHA1";

	public PasswordEncrypter(byte[] keyBytes) {
		Provider sunJce = new com.sun.crypto.provider.SunJCE();
		Security.addProvider(sunJce);

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

	public String enCrypt(String value) {
		byte[] result = mac.doFinal(value.getBytes());

		Base64.Encoder encoder = Base64.getEncoder();
		String encoded = encoder.encodeToString(result);

		return encoded;
	}
}
