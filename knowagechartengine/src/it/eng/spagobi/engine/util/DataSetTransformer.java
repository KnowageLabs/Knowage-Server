package it.eng.spagobi.engine.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.emory.mathcs.backport.java.util.Collections;

public class DataSetTransformer {

	public void print(Object object) {
		System.out.println("-----------------------");
		System.out.println(object);
		System.out.println(object.getClass().toString());
		System.out.println("-----------------------");
	}

	public JSONArray toWordcloud(Object columnsNeeded, Object dataColumnsMapper, List<Object> dataRows, Object serie, Object sizeCriteria,
			Object groupingFunction) throws JSONException {

		Map<String, String> mapper = (Map<String, String>) dataColumnsMapper;

		Map<String, String> columns = (Map<String, String>) columnsNeeded;

		Object serieRawColumn = mapper.get(serie.toString() + "_" + groupingFunction.toString());

		ArrayList<String> listColumns = new ArrayList<String>();

		HashMap<Integer, HashMap> result = new HashMap<Integer, HashMap>();

		for (int i = 0; i < columns.size(); i++) {

			Object cndata = columns.get(i);

			listColumns.add(mapper.get(cndata).toString());

		}

		for (int i = 0; i < dataRows.size(); i++) {
			Map<String, Object> row = (Map<String, Object>) dataRows.get(i);
			HashMap<String, String> record = new HashMap<String, String>();

			/* For every record take these columns */
			for (int j = 0; j < listColumns.size(); j++) {
				Object x = row.get(listColumns.get(j));
				record.put(columns.get(j).toString(), x.toString());
			}

			record.put(serie.toString(), row.get(serieRawColumn).toString());

			result.put(new Integer(i), record);
		}

		JSONArray res = toWordcloudArray(columns, serie, result, sizeCriteria);

		return res;

	}

	private JSONArray toWordcloudArray(Map<String, String> columns, Object serie, HashMap<Integer, HashMap> result, Object sizeCriteria) throws JSONException {

		JSONArray fr = new JSONArray();

		HashMap<String, Double> res = new HashMap<String, Double>();

		for (int i = 0; i < result.size(); i++) {

			for (int j = 0; j < columns.size(); j++) {

				if (!res.containsKey(result.get(i).get(columns.get(j)))) {

					String name = (String) result.get(i).get(columns.get(j));

					Double value = 0.00;

					if (sizeCriteria.toString().equals("serie")) {

						value = value + Double.parseDouble(result.get(i).get(serie).toString());

					} else if (sizeCriteria.toString().equals("occurrences")) {

						value++;

					}

					res.put(name, value);

				}

				else {

					String name = (String) result.get(i).get(columns.get(j));

					Double oldvalue = res.get(name);

					Double newValue = 0.00;

					if (sizeCriteria.toString().equals("serie")) {

						Double value = Double.parseDouble(result.get(i).get(serie).toString());

						newValue = oldvalue + value;

					} else if (sizeCriteria.toString().equals("occurrences")) {

						newValue = oldvalue + 1;

					}

					res.remove(name);

					res.put(name, newValue);

				}

			}

		}

		Iterator it = res.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();

			JSONObject jo = new JSONObject();
			jo.put("name", pair.getKey());
			jo.put("value", pair.getValue());

			fr.put(jo);

		}

