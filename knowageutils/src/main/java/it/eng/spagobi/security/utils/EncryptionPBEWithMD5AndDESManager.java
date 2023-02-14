package it.eng.spagobi.security.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import it.eng.spagobi.security.EncryptionPBEWithMD5AndDES;

public class EncryptionPBEWithMD5AndDESManager {

	private static final Logger logger = Logger.getLogger(EncryptionPBEWithMD5AndDESManager.class);

	private static final String ENCRYPTED_PREFIX = "#encr#";

	public static String encrypt(String encryptingPassword) {
		logger.debug("DataSourceJDBCPasswordManager.encrypt - IN");
		logger.debug(EncryptionPBEWithMD5AndDESManager.class.getName() + ": encrypting password " + encryptingPassword);
		String encrypted = null;
		if (StringUtils.isNotBlank(encryptingPassword) && !encryptingPassword.startsWith(ENCRYPTED_PREFIX)) {
			encrypted = EncryptionPBEWithMD5AndDES.getInstance().encrypt(encryptingPassword);
			encrypted = ENCRYPTED_PREFIX.concat(encrypted);
		}
		logger.debug(EncryptionPBEWithMD5AndDESManager.class.getName() + ": password encrypted");
		logger.debug("EncryptionPBEWithMD5AndDESManager.encrypt - OUT");
		return encrypted;
	}

	public static String decrypt(String decryptingPassword) {
		logger.debug("EncryptionPBEWithMD5AndDESManager.decrypt - IN");
		logger.debug(EncryptionPBEWithMD5AndDESManager.class.getName() + ": decrypting password");
		String returnValue = decryptingPassword;
		if (StringUtils.isNotBlank(decryptingPassword) && decryptingPassword.startsWith(ENCRYPTED_PREFIX)) {

			decryptingPassword = decryptingPassword.replaceFirst(ENCRYPTED_PREFIX, "");
			returnValue = EncryptionPBEWithMD5AndDES.getInstance().decrypt(decryptingPassword);
		}
		logger.debug(EncryptionPBEWithMD5AndDESManager.class.getName() + ": password decrypted");
		logger.debug("EncryptionPBEWithMD5AndDESManager.decrypt - OUT");
		return returnValue;
	}

}
