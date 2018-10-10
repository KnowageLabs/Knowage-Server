package it.eng.spagobi.utilities.parameters;

import java.util.Collection;

import org.apache.log4j.Logger;

import it.eng.spagobi.commons.utilities.StringUtilities;

public final class ParametersUtilities {

	static private Logger logger = Logger.getLogger(ParametersUtilities.class);

	public static final String START_PARAMETER = StringUtilities.START_PARAMETER;
	public static final String END_PARAMETER = "}";

	public static boolean isParameter(String parameter) {
		return parameter.startsWith(START_PARAMETER) && parameter.endsWith(END_PARAMETER);
	}

	public static boolean containsParameter(Collection<String> parameters) {
		for (String parameter : parameters) {
			if (isParameter(parameter)) {
				logger.debug("At least the string [" + parameter + "] is a parameter.");
				return true;
			}
		}
		return false;
	}

	public static String getParameterName(String parameter) {
		logger.debug("Getting name from parameter [" + parameter + "]");
		return parameter.substring(3, parameter.length() - 1);
	}
}
