/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.persist.temporarytable;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @authors Davide Zerbetto (davide.zerbetto@eng.it)
 * 
 */
public class HSQLTypeTranslator implements INativeDBTypeable {

	private static Logger logger = Logger.getLogger(HSQLTypeTranslator.class);

	private static Map<String, String> hsqlTypeMapping;
	static {
		hsqlTypeMapping = new HashMap<String, String>();
		hsqlTypeMapping.put("java.lang.Integer", "INTEGER");
		hsqlTypeMapping.put("java.lang.String", "VARCHAR");
		hsqlTypeMapping.put("java.lang.Boolean", "BOOLEAN");
		hsqlTypeMapping.put("java.lang.Float", "DOUBLE");
		hsqlTypeMapping.put("java.lang.Double", "DOUBLE");
		hsqlTypeMapping.put("java.util.Date", "DATE");
		hsqlTypeMapping.put("java.sql.Date", "DATE");
		hsqlTypeMapping.put("java.sql.Timestamp", "TIMESTAMP");
		hsqlTypeMapping.put("java.math.BigDecimal", "NUMERIC");
		hsqlTypeMapping.put("java.lang.BigDecimal", "NUMERIC");
	}

	@SuppressWarnings("rawtypes")
	public String getNativeTypeString(String typeJavaName, Map properties) {
		logger.debug("Translating java type " + typeJavaName
				+ " with properties " + properties);
		// convert java type in SQL type
		String queryType = "";
		String typeSQL = "";

		// proeprties
		Integer size = null;
		Integer precision = null;
		Integer scale = null;

		if (properties != null) {
			if (properties.get(SIZE) != null)
				size = Integer.valueOf(properties.get(SIZE).toString());
			if (properties.get(PRECISION) != null)
				precision = Integer.valueOf(properties.get(PRECISION)
						.toString());
			if (properties.get(SCALE) != null)
				scale = Integer.valueOf(properties.get(SCALE).toString());
		}

		typeSQL = hsqlTypeMapping.get(typeJavaName);

		// write Type
		queryType += " " + typeSQL + "";

		if (typeJavaName.equalsIgnoreCase(String.class.getName())) {
			if (size != null && size != 0) {
				queryType += "(" + size + ")";
			}
		} else if (typeJavaName.equalsIgnoreCase(BigDecimal.class.getName())) {
			if ((precision != null)) {
				if (scale != null) {
					queryType += "(" + precision + "," + scale + ")";
				} else {
					queryType += "(" + precision + ")";
				}
			}
		}
		logger.debug("The translated HSQL type is " + queryType);
		queryType += " ";
		return queryType;
	}

}
