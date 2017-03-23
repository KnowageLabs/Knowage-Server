package it.eng.spagobi.tools.dataset.associativity.strategy;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.tools.dataset.associativity.IAssociativityManager;
import it.eng.spagobi.tools.dataset.graph.associativity.Config;

import org.apache.log4j.Logger;

public class AssociativeStrategyFactory {

	public final static String INNER_STRATEGY = "INNER";
	public final static String OUTER_STRATEGY = "OUTER";

	static private Logger logger = Logger.getLogger(AssociativeStrategyFactory.class);

	public static IAssociativityManager createStrategyInstance(Config config, UserProfile userProfile) throws Exception {
		logger.debug("IN");

		correctStrategy(config);

		switch (config.getStrategy()) {
		case INNER_STRATEGY:
			return new InnerAssociativityManager(config, userProfile);
		case OUTER_STRATEGY:
			return getOuterStrategy(config, userProfile);
		default:
			logger.info("Impossible to find a strategy named [" + config.getStrategy() + "].");
			logger.info("Getting " + INNER_STRATEGY + " strategy as default.");
			return new InnerAssociativityManager(config, userProfile);
		}
	}

	private static void correctStrategy(Config config) {
		String strategy = config.getStrategy();
		logger.debug("Verifying strategy [" + strategy + "]");
		if (strategy == null || strategy.isEmpty()) {
			config.setStrategy(INNER_STRATEGY);
		} else if (strategy.equals(OUTER_STRATEGY)) {
			try {
				Class.forName("it.eng.knowage.tools.dataset.associativity.strategy.OuterAssociativityManager");
			} catch (ClassNotFoundException e) {
				logger.info("Outer associativity not available.");
				config.setStrategy(INNER_STRATEGY);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static IAssociativityManager getOuterStrategy(Config config, UserProfile userProfile) throws Exception {
		Class cls = Class.forName("it.eng.knowage.tools.dataset.associativity.strategy.OuterAssociativityManager");
		return (IAssociativityManager) cls.getConstructor(Config.class, UserProfile.class).newInstance(config, userProfile);
	}

}
