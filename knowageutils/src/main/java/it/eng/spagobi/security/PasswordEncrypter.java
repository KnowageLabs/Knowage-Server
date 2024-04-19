package it.eng.spagobi.security;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class PasswordEncrypter {
	private Mac mac = null;
	private String PROVIDER = "HmacSHA1";

	public PasswordEncrypter(byte[] keyBytes) {
		// All com.sun.crypto.** classes are JDK internal APIs that are not supported and should not be used. 
		// Provider sunJce = new com.sun.crypto.provider.SunJCE();
		
		// Old code can't access a com.sun class by name. 
		// Access algorithms from the provider by calls like javax.crypto.Cipher
		Cipher cipher = null;
		
		try {
			cipher = Cipher.getInstance("AES/CTR/NoPadding");			
			// Cipher.getInstance("AES/CTR/NoPadding", "SunJCE") is not recommended,
			// otherwise, applications are tied to specific providers that may not be available on other Java implementations.
			
		} catch (NoSuchAlgorithmException e) {
			throw new Error("Unable to find algorithm for security initialization", e);
		} catch (NoSuchPaddingException e) {
			throw new Error("Unable to find algorithm for security initialization", e);
		}
		
		Provider sunJce = cipher.getProvider();
		
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
