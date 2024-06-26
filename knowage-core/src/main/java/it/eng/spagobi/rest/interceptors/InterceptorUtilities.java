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

package it.eng.spagobi.rest.interceptors;

import java.util.HashMap;
import java.util.Iterator;

import javax.ws.rs.core.MultivaluedMap;

/**
 *
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
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

}
