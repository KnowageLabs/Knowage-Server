package it.eng.knowage.encryption;

import org.jasypt.encryption.pbe.PBEStringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.salt.ZeroSaltGenerator;

/**
 *
 */
public class EncryptorFactory {

	public static final String ORACLE_AES_256_CBC_PKCS5 = "OracleAES256CBCPKCS5";

	private static final EncryptorFactory INSTANCE = new EncryptorFactory();

	public static EncryptorFactory getInstance() {
		return INSTANCE;
	}

	private EncryptorFactory() {
	}

	public PBEStringEncryptor createDefault() {
		EncryptionConfiguration cfg = EncryptionPreferencesRegistry.getInstance()
				.getConfiguration(EncryptionPreferencesRegistry.DEFAULT_CFG_KEY);

		return create(cfg);
	}

	public PBEStringEncryptor create(EncryptionConfiguration cfg) {
		String algorithm = cfg.getAlgorithm();
		String password = cfg.getEncryptionPwd();

		return create(algorithm, password);
	}

	public PBEStringEncryptor create(String algorithm, String password) {
		if (isOracleAes256CbcPkcs5(algorithm)) {
			return new OracleAes256CbcPkcs5StringEncryptor(password);
		}

		StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
		encryptor.setAlgorithm(algorithm);
		encryptor.setPassword(password);
		encryptor.setSaltGenerator(new ZeroSaltGenerator());

		return encryptor;
	}

	private boolean isOracleAes256CbcPkcs5(String algorithm) {
		return ORACLE_AES_256_CBC_PKCS5.equalsIgnoreCase(algorithm);
	}
}