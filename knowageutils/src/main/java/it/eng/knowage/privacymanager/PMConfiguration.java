package it.eng.knowage.privacymanager;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PMConfiguration {

	private static final Logger LOGGER = LogManager.getLogger(PMConfiguration.class);
	private static final String KNOWAGE_PM_CONF_PATH = "kn.privacy.manager.configuration.path";

	private static PMConfiguration INSTANCE = new PMConfiguration();

	public static synchronized PMConfiguration getInstance() {
		if (!INSTANCE.isConfigured) {
			INSTANCE.initialize();
		}
		return INSTANCE;
	}

	private Properties prop;
	private boolean isConfigured = false;

	private PMConfiguration() {
	}

	public String getProperty(String name) {
		return this.prop.getProperty(name);
	}

	private void initialize() {

		this.prop = new Properties();

		String filePath = System.getProperty(KNOWAGE_PM_CONF_PATH);
		if (filePath == null) {
			filePath = System.getenv(KNOWAGE_PM_CONF_PATH);
		}

		if (StringUtils.isNotEmpty(filePath) && Files.exists(Paths.get(filePath))) {
			try (FileReader reader = new FileReader(filePath)) {
				this.prop.load(reader);
			} catch (IOException e) {
				LOGGER.warn("Cannot read PM client configuration at {}", filePath);
			}

			isConfigured = true;
		} else {
			LOGGER.debug("PM client configuration at {} does not exist", filePath);
		}
	}

	public boolean isConfigured() {
		return isConfigured;
	}
}
