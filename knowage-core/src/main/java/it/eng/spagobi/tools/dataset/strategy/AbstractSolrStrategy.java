/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2018 Engineering Ingegneria Informatica S.p.A.
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
 *
 */
package it.eng.spagobi.tools.dataset.strategy;

import org.apache.solr.client.solrj.response.FieldStatsInfo;

import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.query.AggregationFunctions;
import it.eng.spagobi.tools.dataset.common.query.IAggregationFunction;

public abstract class AbstractSolrStrategy extends AbstractEvaluationStrategy {

	public AbstractSolrStrategy(IDataSet dataSet) {
		super(dataSet);
	}

	protected Object getValue(FieldStatsInfo fieldStats, IAggregationFunction aggregationFunction) {
		if (AggregationFunctions.COUNT.equals(aggregationFunction.getName())) {
			return fieldStats.getCount();
		}
		if (AggregationFunctions.COUNT_DISTINCT.equals(aggregationFunction.getName())) {
			return fieldStats.getCountDistinct();
		}
		if (AggregationFunctions.MIN.equals(aggregationFunction.getName())) {
			return fieldStats.getMin();
		}
		if (AggregationFunctions.MAX.equals(aggregationFunction.getName())) {
			return fieldStats.getMax();
		}
		if (AggregationFunctions.SUM.equals(aggregationFunction.getName())) {
			return fieldStats.getSum();
		}
		if (AggregationFunctions.AVG.equals(aggregationFunction.getName())) {
			return fieldStats.getMean();
		}
		throw new IllegalArgumentException("The function " + aggregationFunction.getName() + " is not valid here");
	}

}