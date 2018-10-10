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
package it.eng.knowage.engine.cockpit.api.export.excel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.error.EMFAbstractError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @authors Francesco Lucchi (francesco.lucchi@eng.it)
 *
 */

public class ExcelExporter {

	static final String CSV_DELIMITER = ";";
	static final String CSV_LINE_FEED = "\n";

	static private Logger logger = Logger.getLogger(ExcelExporter.class);

	private final String outputType;
	private final String userUniqueIdentifier;
	private final Map<String, String[]> parameterMap;

	public ExcelExporter(String outputType, String userUniqueIdentifier, Map<String, String[]> parameterMap) {
		this.outputType = outputType;
		this.userUniqueIdentifier = userUniqueIdentifier;
		this.parameterMap = parameterMap;
	}

	public String getMimeType() {
		String mimeType;
		if ("xlsx".equalsIgnoreCase(outputType)) {
			mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
		} else if ("xls".equalsIgnoreCase(outputType)) {
			mimeType = "application/vnd.ms-excel";
		} else {
			throw new SpagoBIRuntimeException("Unsupported output type [" + outputType + "]");
		}
		return mimeType;
	}

	public byte[] getBinaryData(Integer documentId, String documentLabel, String templateString) {
		Workbook wb;

		if ("xlsx".equalsIgnoreCase(outputType)) {
			wb = new XSSFWorkbook();
		} else if ("xls".equalsIgnoreCase(outputType)) {
			wb = new HSSFWorkbook();
		} else {
			throw new SpagoBIRuntimeException("Unsupported output type [" + outputType + "]");
		}

		if (templateString == null) {
			ObjTemplate template;
			try {
				template = DAOFactory.getObjTemplateDAO().getBIObjectActiveTemplate(documentId);
				if (template == null) {
					throw new SpagoBIRuntimeException("Unable to get template for document with id [" + documentId + "]");
				}
				templateString = new String(template.getContent());
			} catch (EMFAbstractError e) {
				throw new SpagoBIRuntimeException("Unable to get template for document with id [" + documentId + "]");
			}
		}

		String[] csvData = getCsvSheets(templateString);
		if (csvData != null) {
			importCsvData(csvData, wb);
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			wb.write(out);
			out.flush();
			out.close();
		} catch (IOException e) {
			throw new SpagoBIRuntimeException("Unable to generate output file with extension [" + outputType + "]", e);
		}

		return out.toByteArray();
	}

	private String[] getCsvSheets(String templateString) {
		List<String> csvs = new ArrayList<String>(0);
		try {
			JSONObject template = new JSONObject(templateString);

			JSONObject configuration = template.getJSONObject("configuration");

			JSONArray sheets = template.getJSONArray("sheets");
			for (int i = 0; i < sheets.length(); i++) {
				JSONObject sheet = sheets.getJSONObject(i);
				int sheetIndex = sheet.getInt("index");

				JSONArray widgets = sheet.getJSONArray("widgets");
				for (int j = 0; j < widgets.length(); j++) {
					JSONObject widget = widgets.getJSONObject(j);
					String widgetType = widget.getString("type");
					if ("table".equals(widgetType)) {
						JSONObject datasetObj = widget.getJSONObject("dataset");
						int datasetId = datasetObj.getInt("dsId");
						IDataSet iDataset = DAOFactory.getDataSetDAO().loadDataSetById(datasetId);
						String datasetLabel = iDataset.getLabel();

						Map<String, Object> map = new java.util.HashMap<String, Object>();

						JSONObject aggregations = getAggregationsFromTableWidget(widget, configuration);
						logger.debug("aggregations = " + aggregations);
						map.put("aggregations", URLEncoder.encode(aggregations.toString(), "UTF-8"));

						JSONObject parameters = getParametersFromTableWidget(widget, configuration);
						logger.debug("parameters = " + parameters);
						map.put("parameters", URLEncoder.encode(parameters.toString(), "UTF-8"));

						JSONObject summaryRow = getSummaryRowFromTableWidget(widget);
						if (summaryRow != null) {
							logger.debug("summaryRow = " + summaryRow);
							map.put("summaryRow", URLEncoder.encode(summaryRow.toString(), "UTF-8"));
						}

						if (getRealtimeFromTableWidget(datasetId, configuration)) {
							logger.debug("realtime = true");
							map.put("realtime", true);
						}

						int limit = getLimitFromTableWidget(widget);
						if (limit > 0) {
							logger.debug("limit = " + limit);
							map.put("limit", limit);
						}

						JSONObject likeSelections = getLikeSelectionsFromTableWidget(widget, configuration);
						if (likeSelections != null) {
							logger.debug("likeSelections = " + likeSelections);
							map.put("likeSelections", URLEncoder.encode(likeSelections.toString(), "UTF-8"));
						}

						JSONObject selections = getSelectionsFromTableWidget(widget, configuration);
						logger.debug("selections = " + URLEncoder.encode(selections.toString(), "UTF-8"));

						ExcelExporterClient client = new ExcelExporterClient();
						String csv;
						try {
							JSONObject datastore = client.getDataStore(map, datasetLabel, userUniqueIdentifier, selections.toString());
							logger.debug("datastore = " + datastore.toString());
							csv = getCsvSheet(datastore, widget);
						} catch (Exception e) {
							logger.error("Unable to get data", e);
							csv = "";
						}
						csvs.add(csv);
					}
				}
			}
		} catch (JSONException | EMFUserError | UnsupportedEncodingException e) {
			logger.error("Unable to load template", e);
		}

		return csvs.toArray(new String[0]);
	}

