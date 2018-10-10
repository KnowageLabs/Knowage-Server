package it.eng.spagobi.tools.dataset.associativity.strategy;

import org.apache.log4j.Logger;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.tools.dataset.associativity.IAssociativityManager;
import it.eng.spagobi.tools.dataset.graph.associativity.Config;

public class AssociativeStrategyFactory {

	public final static String OUTER_STRATEGY = "OUTER";

	static private Logger logger = Logger.getLogger(AssociativeStrategyFactory.class);

	public static IAssociativityManager createStrategyInstance(Config config, UserProfile userProfile) throws Exception {
		logger.debug("IN");

		correctStrategy(config);

		switch (config.getStrategy()) {
		case OUTER_STRATEGY:
			return new OuterAssociativityManager(config, userProfile);
		default:
			logger.info("Impossible to find a strategy named [" + config.getStrategy() + "].");
			logger.info("Getting " + OUTER_STRATEGY + " strategy as default.");
			return new OuterAssociativityManager(config, userProfile);
		}
	}

	private static void correctStrategy(Config config) {
		String strategy = config.getStrategy();
		logger.debug("Verifying strategy [" + strategy + "]");
		if (strategy == null || strategy.isEmpty()) {
			logger.debug("Strategy not set. Setting it to " + OUTER_STRATEGY + " as default.");
			config.setStrategy(OUTER_STRATEGY);
		}
	}
}
