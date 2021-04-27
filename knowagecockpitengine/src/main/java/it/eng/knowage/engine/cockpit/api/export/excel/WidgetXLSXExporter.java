/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.knowage.engine.cockpit.api.crosstable.CrossTab;
import it.eng.knowage.engine.cockpit.api.crosstable.CrosstabBuilder;
import it.eng.knowage.engine.cockpit.api.crosstable.NodeComparator;
import it.eng.knowage.engine.cockpit.api.export.excel.crosstab.CrosstabXLSXExporter;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.json.JSONUtils;

class WidgetXLSXExporter {

	static private Logger logger = Logger.getLogger(WidgetXLSXExporter.class);

	ExcelExporter excelExporter;
	String widgetType;
	String templateString;
	long widgetId;
	Workbook wb;
	JSONObject optionsObj;

	public WidgetXLSXExporter(ExcelExporter excelExporter, String widgetType, String templateString, long widgetId, Workbook wb, JSONObject options) {
		super();
		this.excelExporter = excelExporter;
		this.widgetType = widgetType;
		this.templateString = templateString;
		this.widgetId = widgetId;
		this.wb = wb;
		this.optionsObj = options;
	}

	public int export() {
		try {
			if (widgetType.equalsIgnoreCase("static-pivot-table") && optionsObj != null) {
				// crosstab widget object must be retrieved BE side
				return exportCrossTabWidget();
			} else if (widgetType.equalsIgnoreCase("map")) {
				// map widget supports multiple datasets
				return exportMapWidget();
			} else if (widgetType.equalsIgnoreCase("table")) {
				// table widget supports pagination
				return exportTableWidget();
			} else {
				return exportGenericWidget();
			}
		} catch (Exception e) {
			// log exception and skip widget
			logger.error("Could not export widget [id=" + widgetId + " type=" + widgetType + "]", e);
		}
		return 0;
	}

	private int exportGenericWidget() {
		try {
			JSONObject template = new JSONObject(templateString);
			JSONObject widget = getWidgetById(template, widgetId);
			String widgetName = getWidgetName(widget);

			JSONObject dataStore = excelExporter.getDataStoreForWidget(template, widget);
			if (dataStore != null) {
				String cockpitSheetName = getCockpitSheetName(template, widgetId);
				excelExporter.createAndFillExcelSheet(dataStore, wb, widgetName, cockpitSheetName);
				return 1;
			}

		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Unable to export widget: " + widgetId, e);
		}
		return 0;
	}

