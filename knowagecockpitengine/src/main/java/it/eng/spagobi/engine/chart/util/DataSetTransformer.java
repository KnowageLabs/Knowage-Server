/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.engine.chart.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.emory.mathcs.backport.java.util.Collections;
import it.eng.knowage.engine.cockpit.CockpitEngineRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

public class DataSetTransformer {

	public static transient Logger logger = Logger.getLogger(DataSetTransformer.class);

	public void print(Object object) {

		System.out.println("-----------------------");
		System.out.println(object);
		if (object != null)
			System.out.println(object.getClass().toString());
		System.out.println("-----------------------");

		logger.debug("-----------------------");
		logger.debug(object);
		if (object != null)
			logger.debug(object.getClass().toString());
		logger.debug("-----------------------");

	}

	public JSONArray toWordcloud(Object columnsNeeded, Object dataColumnsMapper, List<Object> dataRows, Object serie, Object sizeCriteria,
			Object groupingFunction) throws JSONException {

		Map<String, String> mapper = (Map<String, String>) dataColumnsMapper;

		Map<String, String> columns = (Map<String, String>) columnsNeeded;
		Object serieRawColumn = null;
		if (serie != null) {
			serieRawColumn = mapper.get(serie.toString() + "_" + groupingFunction.toString());
		}
		ArrayList<String> listColumns = new ArrayList<>();

		HashMap<Integer, HashMap> result = new HashMap<>();

		for (int i = 0; i < columns.size(); i++) {

			Object cndata = columns.get(i);

			listColumns.add(mapper.get(cndata).toString());

		}

		String categoryIdColumn = null;
		// get the name of the category id if exist
		Iterator<String> it = mapper.values().iterator();
		while (it.hasNext()) {
			String string = it.next();
			if (!listColumns.contains(string) && string != serieRawColumn) {
				categoryIdColumn = string;
				break;
			}
		}

		for (int i = 0; i < dataRows.size(); i++) {
			Map<String, Object> row = (Map<String, Object>) dataRows.get(i);
			HashMap<String, String> record = new HashMap<>();

			/* For every record take these columns */
			for (int j = 0; j < listColumns.size(); j++) {
				Object x = row.get(listColumns.get(j));
				record.put(columns.get(j).toString(), x.toString());
			}

			if (categoryIdColumn != null) {
				record.put("category_id_value", row.get(categoryIdColumn).toString());
			}

			if (serie != null) {

				record.put(serie.toString(), row.get(serieRawColumn).toString());

				// Added for the purposes of the cross-navigation execution.
				// (author: danristo)
				record.put("seriesItemName", serie.toString());

			}

			result.put(new Integer(i), record);
		}

		JSONArray res = toWordcloudArray(columns, serie, result, sizeCriteria);

		return res;

	}

	private JSONArray toWordcloudArray(Map<String, String> columns, Object serie, HashMap<Integer, HashMap> result, Object sizeCriteria) throws JSONException {

		JSONArray fr = new JSONArray();

		HashMap<String, String> categoryToId = new HashMap<>();

		HashMap<String, Map<String, Double>> res = new HashMap<>();

		for (int i = 0; i < result.size(); i++) {

			for (int j = 0; j < columns.size(); j++) {

				if (!res.containsKey(result.get(i).get(columns.get(j)))) {

					String name = (String) result.get(i).get(columns.get(j));
					HashMap<String, Double> valueMap = new HashMap<>();
					String categoryName = columns.get(j);
					Double value = 0.00;

					if (sizeCriteria.toString().equals("serie")) {

						value = value + Double.parseDouble(result.get(i).get(serie).toString());

					} else if (sizeCriteria.toString().equals("occurrences")) {

						value++;

					} else {
						value = value + Double.parseDouble(result.get(i).get(serie).toString());
					}
					valueMap.put(categoryName, value);
					res.put(name, valueMap);
					categoryToId.put((String) result.get(i).get(categoryName), (String) result.get(i).get("category_id_value"));
				}

				else {

					String name = (String) result.get(i).get(columns.get(j));
					String categoryName = columns.get(j);
					// HashMap<String, Double> valueMap = new HashMap<>();
					HashMap<String, Double> oldValueMap = (HashMap<String, Double>) res.get(name);
					Double oldvalue = oldValueMap.get(categoryName);
					if (oldvalue == null) {
						oldvalue = 0.00;
					}
					Double newValue = 0.00;

					if (sizeCriteria.toString().equals("serie")) {

						Double value = Double.parseDouble(result.get(i).get(serie).toString());

						newValue = oldvalue + value;

					} else if (sizeCriteria.toString().equals("occurrences")) {

						newValue = oldvalue + 1;

					}
					oldValueMap.put(categoryName, newValue);
					res.remove(name);

					res.put(name, oldValueMap);
					categoryToId.put((String) result.get(i).get(categoryName), (String) result.get(i).get("category_id_value"));

				}

			}

		}

		Iterator it = res.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			HashMap<String, Double> pairValue = (HashMap<String, Double>) pair.getValue();

			JSONObject jo = new JSONObject();
			jo.put("name", pair.getKey());
			ArrayList<String> categoryNames = new ArrayList<>();
			Double value = 0.00;
			for (String category : pairValue.keySet()) {
				categoryNames.add(category);
				value += pairValue.get(category);

			}
			jo.put("categoryName", categoryNames);
			jo.put("value", value);

			// Added for the purposes of the cross-navigation execution.
			// (author: danristo)
			jo.put("seriesItemName", serie.toString());

			jo.put("categoriId", categoryToId.get(pair.getKey()));
			fr.put(jo);

		}