	private String getCsvSheet(JSONObject datastore, JSONObject widget) throws JSONException {
		StringBuilder sb = new StringBuilder();

		JSONObject content = widget.getJSONObject("content");
		JSONArray columns = content.getJSONArray("columnSelectedOfDataset");
		int columnCount = columns.length();
		List<String> headers = new ArrayList<String>(columnCount);
		for (int i = 0; i < columnCount; i++) {
			JSONObject column = columns.getJSONObject(i);
			String alias = column.getString("alias");
			headers.add(alias);
			String aliasToShow = column.getString("aliasToShow");
			sb.append(aliasToShow);
			sb.append(CSV_DELIMITER);
		}
		sb.append(CSV_LINE_FEED);

		String[] columnMap = new String[columnCount];

		JSONObject metaData = datastore.getJSONObject("metaData");
		JSONArray fields = metaData.getJSONArray("fields");
		for (int i = 1; i < fields.length(); i++) {
			JSONObject field = fields.getJSONObject(i);
			String name = field.getString("name");
			String header = field.getString("header");
			int index = headers.indexOf(header);
			if (index > -1) {
				columnMap[index] = name;
			}
		}

		JSONArray rows = datastore.getJSONArray("rows");
		for (int i = 0; i < rows.length(); i++) {
			JSONObject row = rows.getJSONObject(i);
			for (String column : columnMap) {
				String value = row.optString(column);
				if (value != null) {
					sb.append(value);
				}
				sb.append(CSV_DELIMITER);
			}
			sb.append(CSV_LINE_FEED);
		}

		return sb.toString();
	}

