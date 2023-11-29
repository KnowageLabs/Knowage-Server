package it.eng.spagobi.security.utils;

public class PasswordEncryptionToolMain {

	public static void main(String[] args) {
		PasswordEncryptionToolController changeSecretKeyConverterBatch = new PasswordEncryptionToolController();

		System.out.println(changeSecretKeyConverterBatch.execute(args));
	}

}
