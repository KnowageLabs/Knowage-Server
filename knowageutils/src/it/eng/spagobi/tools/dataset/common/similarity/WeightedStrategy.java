/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.spagobi.tools.dataset.common.similarity;

import gnu.trove.set.hash.TLongHashSet;

import org.apache.log4j.Logger;

public class WeightedStrategy extends AbstractSimilarityStrategy {

	public final static String NAME = "weighted";

	private final static Logger logger = Logger.getLogger(WeightedStrategy.class);

	@Override
	public double measureCoefficient(TLongHashSet setA, TLongHashSet setB) {
		logger.debug("IN");
		logger.debug("First set has [" + setA.size() + "] elements, while the second set has [" + setB.size() + "] elements");

		long startTime = System.currentTimeMillis();
		double intersect = intersectCount(setA, setB);
		logger.debug("Intersect computed in about: " + (System.currentTimeMillis() - startTime) + "ms");
		logger.debug("Intersect cardinality: " + intersect);
		double notIntersect = notIntersectCount(setA, setB);

		double coefficient = intersect / notIntersect;
		logger.debug("Similary coefficient: " + coefficient);
		logger.debug("OUT");
		return round(coefficient);
	}

}
