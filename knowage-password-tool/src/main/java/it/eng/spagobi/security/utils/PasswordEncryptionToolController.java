package it.eng.spagobi.security.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class PasswordEncryptionToolController {

	private String completeFilePathAndName = null;
	private String plainTextPassword = null;

	public String execute(String[] args) {

		completeFilePathAndName = args[0];
		plainTextPassword = args[1];

		boolean exists = new File(completeFilePathAndName).exists();

		if (exists) {
			return createFile();
		}

		return null;

	}

	private String createFile() {

		byte[] fileContent = null;
		try {

			File file = new File(completeFilePathAndName);
			fileContent = Files.readAllBytes(file.toPath());

		} catch (IOException e) {
			throw new RuntimeException("", e);
		}

		PasswordEncrypter passwordEncrypter = new PasswordEncrypter(fileContent);

		return "v2#SHA#" + passwordEncrypter.encrypt(plainTextPassword);

	}

}