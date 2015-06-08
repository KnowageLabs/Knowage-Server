package it.eng.spagobi.engine.util;

import it.eng.spagobi.engine.chart.api.JsonChartTemplateService;
import it.eng.spagobi.utilities.tree.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

	/* Merging codes - Sunburst */

	public JSONArray toTree(Object columnsNeeded, Object serie, Object dataColumnsMapper, List<Object> dataRows) throws JSONException
	{
		// Data columns mapper (as map)
		Map<String,Object> mapper = (Map<String,Object>)dataColumnsMapper;

		// Columns that we need for making a sequence (as map)
		Map<String,Object> columns = (Map<String,Object>)columnsNeeded;

		// In this array list we will put raw names of necessary columns
		ArrayList<String> listColumns = new ArrayList<String>();

		// End result - map of maps (records with their columns values)
		HashMap<Integer,HashMap> result = new HashMap<Integer, HashMap>();

		// Take value of the SERIE column (the one that contains numerical values)
		Object serieRawColumn = mapper.get(serie.toString()).toString();

		// Take raw names of all of the columns that we need for creating a sequence
		for (int i=0; i<columns.size(); i++)
		{
			Object z = columns.get(i);
			listColumns.add(mapper.get(z).toString());
		}

		/* Pass through all records in order to get values of just those 
		 * columns that we need for sequence. */
		for (int i=0; i<dataRows.size(); i++)
		{
			Map<String,Object> row = (Map<String,Object>)dataRows.get(i);
			HashMap<String,String> record = new HashMap<String, String>();

			/* For every record take these columns */
			for (int j=0; j<listColumns.size(); j++)
			{
				Object x = row.get(listColumns.get(j));
				record.put(columns.get(j).toString(), x.toString());				
			}

			record.put(serie.toString(), row.get(serieRawColumn).toString());

			result.put(new Integer(i),record);			
		}

		JSONArray res = countSequence(columns,serie,result);

		return res;
	}

	public JSONArray countSequence(Map<String,Object> columns, Object serie, HashMap<Integer, HashMap> result) throws JSONException
	{		
		HashMap<String,Double> endresult = new HashMap<String, Double>();
		JSONArray ja = new JSONArray();

		// Going through all records 
		for (int i=0; i<result.size(); i++)
		{
			HashMap<String,Object> singleRecord = (HashMap<String,Object>)result.get(new Integer(i));
			String sequence = "";

			// Columns that we now need for creating the sequence
			for (int j=0; j<columns.size(); j++)
			{
				if (j==0)
				{
					sequence = singleRecord.get(columns.get(j)).toString();
				}
				else 
				{
					sequence = sequence + "-" + singleRecord.get(columns.get(j)).toString();	
				}
			}
			System.out.println(serie);
			Double value = Double.parseDouble(singleRecord.get(serie).toString());			
			System.out.println(value);
			JSONObject jo = new JSONObject();

			if (!endresult.containsKey(sequence))
			{
				endresult.put(sequence,value);				

				jo.put("sequence", sequence);
				jo.put("value", value);
			}
			else
			{
				Double oldValue = endresult.get(sequence);
				endresult.put(sequence,value+oldValue);

				jo.put("sequence", sequence);
				jo.put("value", value+oldValue);
			}

			ja.put(jo);
		}

		return ja;
	}

	public JSONObject createTreeChart(Object columnsNeeded, Object serie, Object dataColumnsMapper, List<Object> dataRows) throws JSONException
	{
		Map<String,String> mapper = (Map<String,String>)dataColumnsMapper;

		Map<String,String> columns = (Map<String,String>)columnsNeeded;

		Object serieRawColumn = mapper.get(serie.toString()+"_SUM");

		ArrayList<String> listColumns = new ArrayList<String>();

		HashMap<Integer,HashMap> result = new HashMap<Integer, HashMap>();

		for (int i = 0; i<columns.size();i++){

			Object cndata = columns.get(i);

			listColumns.add(mapper.get(cndata).toString());

		}

		for (int i=0; i<dataRows.size(); i++)
		{
			Map<String,Object> row = (Map<String,Object>)dataRows.get(i);
			HashMap<String,String> record = new HashMap<String, String>();

			/* For every record take these columns */
			for (int j=0; j<listColumns.size(); j++)
			{
				Object x = row.get(listColumns.get(j));
				record.put(columns.get(j).toString(), x.toString());				
			}

			record.put(serie.toString(), row.get(serieRawColumn).toString());

			result.put(new Integer(i),record);			
		}

		JSONObject res = createTreeMap(columns,serie,result);

		return res;

	}

	public JSONObject createTreeMap(Map<String,String> columns, Object serie, HashMap<Integer, HashMap> result) throws JSONException
	{		

		JSONObject root = new JSONObject();

		JSONObject currentNode = null;

		for (int i=0; i<result.size();i++){
			currentNode = root;
			for (int j=0;j<columns.size();j++){

				String nodeName = ""+result.get(i).get(columns.get(j));
				JSONObject existingNodeValue = (JSONObject)currentNode.optJSONObject(nodeName);

				if(existingNodeValue==null){

					if (j!=columns.size()-1){
						currentNode.put(nodeName, new JSONObject());
					}
					else
					{
						JSONObject njo = new JSONObject();
						njo.put("value", result.get(i).get(serie));
						currentNode.put(nodeName, njo);
						break;
					}

				}

				currentNode = currentNode.getJSONObject(nodeName);

			}

		}
		return root;
	}


}
