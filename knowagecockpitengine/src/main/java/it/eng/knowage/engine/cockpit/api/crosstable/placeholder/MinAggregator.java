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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Singleton of a min aggregator.
 *
 * @author Marco Libanori
 *
 */
public class MinAggregator implements AggregatorDelegate {

	private static final AggregatorDelegate instance = new MinAggregator();

	protected MinAggregator() {

	}

	public static AggregatorDelegate instance() {
		return instance;
	}

	@Override
	public Double aggregate(List<ValueWithWeightPlaceholder> values) {
		List<Double> altList = new ArrayList<Double>();
		for (Placeholder currPlaceholder : values) {
			altList.add(currPlaceholder.getValue());
		}
		return altList.isEmpty() ? null : Collections.min(altList);
	}

}
