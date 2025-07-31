package it.eng.spagobi.utilities.csp;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.eng.spagobi.commons.utilities.SpagoBIUtilities;

public class CSPSingleton {

	private static final Logger LOGGER = LogManager.getLogger(CSPSingleton.class);

	private static CSPSingleton instance;
	private String cspPolicy;
	private static final String FILE_CSP = "security-policy.csp";

	private CSPSingleton() {

		String cspFilePath = SpagoBIUtilities.getRootResourcePath() + File.separator + FILE_CSP;
		File cspFile = new File(cspFilePath);
		if (!cspFile.exists()) {
			cspPolicy = null;
			LOGGER.warn("File " + FILE_CSP + " not found");
		} else {
			try {
				cspPolicy = new String(Files.readAllBytes(Paths.get(cspFilePath)), StandardCharsets.UTF_8);
			} catch (IOException e) {
				LOGGER.warn("File " + FILE_CSP + " error reading");
			}
		}

	}

	public static synchronized CSPSingleton getInstance() {
		if (instance == null) {
			instance = new CSPSingleton();
		}
		return instance;
	}

	public String getCspPolicy() {
		return cspPolicy;
	}

}
