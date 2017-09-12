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
