package it.eng.spagobi.security.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import it.eng.spagobi.security.EncryptionPBEWithMD5AndDES;

public class DataSourceJDBCPasswordManager {

	private static final Logger logger = Logger.getLogger(DataSourceJDBCPasswordManager.class);

	private static final String ENCRYPTED_PREFIX = "#encr#";

	public static String encrypt(String encryptingPassword) {
		logger.debug("DataSourceJDBCPasswordManager.encrypt - IN");
		logger.debug(DataSourceJDBCPasswordManager.class.getName() + ": encrypting password " + encryptingPassword);
		String encrypted = null;
		if (StringUtils.isNotBlank(encryptingPassword) && !encryptingPassword.startsWith(ENCRYPTED_PREFIX)) {
			encrypted = EncryptionPBEWithMD5AndDES.getInstance().encrypt(encryptingPassword);
			encrypted = ENCRYPTED_PREFIX.concat(encrypted);
		}
		logger.debug(DataSourceJDBCPasswordManager.class.getName() + ": password encrypted");
		logger.debug("DataSourceJDBCPasswordManager.encrypt - OUT");
		return encrypted;
	}

	public static String decrypt(String decryptingPassword) {
		logger.debug("DataSourceJDBCPasswordManager.decrypt - IN");
		logger.debug(DataSourceJDBCPasswordManager.class.getName() + ": decrypting password");
		String returnValue = decryptingPassword;
		if (StringUtils.isNotBlank(decryptingPassword) && decryptingPassword.startsWith(ENCRYPTED_PREFIX)) {

			decryptingPassword = decryptingPassword.replaceFirst(ENCRYPTED_PREFIX, "");
			returnValue = EncryptionPBEWithMD5AndDES.getInstance().decrypt(decryptingPassword);
		}
		logger.debug(DataSourceJDBCPasswordManager.class.getName() + ": password decrypted");
		logger.debug("DataSourceJDBCPasswordManager.decrypt - OUT");
		return returnValue;
	}

}
