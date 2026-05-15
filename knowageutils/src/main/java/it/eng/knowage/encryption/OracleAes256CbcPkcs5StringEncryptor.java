package it.eng.knowage.encryption;

import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.jasypt.encryption.pbe.PBEStringEncryptor;
import org.jasypt.exceptions.EncryptionInitializationException;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;

public class OracleAes256CbcPkcs5StringEncryptor implements PBEStringEncryptor {

    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String KEY_ALGORITHM = "AES";

    private static final int AES_256_KEY_LENGTH_BYTES = 32;
    private static final int AES_BLOCK_SIZE_BYTES = 16;

    private String password;

    public OracleAes256CbcPkcs5StringEncryptor() {
    }

    public OracleAes256CbcPkcs5StringEncryptor(String password) {
        setPassword(password);
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String encrypt(String message) {
        if (message == null) {
            return null;
        }

        try {
            byte[] plainBytes = message.getBytes(StandardCharsets.UTF_8);
            byte[] encryptedBytes = doCipher(Cipher.ENCRYPT_MODE, plainBytes);

            return bytesToHex(encryptedBytes);
        } catch (Exception e) {
            throw new EncryptionOperationNotPossibleException();
        }
    }

    @Override
    public String decrypt(String encryptedMessage) {
        if (encryptedMessage == null) {
            return null;
        }

        try {
            byte[] encryptedBytes = hexToBytes(encryptedMessage);
            byte[] decryptedBytes = doCipher(Cipher.DECRYPT_MODE, encryptedBytes);

            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new EncryptionOperationNotPossibleException();
        }
    }

    private byte[] doCipher(int mode, byte[] input) throws Exception {
        byte[] keyBytes = getKeyBytes();

        /*
         * Oracle:
         * DBMS_CRYPTO.ENCRYPT_AES256
         * + DBMS_CRYPTO.CHAIN_CBC
         * + DBMS_CRYPTO.PAD_PKCS5
         *
         * Key:
         * UTL_I18N.STRING_TO_RAW(val, 'AL32UTF8')
         *
         * IV:
         * not passed. Oracle's RAW encrypt/decrypt signature has iv DEFAULT NULL;
         * Java CBC still needs an IV object, so we use 16 zero bytes.
         */
        byte[] ivBytes = new byte[AES_BLOCK_SIZE_BYTES];

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);

        cipher.init(
                mode,
                new SecretKeySpec(keyBytes, KEY_ALGORITHM),
                new IvParameterSpec(ivBytes)
        );

        return cipher.doFinal(input);
    }

    private byte[] getKeyBytes() {
        if (password == null) {
            throw new EncryptionInitializationException("Encryption password is null");
        }

        byte[] keyBytes = password.getBytes(StandardCharsets.UTF_8);

        if (keyBytes.length != AES_256_KEY_LENGTH_BYTES) {
            throw new EncryptionInitializationException(
                    "Invalid AES-256 key length: expected " + AES_256_KEY_LENGTH_BYTES +
                            " bytes, found " + keyBytes.length
            );
        }

        return keyBytes;
    }

    private static byte[] hexToBytes(String hex) {
        String cleanHex = hex
                .replace(" ", "")
                .replace(":", "")
                .replace("-", "")
                .trim();

        if (cleanHex.length() % 2 != 0) {
            throw new IllegalArgumentException("Invalid hex string length");
        }

        byte[] bytes = new byte[cleanHex.length() / 2];

        for (int i = 0; i < cleanHex.length(); i += 2) {
            bytes[i / 2] = (byte) Integer.parseInt(cleanHex.substring(i, i + 2), 16);
        }

        return bytes;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);

        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }

        return sb.toString();
    }
}
