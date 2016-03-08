/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 */
package it.eng.spagobi.engines.whatif.parameters;

import it.eng.spagobi.engines.whatif.template.WhatIfTemplate;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class MDXParametersUtilities {

	public static transient Logger logger = Logger.getLogger(MDXParametersUtilities.class);

	public static String substituteParametersInMDXQuery(String mdxQueryStr, List<WhatIfTemplate.Parameter> parameters, Map env) {
		String toReturn = mdxQueryStr;
		if (parameters != null && parameters.size() > 0) {
			for (int i = 0; i < parameters.size(); i++) {
				WhatIfTemplate.Parameter parameter = parameters.get(i);
				String name = parameter.getName();
				String alias = parameter.getAlias();

				Object value = env.get(name);
				if (value == null) {
					throw new SpagoBIEngineRuntimeException("Parameter [" + name + "] has no value");
				}
				if (!(value instanceof String)) {
					// since parameters values are decoded by
					// ParametersDecoder.getDecodedRequestParameters(
					// servletRequest ) and put in env, if the value is not a
					// String it means that it is an array (multi-value
					// parameter)
					throw new SpagoBIEngineRuntimeException("Parameter [" + name + "] contains more than one value, that is not admissible");
				}
				String parameterValue = (String) value;
				if ((parameterValue == null) || parameterValue.trim().equals("")) {
					logger.error("Parameter with name [" + name + "] has no value; it will be skipped.");
					continue;
				}
				// substitutes profile attributes (syntax ${xx} )
				toReturn = substituteProfileAttributeInMDXQuery(toReturn, alias, parameterValue);
				// substitutes parameters (syntax $P{xx} )
				toReturn = substituteParameterInMDXQuery(toReturn, alias, parameterValue);
			}
		}
		return toReturn;
	}

	public static String substituteProfileAttributeInMDXQuery(String query, String pname, String pvalue) {
		String newQuery = query;
		// substitute parameters using Mondrian syntax
		int index = -1;
		int ptr = 0;
		while ((index = newQuery.indexOf("Parameter", ptr)) != -1) {
			ptr = newQuery.indexOf("(", index);
			String firstArg = newQuery.substring(newQuery.indexOf("(", ptr) + 1, newQuery.indexOf(",", ptr));
			if (!firstArg.trim().equalsIgnoreCase("\"" + pname + "\"")) {
				continue;
			}
			ptr = newQuery.indexOf(",", ptr) + 1; // 2 arg
			String secondArg = newQuery.substring(ptr, newQuery.indexOf(",", ptr));
			ptr = newQuery.indexOf(",", ptr) + 1; // 3 arg
			String thirdArg = newQuery.substring(ptr, newQuery.indexOf(",", ptr));
			// if the parameter type is STRING, add double apix to the value
			// passed by SpagoBI
			if (secondArg.equalsIgnoreCase("STRING")) {
				newQuery = newQuery.substring(0, ptr) + '"' + pvalue + '"' + newQuery.substring(newQuery.indexOf(",", ptr + 1), newQuery.length());
			} else {
				newQuery = newQuery.substring(0, ptr) + pvalue + newQuery.substring(newQuery.indexOf(",", ptr + 1), newQuery.length());
			}
		}
		// substitute the spagobi parameter sintax
		index = -1;
		ptr = 0;
		while ((index = newQuery.indexOf("${", ptr)) != -1) {
			int indexEnd = newQuery.indexOf("}", index);
			ptr = indexEnd;
			String namePar = newQuery.substring(index + 2, indexEnd);

			// TODO manage property parameters type
			// If the parameter comes from a property, a double apix has to be
			// added
			// but we have to pay attention to recognize property parameters and
			// filter
			// conditions

			if (!namePar.trim().equalsIgnoreCase(pname)) {
				continue;
			}
			newQuery = newQuery.substring(0, index) + pvalue + newQuery.substring(indexEnd + 1, newQuery.length());
		}
		// return query
		return newQuery;
	}

	public static String substituteParameterInMDXQuery(String query, String pname, String pvalue) {
		String newQuery = query;
		// substitute parameters using Mondrian syntax
		int index = -1;
		int ptr = 0;
		while ((index = newQuery.indexOf("Parameter", ptr)) != -1) {
			ptr = newQuery.indexOf("(", index);
			String firstArg = newQuery.substring(newQuery.indexOf("(", ptr) + 1, newQuery.indexOf(",", ptr));
			if (!firstArg.trim().equalsIgnoreCase("\"" + pname + "\"")) {
				continue;
			}
			ptr = newQuery.indexOf(",", ptr) + 1; // 2 arg
			String secondArg = newQuery.substring(ptr, newQuery.indexOf(",", ptr));
			ptr = newQuery.indexOf(",", ptr) + 1; // 3 arg
			String thirdArg = newQuery.substring(ptr, newQuery.indexOf(",", ptr));
			// if the parameter type is STRING, add double apix to the value
			// passed by SpagoBI
			if (secondArg.equalsIgnoreCase("STRING")) {
				newQuery = newQuery.substring(0, ptr) + '"' + pvalue + '"' + newQuery.substring(newQuery.indexOf(",", ptr + 1), newQuery.length());
			} else {
				newQuery = newQuery.substring(0, ptr) + pvalue + newQuery.substring(newQuery.indexOf(",", ptr + 1), newQuery.length());
			}
		}
		// substitute the spagobi parameter sintax
		index = -1;
		ptr = 0;
		while ((index = newQuery.indexOf("$P{", ptr)) != -1) {
			int indexEnd = newQuery.indexOf("}", index);
			ptr = indexEnd;
			String namePar = newQuery.substring(index + 3, indexEnd);

			// TODO manage property parameters type
			// If the parameter comes from a property, a double apix has to be
			// added
			// but we have to pay attention to recognize property parameters and
			// filter
			// conditions

			if (!namePar.trim().equalsIgnoreCase(pname)) {
				continue;
			}
			newQuery = newQuery.substring(0, index) + pvalue + newQuery.substring(indexEnd + 1, newQuery.length());
		}
		// return query
		return newQuery;
	}
}
