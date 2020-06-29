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
package it.eng.spagobi.security;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import it.eng.knowage.wapp.Version;
import sun.misc.BASE64Encoder;

/**
 * @author Franco vuoto (franco.vuoto@eng.it)
 * @author Alessandro Pegoraro (alessandro.pegoraro@eng.it)
 *
 */
public enum AsymmetricProvider {
	INSTANCE(getKeyBytes()), OLD_INSTANCE(getOldKeyBytes());

	private String PROVIDER = "HmacSHA1";
	private Mac mac = null;

	private AsymmetricProvider(byte[] keyBytes) {
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

		BASE64Encoder encoder = new BASE64Encoder();
		String encoded = encoder.encode(result);

		return encoded;
	}

	private static byte[] getKeyBytes() {
		String documentationLink = String.format(
				"Please, read the documentation https://knowage-suite.readthedocs.io/en/%s.%s/installation-guide/manual-installation.html?highlight=server.xml#environment-variables-definition",
				Version.getMajorVersion(), Version.getMinorVersion());
		Logger logger = Logger.getLogger(AsymmetricProvider.class);
		byte[] fileContent = null;
		try {
			String fileLocation = (String) new InitialContext().lookup("java:/comp/env/password_encryption_secret");

			File file = new File(fileLocation);
			fileContent = Files.readAllBytes(file.toPath());

		} catch (NamingException e) {
			String message = "Unable to find resource for security initialization. [password_encryption_secret] envinronment variable is missing.";
			logger.error(String.format("%s %s", message, documentationLink));
			throw new Error(message, e);
		} catch (IOException e) {
			String message = "Unable to find file for security initialization.";
			logger.error(String.format("%s %s", message, documentationLink));
			throw new Error(message, e);
		}

		return fileContent;
	}

	private static byte[] getOldKeyBytes() {
		byte[] keyBytes = { (byte) 0x06, (byte) 0xAB, (byte) 0x12, (byte) 0xE4, (byte) 0xE4, (byte) 0xE4, (byte) 0xE4, (byte) 0x12, (byte) 0x13, (byte) 0xE4,
				(byte) 0x12, (byte) 0xCC, (byte) 0xEF, (byte) 0xE4, (byte) 0x06, (byte) 0x07, (byte) 0xE4, (byte) 0x07, (byte) 0x12, (byte) 0xCD, (byte) 0xE4,
				(byte) 0x07, (byte) 0xFE, (byte) 0xFF, (byte) 0x07, (byte) 0xE4, (byte) 0x08 };
		return keyBytes;
	}
}