package it.eng.knowage.backendservices.rest.widgets;

import java.util.List;

import org.apache.log4j.Logger;

import it.eng.spagobi.commons.IConfiguration;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class PythonUtils {

	static protected Logger logger = Logger.getLogger(PythonUtils.class);

	private PythonUtils() {
	}

	public static String getPythonAddress(String envLabel) {
		List<IConfiguration> allRConfigs = SingletonConfig.getInstance().getConfigsValueByCategory("PYTHON_CONFIGURATION");
		for (IConfiguration cfg : allRConfigs) {
			if (cfg.getLabel().equals(envLabel)) {
				return "https://" + cfg.getValueCheck() + "/";
			}
		}
		throw new SpagoBIRuntimeException("Cannot retrieve Python address from label [" + envLabel + "]");
	}

}
