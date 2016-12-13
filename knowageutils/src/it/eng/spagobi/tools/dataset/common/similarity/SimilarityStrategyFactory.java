package it.eng.spagobi.tools.dataset.common.similarity;

import org.apache.log4j.Logger;

public final class SimilarityStrategyFactory {

	static private Logger logger = Logger.getLogger(SimilarityStrategyFactory.class);

	public static ISimilarityStrategy getJaccardStrategy() {
		return createStrategyInstance("Jaccard");
	}

	public static ISimilarityStrategy getSorensenDiceStrategy() {
		return createStrategyInstance("SorensenDice");
	}

	public static ISimilarityStrategy getWeightedStrategy() {
		return createStrategyInstance("Weighted");
	}

	public static ISimilarityStrategy createStrategyInstance(String strategy) {
		logger.debug("IN");
		logger.debug("Looking for strategy named [" + strategy + "]");

		if (strategy == null) {
			strategy = "";
		}

		switch (strategy) {
		case JaccardStrategy.NAME:
			return new JaccardStrategy();
		case SorensenDiceStrategy.NAME:
			return new SorensenDiceStrategy();
		case WeightedStrategy.NAME:
			return new WeightedStrategy();
		default:
			logger.debug("Impossible to find a strategy named [" + strategy + "].");
			logger.debug("Getting Jaccard strategy as default.");
			return new JaccardStrategy();
		}
	}
}