	private int exportTableWidget() {
		try {
			JSONObject template = new JSONObject(templateString);
			JSONObject widget = getWidgetById(template, widgetId);
			String widgetName = getWidgetName(widget);
			String cockpitSheetName = getCockpitSheetName(template, widgetId);
			Sheet sheet = excelExporter.createUniqueSafeSheet(wb, widgetName, cockpitSheetName);

			int offset = 0;
			int fetchSize = Integer.parseInt(SingletonConfig.getInstance().getConfigValue("SPAGOBI.API.DATASET.MAX_ROWS_NUMBER"));
			JSONObject dataStore = excelExporter.getDataStoreForWidget(template, widget, offset, fetchSize);
			if (dataStore != null) {
				int totalNumberOfRows = dataStore.getInt("results");
				while (offset < totalNumberOfRows) {
					excelExporter.fillSheetWithData(dataStore, wb, sheet, widgetName, offset);
					offset += fetchSize;
					dataStore = excelExporter.getDataStoreForWidget(template, widget, offset, fetchSize);
				}
				return 1;
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Unable to export generic widget: " + widgetId, e);
		}
		return 0;
	}

	private int exportMapWidget() {
		int exportedSheets = 0;
		try {
			JSONObject template = new JSONObject(templateString);
			JSONObject widget = getWidgetById(template, widgetId);
			String widgetName = getWidgetName(widget);

			JSONArray dataStoreArray = excelExporter.getMultiDataStoreForWidget(template, widget);
			for (int i = 0; i < dataStoreArray.length(); i++) {
				try {
					JSONObject dataStore = dataStoreArray.getJSONObject(i);
					if (dataStore != null) {
						String cockpitSheetName = getCockpitSheetName(template, widgetId) + String.valueOf(i);
						excelExporter.createAndFillExcelSheet(dataStore, wb, widgetName, cockpitSheetName);
						exportedSheets++;
					}
				} catch (Exception e) {
					logger.error("Couldn't export layer [" + (i + 1) + "] of map widget [" + widgetId + "]");
				}
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Unable to export map widget: " + widgetId, e);
		}
		return exportedSheets;
	}

	private int exportCrossTabWidget() {
		try {
			JSONObject template = new JSONObject(templateString);
			JSONObject widget = getWidgetById(template, widgetId);
			String widgetName = getWidgetName(widget);

			JSONObject crosstabDefinition = optionsObj.getJSONObject("crosstabDefinition");
			CrossTab cs = buildCrossTab(crosstabDefinition);
			CrosstabXLSXExporter crossTabExporter = buildCrossTabXLSXExporter(crosstabDefinition);

			int totalRowsNumber = cs.getTotalNumberOfRows();
			int windowSize = Integer.parseInt(SingletonConfig.getInstance().getConfigValue("KNOWAGE.DASHBOARD.EXPORT.EXCEL.STREAMING_WINDOW_SIZE"));
			if (totalRowsNumber <= windowSize) {
				// crosstab fits in memory
				String cockpitSheetName = getCockpitSheetName(template, widgetId);
				Sheet sheet = excelExporter.createUniqueSafeSheet(wb, widgetName, cockpitSheetName);
				crossTabExporter.fillExcelSheetWithData(sheet, cs, wb.getCreationHelper(), 0, excelExporter.getLocale());
				return 1;
			} else {
				// export crosstab as generic widget
				logger.warn("Crosstab [" + widgetId + "] has more rows than streaming windows size. It will be exported as a generic widget.");
				return exportGenericWidget();
			}

		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Unable to export crosstab widget: " + widgetId, e);
		}
	}

	CrosstabXLSXExporter buildCrossTabXLSXExporter(JSONObject crosstabDefinition) throws JSONException {
		JSONArray measures = crosstabDefinition.optJSONArray("measures");
		Map<String, List<Threshold>> thresholdColorsMap = getThresholdColorsMap(measures);
		JSONObject variables = optionsObj.optJSONObject("variables");
		CrosstabXLSXExporter crossTabExporter = new CrosstabXLSXExporter(null, variables, thresholdColorsMap);
		return crossTabExporter;
	}

	CrossTab buildCrossTab(JSONObject crosstabDefinition) throws JSONException {
		// the id of the crosstab in the client configuration array
		JSONObject sortOptions = optionsObj.getJSONObject("sortOptions");
		JSONArray columnsSortKeysJo = sortOptions.optJSONArray("columnsSortKeys");
		JSONArray rowsSortKeysJo = sortOptions.optJSONArray("rowsSortKeys");
		JSONArray measuresSortKeysJo = sortOptions.optJSONArray("measuresSortKeys");
		int myGlobalId = sortOptions.optInt("myGlobalId");
		List<Map<String, Object>> columnsSortKeys = JSONUtils.toMap(columnsSortKeysJo);
		List<Map<String, Object>> rowsSortKeys = JSONUtils.toMap(rowsSortKeysJo);
		List<Map<String, Object>> measuresSortKeys = JSONUtils.toMap(measuresSortKeysJo);
		if (optionsObj != null) {
			logger.debug("Export cockpit crosstab optionsObj.toString(): " + optionsObj.toString());
		}
		Map<Integer, NodeComparator> columnsSortKeysMap = toComparatorMap(columnsSortKeys);
		Map<Integer, NodeComparator> rowsSortKeysMap = toComparatorMap(rowsSortKeys);
		Map<Integer, NodeComparator> measuresSortKeysMap = toComparatorMap(measuresSortKeys);
		CrosstabBuilder builder = new CrosstabBuilder(excelExporter.getLocale(), crosstabDefinition, optionsObj.getJSONArray("jsonData"),
				optionsObj.getJSONObject("metadata"), null);

		CrossTab cs = builder.getSortedCrosstabObj(columnsSortKeysMap, rowsSortKeysMap, measuresSortKeysMap, myGlobalId);
		return cs;
	}

	private Map<String, List<Threshold>> getThresholdColorsMap(JSONArray measures) {
		Map<String, List<Threshold>> toReturn = new HashMap<String, List<Threshold>>();
		try {
			for (int i = 0; i < measures.length(); i++) {
				JSONObject measure = measures.getJSONObject(i);
				String id = measure.getString("id");
				if (!measure.has("ranges"))
					continue;
				JSONArray ranges = measure.getJSONArray("ranges");
				List<Threshold> allThresholds = new ArrayList<Threshold>();
				for (int j = 0; j < ranges.length(); j++) {
					JSONObject range = ranges.getJSONObject(j);
					String operator = range.getString("operator");
					if (!operator.equals("none")) {
						Double value = range.getDouble("value");
						String color = range.getString("background-color");
						Threshold threshold = new Threshold(operator, value, color);
						allThresholds.add(threshold);
					}
				}
				toReturn.put(id, allThresholds);
			}
		} catch (Exception e) {
			logger.error("Unable to build threshold color map", e);
			Map<String, List<Threshold>> emptyMap = new HashMap<String, List<Threshold>>();
			return emptyMap;
		}
		return toReturn;
	}

	private Map<Integer, NodeComparator> toComparatorMap(List<Map<String, Object>> sortKeyMap) {
		Map<Integer, NodeComparator> sortKeys = new HashMap<Integer, NodeComparator>();

		for (int s = 0; s < sortKeyMap.size(); s++) {
			Map<String, Object> sMap = sortKeyMap.get(s);
			NodeComparator nc = new NodeComparator();

			nc.setParentValue((String) sMap.get("parentValue"));
			nc.setMeasureLabel((String) sMap.get("measureLabel"));
			if (sMap.get("direction") != null) {
				// the values in sMap sometimes have decimal part (es. "1.0"), so we need to parse them as double and then convert them to int
				nc.setDirection(Double.valueOf(sMap.get("direction").toString()).intValue());
				sortKeys.put(Double.valueOf(sMap.get("column").toString()).intValue(), nc);
			}
		}
		return sortKeys;
	}

	private String getCockpitSheetName(JSONObject template, long widgetId) {
		try {
			JSONArray sheets = template.getJSONArray("sheets");
			if (sheets.length() == 1)
				return "";
			for (int i = 0; i < sheets.length(); i++) {
				JSONObject sheet = sheets.getJSONObject(i);
				JSONArray widgets = sheet.getJSONArray("widgets");
				for (int j = 0; j < widgets.length(); j++) {
					JSONObject widget = widgets.getJSONObject(j);
					if (widgetId == widget.getLong("id"))
						return sheet.getString("label");
				}
			}
			return "";
		} catch (Exception e) {
			logger.error("Unable to retrieve cockpit sheet name from template", e);
			return "";
		}
	}

	private String getWidgetName(JSONObject widget) throws JSONException {
		String widgetName = null;
		JSONObject style = widget.optJSONObject("style");
		if (style != null) {
			JSONObject title = style.optJSONObject("title");
			if (title != null) {
				widgetName = title.optString("label");
			} else {
				JSONObject content = widget.optJSONObject("content");
				if (content != null) {
					widgetName = content.getString("name");
				}
			}
		}
		return widgetName;
	}

	private JSONObject getWidgetById(JSONObject template, long widgetId) {
		try {

			JSONArray sheets = template.getJSONArray("sheets");
			for (int i = 0; i < sheets.length(); i++) {
				JSONObject sheet = sheets.getJSONObject(i);
				JSONArray widgets = sheet.getJSONArray("widgets");
				for (int j = 0; j < widgets.length(); j++) {
					JSONObject widget = widgets.getJSONObject(j);
					long id = widget.getLong("id");
					if (id == widgetId) {
						return widget;
					}
				}
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while getting widget with id [" + widgetId + "] from template", e);
		}
		throw new SpagoBIRuntimeException("Unable to find widget with id [" + widgetId + "] in template");
	}

}