	private JSONObject getAggregationsFromTableWidget(JSONObject widget, JSONObject configuration) throws JSONException {
		JSONObject aggregations = new JSONObject();

		JSONArray measures = new JSONArray();
		aggregations.put("measures", measures);

		JSONArray categories = new JSONArray();
		aggregations.put("categories", categories);

		String sortingColumn = null;
		String sortingOrder = null;
		JSONObject settings = widget.optJSONObject("content");
		if (settings != null) {
			sortingColumn = settings.optString("sortingColumn");
			sortingOrder = settings.optString("sortingOrder");
		}

		boolean isSortingDefined = sortingColumn != null && !sortingColumn.isEmpty() && sortingOrder != null && !sortingOrder.isEmpty();
		boolean isSortingUsed = false;

		JSONObject content = widget.optJSONObject("content");
		if (content != null) {
			JSONArray columns = content.optJSONArray("columnSelectedOfDataset");
			if (columns != null) {
				for (int i = 0; i < columns.length(); i++) {
					JSONObject column = columns.getJSONObject(i);

					JSONObject categoryOrMeasure = new JSONObject();
					categoryOrMeasure.put("id", column.getString("alias"));
					categoryOrMeasure.put("alias", column.getString("aliasToShow"));
					categoryOrMeasure.put("columnName", column.getString("name"));
					if (isSortingDefined && sortingColumn.equals(column.getString("name"))) {
						categoryOrMeasure.put("orderType", sortingOrder);
						isSortingUsed = true;
					} else {
						categoryOrMeasure.put("orderType", "");
					}

					String fieldType = column.getString("fieldType");
					if ("ATTRIBUTE".equalsIgnoreCase(fieldType)) {
						categories.put(categoryOrMeasure);
					} else if ("MEASURE".equalsIgnoreCase(fieldType)) {
						categoryOrMeasure.put("funct", column.getString("aggregationSelected"));
						measures.put(categoryOrMeasure);
					} else {
						throw new SpagoBIRuntimeException("Unsupported field type");
					}
				}

				if (isSortingDefined && !isSortingUsed) {
					JSONObject category = new JSONObject();
					category.put("alias", sortingColumn);
					category.put("columnName", sortingColumn);
					category.put("id", sortingColumn);
					category.put("orderType", sortingOrder);
					categories.put(category);
				}
			}
		}

		JSONObject dataset = getDatasetFromWidget(widget, configuration);
		String datasetName = dataset.getString("name");
		aggregations.put("dataset", datasetName);

		return aggregations;
	}

	private JSONObject getParametersFromTableWidget(JSONObject widget, JSONObject configuration) throws JSONException {
		JSONObject dataset = getDatasetFromWidget(widget, configuration);
		JSONObject parameters = dataset.getJSONObject("parameters");

		Iterator<String> keys = parameters.keys();
		while (keys.hasNext()) {
			String parameter = keys.next();
			String value = parameters.getString(parameter);
			String parameterRegex = "\\$P\\{" + parameter + "\\}";
			if (value.matches(parameterRegex)) {
				String[] parameterArray = parameterMap.get(parameter);
				if (parameterArray != null && parameterArray.length > 0) {
					String parameterValue = parameterArray[0];
					String multiValueRegex = "\\{;\\{(.*)\\}(.*)\\}";
					Pattern pattern = Pattern.compile(multiValueRegex);
					Matcher matcher = pattern.matcher(parameterValue);
					if (matcher.matches()) {
						String[] split = matcher.group(1).split(";");
						parameterValue = "'" + StringUtils.join(split, "','") + "'";
					}
					value = value.replaceAll(parameterRegex, parameterValue);
					parameters.put(parameter, value);
				} else {
					parameters.put(parameter, "");
				}
			}
		}

		return parameters;
	}

	private JSONObject getSummaryRowFromTableWidget(JSONObject widget) throws JSONException {
		JSONObject settings = widget.optJSONObject("settings");
		if (settings != null) {
			JSONObject summary = settings.optJSONObject("summary");
			if (summary.optBoolean("enabled")) {
				JSONObject summaryRow = new JSONObject();

				JSONArray measures = new JSONArray();
				JSONObject content = widget.optJSONObject("content");
				if (content != null) {
					JSONArray columns = content.optJSONArray("columnSelectedOfDataset");
					if (columns != null) {
						for (int i = 0; i < columns.length(); i++) {
							JSONObject column = columns.getJSONObject(i);
							if ("MEASURE".equalsIgnoreCase(column.getString("fieldType"))) {
								JSONObject measure = new JSONObject();
								measure.put("id", column.getString("alias"));
								measure.put("alias", column.getString("aliasToShow"));
								measure.put("funct", column.getString("funcSummary"));
								measure.put("columnName", column.getString("name"));
								measures.put(measure);
							}
						}
					}
				}
				summaryRow.put("measures", measures);

				JSONObject dataset = widget.optJSONObject("dataset");
				if (dataset != null) {
					int dsId = dataset.getInt("dsId");
					summaryRow.put("dataset", dsId);
				}

				return summaryRow;
			}
		}
		return null;
	}

	private boolean getRealtimeFromTableWidget(int dsId, JSONObject configuration) throws JSONException {
		JSONObject dataset = getDataset(dsId, configuration);
		return !dataset.getBoolean("useCache");
	}

