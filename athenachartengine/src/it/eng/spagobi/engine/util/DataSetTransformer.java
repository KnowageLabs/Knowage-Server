package it.eng.spagobi.engine.util;

import it.eng.spagobi.engine.chart.api.JsonChartTemplateService;
import it.eng.spagobi.utilities.tree.Node;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.lf5.util.DateFormatManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DataSetTransformer {

	public JSONArray toWordcloud(Object columnsNeeded,Object dataColumnsMapper, List<Object> dataRows, Object serie, Object sizeCriteria) throws JSONException{

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

		JSONArray res = toWordcloudArray(columns,serie,result, sizeCriteria);

		return res;

	}

	private JSONArray toWordcloudArray(Map<String, String> columns, Object serie, HashMap<Integer, HashMap> result, Object sizeCriteria) throws JSONException {

		JSONArray fr = new JSONArray();

		HashMap<String, Double> res = new HashMap<String, Double>();

		for (int i=0; i<result.size();i++){

			for (int j=0; j<columns.size();j++){

				if (!res.containsKey(result.get(i).get(columns.get(j)))){

					String name = (String) result.get(i).get(columns.get(j));
					
					Double value = 0.00;

					if (sizeCriteria.toString().equals("serie")){

					value = value + Double.parseDouble(result.get(i).get(serie).toString());
					
					}
					else if (sizeCriteria.toString().equals("occurrences")){
						
					value++;	
						
					}

					res.put(name, value);

				}

				else{

					String name = (String) result.get(i).get(columns.get(j));

					Double oldvalue = res.get(name);
					
					Double newValue = 0.00;
					
					if (sizeCriteria.toString().equals("serie")){

					Double value = Double.parseDouble(result.get(i).get(serie).toString());

					newValue = oldvalue+value;
					
					}
					else if (sizeCriteria.toString().equals("occurrences")){
						
						newValue = oldvalue+1;	
							
					}
					
					
					res.remove(name);

					res.put(name, newValue);

				}

			}

		}

		Iterator it = res.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry)it.next();

			JSONObject jo = new JSONObject();
			jo.put("name", pair.getKey());
			jo.put("value", pair.getValue());

			fr.put(jo);

		}
		
		return fr;
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
		Object serieRawColumn = mapper.get(serie.toString()+"_SUM").toString();

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
					sequence = sequence + "_SEP_" + singleRecord.get(columns.get(j)).toString();	
				}
			}

			Double value = Double.parseDouble(singleRecord.get(serie).toString());			

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

	public JSONArray getGroupsForParallelChart(Object columnsNeeded, Object dataColumnsMapper, List<Object> dataRows) throws JSONException{

		JSONArray ja = new JSONArray();

		Map<String,String> columns = (Map<String,String>)columnsNeeded;

		Map<String,String> mapper = (Map<String,String>)dataColumnsMapper;

		String group = columns.get(0);

		String groupvalue = mapper.get(group);

		ArrayList<String> al = new ArrayList<String>(); 

		int j = 0;

		for (int i =0; i<dataRows.size();i++){

			Map<String,Object> row = (Map<String, Object>) dataRows.get(i);

			if (!al.contains(row.get(groupvalue))){

				al.add((String) row.get(groupvalue));
				JSONObject jo = new JSONObject();
				jo.put((new Integer(j)).toString(), row.get(groupvalue).toString());
				ja.put(jo);
				j++;
			}
		}

		return ja;

	}

	public JSONArray getSeriesForParallelChart(Object serieNeeded) throws JSONException{

		JSONArray ja = new JSONArray();

		Map<String,String> series= (Map<String,String>)serieNeeded;

		ArrayList<String> al = new ArrayList<String>();

		int j = 0;

		for (int i = 0; i<series.size();i++){

			if (!al.contains(series.get(i))){

				al.add(series.get(i)+"_SUM");
				JSONObject jo = new JSONObject();
				jo.put((new Integer(j).toString()), series.get(i));
				ja.put(jo);
				j++;
			}

		}

		return ja;

	}

	public JSONArray getColorPallete(Object colorsRequired) throws JSONException{

		JSONArray ja = new JSONArray();

		Map<String,String> colorsReq= (Map<String,String>)colorsRequired;

		ArrayList<String> al = new ArrayList<String>();

		int j = 0;

		for (int i = 0; i<colorsReq.size();i++){

			if (!al.contains(colorsReq.get(i))){

				al.add(colorsReq.get(i));
				JSONObject jo = new JSONObject();
				jo.put((new Integer(j).toString()), colorsReq.get(i));
				ja.put(jo);
				j++;
			}

		}

		return ja;

	}

	public JSONArray toParallelChart(Object columnsNeeded,Object dataColumnsMapper, List<Object> dataRows, Object serieNeeded) throws JSONException{

		JSONArray res = new JSONArray();

		Map<String,String> mapper = (Map<String,String>)dataColumnsMapper;

		Map<String,String> columns = (Map<String,String>)columnsNeeded;

		Map<String,String> series = (Map<String,String>)serieNeeded;

		Map<String,String> colMapper = new HashMap<String, String>();

		ArrayList<String> listColumns = new ArrayList<String>();

		for (int i = 0; i<series.size(); i++){

			Object serie = series.get(i)+"_SUM";

			listColumns.add(mapper.get(serie));

			colMapper.put(mapper.get(serie), series.get(i));

		}

		for (int i = 0; i<columns.size(); i++){

			Object column = columns.get(i);
			listColumns.add(mapper.get(column));

			colMapper.put(mapper.get(column), columns.get(i).toString());

		}

		for (int i = 0; i<dataRows.size(); i++){

			Map<String, String> row = (Map<String, String>) dataRows.get(i);

			JSONObject jo = new JSONObject();

			for (int j = 0; j<listColumns.size(); j++){

				Object x = row.get(listColumns.get(j));

				jo.put(colMapper.get(listColumns.get(j)), x);

			}

			res.put(jo);

		}

		return res;

	}

	public Map getData(List<Object> dataRows,Object serie,Object columnsNeeded,Object dataColumnsMapper){

		Map<String,String> mapper = (Map<String,String>)dataColumnsMapper;

		Map<String,String> columns = (Map<String,String>)columnsNeeded;

		Object serieRawColumn = mapper.get(serie.toString()+"_SUM");

		ArrayList<String> listColumns = new ArrayList<String>();

		HashMap<Integer,HashMap> firstresult = new HashMap<Integer, HashMap>();

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

			firstresult.put(new Integer(i),record);			
		}

		return firstresult;

	}

	public JSONArray getDateResult(Map<Integer,HashMap> firstresult, Object column) throws ParseException{
		
		JSONArray dateResult = new JSONArray();

		ArrayList<Date> dates = new ArrayList<Date>();
		
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

		for (int i=0; i<firstresult.size();i++){

			Date date = df.parse(firstresult.get(i).get(column.toString()).toString());

			dates.add(date);

		}
		Date minDate = dates.get(0);

		Date maxDate = dates.get(0);

		for (int i =0; i<dates.size();i++){

			if (dates.get(i).getTime()<minDate.getTime()){

				minDate = dates.get(i);

			}
			if (dates.get(i).getTime()>maxDate.getTime()){

				maxDate = dates.get(i);

			}

		}
		
		String minDate1 = df.format(minDate);
		
		String maxDate1 = df.format(maxDate);

		dateResult.put(minDate1);

		dateResult.put(maxDate1);

		return dateResult;

	}

	public JSONArray getStoreResult(Map<Integer,HashMap> firstresult, Object column){

		JSONArray storeResult = new JSONArray();

		HashMap<Integer, String> storeResultMap = new HashMap<Integer, String>();

		int value = 0;

		for (int i =0; i<firstresult.size();i++){

			if (!storeResultMap.containsValue(firstresult.get(i).get(column.toString()))){

				storeResultMap.put(value, (String) (firstresult.get(i).get(column.toString())));

				value++;

			}

		}

		for (int i=0; i<storeResultMap.size();i++){

			storeResult.put(storeResultMap.get(i));

		}

		return storeResult;

	}
	
	public JSONArray getResult(Map<Integer,HashMap> firstresult, Object serie, HashMap<String, String> columns) throws JSONException, ParseException{
		
		JSONArray result = new JSONArray();
		
		for (int i =0; i<firstresult.size();i++){
			
			JSONObject jo = new JSONObject();

			Double serieValue =Double.valueOf((String) firstresult.get(i).get(serie.toString()));
			
			jo.put(serie.toString(), serieValue);
			
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			
			for (int j=0;j<columns.size();j++){
				
				Date date = df.parse(firstresult.get(i).get(columns.get(0)).toString());
				
				String trueDate = df.format(date);
				
				jo.put(columns.get(j).toString(), trueDate);
				
				String value = (String) firstresult.get(i).get(columns.get(1).toString());
				
				jo.put(columns.get(1).toString(), value);
				
			}
			
			result.put(jo);
		}
		
		return result;
		
	}
	
	public JSONObject getSerieName(Object serie) throws JSONException{
		
		JSONObject jo = new JSONObject();
		
		jo.put("value", serie);
		
		return jo;
		
	}
	
	public JSONArray getColumnNames(Map columns) throws JSONException{
		
		JSONArray ja = new JSONArray();
		
		for (int i=0;i<columns.size();i++){
			
			JSONObject jo = new JSONObject();
			
			jo.put("value", columns.get(i));
			
			ja.put(jo);
			
		}
		
		return ja;
		
	}

}
