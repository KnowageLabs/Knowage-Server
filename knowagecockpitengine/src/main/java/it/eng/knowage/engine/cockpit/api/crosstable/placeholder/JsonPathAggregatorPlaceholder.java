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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.jayway.jsonpath.DocumentContext;

import it.eng.knowage.engine.cockpit.api.crosstable.CrossTab;
import it.eng.knowage.engine.cockpit.api.crosstable.CrossTab.CellType;
import it.eng.knowage.engine.cockpit.api.crosstable.CrossTab.MeasureInfo;

/**
 * Placeholder for values that are results of some aggregations.
 *
 * @author Marco Libanori
 */
public class JsonPathAggregatorPlaceholder implements Placeholder {

	private static Logger logger = Logger.getLogger(JsonPathAggregatorPlaceholder.class);

	final CrossTab crossTab;
	final MeasureInfo measure;
	final AggregatorDelegate delegate;
	final CellType cellType;
	final String path;
	final DocumentContext parsedValuesDataStore;

	public JsonPathAggregatorPlaceholder(
			final CrossTab crossTab,
			final MeasureInfo measure,
			final AggregatorDelegate delegate,
			final CellType cellType,
			final String path) {

		this.crossTab = crossTab;
		this.parsedValuesDataStore = crossTab.getParsedValuesDataStore();
		this.measure = measure;
		this.delegate = delegate;
		this.cellType = cellType;
		this.path = path;
	}

	@Override
	public final String getValueAsString() {
		Double currValue = getValue();
		return /* path + " - " + */ (currValue != null ? getValue().toString() : "");
	}

	private void filterNullValues(List<ValueWithWeightPlaceholder> values) {
		Iterator<ValueWithWeightPlaceholder> iterator = values.iterator();
		while (iterator.hasNext()) {
			ValueWithWeightPlaceholder next = iterator.next();
			if (next.getValue() == null) {
				iterator.remove();
			}
		}
	}


	@Override
	public final Double getValue() {
		try {
			final List<ValueWithWeightPlaceholder> values = selectValues();

			filterNullValues(values);

			return delegate.aggregate(values);
		} catch (Exception e) {
			logger.error("Error during aggregation at path \"" + path + "\"", e);
			throw e;
		}
	}

	@Override
	public String toString() {
		return "JsonPathAggregatorPlaceholder [path=" + path + "]";
	}

	private List<ValueWithWeightPlaceholder> selectValues() {
		List<ValueWithWeightPlaceholder> ret = new ArrayList<ValueWithWeightPlaceholder>();
		Object read = parsedValuesDataStore.read(path);
		if (read instanceof net.minidev.json.JSONArray) {
			net.minidev.json.JSONArray coll = (net.minidev.json.JSONArray) read;
			for (int i = 0; i<coll.size(); i++) {
				String value = null;
				String weight = "1.0";

				Object object = coll.get(i);

				/*
				 * This depends by the query.
				 *
				 *  If you select more than one field, you will get a map.
				 *  See the attribute path.
				 */
				if (object instanceof String) {
					value = String.valueOf(object);
				} else if (object instanceof Number) {
					value = object instanceof BigDecimal ? ((BigDecimal) object).toPlainString() : String.valueOf(object);
				} else if (object instanceof Map) {
					Map<String, String> currObject = (Map<String, String>) object;

					Iterator<Entry<String, String>> iterator = currObject.entrySet().iterator();
					value = String.valueOf(iterator.next().getValue());
					if (iterator.hasNext()) {
						weight = String.valueOf(iterator.next().getValue());
					}
				} else {
					throw new IllegalStateException("Cannot manage type " + object.getClass());
				}

				ret.add(new ValueWithWeightPlaceholder(value, weight, measure));
			}
		}
		return ret;
	}

	@Override
	public MeasureInfo getMeasureInfo() {
		return measure;
	}

	@Override
	public CellType getCellType() {
		return cellType;
	}

	@Override
	public boolean isAvailable() {
		return true;
	}

	@Override
	public boolean isAggregation() {
		return true;
	}

}