	private JSONObject getDataset(int dsId, JSONObject configuration) throws JSONException {
		JSONArray datasets = configuration.getJSONArray("datasets");
		for (int i = 0; i < datasets.length(); i++) {
			JSONObject dataset = (JSONObject) datasets.get(i);
			int id = dataset.getInt("dsId");
			if (id == dsId) {
				return dataset;
			}
		}
		return null;
	}

	private JSONObject getDatasetFromWidget(JSONObject widget, JSONObject configuration) throws JSONException {
		JSONObject widgetDataset = widget.optJSONObject("dataset");
		if (widgetDataset != null) {
			int dsId = widgetDataset.getInt("dsId");
			JSONObject configurationDataset = getDataset(dsId, configuration);
			return configurationDataset;
		}
		return null;
	}

	private int getLimitFromTableWidget(JSONObject widget) throws JSONException {
		JSONObject limitRows = widget.optJSONObject("limitRows");
		if (limitRows != null && limitRows.getBoolean("enable")) {
			return limitRows.getInt("rows");
		}
		return 0;
	}

	private JSONObject getLikeSelectionsFromTableWidget(JSONObject widget, JSONObject configuration) throws JSONException {
		JSONObject search = widget.optJSONObject("search");
		if (search != null) {
			String text = search.optString("text");
			JSONArray columns = search.optJSONArray("columns");
			if (text != null && !text.isEmpty() && columns != null && columns.length() > 0) {
				JSONObject obj = new JSONObject();
				obj.put(columns.join(","), text);

				JSONObject dataset = getDatasetFromWidget(widget, configuration);
				String datasetName = dataset.getString("name");

				JSONObject likeSelections = new JSONObject();
				likeSelections.put(datasetName, obj);
				return likeSelections;
			}
		}
		return null;
	}

	private JSONObject getSelectionsFromTableWidget(JSONObject widget, JSONObject configuration) throws JSONException {
		JSONObject dataset = getDatasetFromWidget(widget, configuration);
		String datasetName = dataset.getString("name");

		JSONObject selections = new JSONObject();
		JSONObject datasetFilters = new JSONObject();

		// get configuration filters
		JSONObject configurationFilters = configuration.optJSONObject("filters");
		if (configurationFilters != null) {
			JSONObject obj = configurationFilters.optJSONObject(datasetName);
			if (obj != null) {
				String[] names = JSONObject.getNames(obj);
				if (names != null) {
					for (int i = 0; i < names.length; i++) {
						String filter = names[i];
						JSONArray array = new JSONArray();
						array.put("('" + obj.get(filter) + "')");
						datasetFilters.put(filter, array);
					}
				}
			}
		}

		// get widget filters
		JSONArray widgetFilters = widget.optJSONArray("filters");
		if (widgetFilters != null) {
			for (int i = 0; i < widgetFilters.length(); i++) {
				JSONObject widgetFilter = widgetFilters.getJSONObject(i);
				JSONArray filterVals = widgetFilter.getJSONArray("filterVals");
				if (filterVals.length() > 0) {
					String colName = widgetFilter.getString("colName");
					JSONArray array = new JSONArray();
					for (int j = 0; j < filterVals.length(); j++) {
						Object filterVal = filterVals.get(j);
						array.put("('" + filterVal + "')");
					}
					datasetFilters.put(colName, array);
				}
			}
		}

		selections.put(datasetName, datasetFilters);
		return selections;
	}

	private void importCsvData(String[] csvData, Workbook wb) {
		for (int csvIndex = 0; csvIndex < csvData.length; csvIndex++) {
			Sheet sheet = wb.createSheet();
			String data = csvData[csvIndex];
			String[] rows = data.split(CSV_LINE_FEED);
			for (int rowIndex = 0; rowIndex < rows.length; rowIndex++) {
				Row row = sheet.createRow(rowIndex);
				String[] columns = rows[rowIndex].split(CSV_DELIMITER);
				for (int columnIndex = 0; columnIndex < columns.length; columnIndex++) {
					Cell cell = row.createCell(columnIndex);
					cell.setCellValue(columns[columnIndex]);
				}
			}
		}
	}

}
