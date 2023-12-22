package it.eng.knowage.privacymanager;

import java.io.FileReader;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PMConfiguration {
	private static final Logger LOGGER = LogManager.getLogger(PMConfiguration.class);

	private static PMConfiguration singleton = null;

	private Properties prop;

	private static final String KNOWAGE_PM_CONF_PATH = "kn.privacy.manager.configuration.path";

	private PMConfiguration() {
	}

	public static synchronized PMConfiguration getInstance() {
		if (singleton == null) {
			singleton = new PMConfiguration();

			try {
				singleton.initialize();
			} catch (Exception e) {
				LOGGER.error("Error while initializing Privacy Manager Integration Configurations", e);
			}
		}
		return singleton;
	}

	private void initialize() throws Exception {

		this.prop = new Properties();
		String filePath = System.getProperty(KNOWAGE_PM_CONF_PATH);
		if (filePath == null) {
			filePath = System.getenv(KNOWAGE_PM_CONF_PATH);
		}

		if (filePath == null) {
			throw new Exception("Configuration file undefined in the environment");
		}

		FileReader reader = new FileReader(filePath);
		this.prop.load(reader);
	}

	public String getProperty(String name) {
		return this.prop.getProperty(name);
	}
}
