package it.eng.knowage.commons.security;

import org.apache.log4j.Logger;

/**
 *
 * @author albnale
 * @since 2020/08/26
 *
 *        This class aims to be used for "immutable" configurations. These configurations are set once and never modified. For example KNOWAGE.CONTEXT.
 */

public class KnowageSystemConfiguration {

	private static final String KNOWAGE_DEFAULT_CONTEXT = "/knowage";
	private static final String KNOWAGE_CONTEXT = "KNOWAGE.CONTEXT";

	private static transient Logger logger = Logger.getLogger(KnowageSystemConfiguration.class);

	public static String getKnowageContext() {
		logger.debug("IN");
		String path = "";
		try {
			logger.debug("Trying to recover Knowage context from system properties");

			path = System.getProperty(KNOWAGE_CONTEXT);
			if (path == null) {
				logger.debug(KNOWAGE_CONTEXT + " not set, using the default value ");
				path = KNOWAGE_DEFAULT_CONTEXT;
			}
			logger.debug(KNOWAGE_CONTEXT + ": " + path);
		} catch (Exception e) {
			logger.error("Error while recovering Knowage context address", e);
		}
		logger.debug("OUT:" + path);
		return path;
	}

}
