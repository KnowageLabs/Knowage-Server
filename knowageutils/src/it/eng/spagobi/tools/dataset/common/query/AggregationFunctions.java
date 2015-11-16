/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.common.query;


import java.util.HashMap;
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
		public String getName() {return NONE;}
		public String apply(String fieldName) {
			return fieldName;
		}
	};
	
	public static IAggregationFunction SUM_FUNCTION = new IAggregationFunction() {
		public String getName() {return SUM;}
		public String apply(String fieldName) {
			return "SUM(" + fieldName + ")";
		}
	};
	
	public static IAggregationFunction AVG_FUNCTION = new IAggregationFunction() {
		public String getName() {return AVG;}
		public String apply(String fieldName) {
			return "AVG(" + fieldName + ")";
		}
	};
	
	public static IAggregationFunction MAX_FUNCTION = new IAggregationFunction() {
		public String getName() {return MAX;}
		public String apply(String fieldName) {
			return "MAX(" + fieldName + ")";
		}
	};
	
	public static IAggregationFunction MIN_FUNCTION = new IAggregationFunction() {
		public String getName() {return MIN;}
		public String apply(String fieldName) {
			return "MIN(" + fieldName + ")";
		}
	};
	
	public static IAggregationFunction COUNT_FUNCTION = new IAggregationFunction() {
		public String getName() {return COUNT;}
		public String apply(String fieldName) {
			return "COUNT(" + fieldName + ")";
		}
	};
	
	public static IAggregationFunction COUNT_DISTINCT_FUNCTION = new IAggregationFunction() {
		public String getName() {return COUNT_DISTINCT;}
		public String apply(String fieldName) {
			return "COUNT(DISTINCT " + fieldName + ")";
		}
	};
	
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
