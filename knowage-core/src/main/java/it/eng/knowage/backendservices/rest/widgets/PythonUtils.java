package it.eng.knowage.backendservices.rest.widgets;

import java.util.List;

import org.apache.log4j.Logger;

import it.eng.spagobi.api.v2.ConfigResource;
import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class PythonUtils {

	static protected Logger logger = Logger.getLogger(PythonUtils.class);

	private PythonUtils() {
	}

	static String getPythonAddress(String envLabel) {
		ConfigResource configResource = new ConfigResource();
		List<Config> allPythonConfigs = configResource.getConfigsByCategory("PYTHON_CONFIGURATION");
		for (Config cfg : allPythonConfigs) {
			if (cfg.getLabel().equals(envLabel)) {
				return cfg.getValueCheck() + "/";
			}
		}
		throw new SpagoBIRuntimeException("Cannot retrieve Python address from label [" + envLabel + "]");
	}

}
