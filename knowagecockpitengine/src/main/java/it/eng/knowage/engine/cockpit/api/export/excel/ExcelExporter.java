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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogMF;
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
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.utils.ParamDefaultValue;
import it.eng.spagobi.utilities.assertion.Assert;
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
	private Map<String, String> i18nMessages;

	public ExcelExporter(String outputType, String userUniqueIdentifier, Map<String, String[]> parameterMap) {
		this.outputType = outputType;
		this.userUniqueIdentifier = userUniqueIdentifier;
		this.parameterMap = parameterMap;

		Locale locale = getLocale(parameterMap);
		try {
			i18nMessages = DAOFactory.getI18NMessageDAO().getAllI18NMessages(locale);
			LogMF.debug(logger, "Loaded messages [{0}]", i18nMessages);
		} catch (EMFUserError e) {
			throw new SpagoBIRuntimeException("Error while retrieving the I18N messages", e);
		}
	}

	private Locale getLocale(Map<String, String[]> parameterMap) {
		try {
			Assert.assertNotNull(parameterMap, "Empty input parameters map");
			Assert.assertNotNull(parameterMap.get(SpagoBIConstants.SBI_LANGUAGE), "Missing language code in input parameters map");
			Assert.assertNotNull(parameterMap.get(SpagoBIConstants.SBI_COUNTRY), "Missing country code in input parameters map");
			Assert.assertTrue(parameterMap.get(SpagoBIConstants.SBI_LANGUAGE).length == 1, "More than one language code in input parameters map");
			Assert.assertTrue(parameterMap.get(SpagoBIConstants.SBI_COUNTRY).length == 1, "More than one country code in input parameters map");

			String language = parameterMap.get(SpagoBIConstants.SBI_LANGUAGE)[0];
			String country = parameterMap.get(SpagoBIConstants.SBI_COUNTRY)[0];
			Locale toReturn = new Locale(language, country);
			return toReturn;
		} catch (Exception e) {
			logger.warn("Could get locale information from input parameters map", e);
			return Locale.ENGLISH;
		}

	}

	private String getI18NMessage(String code) {
		if (!i18nMessages.containsKey(code)) {
			return code;
		}
		return i18nMessages.get(code);
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

		ExcelSheet[] excelSheets = getExcelSheets(templateString);
		if (excelSheets != null) {
			importCsvData(excelSheets, wb);
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

	private ExcelSheet[] getExcelSheets(String templateString) {
		List<ExcelSheet> sheets = new ArrayList<>(0);
		try {
			JSONObject template = new JSONObject(templateString);
			sheets.addAll(getCsvsFromDatasets(template));
			sheets.addAll(getCsvsFromWidgets(template));
		} catch (JSONException | EMFUserError | UnsupportedEncodingException e) {
			logger.error("Unable to load template", e);
		}

		return sheets.toArray(new ExcelSheet[0]);
	}

	private List<ExcelSheet> getCsvsFromDatasets(JSONObject template) throws JSONException, EMFUserError, UnsupportedEncodingException {
		logger.debug("IN");

		JSONObject configuration = template.getJSONObject("configuration");
		JSONArray datasetsObj = configuration.getJSONArray("datasets");

		List<ExcelSheet> excelSheets = new ArrayList<>(datasetsObj.length());
		for (int i = 0; i < datasetsObj.length(); i++) {
			JSONObject datasetObj = datasetsObj.getJSONObject(i);
			int datasetId = datasetObj.getInt("dsId");
			IDataSet dataset = DAOFactory.getDataSetDAO().loadDataSetById(datasetId);
			String datasetLabel = dataset.getLabel();

			JSONObject body = new JSONObject();

			JSONObject parameters = datasetObj.getJSONObject("parameters");
			logger.debug("parameters = " + parameters);
			body.put("parameters", getReplacedParameters(parameters, datasetId));

			JSONObject aggregations = getAggregationsFromDataset(dataset);
			logger.debug("aggregations = " + aggregations);
			body.put("aggregations", aggregations);

			Map<String, Object> map = new java.util.HashMap<String, Object>();

			if (getRealtimeFromTableWidget(datasetId, configuration)) {
				logger.debug("realtime = true");
				map.put("realtime", true);
			}

			JSONObject datastoreObj = getDatastore(datasetLabel, map, body.toString());
			String csv = datastoreObj != null ? getCsvSheet(datastoreObj) : "";
			excelSheets.add(new ExcelSheet(datasetLabel, csv));
		}

		logger.debug("OUT");
		return excelSheets;
	}

	private JSONObject getAggregationsFromDataset(IDataSet dataset) throws JSONException {
		JSONObject aggregations = new JSONObject();

		JSONArray categories = new JSONArray();
		aggregations.put("categories", categories);

		IMetaData metadata = dataset.getMetadata();
		for (int i = 0; i < metadata.getFieldCount(); i++) {
			JSONObject category = new JSONObject();
			String alias = metadata.getFieldAlias(i);
			category.put("id", alias);
			category.put("alias", alias);
			category.put("columnName", metadata.getFieldName(i));
			category.put("orderType", "");

			categories.put(category);
		}

		JSONArray measures = new JSONArray();
		aggregations.put("measures", measures);

		aggregations.put("dataset", dataset.getLabel());

		return aggregations;
	}

	private List<ExcelSheet> getCsvsFromWidgets(JSONObject template) throws JSONException, EMFUserError, UnsupportedEncodingException {
		logger.debug("IN");

		JSONObject configuration = template.getJSONObject("configuration");
		JSONArray sheets = template.getJSONArray("sheets");

		List<ExcelSheet> excelSheets = new ArrayList<>();

		for (int i = 0; i < sheets.length(); i++) {
			JSONObject sheet = sheets.getJSONObject(i);
			int sheetIndex = sheet.getInt("index");

			JSONArray widgets = sheet.getJSONArray("widgets");
			for (int j = 0; j < widgets.length(); j++) {
				JSONObject widget = widgets.getJSONObject(j);
				String widgetType = widget.getString("type");
				JSONObject content = widget.getJSONObject("content");
				String widgetName = content.getString("name");

				if ("table".equals(widgetType)) {
					JSONObject datasetObj = widget.getJSONObject("dataset");
					int datasetId = datasetObj.getInt("dsId");
					IDataSet dataset = DAOFactory.getDataSetDAO().loadDataSetById(datasetId);
					String datasetLabel = dataset.getLabel();

					JSONObject body = new JSONObject();

					JSONObject aggregations = getAggregationsFromTableWidget(widget, configuration);
					logger.debug("aggregations = " + aggregations);
					body.put("aggregations", aggregations);

					JSONObject parameters = getParametersFromTableWidget(widget, configuration);
					logger.debug("parameters = " + parameters);
					body.put("parameters", parameters);

					JSONObject summaryRow = getSummaryRowFromTableWidget(widget);
					if (summaryRow != null) {
						logger.debug("summaryRow = " + summaryRow);
						body.put("summaryRow", summaryRow);
					}

					JSONObject likeSelections = getLikeSelectionsFromTableWidget(widget, configuration);
					if (likeSelections != null) {
						logger.debug("likeSelections = " + likeSelections);
						body.put("likeSelections", likeSelections);
					}

					JSONObject selections = getSelectionsFromTableWidget(widget, configuration);
					logger.debug("selections = " + selections);
					body.put("selections", selections);

					Map<String, Object> map = new java.util.HashMap<String, Object>();

					if (getRealtimeFromTableWidget(datasetId, configuration)) {
						logger.debug("realtime = true");
						map.put("realtime", true);
					}

					int limit = getLimitFromTableWidget(widget);
					if (limit > 0) {
						logger.debug("limit = " + limit);
						map.put("limit", limit);
					}

					JSONObject datastoreObj = getDatastore(datasetLabel, map, body.toString());
					String csv = datastoreObj != null ? getCsvSheet(datastoreObj, widget) : "";
					excelSheets.add(new ExcelSheet("Sheet" + sheetIndex + " Widget" + j + " " + widgetName, csv));
				}
			}
		}

		logger.debug("OUT");
		return excelSheets;
	}

	private JSONObject getDatastore(String datasetLabel, Map<String, Object> map, String selections) {
		ExcelExporterClient client = new ExcelExporterClient();
		try {
			JSONObject datastore = client.getDataStore(map, datasetLabel, userUniqueIdentifier, selections);
			return datastore;
		} catch (Exception e) {
			logger.error("Unable to get data", e);
			return null;
		}
	}

	private String getCsvSheet(JSONObject datastore) throws JSONException {
		StringBuilder sb = new StringBuilder();

		JSONObject metaData = datastore.getJSONObject("metaData");
		JSONArray fields = metaData.getJSONArray("fields");
		String[] columnMap = new String[fields.length() - 1];
		for (int i = 1; i < fields.length(); i++) {
			JSONObject field = fields.getJSONObject(i);
			String name = field.getString("name");
			String header = field.getString("header");
			columnMap[i - 1] = name;
			sb.append(header);
			sb.append(CSV_DELIMITER);
		}
		sb.append(CSV_LINE_FEED);

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

	private String getCsvSheet(JSONObject datastore, JSONObject widget) throws JSONException {
		StringBuilder sb = new StringBuilder();

		JSONObject content = widget.getJSONObject("content");
		JSONArray columns = content.getJSONArray("columnSelectedOfDataset");
		int columnCount = columns.length();
		List<String> headers = new ArrayList<String>(columnCount);
		for (int i = 0; i < columnCount; i++) {
			JSONObject column = columns.getJSONObject(i);
			String aliasToShow = column.getString("aliasToShow");
			headers.add(aliasToShow);
			aliasToShow = getI18NMessage(aliasToShow);
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

		Integer datasetId = dataset.getInt("dsId");
		return getReplacedParameters(parameters, datasetId);
	}

	private JSONObject getReplacedParameters(JSONObject parameters, Integer datasetId) throws JSONException {
		JSONObject newParameters = new JSONObject(parameters.toString());
		Iterator<String> keys = newParameters.keys();
		while (keys.hasNext()) {
			String parameter = keys.next();
			String value = newParameters.getString(parameter);
			String parameterRegex = "\\$P\\{(.*)\\}";
			Matcher parameterMatcher = Pattern.compile(parameterRegex).matcher(value);
			if (parameterMatcher.matches()) {
				String newValue = "";
				String parameterName = parameterMatcher.group(1);
				String[] parameterArray = parameterMap.get(parameterName);
				if (parameterArray != null && parameterArray.length > 0) {
					String parameterValue = parameterArray[0];
					String multiValueRegex = "\\{;\\{(.*)\\}(.*)\\}";
					Matcher multiValueMatcher = Pattern.compile(multiValueRegex).matcher(parameterValue);
					if (multiValueMatcher.matches()) {
						String[] split = multiValueMatcher.group(1).split(";");
						parameterValue = "'" + StringUtils.join(split, "','") + "'";
					}
					newValue = value.replaceAll(parameterRegex, parameterValue);
				} else {
					if (datasetId != null) {
						newValue = getParameterDefaultValue(datasetId, parameter);
					}
				}
				newParameters.put(parameter, newValue);
			}
		}
		return newParameters;
	}

	private String getParameterDefaultValue(int datasetId, String parameter) {
		String newValue = null;
		try {
			IDataSet iDataSet = DAOFactory.getDataSetDAO().loadDataSetById(datasetId);
			if (iDataSet != null) {
				ParamDefaultValue paramDefaultValue = (ParamDefaultValue) iDataSet.getDefaultValues().get(parameter);
				if (paramDefaultValue != null) {
					newValue = paramDefaultValue.getDefaultValue();
				}
			}
		} catch (EMFUserError e) {
			throw new SpagoBIRuntimeException("Error while retrieving dataset with id [" + datasetId + "]", e);
		}
		return newValue;
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

	private void importCsvData(ExcelSheet[] excelSheets, Workbook wb) {
		for (int i = 0; i < excelSheets.length; i++) {
			ExcelSheet excelSheet = excelSheets[i];

			Sheet sheet = wb.createSheet(excelSheet.getLabel());

			String data = excelSheet.getCsv();
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