		return fr;
	}

	/**
	 * Method that serves for preparing the data that JS code will use for rendering the CHORD chart
	 *
	 * @param dataRows          List of objects that represent the result of the query based on the result of the dataset linked to the chart (document) and on
	 *                          the query formed upon the XML of the document (VALUES tag, particularly CATEGORY and SERIE subtags)
	 * @param columnsNeeded     Categories (columns of the resulting table) that are needed by the request formulated through the XML tag CATEGORY
	 * @param serie             Column of the resulting table that is specified as the SERIE column through the XML tag SERIE
	 * @param dataColumnsMapper Mapping between the name of the columns (categories and serie) and their ordinal (raw value: column_1, column_2, ...)
	 * @throws JSONException
	 *
	 * @author Danilo Ristovski (danilo.ristovski@mht.net)
	 */
	public JSONObject toMatrix(List<Object> dataRows, Object columnsNeeded, Object serie, Object dataColumnsMapper, Object groupingFunction)
			throws JSONException {

		Map<String, Object> categories = (Map<String, Object>) columnsNeeded;
		Map<String, Object> columnsMapper = (Map<String, Object>) dataColumnsMapper;

		String aggregationType = groupingFunction.toString();

		/**
		 * List of raw names of the columns (their ordinal) that are specified as categories (in order defined by the XML template).
		 */
		ArrayList<String> categoriesColumnNames = new ArrayList<>();

		// dataColumnsMapper => {CUSTOMER_CITY=column_1, STORE_CITY=column_2,
		// VALUE_SUM=column_3}

		for (int i = 0; i < categories.size(); i++) {
			categoriesColumnNames.add((String) columnsMapper.get(categories.get(i)));
		}

		String rawColumnNameRow = categoriesColumnNames.get(0); // => column_1
		String rawColumnNameColumn = categoriesColumnNames.get(1); // =>
																	// column_2

		/**
		 * Take all columns of the future perfect matrix (same values in the same order both in rows and columns of the matrix). These are the target items
		 * (items towards which source items (rows of the matrix) are going. This variable serves as a container of all columns and rows at the same time (since
		 * the matrix must be perfect - all items in its rows and columns must be the same and in the same order).
		 */
		ArrayList<String> allColumns = new ArrayList<>();

		for (int i = 0; i < dataRows.size(); i++) {

			/**
			 * Take each record from the 'dataRows' parameter, i.e. each record from the dataset and put it inside the local (temporary) 'records' variable.
			 */
			Map<String, Object> record = (Map<String, Object>) dataRows.get(i);

			if (!allColumns.contains(record.get(rawColumnNameColumn))) {
				allColumns.add((String) record.get(rawColumnNameColumn));
			}

		}

		/**
		 * All columns (and rows) are now arranged in the ascending order so we can have (i,i) mapping between columns and rows inside the matrix with the same
		 * name (as said here: https://github.com/mbostock/d3/wiki/Chord-Layout, under "# chord.matrix([matrix])" header of the text).
		 */
		Collections.sort(allColumns);

		/**
		 * Create an unordered map of maps that will only contain data about pairs of row and columns available in existing data obtained when executing the not
		 * pivoted dataset. Later we are going to create a matrix in the form of the JSON object, so we will extend the second dimension of this structure (add
		 * columns for which rows we do not have corresponding data and fill it with value of zero - form a complete matrix).
		 */

		Map<String, HashMap<String, Float>> availableDataMapOfMaps = new HashMap<>();

		for (int i = 0; i < dataRows.size(); i++) {

			/**
			 * Current record (row) from the map of maps of available data (primitive (not pivoted) dataset).
			 */
			Map<String, Object> record = (Map<String, Object>) dataRows.get(i);

			/**
			 * Value (name) of the current record's row from the map of maps. Current row of the matrix.
			 */
			String currentRow = (String) record.get(rawColumnNameRow);

			/**
			 * Value (name) of the current record's column from the map of maps. Current column of the matrix.
			 */
			String currentColumn = (String) record.get(rawColumnNameColumn);

			/**
			 * Put a new map for the row that is not contained by the map of maps.
			 */
			if (!availableDataMapOfMaps.containsKey(currentRow)) {

				HashMap<String, Float> submapWithNewColumn = new HashMap<>();

				if (record.get(columnsMapper.get(serie + "_" + aggregationType)).getClass().toString().equals("class java.lang.Integer")) {

					/**
					 * Serie value for the current row and column (their intersection - value inside the intersection of the future matrix's current row and
					 * current column (values in variables of an appropriate name in this code.
					 *
					 * NOTE: The same goes for other variables of the same name ('serieValueForXOfRowAndColumn') in the code afterwards.
					 */
					Integer serieValueForXOfRowAndColumn = (int) record.get(columnsMapper.get(serie + "_" + aggregationType));
					submapWithNewColumn.put(currentColumn, Float.parseFloat(Integer.toString(serieValueForXOfRowAndColumn)));

				} else {

					String serieValueForXOfRowAndColumn = (record.get(columnsMapper.get(serie + "_" + aggregationType))).toString();
					submapWithNewColumn.put(currentColumn, Float.parseFloat(serieValueForXOfRowAndColumn));
				}

				availableDataMapOfMaps.put(currentRow, submapWithNewColumn);

			} else {

				if (record.get(columnsMapper.get(serie + "_" + aggregationType)).getClass().toString().equals("class java.lang.Integer")) {

					Integer serieValueForXOfRowAndColumn = (int) record.get(columnsMapper.get(serie + "_" + aggregationType));
					availableDataMapOfMaps.get(currentRow).put(currentColumn, Float.parseFloat(Integer.toString(serieValueForXOfRowAndColumn)));

				} else {

					String serieValueForXOfRowAndColumn = (record.get(columnsMapper.get(serie + "_" + aggregationType))).toString();
					availableDataMapOfMaps.get(currentRow).put(currentColumn, Float.parseFloat(serieValueForXOfRowAndColumn));

				}

			}

		}

		/**
		 * The final JSON object (it will be sent towards the client - rendering part).
		 */
		JSONObject finalJsonResult = new JSONObject();

		/**
		 * JSON object for collecting the data (labels with their values and JSON array) for meta data ('root' label and 'fields' JSON array) connected to the
		 * final data obtained from the dataset.
		 */
		JSONObject jsonObjectMetadata = new JSONObject();

		/**
		 * 'root' label tells us what is the name of the label inside the final JSON that contains the data (values for rendering the chart)
		 */
		jsonObjectMetadata.put("root", "rows");

		/**
		 * JSON array under the 'fields' label of the 'metaData' label that will contain information about all columns of the matrix that we need to create so
		 * to deliver it to the rendering (client) side. It will contain information about all columns (name and their ordinal value) in alphabetically
		 * ascending order.
		 */
		JSONArray jsonArrayMetadataFields = new JSONArray();

		/**
		 * Ordinal value of the columns that are arranged in ascending order inside the 'allColumns'.
		 */
		int columnCounter = 1;

		/**
		 * Populate the 'jsonArrayMetadataFields' array with the fields and the data that will contain necessary data (name, data index and header) about the
		 * all the columns arranged in ascending order.
		 */
		for (int i = 0; i < allColumns.size(); i++) {

			JSONObject jsonObjectFields = new JSONObject(); // old name: jo2
			String columnOrder = "column_" + columnCounter;

			jsonObjectFields.put("name", columnOrder);
			jsonObjectFields.put("dataIndex", columnOrder);
			jsonObjectFields.put("header", allColumns.get(i));

			jsonArrayMetadataFields.put(jsonObjectFields);

			columnCounter++;

		}

		/**
		 * Join this JSON array under the 'fields' label of the 'metaData' label of the final JSON object. It serves as an informer of which fields are we
		 * expecting when data comes from the server-side and which rows and columns is the CHORD matrix going to possess.
		 */
		jsonObjectMetadata.put("fields", jsonArrayMetadataFields);

		/**
		 * Set the 'metaData' label of the final JSON object and link to it necessary data.
		 */
		finalJsonResult.put("metaData", jsonObjectMetadata);

		/**
		 * Label that contains the information about the total number of rows/column if the matrix that we are now going to create through the form of JSON
		 * object.
		 */
		finalJsonResult.put("results", availableDataMapOfMaps.size());

		/**
		 * JSON array that will contain JSON objects that are representing all rows with data about values of their intersection with the appropriate column
		 * pair and that are arranged in ascending order. This way we are extending dimension of current data, so we are going to have all (row,column) pairs
		 * with their dedicated appropriate values (as said here: https://github.com/mbostock/d3/wiki/Chord-Layout, under this header: "#
		 * chord.matrix([matrix])").
		 */
		JSONArray jsonArrayResultsRows = new JSONArray();

		/**
		 * Go through all rows sorted in alphabetically ascending order.
		 */
		for (int i = 0; i < allColumns.size(); i++) {

			/**
			 * For every new row create a JSON object that will contain values of intersection with all existing columns (previously arranged in alphabetically
			 * ascending order) of future matrix (in form of the final JSON object). This JSON object will be inserted into the JSON array of numerical results
			 * ('rows' label of the final JSON object). These data are going to be used by the client (JS, particularly D3) code that will render the CHORD
			 * chart.
			 */
			JSONObject jsonObjectRowWithAllItsColumns = new JSONObject();

			/**
			 * As zeroth column (label) set the name of the row to which we are going to link data (values) of the intersection of it with the particular
			 * column.
			 */
			jsonObjectRowWithAllItsColumns.put("column_0", allColumns.get(i));

			/**
			 * With this two-step for-loop we will have appropriate matrix - the (row,column) pairs will be in the form in which i-th row with some name will be
			 * of the same name as the i-th column (names of rows and columns on the main diagonal of the matrix will be the same). (as specified here:
			 * https://github.com/mbostock/d3/wiki/Chord-Layout)
			 */
			for (int j = 0; j < allColumns.size(); j++) {

				String columnOrder = "column_" + (j + 1);

				if (availableDataMapOfMaps.get(allColumns.get(i)) != null && availableDataMapOfMaps.get(allColumns.get(i)).get(allColumns.get(j)) != null) {

					jsonObjectRowWithAllItsColumns.put(columnOrder, availableDataMapOfMaps.get(allColumns.get(i)).get(allColumns.get(j)));

				} else {

					jsonObjectRowWithAllItsColumns.put(columnOrder, (float) 0);

				}

			}

			jsonArrayResultsRows.put(jsonObjectRowWithAllItsColumns);

		}

		/**
		 * Set the JSON array of all rows of the matrix (and their intersected values with all the columns) into the final JSON object and link it to the 'rows'
		 * label.
		 */
		finalJsonResult.put("rows", jsonArrayResultsRows);

		return finalJsonResult;
	}

	/* Merging codes - Sunburst */

	public JSONArray toTree(Object columnsNeeded, Object serie, Object dataColumnsMapper, List<Object> dataRows, Object groupingFunction) throws JSONException {
		// Data columns mapper (as map)
		Map<String, Object> mapper = (Map<String, Object>) dataColumnsMapper;

		// Columns that we need for making a sequence (as map)
		Map<String, Object> columns = (Map<String, Object>) columnsNeeded;

		/**
		 * In this array list we will put raw names (column_1, column_2 etc) of necessary columns
		 */
		ArrayList<String> listColumns = new ArrayList<>();

		// End result - map of maps (records with their columns values)
		HashMap<Integer, HashMap> result = new HashMap<>();

		// Take value of the SERIE column (the one that contains numerical
		// values)
		Object serieRawColumn = mapper.get(serie.toString() + "_" + groupingFunction.toString()).toString();

		/**
		 * Take raw names (column_1, column_2, etc) of all of the columns that we need for creating a sequence.
		 */
		for (int i = 0; i < columns.size(); i++) {
			Object z = columns.get(i);

			listColumns.add(mapper.get(z).toString());
		}

		/*
		 * Pass through all records in order to get values of just those columns that we need for sequence.
		 */
		for (int i = 0; i < dataRows.size(); i++) {
			Map<String, Object> row = (Map<String, Object>) dataRows.get(i);
			HashMap<String, String> record = new HashMap<>();

			/* For every record take these columns */

			for (int j = 0; j < listColumns.size(); j++) {
				Object x = row.get(listColumns.get(j));

				record.put(columns.get(j).toString(), x.toString());
				record.put(serie.toString(), row.get(serieRawColumn).toString());

				result.put(new Integer(i), record);
			}
		}

		JSONArray res = countSequence(columns, serie, result);

		return res;
	}

	public JSONArray countSequence(Map<String, Object> columns, Object serie, HashMap<Integer, HashMap> result) throws JSONException {
		HashMap<String, Double> endresult = new HashMap<>();
		JSONArray ja = new JSONArray();

		// Going through all records
		for (int i = 0; i < result.size(); i++) {
			HashMap<String, Object> singleRecord = result.get(new Integer(i));
			String sequence = "";

			// Columns that we now need for creating the sequence
			for (int j = 0; j < columns.size(); j++) {
				if (j == 0) {
					sequence = singleRecord.get(columns.get(j)).toString();
				}

				else {
					sequence = sequence + "_SEP_" + singleRecord.get(columns.get(j)).toString();
				}
			}

			Double value = Double.parseDouble(singleRecord.get(serie).toString());

			JSONObject jo = new JSONObject();

			if (!endresult.containsKey(sequence)) {
				endresult.put(sequence, value);

				jo.put("sequence", sequence);
				jo.put("value", value);
			} else {
				Double oldValue = endresult.get(sequence);
				endresult.put(sequence, value + oldValue);

				jo.put("sequence", sequence);
				jo.put("value", value + oldValue);
			}

			ja.put(jo);
		}

		return ja;
	}

	public JSONObject createTreeChart(Object columnsNeeded, Object serie, Object dataColumnsMapper, List<Object> dataRows, Object groupingFunction)
			throws JSONException {
		Map<String, String> mapper = (Map<String, String>) dataColumnsMapper;

		Map<String, String> columns = (Map<String, String>) columnsNeeded;

		Object serieRawColumn = mapper.get(serie.toString() + "_" + groupingFunction.toString());

		ArrayList<String> listColumns = new ArrayList<>();

		HashMap<Integer, HashMap> result = new HashMap<>();

		for (int i = 0; i < columns.size(); i++) {

			Object cndata = columns.get(i);

			listColumns.add(mapper.get(cndata).toString());

		}

		for (int i = 0; i < dataRows.size(); i++) {
			Map<String, Object> row = (Map<String, Object>) dataRows.get(i);
			HashMap<String, String> record = new HashMap<>();

			/* For every record take these columns */
			for (int j = 0; j < listColumns.size(); j++) {
				Object x = row.get(listColumns.get(j));
				record.put(columns.get(j).toString(), x.toString());
			}

			record.put(serie.toString(), row.get(serieRawColumn).toString());

			result.put(new Integer(i), record);
		}

		JSONObject res = createTreeMap(columns, serie, result);

		return res;

	}

	public JSONObject createTreeMap(Map<String, String> columns, Object serie, HashMap<Integer, HashMap> result) throws JSONException {

		JSONObject root = new JSONObject();

		JSONObject currentNode = null;

		for (int i = 0; i < result.size(); i++) {
			currentNode = root;
			for (int j = 0; j < columns.size(); j++) {

				String nodeName = "" + result.get(i).get(columns.get(j));
				JSONObject existingNodeValue = currentNode.optJSONObject(nodeName);

				if (existingNodeValue == null) {

					if (j != columns.size() - 1) {
						currentNode.put(nodeName, new JSONObject());
					} else {
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

	public String getGroupsForParallelChart(Object columnsNeeded, Object dataColumnsMapper, List<Object> dataRows) throws JSONException {

		JSONArray ja = new JSONArray();

		Map<String, String> columns = (Map<String, String>) columnsNeeded;

		Map<String, String> mapper = (Map<String, String>) dataColumnsMapper;

		String group = columns.get(0);

		String groupvalue = mapper.get(group);

		ArrayList<String> al = new ArrayList<>();

		int j = 0;

		for (int i = 0; i < dataRows.size(); i++) {

			Map<String, Object> row = (Map<String, Object>) dataRows.get(i);

			if (!al.contains(row.get(groupvalue))) {

				if (row.get(groupvalue).getClass().equals(Integer.class)) {
					al.add(row.get(groupvalue) + "");
				} else {
					al.add((String) row.get(groupvalue));
				}

				JSONObject jo = new JSONObject();
				String value = row.get(groupvalue).toString();
				value = value.replace("\\", "sepslashsep");
				value = value.replace("\"", "\\\"");
				value = value.replace("'", "sepquotesep");

				jo.put((new Integer(j)).toString(), value);
				ja.put(jo);
				j++;
			}
		}

		return ja.toString();

	}

	/**
	 * Method that serves for preparing the data that JS code will use for rendering the BAR chart when order by some column is needed
	 *
	 * @param dataRows List of objects that represent the result of the query based on the result of the dataset linked to the chart (document) and on the query
	 *                 formed upon the XML of the document (VALUES tag, particularly CATEGORY and SERIE subtags) author rselakov, radmila.selakovic@mht.net
	 *
	 */

	public LinkedHashMap<String, ArrayList<String>> prepareDataForGroupingForBar(List<Object> dataRows, String isCockpitEngine, String categorieColumnsMapped)
			throws JSONException {
		boolean isCockpit = Boolean.parseBoolean(isCockpitEngine);
		LinkedHashMap<String, ArrayList<String>> map = new LinkedHashMap<>();
		String primCat;
		String secCat;
		if (isCockpit) {
			primCat = "column_1";
			secCat = "column_2";
		} else {
			primCat = categorieColumnsMapped;
			secCat = "column_" + (Integer.parseInt(categorieColumnsMapped.substring(7)) + 1);
		}
		for (Object singleObject : dataRows) {
			Map mapObject = (Map) singleObject;
			if (mapObject.get(primCat) != null && !map.containsKey(mapObject.get(primCat).toString())) {
				ArrayList<String> newListOfOrderColumnItems = new ArrayList<>();
				newListOfOrderColumnItems.add(getStringOrNull(mapObject.get(secCat)));
				map.put(getStringOrNull(mapObject.get(primCat)), newListOfOrderColumnItems);
			} else {
				if (mapObject.get(primCat) != null) {
					ArrayList oldArrayList = map.get(mapObject.get(primCat).toString());
					oldArrayList.add(mapObject.get(secCat));
					map.put(getStringOrNull(mapObject.get(primCat)), oldArrayList);
				}
			}
		}

		return map;

	}

	public JSONObject prepareDataForOrderingColumnForBar(List<Object> dataRows, List<Object> metadataRows, Map<String, Object> drillOrder, String groupBy) {
		String orderColumn = "";
		if (drillOrder != null) {
			for (String key : drillOrder.keySet()) {
				LinkedHashMap<String, String> obj = (LinkedHashMap<String, String>) drillOrder.get(key);
				orderColumn = obj.get("orderColumn");
			}
		}
		String columnIndex = "";
		String groupByIndex = "";
		for (Object object : metadataRows) {
			if (object instanceof LinkedHashMap) {
				LinkedHashMap<String, String> obj = (LinkedHashMap<String, String>) object;
				String objHeader = obj.get("header");
				if (objHeader.equals(orderColumn)) {
					columnIndex = obj.get("dataIndex");
				}
				if (objHeader.equals(groupBy)) {
					groupByIndex = obj.get("dataIndex");
				}
			}
		}
		LinkedHashMap<String, String> pair = new LinkedHashMap<String, String>();
		JSONObject jsonPair = null;
		if (!columnIndex.equals("") && !groupByIndex.equals("")) {
			for (Object object : dataRows) {
				if (object instanceof LinkedHashMap) {
					LinkedHashMap<Object, Object> obj = (LinkedHashMap<Object, Object>) object;
					String value = "" + obj.get(columnIndex);
					String valueToEscape = "" + obj.get(groupByIndex);
					pair.put("" + valueToEscape, value);
				}
			}
			try {
				jsonPair = new JSONObject(pair);
			} catch (JSONException e) {
				throw new SpagoBIServiceException("Error while creating JSONObject for column ordering", e);
			}
		}

		return jsonPair;
	}

	public String escapeStringForJavascript(JSONObject stringToEscaPe) {
		return StringEscapeUtils.escapeEcmaScript(stringToEscaPe.toString());
	}

	public LinkedHashMap<String, ArrayList<JSONObject>> prepareDataForGrouping(List<Object> dataRows, String isCockpitEngine, String groupSeries,
			String groupSeriesCateg, Map<String, String> dataColumnsMapper, Map<String, String> categorieColumns, String groupedSerie,
			Map<String, Object> drillOrder) {

		boolean isCockpit = Boolean.parseBoolean(isCockpitEngine);
		boolean groupSeriesBool = Boolean.parseBoolean(groupSeries);
		ArrayList<Object> categories = new ArrayList<>();
		LinkedHashMap<String, ArrayList<JSONObject>> map = new LinkedHashMap<String, ArrayList<JSONObject>>();
		try {
			String columnForGroupingSerie = "";
			if (!groupSeriesBool) {
				columnForGroupingSerie = dataColumnsMapper.get(groupedSerie).toLowerCase();
			}

			removeOrderColumn(dataColumnsMapper, drillOrder, categorieColumns);
			String primCat;
			String secCat;
			String seria;
			if (!isCockpit) {
				if (groupSeriesBool) {
					primCat = "column_1";
					secCat = "column_2";
					seria = "column_3";
				} else {
					primCat = dataColumnsMapper.get(categorieColumns.get("column").toLowerCase());
					secCat = columnForGroupingSerie;
					seria = dataColumnsMapper.get(categorieColumns.get("groupby").toLowerCase());
				}
			} else {
				if (groupSeriesBool) {
					primCat = "column_2";
					secCat = "column_3";
					seria = "column_1";

				} else {
					primCat = "column_1";
					secCat = columnForGroupingSerie;
					seria = "column_2";

				}

			}
			logger.debug("primCat: " + primCat);
			logger.debug("secCat: " + secCat);
			logger.debug("seria: " + seria);
			for (Object singleObject : dataRows) {
				categories.add(((Map) singleObject).get(primCat));
			}

			Set<Object> categoriesSet = new LinkedHashSet<>(categories);
			Object[] categoriesList = categoriesSet.toArray(new Object[categoriesSet.size()]);

			Map<Object, Integer> categoriesListIndexMap = new HashMap<Object, Integer>();
			for (int i = 0; i < categoriesList.length; i++) {
				categoriesListIndexMap.put(categoriesList[i], i);
			}

			for (String key : dataColumnsMapper.keySet()) {
				String newCol = "";

				String serieValue = "";
				for (Object singleObject : dataRows) {
					if (!dataColumnsMapper.get(key).equals(primCat) && !dataColumnsMapper.get(key).equals(secCat)) {

						if (!dataColumnsMapper.get(key).equals(seria)) {
							newCol = key;
							serieValue = ((Map) singleObject).get(dataColumnsMapper.get(key)).toString();
						} else {

							newCol = ((Map) singleObject).get(seria).toString();
							serieValue = ((Map) singleObject).get(secCat).toString();

						}

					} else {
						continue;
					}
					ArrayList<JSONObject> newListOfOrderColumnItems = map.get(newCol);
					if (newListOfOrderColumnItems == null) {
						newListOfOrderColumnItems = new ArrayList<JSONObject>();
						for (int i = 0; i < categoriesList.length; i++) {
							Object category = categoriesList[i];
							JSONObject jo = new JSONObject();
							jo.put("name", category);
							jo.put("y", "");
							newListOfOrderColumnItems.add(jo);
						}
						map.put(newCol, newListOfOrderColumnItems);
					}

					JSONObject jo = newListOfOrderColumnItems.get(categoriesListIndexMap.get(((Map) singleObject).get(primCat)));
					jo.put("y", serieValue);

				}

			}

			logger.debug("map: " + map);

		} catch (Exception e) {
			throw new CockpitEngineRuntimeException("Cannot group data", e);
		}
		return map;
	}

	/**
	 * @param dataColumnsMapper
	 * @param drillOrder
	 * @param categorieColumns
	 */
	private void removeOrderColumn(Map<String, String> dataColumnsMapper, Map<String, Object> drillOrder, Map<String, String> categorieColumns) {
		if (drillOrder != null) {
			for (String key : drillOrder.keySet()) {
				Map<String, String> keyMapper = (Map<String, String>) drillOrder.get(key);
				if (keyMapper.get("orderColumn") != null && !keyMapper.get("orderColumn").equals("")
						&& !keyMapper.get("orderColumn").equals(categorieColumns.get("column")) && !drillOrder.containsKey(keyMapper.get("orderColumn"))) {
					dataColumnsMapper.remove(keyMapper.get("orderColumn").toLowerCase());
				}
			}
		} else {
			if (categorieColumns.get("orderColumn") != null && !categorieColumns.get("orderColumn").equals("")
					&& !categorieColumns.get("orderColumn").equals(categorieColumns.get("column"))
					&& !categorieColumns.get("groupby").contains(categorieColumns.get("orderColumn"))) {
				dataColumnsMapper.remove(categorieColumns.get("orderColumn").toLowerCase());
			}
		}
	}

	public LinkedHashMap<String, ArrayList<JSONObject>> prepareDataForGroupingBubble(List<Object> dataRows, Map<String, String> dataColumnsMapper,
			Map<String, String> categorieColumns, String groupedSerie, String serieForZAxis, String serieForXAxis, String coloredCategory) {

		ArrayList<Object> categories = new ArrayList<>();
		LinkedHashMap<String, ArrayList<JSONObject>> map = new LinkedHashMap<String, ArrayList<JSONObject>>();
		try {
			String columnForGroupingSerie = dataColumnsMapper.get(groupedSerie).toLowerCase();

			if (!categorieColumns.get("orderColumn").equals("") && !categorieColumns.get("orderColumn").equals(categorieColumns.get("column"))
					&& !categorieColumns.get("groupby").contains(categorieColumns.get("orderColumn"))) {
				dataColumnsMapper.remove(categorieColumns.get("orderColumn").toLowerCase());
			}

			String primCateg = categorieColumns.get("column");
			String primColumn = dataColumnsMapper.get((categorieColumns).get("column"));
			String seriaColumn = null;
			String seria = null;
			if (categorieColumns.get("groupby") != null && categorieColumns.get("groupby") != "") {
				seriaColumn = dataColumnsMapper.get(categorieColumns.get("groupby"));
				seria = categorieColumns.get("groupby");
			} else {
				seriaColumn = primColumn;
				seria = primCateg;
			}
			if (!coloredCategory.equals("") && coloredCategory.equals(primCateg)) {
				String temp = seria;
				String tempColumn = seriaColumn;
				seriaColumn = primColumn;
				seria = primCateg;
				primCateg = temp;
				primColumn = tempColumn;
			}

			String secCat = columnForGroupingSerie;

			String z = dataColumnsMapper.get(serieForZAxis);
			String x = dataColumnsMapper.get(serieForXAxis);

			logger.debug("primCat: " + primColumn);
			logger.debug("secCat: " + secCat);
			logger.debug("seria: " + seriaColumn);

			for (Object singleObject : dataRows) {
				categories.add(((Map) singleObject).get(primColumn));
			}

			Set<Object> categoriesSet = new LinkedHashSet<>(categories);// bez duplikata
			Object[] categoriesList = categoriesSet.toArray(new Object[categoriesSet.size()]);

			Map<Object, Integer> categoriesListIndexMap = new HashMap<Object, Integer>(); // canada 0 maxico 1 usa 2
			for (int i = 0; i < categoriesList.length; i++) {
				categoriesListIndexMap.put(categoriesList[i], i);
			}

			for (String key : dataColumnsMapper.keySet()) {
				String newCol = "";

				String serieValue = "";
				String zValue = "";
				String xValue = "";

				for (Object singleObject : dataRows) {
					if (key.equals(seria)) {
						newCol = ((Map) singleObject).get(seriaColumn).toString();
						serieValue = ((Map) singleObject).get(secCat).toString();
					} else if (isDifferent(key, primCateg) && isDifferent(key, serieForXAxis) && isDifferent(key, serieForZAxis)
							&& isDifferent(key, groupedSerie)) {

						newCol = key;
						serieValue = ((Map) singleObject).get(dataColumnsMapper.get(key)).toString();

					}

					if (!"".equals(newCol)) {
						ArrayList<JSONObject> newListOfOrderColumnItems = map.get(newCol);
						if (newListOfOrderColumnItems == null) {
							newListOfOrderColumnItems = new ArrayList<JSONObject>();
							for (int i = 0; i < categoriesList.length; i++) {
								Object category = categoriesList[i];
								JSONObject jo = new JSONObject();
								jo.put("name", category);
								jo.put("x", "");
								jo.put("z", "");
								jo.put("y", "");
								jo.put("tooltipConf", new JSONObject());

								newListOfOrderColumnItems.add(jo);
							}
							map.put(newCol, newListOfOrderColumnItems);
						}

						JSONObject jo = newListOfOrderColumnItems.get(categoriesListIndexMap.get(((Map) singleObject).get(primColumn)));
						jo.put("y", serieValue);
						jo.put("x", ((Map) singleObject).get(x).toString());
						jo.put("z", ((Map) singleObject).get(z).toString());
						JSONObject tooltipConf = new JSONObject();
						for (String column : dataColumnsMapper.keySet()) {
							tooltipConf.put(column, ((Map) singleObject).get(dataColumnsMapper.get(column)));
						}
						jo.put("tooltipConf", tooltipConf);

					}

				}

			}

			logger.debug("map: " + map);
		} catch (Exception e) {
			throw new CockpitEngineRuntimeException("Cannot group data", e);
		}
		return map;

	}

	/**
	 * @param seria
	 * @param x
	 * @param z
	 * @param groupedSerie
	 * @return
	 */
	private boolean isDifferent(String key, String seria) {
		if (!key.equals(seria))
			return true;
		return false;
	}

	/**
	 * Method that serves for preparing the data that JS code will use for rendering the SCATTER chart
	 *
	 * @param dataRows        List of objects that represent the result of the query based on the result of the dataset linked to the chart (document) and on
	 *                        the query formed upon the XML of the document (VALUES tag, particularly CATEGORY and SERIE subtags)
	 * @param columnCategorie column_x that is category
	 * @param columnSerie     column_y,z that is serie
	 * @author rselakov, radmila.selakovic@mht.net
	 *
	 */

	public LinkedHashMap<String, Set<ArrayList<Object>>> prepareDataForScater(List<Object> dataRows, String columnCategorie, String isCockpitEngine,
			String columnSerie) {
		LinkedHashMap<String, Set<ArrayList<Object>>> map = new LinkedHashMap<>();
		try {
			boolean isCockpit = Boolean.parseBoolean(isCockpitEngine);
			Map<String, Integer> mapOfIndex = new HashMap<>();
			String columnX = !isCockpit ? "column_1" : "column_2";
			for (Object singleObject : dataRows) {
				Map mapObject = (Map) singleObject;
				if (!map.containsKey(mapObject.get(columnCategorie))) {
					Set<ArrayList<Object>> a = new HashSet<>();
					ArrayList<Object> t = new ArrayList<>();
					if (Number.class.isAssignableFrom(mapObject.get(columnCategorie).getClass())) {
						t.add(mapObject.get(columnCategorie));
					} else {
						t.add("'" + mapObject.get(columnCategorie) + "'");
					}
					if (columnSerie == null) {
						t.add(getStringOrNull(mapObject.get(columnX)));
					} else {
						t.add(getStringOrNull(mapObject.get(columnSerie)));
					}

					a.add(t);
					mapOfIndex.put(getStringOrNull(mapObject.get(columnCategorie)), map.entrySet().size());
					map.put(getStringOrNull(mapObject.get(columnCategorie)), a);
				} else {
					if (mapOfIndex.containsKey(mapObject.get(columnCategorie))) {
						ArrayList<Object> a = new ArrayList<>();
						if (Number.class.isAssignableFrom(mapObject.get(columnCategorie).getClass())) {
							a.add(mapOfIndex.get(mapObject.get(columnCategorie)));
						} else {
							a.add(mapOfIndex.get("'" + mapObject.get(columnCategorie) + "'"));
						}
						if (columnSerie == null) {
							a.add(getStringOrNull(mapObject.get(columnX)));
						} else {
							a.add(getStringOrNull(mapObject.get(columnSerie)));
						}

						Set<ArrayList<Object>> valueOfMap = map.get(mapObject.get(columnCategorie));
						valueOfMap.add(a);
					}
				}

			}
		} catch (Exception e) {
			throw new CockpitEngineRuntimeException("Cannot group data", e);
		}
		return map;

	}

	public ArrayList<JSONObject> getXAxisMap(LinkedHashMap<String, String> category, String categoryDate) {
		ArrayList<JSONObject> xAxisMap = new ArrayList<>();
		String groupBys = category.get("groupby");
		String[] gbys = groupBys.split(", ");

		try {
			int id = 0;
			JSONObject xAxis = new JSONObject();
			xAxis.put("id", id);
			if (category.get("name").equals(categoryDate)) {
				xAxis.put("type", "datetime");
				id++;
			} else {
				xAxis.put("type", "category");
				id++;
			}
			xAxis.put("name", category.get("name"));
			xAxisMap.add(xAxis);
			for (int i = 0; i < gbys.length; i++) {
				if (!gbys[i].equals("")) {
					JSONObject xAxis1 = new JSONObject();
					xAxis1.put("id", id);
					if (gbys[i].equals(categoryDate)) {
						xAxis1.put("type", "datetime");
						id++;
					} else {
						xAxis1.put("type", "category");
						id++;
					}
					xAxis1.put("name", gbys[i]);
					xAxisMap.add(xAxis1);
				}

			}
		} catch (JSONException e) {
			throw new SpagoBIServiceException("Error while creating xaxis map", e.getMessage(), e);
		}

		return xAxisMap;
	}

	public LinkedHashMap<String, LinkedHashMap> seriesMapTransformedMethod(LinkedHashMap<String, LinkedHashMap> serieMap) throws JSONException {
		LinkedHashMap<String, LinkedHashMap> newSerieMap = new LinkedHashMap<>();
		String serieName = "";
		int counter = 1;
		for (Map.Entry<String, LinkedHashMap> entry : serieMap.entrySet()) {

			String key = entry.getKey();
			LinkedHashMap value = entry.getValue();
			if (value.get("type").equals("arearangelow") || value.get("type").equals("arearangehigh")) {
				if (counter == 1) {
					serieName += value.get("column") + " / ";
					counter += 1;
				} else {
					serieName += value.get("column") + " ";
				}
				value.put("type", "arearange");

				value.put("column", serieName);
				value.put("name", serieName);
				key = "common";

				newSerieMap.put(key, value);

			} else {
				newSerieMap.put(key, value);
			}

		}
		return newSerieMap;

	}

	public String seriesRangeName(LinkedHashMap<String, LinkedHashMap> serieMap) throws JSONException {
		String serieName = "";
		for (Map.Entry<String, LinkedHashMap> entry : serieMap.entrySet()) {
			LinkedHashMap value = entry.getValue();
			if (value.get("type").equals("arearangelow") || value.get("type").equals("arearangehigh")) {
				serieName += value.get("column") + " ";
			}
		}
		return serieName;
	}

	/**
	 * @param serieScaleFactor The scaling factor of the current series item can be empty (no scaling - pure (original) value) or "k" (kilo), "M" (mega), "G"
	 *                         (giga), "T" (tera), "P" (peta), "E" (exa). That means we will scale our values according to this factor and display these
	 *                         abbreviations (number suffix) along with the scaled number.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	public JSONArray getSeriesForParallelChart(Object serieNeeded, Object groupingFunction, Object seriePrefix, Object seriePostfix, Object seriePrecision,
			Object serieScaleFactor) throws JSONException {

		JSONArray ja = new JSONArray();

		Map<String, String> series = (Map<String, String>) serieNeeded;
		Map<String, String> groupings = (Map<String, String>) groupingFunction;
		Map<String, String> prefix = (Map<String, String>) seriePrefix;
		Map<String, String> postfix = (Map<String, String>) seriePostfix;
		Map<String, String> precision = (Map<String, String>) seriePrecision;
		Map<String, String> scaleFactor = (Map<String, String>) serieScaleFactor;

		ArrayList<String> al = new ArrayList<>();

		int j = 0;

		for (int i = 0; i < series.size(); i++) {

			if (!al.contains(series.get(i))) {

				al.add(series.get(i) + "_" + groupings.get(i).toLowerCase());
				JSONObject jo = new JSONObject();
				jo.put((new Integer(j).toString()), series.get(i));
				jo.put("prefix", prefix.get(i));
				jo.put("postfix", postfix.get(i));

				if (precision.get(i).getClass().equals(String.class) && precision.get(i).equals("")) {
					jo.put("precision", 0);
				} else {
					jo.put("precision", new Integer(precision.get(i)));
				}

				// @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
				// [JIRA 1060 and 1061]
				jo.put("scaleFactor", scaleFactor.get(i));

				ja.put(jo);
				j++;
			}

		}

		return ja;

	}

	public JSONArray getColorPallete(Object colorsRequired) throws JSONException {

		JSONArray ja = new JSONArray();

		/**
		 * If we receive colors from the velocity model of the PARALLEL chart with the ArrayList structure, then we are dealing with the single color that is
		 * going to be presented on the chart (as color of chart lines). That single color can be result of the situation in which user (1) did not specify any
		 * color in the Designer or (2) he specified single color (just one). In case (1) VM is sets a single default color value for lines on the chart,
		 * otherwise it takes the single one that user provided on the Designer.
		 *
		 * @author: danristo (danilo.ristovski@mht.net)
		 */
		if (colorsRequired.getClass().equals(java.util.ArrayList.class)) {

			ArrayList<String> colorsReq = (ArrayList<String>) colorsRequired;

			if (colorsReq.size() == 1) {

				JSONObject jo = new JSONObject();
				jo.put((new Integer(0).toString()), colorsReq.get(0));
				ja.put(jo);

			}
		}

		/**
		 * If we receive colors from the velocity model of the PARALLEL chart with the LinkedHashMap structure, then we are dealing with the collection of
		 * colors that are going to be presented on the chart (as colors of chart lines).
		 *
		 * @commentedBy: danristo (danilo.ristovski@mht.net)
		 */
		else {

			Map<String, String> colorsReq = (Map<String, String>) colorsRequired;

			ArrayList<String> al = new ArrayList<>();

			int j = 0;

			for (int i = 0; i < colorsReq.size(); i++) {

				if (!al.contains(colorsReq.get(i))) {

					al.add(colorsReq.get(i));
					JSONObject jo = new JSONObject();
					jo.put((new Integer(j).toString()), colorsReq.get(i));
					ja.put(jo);
					j++;
				}

			}
		}

		return ja;

	}

	public JSONArray toParallelChart(Object columnsNeeded, Object dataColumnsMapper, List<Object> dataRows, Object serieNeeded, Object groupingFunction)
			throws JSONException {

		JSONArray res = new JSONArray();

		Map<String, String> mapper = (Map<String, String>) dataColumnsMapper;
		Map<String, String> columns = (Map<String, String>) columnsNeeded;
		Map<String, String> series = (Map<String, String>) serieNeeded;
		Map<String, String> groupings = (Map<String, String>) groupingFunction;
		Map<String, String> colMapper = new HashMap<>();
		ArrayList<String> listColumns = new ArrayList<>();

		for (int i = 0; i < series.size(); i++) {

			Object serie = series.get(i) + "_" + groupings.get(i).toLowerCase();

			listColumns.add(mapper.get(serie));

			colMapper.put(mapper.get(serie), series.get(i));

		}

		for (int i = 0; i < columns.size(); i++) {

			Object column = columns.get(i);
			listColumns.add(mapper.get(column));

			colMapper.put(mapper.get(column), columns.get(i).toString());

		}

		for (int i = 0; i < dataRows.size(); i++) {

			Map<String, String> row = (Map<String, String>) dataRows.get(i);

			JSONObject jo = new JSONObject();

			for (int j = 0; j < listColumns.size(); j++) {

				Object x = row.get(listColumns.get(j));

				/*
				 * iif(x instanceof String){ String xS = (String)x; x= xS.replace("'", "\'"); }
				 *
				 * f(x instanceof String){ String xS = (String)x; x= xS.replace("\"", "\\\""); }
				 */

				jo.put(colMapper.get(listColumns.get(j)), x);
			}

			res.put(jo);

		}

		return res;

	}

	public Map getData(List<Object> dataRows, Object serie, Object columnsNeeded, Object dataColumnsMapper, Object groupingFunction) {

		Map<String, String> mapper = (Map<String, String>) dataColumnsMapper;

		Map<String, String> columns = (Map<String, String>) columnsNeeded;

		Object serieRawColumn = mapper.get(serie.toString() + "_" + groupingFunction.toString());

		ArrayList<String> listColumns = new ArrayList<>();

		HashMap<Integer, HashMap> firstresult = new HashMap<>();

		for (int i = 0; i < columns.size(); i++) {

			Object cndata = columns.get(i);

			if (mapper.get(cndata) != null)
				listColumns.add(mapper.get(cndata).toString());

		}

		for (int i = 0; i < dataRows.size(); i++) {
			Map<String, Object> row = (Map<String, Object>) dataRows.get(i);
			HashMap<String, Object> record = new HashMap<>();

			/* For every record take these columns */
			for (int j = 0; j < listColumns.size(); j++) {
				Object x = row.get(listColumns.get(j));
				record.put(columns.get(j).toString(), x);
			}

			record.put(serie.toString(), row.get(serieRawColumn));

			firstresult.put(new Integer(i), record);
		}

		return firstresult;

	}

	public boolean isFirstCategoryDate(Map<Integer, HashMap> firstresult, Object column) {
		boolean isDate = false;
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

		/**
		 * -------------------------------------------------------------------- ---------------------- KNOWAGE-778 ( "COCKPIT-Chart: heatmap hangs in loading
		 * when the selections return no data" ) ------------------------------------------------------------------ ------------------------ If there is no data
		 * in the "firstresult" (if it is an empty map), return the false boolean value to the VM. The first category (that does not exist) is definitely not of
		 * type DATE.
		 *
		 * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		try {

			if (firstresult.isEmpty()) {

				isDate = false;
				return isDate;

			} else {

				Date date = df.parse(firstresult.get(0).get(column.toString()).toString());
				isDate = true;
				return isDate;

			}

		} catch (ParseException e) {

			isDate = false;
			return isDate;

		}

	}

	public JSONArray getDateResult(Map<Integer, HashMap> firstresult, Object column) throws ParseException {

		JSONArray dateResult = new JSONArray();

		ArrayList<Date> dates = new ArrayList<>();

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

		for (int i = 0; i < firstresult.size(); i++) {

			Date date = df.parse(firstresult.get(i).get(column.toString()).toString());

			dates.add(date);

		}
		Date minDate = dates.get(0);

		Date maxDate = dates.get(0);

		for (int i = 0; i < dates.size(); i++) {

			if (dates.get(i).getTime() < minDate.getTime()) {

				minDate = dates.get(i);

			}
			if (dates.get(i).getTime() > maxDate.getTime()) {

				maxDate = dates.get(i);

			}

		}

		String minDate1 = df.format(minDate);

		String maxDate1 = df.format(maxDate);

		dateResult.put(minDate1);

		dateResult.put(maxDate1);

		return dateResult;

	}

	public JSONArray getStoreResult(Map<Integer, HashMap> firstresult, Object column) {

		JSONArray storeResult = new JSONArray();

		HashMap<Integer, Object> storeResultMap = new HashMap<>();

		int value = 0;

		for (int i = 0; i < firstresult.size(); i++) {

			if (!storeResultMap.containsValue(firstresult.get(i).get(column.toString()))) {

				storeResultMap.put(value, (firstresult.get(i).get(column.toString())));

				value++;

			}

		}

		for (int i = 0; i < storeResultMap.size(); i++) {

			storeResult.put(storeResultMap.get(i));

		}

		return storeResult;

	}

	public JSONArray getResult(Map<Integer, HashMap> firstresult, Object serie, HashMap<String, String> columns, boolean isDate)
			throws JSONException, ParseException {

		JSONArray result = new JSONArray();

		for (int i = 0; i < firstresult.size(); i++) {

			JSONObject jo = new JSONObject();

			if (!firstresult.get(i).get(serie).toString().isEmpty()) {

				Double serieValue = Double.valueOf(firstresult.get(i).get(serie).toString());

				jo.put(serie.toString(), serieValue);

				DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

				for (int j = 0; j < columns.size(); j++) {
					if (isDate) {
						Date date = df.parse(firstresult.get(i).get(columns.get(0)).toString());
						String trueDate = df.format(date);
						jo.put(columns.get(j).toString(), trueDate);
					} else {
						String value = (String) firstresult.get(i).get(columns.get(0).toString());

						jo.put(columns.get(0).toString(), value);
					}

					String value = (String) firstresult.get(i).get(columns.get(1).toString());

					jo.put(columns.get(1).toString(), value);

				}

				result.put(jo);
			}
		}

		return result;

	}

	public JSONObject getSerieName(Object serie) throws JSONException {

		JSONObject jo = new JSONObject();

		jo.put("value", serie);

		return jo;

	}

	public JSONArray getColumnNames(Map columns) throws JSONException {

		JSONArray ja = new JSONArray();

		for (int i = 0; i < columns.size(); i++) {

			JSONObject jo = new JSONObject();

			jo.put("value", columns.get(i));

			ja.put(jo);

		}

		return ja;

	}

	public String getStringOrNull(Object item) {
		return item != null ? item.toString() : null;
	}

	String[] arrayOfGroupingFunction = { "NONE", "SUM", "MIN", "MAX", "AVG", "COUNT_DISTINCT", "COUNT" };

	public String setGroupingFunctionToLowerCase(String value) {
		String returnedValue = "";
		for (int i = 0; i < arrayOfGroupingFunction.length; i++) {
			if (value.contains("_" + arrayOfGroupingFunction[i])) {
				returnedValue = value.replace("_" + arrayOfGroupingFunction[i], "_" + arrayOfGroupingFunction[i].toLowerCase());
				break;
			}

		}
		if (returnedValue != "")
			return returnedValue;
		else
			return value;
		/*
		 * if (value.contains("_SUM")) return value.toLowerCase(); else return value;
		 */
	}

	public JSONObject getColorObject(int counter) {
		String[] defaultColors = { "#7cb5ec", "#434348", "#90ed7d", "#f7a35c", "#8085e9", "#f15c80", "#e4d354", "#2b908f", "#f45b5b", "#91e8e1" };
		JSONObject toReturn = new JSONObject();
		String name = defaultColors[counter].substring(1, defaultColors[counter].length());
		String value = defaultColors[counter];
		try {
			toReturn.put("name", name);
			toReturn.put("value", value);
		} catch (JSONException e) {
			logger.error("Can not create JSON object", e);
		}
		return toReturn;
	}

}
