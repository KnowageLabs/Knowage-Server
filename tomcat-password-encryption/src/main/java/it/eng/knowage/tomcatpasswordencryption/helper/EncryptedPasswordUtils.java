package it.eng.knowage.tomcatpasswordencryption.helper;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class EncryptedPasswordUtils {
    private static final String ENCRYPTED_PREFIX = "#encr#";
    public static final String ENCRYPTION_KEY_SYSTEM_PROPERTY_NAME = "symmetric_encryption_key";

    private EncryptedPasswordUtils() {}

    public static String decrypt(String value) {
        if (value == null || value.isEmpty()) return value;
        if (!value.startsWith(ENCRYPTED_PREFIX)) {
            return value;
        }
        String cipherText = value.substring(ENCRYPTED_PREFIX.length());
        String decryptionKey = System.getProperty(ENCRYPTION_KEY_SYSTEM_PROPERTY_NAME);
        if (decryptionKey == null || decryptionKey.isEmpty()) {
            throw new IllegalStateException("Missing decryption key. Provide it via system property symmetric_encryption_key");
        }

        SimpleStringPBEConfig cfg = new SimpleStringPBEConfig();
        cfg.setPassword(decryptionKey);
        cfg.setPoolSize("1");
        cfg.setStringOutputType("base64");

        StandardPBEStringEncryptor enc = new StandardPBEStringEncryptor();
        enc.setConfig(cfg);
        return enc.decrypt(cipherText);
    }
}