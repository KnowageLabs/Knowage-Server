package it.eng.knowage.commons.security;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

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
	private static String SPAGOBI_HOST = null;

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

	public static String getSpagoBiHost() {
		logger.debug("IN");
		if (SPAGOBI_HOST == null) {
			String tmp = null;
			try {
				logger.debug("Trying to recover SpagoBiHost from ConfigSingleton");
				SingletonConfig spagoConfig = SingletonConfig.getInstance();

				String systemHostVar = spagoConfig.getConfigValue("SPAGOBI.SPAGOBI_HOST_SYSTEMVAR_JNDI");
				if (systemHostVar == null || systemHostVar.length() == 0) {
					String sbTmp = spagoConfig.getConfigValue("SPAGOBI.SPAGOBI_HOST_JNDI");
					if (sbTmp != null) {
						tmp = SpagoBIUtilities.readJndiResource(sbTmp);
					}
					if (tmp == null) {
						logger.debug("SPAGOBI_HOST not set, using the default value ");
						tmp = "http://localhost:8080";
					}
				} else {
					logger.debug("load the host url from the db");
					if (systemHostVar != null) {
						tmp = System.getProperty(systemHostVar);
					}
					if (tmp == null) {
						logger.debug("Using directly value from the db");
						tmp = systemHostVar;
					}
				}

			} catch (Exception e) {
				logger.error("Error while recovering SpagoBI host url", e);
				throw new SpagoBIRuntimeException("Error while recovering SpagoBI host url", e);
			}
			try {
				new URL(tmp);
			} catch (MalformedURLException e) {
				SpagoBIRuntimeException sre = new SpagoBIRuntimeException("SpagoBI host URL is malformed!!", e);
				sre.addHint("Check configuration for host_url environment variable");
				throw sre;
			}
			SPAGOBI_HOST = tmp;
		}
		logger.debug("OUT:" + SPAGOBI_HOST);
		return SPAGOBI_HOST;
	}

}
