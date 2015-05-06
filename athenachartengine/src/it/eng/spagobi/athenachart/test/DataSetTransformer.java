package it.eng.spagobi.athenachart.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DataSetTransformer {

	public JSONArray toColumn(Object columnsNeeded,Object dataColumnsMapper, List<Object> dataRows) throws JSONException{

		ArrayList<String>lista= new ArrayList<>();

		HashMap<String, Integer> result =  new HashMap<String, Integer>();

		Map<String, String> dcm = (Map<String, String>) dataColumnsMapper;

		Map<String, String> cn = (Map<String, String>) columnsNeeded;

		ArrayList<String> dcmnc = new ArrayList<>();

		for (int i = 0; i<cn.size();i++){

			Object cndata = cn.get(i);

			lista.add(dcm.get(cndata).toString());

		}

		for (int i=0; i<dataRows.size();i++){

			Map<String,Object> row = (Map<String, Object>) dataRows.get(i);

			for (int j=0; j<lista.size();j++){

				System.out.println(row.get(lista.get(j).toString()));

				if (result.containsKey(row.get(lista.get(j).toString()))){

					int value = result.get(row.get(lista.get(j)));

					result.put((String) row.get(lista.get(j).toString()), value+1);

				}

				else{

					result.put((String) row.get(lista.get(j)), 1);
				}
			}

		}

		JSONArray ja = new JSONArray();

		for (String key : result.keySet()) {
			System.out.println(key + " " + result.get(key));

			JSONObject jo = new JSONObject();

			jo.put("name", key);
			jo.put("count", result.get(key));

			ja.put(jo);

		}

		return ja;

	}

}
