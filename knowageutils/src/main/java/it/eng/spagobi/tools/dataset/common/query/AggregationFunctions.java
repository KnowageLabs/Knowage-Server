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
package it.eng.spagobi.tools.dataset.common.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AggregationFunctions {

	private static Map<String, IAggregationFunction> aggregationFunctions;

	public static String NONE = "NONE";
	public static String SUM = "SUM";
	public static String AVG = "AVG";
	public static String MAX = "MAX";
	public static String MIN = "MIN";
	public static String COUNT = "COUNT";
	public static String COUNT_DISTINCT = "COUNT_DISTINCT";

	public static IAggregationFunction NONE_FUNCTION = new IAggregationFunction() {
		@Override
		public String getName() {
			return NONE;
		}

		@Override
		public String apply(String fieldName) {
			return fieldName;
		}
	};

	public static IAggregationFunction SUM_FUNCTION = new IAggregationFunction() {
		@Override
		public String getName() {
			return SUM;
		}

		@Override
		public String apply(String fieldName) {
			return "SUM(" + fieldName + ")";
		}
	};

	public static IAggregationFunction AVG_FUNCTION = new IAggregationFunction() {
		@Override
		public String getName() {
			return AVG;
		}

		@Override
		public String apply(String fieldName) {
			return "AVG(" + fieldName + ")";
		}
	};

	public static IAggregationFunction MAX_FUNCTION = new IAggregationFunction() {
		@Override
		public String getName() {
			return MAX;
		}

		@Override
		public String apply(String fieldName) {
			return "MAX(" + fieldName + ")";
		}
	};

	public static IAggregationFunction MIN_FUNCTION = new IAggregationFunction() {
		@Override
		public String getName() {
			return MIN;
		}

		@Override
		public String apply(String fieldName) {
			return "MIN(" + fieldName + ")";
		}
	};

	public static IAggregationFunction COUNT_FUNCTION = new IAggregationFunction() {
		@Override
		public String getName() {
			return COUNT;
		}

		@Override
		public String apply(String fieldName) {
			return "COUNT(" + fieldName + ")";
		}
	};

	public static IAggregationFunction COUNT_DISTINCT_FUNCTION = new IAggregationFunction() {
		@Override
		public String getName() {
			return COUNT_DISTINCT;
		}

		@Override
		public String apply(String fieldName) {
			return "COUNT(DISTINCT " + fieldName + ")";
		}
	};

	public static List<String> getAggregationsList() {

		ArrayList<String> listToReturn = new ArrayList<String>();

		listToReturn.add(NONE);
		listToReturn.add(SUM);
		listToReturn.add(AVG);
		listToReturn.add(MAX);
		listToReturn.add(MIN);
		listToReturn.add(COUNT);
		listToReturn.add(COUNT_DISTINCT);

		return listToReturn;

	}

	static {
		aggregationFunctions = new HashMap<String, IAggregationFunction>();
		aggregationFunctions.put(NONE, NONE_FUNCTION);
		aggregationFunctions.put(SUM, SUM_FUNCTION);
		aggregationFunctions.put(AVG, AVG_FUNCTION);
		aggregationFunctions.put(MAX, MAX_FUNCTION);
		aggregationFunctions.put(MIN, MIN_FUNCTION);
		aggregationFunctions.put(COUNT, COUNT_FUNCTION);
		aggregationFunctions.put(COUNT_DISTINCT, COUNT_DISTINCT_FUNCTION);
	}

	public static IAggregationFunction get(String functionName) {
		IAggregationFunction toReturn = null;
		if (functionName != null && aggregationFunctions.containsKey(functionName.toUpperCase())) {
			toReturn = aggregationFunctions.get(functionName.toUpperCase());
		} else {
			toReturn = NONE_FUNCTION;
		}
		return toReturn;
	}

}
