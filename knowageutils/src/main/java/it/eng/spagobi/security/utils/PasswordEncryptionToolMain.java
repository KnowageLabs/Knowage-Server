package it.eng.spagobi.security.utils;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class PasswordEncryptionToolMain {

	public static void main(String[] args) {
		PasswordEncryptionToolController changeSecretKeyConverterBatch = new PasswordEncryptionToolController();

		try {
			System.out.println(changeSecretKeyConverterBatch.execute(args));
		} catch (InvalidKeyException | NoSuchAlgorithmException | IOException e) {
			throw new RuntimeException("Errors occured while encrypting password", e);
		}
	}

}
