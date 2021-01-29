package it.eng.knowage.backendservices.rest.widgets;

import java.util.List;

import org.apache.log4j.Logger;

import it.eng.spagobi.commons.IConfiguration;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class PythonUtils {

	/**
	 *
	 */
	public static final String PYTHON_ENVIRONMENT_CATEGORY = "PYTHON_CONFIGURATION";

	static protected Logger logger = Logger.getLogger(PythonUtils.class);

	private PythonUtils() {
	}

	public static String getPythonAddress(String envLabel) {
		List<IConfiguration> allRConfigs = SingletonConfig.getInstance().getConfigsValueByCategory(PYTHON_ENVIRONMENT_CATEGORY);
		for (IConfiguration cfg : allRConfigs) {
			if (cfg.getLabel().equals(envLabel)) {
				return cfg.getValueCheck() + "/";
			}
		}
		throw new SpagoBIRuntimeException("Cannot retrieve Python address from label [" + envLabel + "]");
	}

}
