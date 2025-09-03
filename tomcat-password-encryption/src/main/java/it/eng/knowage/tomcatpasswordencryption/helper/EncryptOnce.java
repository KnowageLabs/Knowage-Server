package it.eng.knowage.tomcatpasswordencryption.helper;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;

import static it.eng.knowage.tomcatpasswordencryption.helper.EncryptedPasswordUtils.ENCRYPTION_KEY_SYSTEM_PROPERTY_NAME;

public class EncryptOnce {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: java -Dknowage.enc.password=<KEY> [-Dknowage.enc.algorithm=PBEWithMD5AndDES] "
                    + "[-Dknowage.enc.keyObtentionIterations=1000] EncryptOnce <CLEAR_TEXT_PASSWORD>");
            System.exit(1);
        }
        String clear = args[0];
        String encryptionKey =System.getProperty(ENCRYPTION_KEY_SYSTEM_PROPERTY_NAME);
        if (encryptionKey == null || encryptionKey.isEmpty()) {
            System.err.println("Missing decryption key. Provide it via system property symmetric_encryption_key.");
            System.exit(2);
        }

        SimpleStringPBEConfig cfg = new SimpleStringPBEConfig();
        cfg.setPassword(encryptionKey);
        cfg.setPoolSize("1");
        cfg.setStringOutputType("base64");

        StandardPBEStringEncryptor enc = new StandardPBEStringEncryptor();
        enc.setConfig(cfg);

        String cipher = enc.encrypt(clear);
        System.out.println("#encr#" + cipher);
    }
}