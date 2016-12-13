package it.eng.spagobi.tools.dataset.common.similarity;

import gnu.trove.set.hash.TLongHashSet;

import org.apache.log4j.Logger;

public class JaccardStrategy extends AbstractSimilarityStrategy {

	public final static String NAME = "jaccard";

	private final static Logger logger = Logger.getLogger(JaccardStrategy.class);

	@Override
	public double measureCoefficient(TLongHashSet setA, TLongHashSet setB) {
		logger.debug("IN");
		logger.debug("First set has [" + setA.size() + "] elements, while the second set has [" + setB.size() + "] elements");

		long startTime = System.currentTimeMillis();
		double intersect = intersectCount(setA, setB);
		logger.debug("Intersect computed in about: " + (System.currentTimeMillis() - startTime) + "ms");
		logger.debug("Intersect cardinality: " + intersect);

		double coefficient = 2 * intersect / (setA.size() + setB.size());
		logger.debug("Similary coefficient: " + coefficient);
		logger.debug("OUT");
		return round(coefficient);
	}

}