		return fr;
	}

	/**
	 * Method that serves for preparing the data that JS code will use for rendering the CHORD chart
	 *
	 * @param dataRows
	 *            List of objects that represent the result of the query based on the result of the dataset linked to the chart (document) and on the query
	 *            formed upon the XML of the document (VALUES tag, particularly CATEGORY and SERIE subtags)
	 * @param columnsNeeded
	 *            Categories (columns of the resulting table) that are needed by the request formulated through the XML tag CATEGORY
	 * @param serie
	 *            Column of the resulting table that is specified as the SERIE column through the XML tag SERIE
	 * @param dataColumnsMapper
	 *            Mapping between the name of the columns (categories and serie) and their ordinal (raw value: column_1, column_2, ...)
	 * @throws JSONException
	 *
	 * @author Danilo Ristovski (danilo.ristovski@mht.net)
	 */
	public JSONObject toMatrix(List<Object> dataRows, Object columnsNeeded, Object serie, Object dataColumnsMapper) throws JSONException {

		Map<String, Object> categories = (Map<String, Object>) columnsNeeded;
		Map<String, Object> columnsMapper = (Map<String, Object>) dataColumnsMapper;

		/**
		 * List of raw names of the columns (their ordinal) that are specified as categories (in order defined by the XML template).
		 */
		ArrayList<String> categoriesColumnNames = new ArrayList<String>();

		for (int i = 0; i < categories.size(); i++) {
			categoriesColumnNames.add((String) columnsMapper.get(categories.get(i)));
		}

		String rawColumnNameRow = categoriesColumnNames.get(0);
		String rawColumnNameColumn = categoriesColumnNames.get(1);

		ArrayList<String> allColumns = new ArrayList<String>();

		for (int i = 0; i < dataRows.size(); i++) {

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

		Map<String, HashMap<String, Float>> availableDataMapOfMaps = new HashMap<String, HashMap<String, Float>>(); // old name: matrix

		for (int i = 0; i < dataRows.size(); i++) {

			// Current record (row) from the map of maps of available data (primitive (not pivoted) dataset)
			Map<String, Object> record = (Map<String, Object>) dataRows.get(i);

			// Value (name) of the current record's row from the map of maps
			String currentRow = (String) record.get(rawColumnNameRow);

			// Put a new map for the row that is not contained by the map of maps
			if (!availableDataMapOfMaps.containsKey(currentRow)) {

				HashMap<String, Float> submapWithNewColumn = new HashMap<String, Float>(); // old name: aaa

				if (record.get(columnsMapper.get(serie + "_SUM")).getClass().toString().equals("class java.lang.Integer")) {
					submapWithNewColumn.put((String) record.get(rawColumnNameColumn),
							Float.parseFloat(Integer.toString((int) record.get(columnsMapper.get(serie + "_SUM")))));
				} else {
					submapWithNewColumn.put((String) record.get(rawColumnNameColumn),
							Float.parseFloat((String) (record.get(columnsMapper.get(serie + "_SUM")))));
				}

				availableDataMapOfMaps.put(currentRow, submapWithNewColumn);

			} else {

				if (record.get(columnsMapper.get(serie + "_SUM")).getClass().toString().equals("class java.lang.Integer")) {
					availableDataMapOfMaps.get(currentRow).put((String) record.get(rawColumnNameColumn),
							Float.parseFloat(Integer.toString((int) record.get(columnsMapper.get(serie + "_SUM")))));
				} else {
					availableDataMapOfMaps.get(currentRow).put((String) record.get(rawColumnNameColumn),
							Float.parseFloat((String) record.get(columnsMapper.get(serie + "_SUM"))));
				}

			}

		}

		/**
		 * The final JSON object (it will be sent towards client - rendering part)
		 */
		JSONObject finalJsonResult = new JSONObject();

		/**
		 * JSON object for collecting the data (labels with their values and JSON array) for meta data ('root' label and 'fields' JSON array) connected to the
		 * final data obtained from the dataset.
		 */
		JSONObject jsonObjectMetadata = new JSONObject(); // old value: jo1

		/**
		 * 'root' label tells us what is the name of the label inside the final JSON that contains the data (values for rendering the chart)
		 */
		jsonObjectMetadata.put("root", "rows");

		/**
		 * JSON array under the 'fields' label of the 'metaData' label that will contain information about all columns of the matrix that we need to create so
		 * to deliver it to the rendering (client) side. It will contain information about all columns (name and their ordinal value) in alphabetically
		 * ascending order.
		 */
		JSONArray jsonArrayMetadataFields = new JSONArray(); // old value: ja1

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
		 * Join this JSON array under the 'fields' label of the 'metaData' label of the final JSON object
		 */
		jsonObjectMetadata.put("fields", jsonArrayMetadataFields);

		/**
		 * Set the 'metaData' label of the final JSON object and link to it necessary data
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
		 * with their dedicated appropriate values (as said here: https://github.com/mbostock/d3/wiki/Chord-Layout, under this header:
		 * "# chord.matrix([matrix])").
		 */
		JSONArray jsonArrayResultsRows = new JSONArray(); // old name: ja2

		/**
		 * Go through all rows sorted in alphabetically ascending order
		 */
		for (int i = 0; i < allColumns.size(); i++) {

			/**
			 * For every new row create a JSON object that will contain values of intersection with all existing columns (previously arranged in alphabetically
			 * ascending order) of future matrix (in form of the final JSON object). This JSON object will be inserted into the JSON array of numerical results
			 * ('rows' label of the final JSON object). These data are going to be used by the client (JS, particularly D3) code that will render the CHORD
			 * chart.
			 */
			JSONObject jsonObjectRowWithAllItsColumns = new JSONObject(); // old name: jo2

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

				if (availableDataMapOfMaps.get(allColumns.get(i)).get(allColumns.get(j)) != null) {

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
		ArrayList<String> listColumns = new ArrayList<String>();

		// End result - map of maps (records with their columns values)
		HashMap<Integer, HashMap> result = new HashMap<Integer, HashMap>();

		// Take value of the SERIE column (the one that contains numerical values)
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
			HashMap<String, String> record = new HashMap<String, String>();

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
		HashMap<String, Double> endresult = new HashMap<String, Double>();
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

		ArrayList<String> listColumns = new ArrayList<String>();

		HashMap<Integer, HashMap> result = new HashMap<Integer, HashMap>();

		for (int i = 0; i < columns.size(); i++) {

			Object cndata = columns.get(i);

			listColumns.add(mapper.get(cndata).toString());

		}

		for (int i = 0; i < dataRows.size(); i++) {
			Map<String, Object> row = (Map<String, Object>) dataRows.get(i);
			HashMap<String, String> record = new HashMap<String, String>();

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

	public JSONArray getGroupsForParallelChart(Object columnsNeeded, Object dataColumnsMapper, List<Object> dataRows) throws JSONException {

		JSONArray ja = new JSONArray();

		Map<String, String> columns = (Map<String, String>) columnsNeeded;

		Map<String, String> mapper = (Map<String, String>) dataColumnsMapper;

		String group = columns.get(0);

		String groupvalue = mapper.get(group);

		ArrayList<String> al = new ArrayList<String>();

		int j = 0;

		for (int i = 0; i < dataRows.size(); i++) {

			Map<String, Object> row = (Map<String, Object>) dataRows.get(i);

			if (!al.contains(row.get(groupvalue))) {

				al.add((String) row.get(groupvalue));
				JSONObject jo = new JSONObject();
				jo.put((new Integer(j)).toString(), row.get(groupvalue).toString());
				ja.put(jo);
				j++;
			}
		}

		return ja;

	}

	public JSONArray getSeriesForParallelChart(Object serieNeeded, Object groupingFunction) throws JSONException {

		JSONArray ja = new JSONArray();

		Map<String, String> series = (Map<String, String>) serieNeeded;

		Map<String, String> groupings = (Map<String, String>) groupingFunction;

		ArrayList<String> al = new ArrayList<String>();

		int j = 0;

		for (int i = 0; i < series.size(); i++) {

			if (!al.contains(series.get(i))) {

				al.add(series.get(i) + "_" + groupings.get(i));
				JSONObject jo = new JSONObject();
				jo.put((new Integer(j).toString()), series.get(i));
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

			ArrayList<String> al = new ArrayList<String>();

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

		Map<String, String> colMapper = new HashMap<String, String>();

		ArrayList<String> listColumns = new ArrayList<String>();

		for (int i = 0; i < series.size(); i++) {

			Object serie = series.get(i) + "_" + groupings.get(i);

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

		ArrayList<String> listColumns = new ArrayList<String>();

		HashMap<Integer, HashMap> firstresult = new HashMap<Integer, HashMap>();

		for (int i = 0; i < columns.size(); i++) {

			Object cndata = columns.get(i);

			listColumns.add(mapper.get(cndata).toString());

		}

		for (int i = 0; i < dataRows.size(); i++) {
			Map<String, Object> row = (Map<String, Object>) dataRows.get(i);
			HashMap<String, String> record = new HashMap<String, String>();

			/* For every record take these columns */
			for (int j = 0; j < listColumns.size(); j++) {
				Object x = row.get(listColumns.get(j));
				record.put(columns.get(j).toString(), x.toString());
			}

			record.put(serie.toString(), row.get(serieRawColumn).toString());

			firstresult.put(new Integer(i), record);
		}

		return firstresult;

	}

	public JSONArray getDateResult(Map<Integer, HashMap> firstresult, Object column) throws ParseException {

		JSONArray dateResult = new JSONArray();

		ArrayList<Date> dates = new ArrayList<Date>();

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

		HashMap<Integer, String> storeResultMap = new HashMap<Integer, String>();

		int value = 0;

		for (int i = 0; i < firstresult.size(); i++) {

			if (!storeResultMap.containsValue(firstresult.get(i).get(column.toString()))) {

				storeResultMap.put(value, (String) (firstresult.get(i).get(column.toString())));

				value++;

			}

		}

		for (int i = 0; i < storeResultMap.size(); i++) {

			storeResult.put(storeResultMap.get(i));

		}

		return storeResult;

	}

	public JSONArray getResult(Map<Integer, HashMap> firstresult, Object serie, HashMap<String, String> columns) throws JSONException, ParseException {

		JSONArray result = new JSONArray();

		for (int i = 0; i < firstresult.size(); i++) {

			JSONObject jo = new JSONObject();

			Double serieValue = Double.valueOf((String) firstresult.get(i).get(serie.toString()));

			jo.put(serie.toString(), serieValue);

			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

			for (int j = 0; j < columns.size(); j++) {

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

}
