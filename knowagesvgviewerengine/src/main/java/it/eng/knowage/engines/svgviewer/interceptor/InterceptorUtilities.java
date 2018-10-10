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

package it.eng.knowage.engines.svgviewer.interceptor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

public class InterceptorUtilities {




	/**
	 * Trasforms a MultivaluedMap in a HashMap
	 * 
	 * @param multiMap
	 * @return
	 */
	public static HashMap<String, String> fromMultivaluedMapToHashMap(MultivaluedMap<String, String> multiMap) {
		HashMap<String, String> map = new HashMap<String, String>();
		String key, value;

		if (multiMap != null) {
			Iterator<String> it = multiMap.keySet().iterator();
			while (it.hasNext()) {
				key = it.next();
				value = (multiMap.get(key)).toArray().toString();
				map.put(key, value);
			}
		}

		return map;
	}

	/**
	 * Get the content of a map of object and for every value apply the toString. If the value is an array it iterate in all the entries
	 * 
	 * @param stringMap
	 * @param genericMap
	 */
	public static void addGenericMap(Map<String, String> stringMap, Map genericMap) {
		Object key, value;
		Object[] valueArray;
		StringBuilder valueString;
		if (genericMap != null) {
			Iterator iter = genericMap.keySet().iterator();
			while (iter.hasNext()) {
				key = iter.next();
				value = genericMap.get(key);
				if (value instanceof Object[]) {
					valueArray = (Object[]) value;
					valueString = new StringBuilder("");
					valueString.append("[");
					for (int i = 0; i < valueArray.length; i++) {
						if (valueArray[i] != null) {
							valueString.append(valueArray[i].toString());
							if (i < valueArray.length - 1) {
								valueString.append(",");
							}
						}
					}
					valueString.append("]");
					stringMap.put(key.toString(), valueString.toString());
				} else {
					stringMap.put(key.toString(), value.toString());
				}
			}
		}
	}




}
