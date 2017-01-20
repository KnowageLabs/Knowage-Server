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

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

public class SimilarityEvaluator {

	private static transient Logger logger = Logger.getLogger(SimilarityEvaluator.class);

	private final ISimilarityStrategy strategy;
	private final int top;
	private final double threshold;

	private final boolean evaluateNumber;

	public SimilarityEvaluator(ISimilarityStrategy strategy) {
		this(strategy, Integer.MAX_VALUE, 0);

	}

	public SimilarityEvaluator(ISimilarityStrategy strategy, int top, double threshold) {
		this(strategy, top, threshold, false);
	}

	public SimilarityEvaluator(ISimilarityStrategy strategy, int top, double threshold, boolean evaluateNumber) {
		this.strategy = strategy;
		this.top = top;
		this.threshold = threshold;
		this.evaluateNumber = evaluateNumber;
	}

	public Set<Similarity> evaluate(List<String> dataSets, Map<String, Map<String, TLongHashSet>> dataSetDomainValues) {
		return evaluate(dataSets, dataSetDomainValues, false);
	}

	public Set<Similarity> evaluate(List<String> dataSets, Map<String, Map<String, TLongHashSet>> dataSetDomainValues, boolean aggregate) {
		logger.debug("IN");
		Set<Similarity> toReturn = new TreeSet<>(Collections.reverseOrder());
		logger.debug("Evaluating dataSet similarity using [" + strategy.getClass().getName() + "] strategy");

		ListIterator<String> dsIterator = dataSets.listIterator();
		while (dsIterator.hasNext()) {
			String currentLabel = dsIterator.next();
			ListIterator<String> subIterator = dataSets.listIterator(dsIterator.nextIndex());
			while (subIterator.hasNext()) {
				String otherLabel = subIterator.next();
				logger.debug("Evaluating similarity between datasets [" + currentLabel + "] and [" + otherLabel + "]");
				Set<Similarity> partialSet = evaluate(currentLabel, dataSetDomainValues.get(currentLabel), otherLabel, dataSetDomainValues.get(otherLabel));
				toReturn.addAll(partialSet);
			}
			dsIterator.remove();
		}

		if (aggregate) {
			toReturn = aggregate(toReturn);
		}

		logger.debug("OUT");
		return toReturn.size() > top ? limit(toReturn) : toReturn;
	}

	private Set<Similarity> evaluate(String labelDatasetA, Map<String, TLongHashSet> valuesDataSetA, String labelDatasetB,
			Map<String, TLongHashSet> valuesDataSetB) {
		logger.debug("IN");
		Set<Similarity> toReturn = new TreeSet<>(Collections.reverseOrder());

		for (String fieldA : valuesDataSetA.keySet()) {
			for (String fieldB : valuesDataSetB.keySet()) {
				logger.debug("Evaluating similarity between fields [" + fieldA + "] and [" + fieldB + "]");
				TLongHashSet setA = valuesDataSetA.get(fieldA);
				TLongHashSet setB = valuesDataSetB.get(fieldB);

				double coefficient = 0;
				if (setA != null && setB != null) {
					coefficient = strategy.measureCoefficient(setA, setB);
				} else if (evaluateNumber && setA == null && setB == null) {
					logger.debug("Cannot use domain values to measure field similarity. Using column name instead.");
					// coefficient = StringUtils.getJaroWinklerDistance(fieldA, fieldB);
					coefficient = fieldA.equalsIgnoreCase(fieldB) ? 1 : 0;
				}
				logger.debug("Coefficient measures [" + coefficient + "%]");
				if (coefficient >= threshold) {
					Similarity similarity = new Similarity(coefficient);
					similarity.addField(new Field(labelDatasetA, fieldA));
					similarity.addField(new Field(labelDatasetB, fieldB));
					toReturn.add(similarity);
				}
			}
		}
		logger.debug("OUT");
		return toReturn;
	}

	/*
	 * This method aggregates a simple set of similarities (each similarity has two elements) to obtain a longer similarities chain
	 */
	private Set<Similarity> aggregate(Set<Similarity> similarities) {
		Set<Similarity> chained = new TreeSet<>(Collections.reverseOrder());
		Set<Similarity> removable = new HashSet<>();

		Iterator<Similarity> itChain = similarities.iterator();
		while (itChain.hasNext()) {
			Similarity chain = itChain.next();

			if (!removable.contains(chain)) {
				Iterator<Similarity> itTarget = similarities.iterator();
				while (itTarget.hasNext()) {
					Similarity target = itTarget.next();
					if (!removable.contains(target) && !chained.contains(target) && !chain.equals(target) && chainable(chain, target)) {
						removable.add(target);
						double coefficient = chain.add(target);
						logger.debug("Update similarity coefficient while chaining: " + coefficient);
					}
				}
				chained.add(chain);
			}
		}
		return chained;
	}

	private boolean chainable(Similarity chain, Similarity target) {
		boolean toReturn = false;
		for (Field targetField : target.getFields()) {
			for (Field chainField : chain.getFields()) {
				if (!chainable(chainField, targetField)) {
					return false;
				}
			}
			if (chain.getFields().contains(targetField)) {
				toReturn = true;
			}
		}
		return toReturn;
	}

	private boolean chainable(Field chain, Field target) {
		if (chain.getDatasetLabel().equals(target.getDatasetLabel()) && !chain.getDatasetColumn().equals(target.getDatasetColumn())) {
			return false;
		}
		return true;
	}

	private Set<Similarity> limit(Set<Similarity> set) {
		logger.debug("IN");
		Set<Similarity> subSet = new TreeSet<>(Collections.reverseOrder());
		Iterator<Similarity> it = set.iterator();
		int count = 0;
		while (it.hasNext() && count < top) {
			subSet.add(it.next());
			count++;
		}
		logger.debug("OUT");
		return subSet;
	}
}
