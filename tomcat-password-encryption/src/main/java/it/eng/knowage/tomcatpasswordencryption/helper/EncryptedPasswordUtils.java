package it.eng.knowage.tomcatpasswordencryption.helper;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class EncryptedPasswordUtils {
    private static final String ENCRYPTED_PREFIX = "#encr#";

    private EncryptedPasswordUtils() {}

    public static String decrypt(String value) {
        if (value == null || value.isEmpty()) return value;
        if (!value.startsWith(ENCRYPTED_PREFIX)) {
            return value;
        }
        String cipherText = value.substring(ENCRYPTED_PREFIX.length());
        String password = resolveKey();
        if (password == null || password.isEmpty()) {
            throw new IllegalStateException("""
                    Missing decryption key. Provide it via system property knowage.enc.password, " +
                                        "environment variable KNOWAGE_ENC_PASSWORD, or a file at ${catalina.base}/conf/knowageTomcatEncryptedPasswordDatasource " +
                                        "or -Dknowage.enc.password.file=/secure/path
                    """);
        }

        SimpleStringPBEConfig cfg = new SimpleStringPBEConfig();
        cfg.setPassword(password);
        cfg.setPoolSize("1");
        cfg.setStringOutputType("base64");

        StandardPBEStringEncryptor enc = new StandardPBEStringEncryptor();
        enc.setConfig(cfg);
        return enc.decrypt(cipherText);
    }

    public static String resolveKey() {
        // Prefer explicit file path via system property
        String fileProp = System.getProperty("knowage.enc.password.file");
        if (fileProp != null && !fileProp.isEmpty()) {
            String fromFile = readFirstLineTrimmed(Path.of(fileProp));
            if (fromFile != null && !fromFile.isEmpty()) return fromFile;
        }

        // Default file under Tomcat conf: ${catalina.base}/conf/passwordEncryptionSecret
        String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase != null && !catalinaBase.isEmpty()) {
            Path defaultPath = Path.of(catalinaBase, "conf", "knowageTomcatEncryptedPasswordDatasource");
            String fromFile = readFirstLineTrimmed(defaultPath);
            if (fromFile != null && !fromFile.isEmpty()) return fromFile;
        }

        return null;
    }

    private static String readFirstLineTrimmed(Path path) {
        try {
            if (Files.isRegularFile(path)) {
                for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty()) return trimmed;
                }
            }
        } catch (IOException ignored) {
        }
        return null;
    }


}