package it.eng.spagobi.security;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.jasypt.encryption.pbe.StandardPBEByteEncryptor;

public class EncryptionPBEWithMD5AndDES {

	private static final Logger logger = Logger.getLogger(EncryptionPBEWithMD5AndDES.class);

	private static final String ENCRYPTION_KEY_SYSTEM_PROPERTY_NAME = "symmetric_encryption_key";
	private static final String ENCRYPTION_ALGORITHM_SYSTEM_PROPERTY_NAME = "encryption_algorithm";
	private static final String ENCRYPTION_ALGORITHM = "PBEWithMD5AndDES";
	private static EncryptionPBEWithMD5AndDES instance = new EncryptionPBEWithMD5AndDES();

	private StandardPBEByteEncryptor encryptor;

	/**
	 * Constructor
	 */
	private EncryptionPBEWithMD5AndDES() {
		logger.debug("EncryptionPBEWithMD5AndDES.constructor - IN".concat(": encryptor creation started"));
		encryptor = new StandardPBEByteEncryptor();
		char[] key = getEncryptionKey();
		String algorithm = getEncryptionAlgorithm();
		encryptor.setAlgorithm(algorithm);
		encryptor.setPassword(new String(key));
		logger.debug("EncryptionPBEWithMD5AndDES.constructor - OUT".concat(": encryptor created"));
	}

	public static EncryptionPBEWithMD5AndDES getInstance() {
		return instance;
	}

	public String encrypt(String password) {
		logger.debug("EncryptionPBEWithMD5AndDES.encrypt - IN".concat(": encrypting password " + password));
		byte[] UTF8passwordBytes = password.getBytes(StandardCharsets.UTF_8);
		byte[] encryptedUTF8Password = encryptor.encrypt(UTF8passwordBytes);
		String base64EncodedEncryptedPassword = Base64.getEncoder().encodeToString(encryptedUTF8Password);
		logger.debug("EncryptionPBEWithMD5AndDES.encrypt - OUT".concat(": password encrypted"));
		return base64EncodedEncryptedPassword;
	}

	public String decrypt(String password) {
		logger.debug("EncryptionPBEWithMD5AndDES.decrypt - IN".concat(": decrypting password"));
		byte[] base64PasswordDecodedByteArray = Base64.getDecoder().decode(password);
		byte[] base64DecryptedPassword = encryptor.decrypt(base64PasswordDecodedByteArray);
		String decryptedUTF8Password = new String(base64DecryptedPassword, StandardCharsets.UTF_8);
		logger.debug("EncryptionPBEWithMD5AndDES.decrypt - OUT".concat(": password decrypted"));
		return decryptedUTF8Password;
	}

	private char[] getEncryptionKey() {
		logger.debug(
				"EncryptionPBEWithMD5AndDES.getEncryptionKey - IN".concat(": reading ").concat(ENCRYPTION_KEY_SYSTEM_PROPERTY_NAME).concat("system property"));
		String key = Optional
				.ofNullable(System.getProperty(ENCRYPTION_KEY_SYSTEM_PROPERTY_NAME, System.getenv(ENCRYPTION_KEY_SYSTEM_PROPERTY_NAME.toUpperCase())))
				.orElseThrow(() -> new RuntimeException("Missing " + ENCRYPTION_KEY_SYSTEM_PROPERTY_NAME));

		logger.debug("EncryptionPBEWithMD5AndDES.getEncryptionKey - OUT".concat(": ").concat(ENCRYPTION_KEY_SYSTEM_PROPERTY_NAME).concat("read"));
		return key.toCharArray();
	}

	private String getEncryptionAlgorithm() {
		logger.debug("EncryptionPBEWithMD5AndDES.getEncryptionAlgorithm - IN".concat(": reading ").concat(ENCRYPTION_ALGORITHM_SYSTEM_PROPERTY_NAME)
				.concat("system property"));

		String algorithm = Optional
				.ofNullable(
						System.getProperty(ENCRYPTION_ALGORITHM_SYSTEM_PROPERTY_NAME, System.getenv(ENCRYPTION_ALGORITHM_SYSTEM_PROPERTY_NAME.toUpperCase())))
				.orElse(ENCRYPTION_ALGORITHM);

		logger.debug("EncryptionPBEWithMD5AndDES.getEncryptionAlgorithm".concat(": set algorithm " + algorithm));
		logger.debug("EncryptionPBEWithMD5AndDES.getEncryptionAlgorithm - OUT".concat(": reading ").concat(ENCRYPTION_ALGORITHM_SYSTEM_PROPERTY_NAME)
				.concat("system property"));

		return algorithm;
	}

}
