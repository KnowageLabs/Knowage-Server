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
package it.eng.spagobi.security.utils;

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
	private static String unableToFindAlgorithmMessage = "Unable to find algorithm for security initialization";
	private static String unableToFindKeyMessage = "Unable to find a valid key for security initialization";

	public PasswordEncrypter(byte[] keyBytes) {
		// All com.sun.crypto.** classes are JDK internal APIs that are not supported and should not be used.
		// Provider sunJce = new com.sun.crypto.provider.SunJCE();

		// Old code can't access a com.sun class by name.
		// Access algorithms from the provider by calls like javax.crypto.Cipher
		Cipher cipher = null;

		// avoid sonar security hotspot issue
		// String passwordEncrypterTransformationAlgorithm = SingletonConfig.getInstance()
		// .getConfigValue("KNOWAGE.DASHBOARD.PASSWORD.ENCRYPTER.TRANSFORMATION_ALGORITHM");
		String passwordEncrypterTransformationAlgorithm = "AES/CTR/NoPadding";

		// String passwordEncrypterProviderAlgorithm = SingletonConfig.getInstance().getConfigValue("KNOWAGE.DASHBOARD.PASSWORD.ENCRYPTER.PROVIDER_ALGORITHM");
		String passwordEncrypterProviderAlgorithm = "HmacSHA1";

		try {
			cipher = Cipher.getInstance(passwordEncrypterTransformationAlgorithm);
			// Cipher.getInstance("AES/CTR/NoPadding", "SunJCE") is not recommended,
			// otherwise, applications are tied to specific providers that may not be available on other Java implementations.

		} catch (NoSuchAlgorithmException e) {
			throw new Error(unableToFindAlgorithmMessage, e);
		} catch (NoSuchPaddingException e) {
			throw new Error(unableToFindAlgorithmMessage, e);
		}

		Provider sunJce = cipher.getProvider();

		Security.addProvider(sunJce);

		SecretKey key = new SecretKeySpec(keyBytes, passwordEncrypterProviderAlgorithm);

		try {
			mac = Mac.getInstance(passwordEncrypterProviderAlgorithm);
			mac.init(key);
		} catch (NoSuchAlgorithmException e) {
			throw new Error(unableToFindAlgorithmMessage, e);
		} catch (InvalidKeyException e) {
			throw new Error(unableToFindKeyMessage, e);
		}
	}

	public String encrypt(String value) {
		byte[] result = mac.doFinal(value.getBytes());

		Base64.Encoder encoder = Base64.getEncoder();
		return encoder.encodeToString(result);
	}
}
