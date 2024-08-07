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
package it.eng.spagobi.tools.dataset.service;

import it.eng.spagobi.tools.dataset.utils.DataSetUtilities;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class ManageDatasetsTest {

	@Test
	public void testGetSingleValue() {
		String[] types = new String[] { DataSetUtilities.GENERIC_TYPE, DataSetUtilities.NUMBER_TYPE, DataSetUtilities.RAW_TYPE, DataSetUtilities.STRING_TYPE };
		String[] values = new String[] { "", "'", "''", "0", "'0", "0'", "'0'", "17", "abc", "'qcd'", "'qcd", "qcd'" };
		Map<String, Map<String, String>> resByValueByType = new HashMap<>();
		for (String type : types) {
			if (!resByValueByType.containsKey(type)) {
				resByValueByType.put(type, new HashMap<>());
			}
			for (String value : values) {
				String res;
				try {
					res = ManageDatasets.getSingleValue(value, type);
				} catch (Exception e) {
					res = "exception";
				}

				resByValueByType.get(type).put(value, res);
				System.out.println(String.format("%s - %s : %s", type, value, res));
			}
		}

	}
}
