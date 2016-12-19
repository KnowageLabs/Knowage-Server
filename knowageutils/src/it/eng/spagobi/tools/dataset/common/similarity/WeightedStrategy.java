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
		int sizeA = setA.size();
		int sizeB = setB.size();
		logger.debug("First set has [" + sizeA + "] elements, while the second set has [" + sizeB + "] elements");

		long startTime = System.currentTimeMillis();
		double weight = weight(sizeA, sizeB);
		logger.debug("Weight: " + weight);
		double intersect = intersectCount(setA, setB);
		double union = unionCount(sizeA, sizeB, (int) intersect);
		logger.debug("Intersect computed in about: " + (System.currentTimeMillis() - startTime) + "ms");
		logger.debug("Intersect cardinality: " + intersect);

		double coefficient = 0;//compute(weight, intersect, union, Double.min(sizeA, sizeB));
		logger.debug("Similary coefficient: " + coefficient);
		logger.debug("OUT");
		return round(coefficient);
	}

	private double weight(int sizeA, int sizeB) {
		//logger.debug("Calculate weigth as min(|A|,|B|)/max(|A|,|B|) -> min(" + sizeA + "," + sizeB + ")/max(" + sizeA + "," + sizeB + ")");
		return 0;// Double.min(sizeA, sizeB) / Double.max(sizeA, sizeB);
	}

	private double compute(double weight, double intersect, double union, double min) {
		return intersect / ((weight * union) + ((1 - weight) * min));
	}

}
