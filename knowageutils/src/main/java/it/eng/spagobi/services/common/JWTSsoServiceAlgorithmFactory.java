package it.eng.spagobi.services.common;

import static java.util.Objects.isNull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.auth0.jwt.algorithms.Algorithm;

import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.security.hmacfilter.HMACUtils;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class JWTSsoServiceAlgorithmFactory {

	private static final Logger logger = LogManager.getLogger(JWTSsoServiceAlgorithmFactory.class);
	private static final JWTSsoServiceAlgorithmFactory INSTANCE = new JWTSsoServiceAlgorithmFactory();
	private static final String KNOWAGE_HMAC_KEY = "knowage.hmacKey";

	private static Algorithm algorithm = null;

	public static final JWTSsoServiceAlgorithmFactory getInstance() {
		return INSTANCE;
	}

	private JWTSsoServiceAlgorithmFactory() {

	}

	public synchronized Algorithm getAlgorithm() {
		if (isNull(algorithm)) {
			try {
				String key = getHMACKey();
				algorithm = Algorithm.HMAC256(key);
			} catch (Exception e) {
				algorithm = null;
				logger.error("Cannot initialize JWT algorithm", e);
				throw new SpagoBIRuntimeException("Cannot initialize JWT algorithm", e);
			}
		}
		return algorithm;
	}

	/**
	 * Gets the HMAC key from configuration
	 *
	 * @return the HMAC key
	 */
	protected static String getHMACKey() {
		try {
			String key = EnginConf.getInstance().getHmacKey();
			if (key == null || key.isEmpty()) {
				key = SpagoBIUtilities.readJndiResource(SingletonConfig.getInstance().getConfigValue(HMACUtils.HMAC_JNDI_LOOKUP));
			}
			if (key == null || key.isEmpty()) {
				key = System.getenv(KNOWAGE_HMAC_KEY);
			}
			return key;
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Cannot retrieve the HMAC key", e);
		}
	}

}
