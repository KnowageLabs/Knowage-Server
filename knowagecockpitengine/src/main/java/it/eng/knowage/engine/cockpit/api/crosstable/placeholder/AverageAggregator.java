/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2020 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.knowage.engine.cockpit.api.crosstable.placeholder;

import java.math.BigDecimal;
import java.util.List;

/**
 * Singleton of an avg aggregator.
 *
 * @author Marco Libanori
 *
 */
public class AverageAggregator implements AggregatorDelegate {

	private static final AggregatorDelegate instance = new AverageAggregator();

	protected AverageAggregator() {

	}

	public static AggregatorDelegate instance() {
		return instance;
	}

	@Override
	public Double aggregate(List<ValueWithWeightPlaceholder> values) {
		double ret = 0.0;
		double weightSum = 0.0;
		if (!values.isEmpty()) {
			for (ValueWithWeightPlaceholder currPlaceholder : values) {
				Double value = currPlaceholder.getValue();
				Double weight = currPlaceholder.getWeight();
				ret += (value * weight);
				weightSum += weight;
			}
			ret = BigDecimal.valueOf(ret / weightSum).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
		}
		return ret;
	}

}
