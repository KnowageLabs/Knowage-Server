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
		Map<String, Map<String, String>> resByValueByType = new HashMap<String, Map<String, String>>();
		for (String type : types) {
			if (!resByValueByType.containsKey(type)) {
				resByValueByType.put(type, new HashMap<String, String>());
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
