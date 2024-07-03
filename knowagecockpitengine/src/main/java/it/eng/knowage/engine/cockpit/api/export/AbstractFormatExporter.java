/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.
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

package it.eng.knowage.engine.cockpit.api.export;

import java.awt.Color;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.commons.lang3.StringUtils;

import it.eng.knowage.engine.cockpit.api.export.pdf.CssColorParser;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.i18n.dao.I18NMessagesDAO;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.SolrDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public abstract class AbstractFormatExporter {
	private static final Logger LOGGER = LogManager.getLogger(AbstractFormatExporter.class);

	protected static final String DATE_FORMAT = "dd/MM/yyyy";
	protected static final CssColorParser CSS_COLOR_PARSER = CssColorParser.getInstance();

	public static final String UNIQUE_ALIAS_PLACEHOLDER = "_$_";
	public static final String TIMESTAMP_FORMAT = "dd/MM/yyyy HH:mm:ss.SSS";

	protected final JSONObject body;
	protected final String userUniqueIdentifier;
	protected Locale locale;
	// TODO : Do we really need a "state" instance here instead of a local variable?
	protected List<Integer> hiddenColumns;
	protected Map<String, String> i18nMessages;
	protected Map<Integer, XSSFCellStyle> formatHash2CellStyle = new HashMap<>();

	protected AbstractFormatExporter(String userUniqueIdentifier, JSONObject body) {
		this.userUniqueIdentifier = userUniqueIdentifier;
		this.body = body;
		locale = getLocaleFromBody(body);
	}

	private Locale getLocaleFromBody(JSONObject body) {
		try {
			String language = body.getString(SpagoBIConstants.SBI_LANGUAGE);
			String country = body.getString(SpagoBIConstants.SBI_COUNTRY);
			return new Locale(language, country);
		} catch (Exception e) {
			LOGGER.warn("Cannot get locale information from input parameters body", e);
			return Locale.ENGLISH;
		}

	}

	protected Map<String, String> getMapFromGroupsArray(JSONArray groupsArray, JSONArray aggr) {
		Map<String, String> returnMap = new HashMap<>();
		try {
			if (aggr != null && groupsArray != null) {

				for (int i = 0; i < groupsArray.length(); i++) {

					String id = groupsArray.getJSONObject(i).getString("id");
					String groupName = groupsArray.getJSONObject(i).getString("name");

					for (int ii = 0; ii < aggr.length(); ii++) {
						JSONObject column = aggr.getJSONObject(ii);

						if (column.has("group") && column.getString("group").equals(id)) {
//							String nameToInsert = getTableColumnHeaderValue(column);
							String nameToInsert = column.getString("aliasToShow");
							returnMap.put(nameToInsert, groupName);
						}

					}
				}
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Couldn't create map from groups array", e);
		}
		return returnMap;

	}

	protected String getWidgetTypeFromCockpitTemplate(String templateString, long widgetId) {
		try {
			JSONObject templateJson = new JSONObject(templateString);
			JSONArray sheets = templateJson.getJSONArray("sheets");
			for (int i = 0; i < sheets.length(); i++) {
				JSONObject sheet = sheets.getJSONObject(i);
				JSONArray widgets = sheet.getJSONArray("widgets");
				for (int j = 0; j < widgets.length(); j++) {
					JSONObject widget = widgets.getJSONObject(j);
					long currWidgetId = widget.getLong("id");
					if (currWidgetId == widgetId) {
						return widget.getString("type");
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("Couldn't get widget {} type, it will be exported as a normal widget.", widgetId);
		}
		LOGGER.error("Couldn't get widget {} type, it will be exported as a normal widget.", widgetId);
		return "";
	}

	protected List<Integer> getHiddenColumnsList(JSONArray columns) {
		List<Integer> localHiddenColumns = new ArrayList<>();
		try {
			for (int i = 0; i < columns.length(); i++) {
				JSONObject column = columns.getJSONObject(i);
				// check if column is hidden with flag "Hide column"
				if (column.has("style")) {
					JSONObject style = column.optJSONObject("style");
					if (style.has("hiddenColumn")) {
						if (style.getString("hiddenColumn").equals("true")) {
							localHiddenColumns.add(i);
						}
					}
				}
				// check if columns is hidden by using variables
				if (column.has("variables")) {
					JSONArray variables = column.getJSONArray("variables");
					for (int j = 0; j < variables.length(); j++) {
						JSONObject variable = variables.getJSONObject(j);
						if (variable.optString("action").equalsIgnoreCase("hide")) {
							if (variableMustHideColumn(column, variable))
								localHiddenColumns.add(i);
						}
					}
				}
			}
			return localHiddenColumns;
		} catch (Exception e) {
			LOGGER.error("Error while getting hidden columns list", e);
			return new ArrayList<>();
		}
	}

	protected boolean variableMustHideColumn(JSONObject column, JSONObject variable) {
		try {
			String variableValue = "";
			Object value = getCockpitVariables().get(variable.getString("variable"));
			if (value instanceof String) {
				// static variable
				variableValue = (String) value;
			} else if (value instanceof JSONObject) {
				// dataset variable
				String key = variable.optString("key");
				variableValue = ((JSONObject) value).optString(key);
			}
			String condition = variable.getString("condition");
			switch (condition) {
			case "==":
				if (variable.getString("value").equals(variableValue))
					return true;
				break;
			case "!=":
				if (!variable.getString("value").equals(variableValue))
					return true;
				break;
			case ">":
				if (variable.getString("value").compareTo(variableValue) > 0)
					return true;
				break;
			case "<":
				if (variable.getString("value").compareTo(variableValue) < 0)
					return true;
				break;
			case ">=":
				if (variable.getString("value").compareTo(variableValue) >= 0)
					return true;
				break;
			case "<=":
				if (variable.getString("value").compareTo(variableValue) <= 0)
					return true;
				break;
			default:
				break;
			}
			return false;
		} catch (Exception e) {
			LOGGER.error("Error while evaluating if column must be hidden according to variable.", e);
			return false;
		}
	}

	protected JSONArray getTableOrderedColumns(JSONArray columnsNew, JSONArray columnsOld) {
		JSONArray columnsOrdered = new JSONArray();
		// new columns are in the correct order
		// for each of them we have to find the correspondent old column and push it into columnsOrdered
		try {
			for (int i = 0; i < columnsNew.length(); i++) {

				if (hiddenColumns.contains(i))
					continue;

				JSONObject columnNew = columnsNew.getJSONObject(i);

//				String newHeader = getTableColumnHeaderValue(columnNew);

				for (int j = 0; j < columnsOld.length(); j++) {
					JSONObject columnOld = columnsOld.getJSONObject(j);
					if (columnOld.getString("header").equals(columnNew.getString("aliasToShow"))) {

						if (columnNew.has("ranges")) {
							JSONArray ranges = columnNew.getJSONArray("ranges");
							columnOld.put("ranges", ranges); // added ranges for column thresholds
						}

						columnsOrdered.put(columnOld);
						break;
					}
				}
			}
			return columnsOrdered;
		} catch (Exception e) {
			LOGGER.error("Error retrieving ordered columns");
			return new JSONArray();
		}
	}

	protected String getTableColumnHeaderValue(JSONObject column) {
		try {
			if (column.has("variables")) {
				JSONArray variables = column.getJSONArray("variables");
				for (int i = 0; i < variables.length(); i++) {
					JSONObject variable = variables.getJSONObject(i);
					if (variable.getString("action").equalsIgnoreCase("header"))
						return getCockpitVariables().getString(variable.getString("variable"));
				}
				return column.getString("aliasToShow");
			} else
				return column.getString("aliasToShow");
		} catch (Exception e) {
			LOGGER.error("Error retrieving table column header values.", e);
			return "";
		}
	}

	protected JSONObject getCockpitVariables() {
		try {
			if (body.get("COCKPIT_VARIABLES") instanceof JSONObject)
				return body.getJSONObject("COCKPIT_VARIABLES");
			else
				return body.getJSONArray("COCKPIT_VARIABLES").getJSONObject(0);
		} catch (JSONException e) {
			LOGGER.error("Cannot retrieve cockpit variables", e);
			return new JSONObject();
		}
	}

	protected JSONObject getWidgetById(JSONObject template, long widgetId) {
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

	public JSONObject getDataStoreForWidget(JSONObject template, JSONObject widget, int offset, int fetchSize) {
		Map<String, Object> map = new java.util.HashMap<>();
		JSONObject datastore = null;
		try {
			JSONObject configuration = template.getJSONObject("configuration");
			JSONObject datasetObj = widget.getJSONObject("dataset");
			int datasetId = datasetObj.getInt("dsId");
			IDataSet dataset = DAOFactory.getDataSetDAO().loadDataSetById(datasetId);
			String datasetLabel = dataset.getLabel();

			if (getRealtimeFromWidget(datasetId, configuration))
				map.put("nearRealtime", true);

			JSONObject cockpitSelections = getCockpitSelectionsFromBody(widget);

			JSONArray summaryRow = getSummaryRowFromWidget(widget);
			if (summaryRow != null)
				cockpitSelections.put("summaryRow", summaryRow);

			if (isSolrDataset(dataset) && !widget.getString("type").equalsIgnoreCase("discovery")) {
				JSONObject jsOptions = new JSONObject();
				jsOptions.put("solrFacetPivot", true);
				cockpitSelections.put("options", jsOptions);
			}

			datastore = getDatastore(datasetLabel, map, cockpitSelections.toString(), offset, fetchSize);
			datastore.put("widgetData", widget);

		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error getting datastore for widget [type=" + widget.optString("type")
					+ "] [id=" + widget.optLong("id") + "]", e);
		}
		return datastore;
	}

	protected JSONObject getDatastore(String datasetLabel, Map<String, Object> map, String selections, int offset,
			int fetchSize) {
		ExporterClient client = new ExporterClient();
		try {
			return client.getDataStore(map, datasetLabel, userUniqueIdentifier, selections, offset, fetchSize);
		} catch (Exception e) {
			String message = "Unable to get data";
			LOGGER.error(message, e);
			throw new SpagoBIRuntimeException(message);
		}
	}

	protected String getInternationalizedHeader(String columnName) {
		if (i18nMessages == null) {
			I18NMessagesDAO messageDao = DAOFactory.getI18NMessageDAO();
			try {
				i18nMessages = messageDao.getAllI18NMessages(locale);
			} catch (Exception e) {
				LOGGER.error("Error while getting i18n messages", e);
				i18nMessages = new HashMap<>();
			}
		}
		return i18nMessages.getOrDefault(columnName, columnName);
	}

	protected boolean isSolrDataset(IDataSet dataSet) {
		if (dataSet instanceof VersionedDataSet) {
			dataSet = ((VersionedDataSet) dataSet).getWrappedDataset();
		}
		return dataSet instanceof SolrDataSet;
	}

	protected JSONObject[] getColumnsStyles(JSONArray columnsOrdered, JSONObject widgetContent) {
		try {
			JSONObject[] toReturn = new JSONObject[columnsOrdered.length() + 10];
			JSONArray columns = widgetContent.getJSONArray("columnSelectedOfDataset");
			for (int i = 0; i < columnsOrdered.length(); i++) {
				JSONObject orderedCol = columnsOrdered.getJSONObject(i);
				for (int j = 0; j < columns.length(); j++) {
					JSONObject col = columns.getJSONObject(j);
					if (col.has("aliasToShow")
//							&& orderedCol.getString("header").equals(getTableColumnHeaderValue(col))) {
							&& orderedCol.getString("header").equals(col.getString("aliasToShow"))) {
						if (col.has("style")) {
							toReturn[i] = col.getJSONObject("style");
						}
						break;
					}
				}
			}
			return toReturn;
		} catch (Exception e) {
			LOGGER.error("Error while retrieving table columns styles.", e);
			return new JSONObject[columnsOrdered.length() + 10];
		}
	}

	protected JSONArray getSummaryRowFromWidget(JSONObject widget) {
		try {
			JSONObject settings = widget.optJSONObject("settings");
			JSONArray jsonArrayForSummary = new JSONArray();
			if (settings != null) {
				JSONObject summary = settings.optJSONObject("summary");
				if (settings.has("summary") && summary.has("enabled") && summary.optBoolean("enabled")) {

					if (summary.has("list")) {
						JSONArray listArray = summary.getJSONArray("list");

						if (listArray.length() > 1) {
							for (int jj = 0; jj < listArray.length(); jj++) {

								JSONObject aggrObj = listArray.getJSONObject(jj);

								if (!aggrObj.has("aggregation")) {

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

													String formula = column.optString("formula");
													String name = formula.isEmpty() ? column.optString("name")
															: formula;
													if (column.has("formula")) {
														measure.put("formula", name);
													} else
														measure.put("columnName", name);

													measure.put("funct", column.getString("funcSummary"));

													boolean hidden = false;

													if (column.has("style")) {

														JSONObject style = column.optJSONObject("style");
														if (style != null) {

															String hideSummary = style.optString("hideSummary");

															if (hideSummary != null && !hideSummary.isEmpty()
																	&& hideSummary.equalsIgnoreCase("true")) {
																hidden = true;
															}

														}

													}
													if (!hidden)
														measures.put(measure);
												}
											}
										}
									}
									JSONObject summaryRow = new JSONObject();
									summaryRow.put("measures", measures);

									JSONObject dataset = widget.optJSONObject("dataset");
									if (dataset != null) {
										int dsId = dataset.getInt("dsId");
										summaryRow.put("dataset", dsId);
									}

									jsonArrayForSummary.put(summaryRow);

								} else {

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

													String formula = column.optString("formula");
													String name = formula.isEmpty() ? column.optString("name")
															: formula;
													if (column.has("formula")) {
														measure.put("formula", name);
													} else
														measure.put("columnName", name);

													measure.put("funct", aggrObj.get("aggregation"));

													boolean hidden = false;

													if (column.has("style")) {

														JSONObject style = column.optJSONObject("style");
														if (style != null) {

															String hideSummary = style.optString("hideSummary");

															if (hideSummary != null && !hideSummary.isEmpty()
																	&& hideSummary.equalsIgnoreCase("true")) {
																hidden = true;
															}

														}

													}
													if (!hidden)
														measures.put(measure);
												}
											}
										}
									}
									JSONObject summaryRow = new JSONObject();
									summaryRow.put("measures", measures);

									JSONObject dataset = widget.optJSONObject("dataset");
									if (dataset != null) {
										int dsId = dataset.getInt("dsId");
										summaryRow.put("dataset", dsId);
									}

									jsonArrayForSummary.put(summaryRow);

								}

							}
						} else {
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

											String formula = column.optString("formula");
											String name = formula.isEmpty() ? column.optString("name") : formula;
											if (column.has("formula")) {
												measure.put("formula", name);
											} else
												measure.put("columnName", name);

											measure.put("funct", column.getString("funcSummary"));

											boolean hidden = false;

											if (column.has("style")) {

												JSONObject style = column.optJSONObject("style");
												if (style != null) {

													String hideSummary = style.optString("hideSummary");

													if (hideSummary != null && !hideSummary.isEmpty()
															&& hideSummary.equalsIgnoreCase("true")) {
														hidden = true;
													}

												}

											}
											if (!hidden)
												measures.put(measure);
										}
									}
								}
							}
							JSONObject summaryRow = new JSONObject();
							summaryRow.put("measures", measures);

							JSONObject dataset = widget.optJSONObject("dataset");
							if (dataset != null) {
								int dsId = dataset.getInt("dsId");
								summaryRow.put("dataset", dsId);
							}

							jsonArrayForSummary.put(summaryRow);
						}
					}
					return jsonArrayForSummary;
				}
			}
			return null;
		} catch (Exception e) {
			throw new SpagoBIRuntimeException(e);
		}
	}

	protected abstract JSONObject getCockpitSelectionsFromBody(JSONObject widget);

	protected boolean getRealtimeFromWidget(int dsId, JSONObject configuration) {
		try {
			JSONObject dataset = getDataset(dsId, configuration);
			return !dataset.optBoolean("useCache");
		} catch (Exception e) {
			throw new SpagoBIRuntimeException(e);
		}
	}

	protected JSONObject getDataset(int dsId, JSONObject configuration) {
		try {
			JSONArray datasets = configuration.getJSONArray("datasets");
			for (int i = 0; i < datasets.length(); i++) {
				JSONObject dataset = (JSONObject) datasets.get(i);
				int id = dataset.getInt("dsId");
				if (id == dsId) {
					return dataset;
				}
			}
			return null;
		} catch (Exception e) {
			throw new SpagoBIRuntimeException(e);
		}
	}

	public Locale getLocale() {
		return locale;
	}

	protected void forceUniqueHeaders(JSONObject cockpitSelections) throws JSONException {
		JSONObject aggregations = cockpitSelections.getJSONObject("aggregations");
		JSONArray measures = aggregations.getJSONArray("measures");
		manipulateDimensions(measures);
		JSONArray categories = aggregations.getJSONArray("categories");
		manipulateDimensions(categories);
	}

	protected void manipulateDimensions(JSONArray dimensions) throws JSONException {
		Set<String> dimensionsAliases = new HashSet<>();
		for (int i = 0; i < dimensions.length(); i++) {
			JSONObject d = dimensions.getJSONObject(i);
			String alias = d.getString("alias");
			if (dimensionsAliases.contains(alias)) {
				d.put("alias", alias + UNIQUE_ALIAS_PLACEHOLDER + i);
			}
			dimensionsAliases.add(alias);
		}
	}

	protected JSONArray filterDataStoreColumns(JSONArray columns) {
		try {
			for (int i = 0; i < columns.length(); i++) {
				String element = columns.getString(i);
				if (element != null && element.equals("recNo")) {
					columns.remove(i);
					break;
				}
			}
		} catch (JSONException e) {
			LOGGER.error("Can not filter Columns Array");
		}
		return columns;
	}

	protected CellStyle getIntCellStyle(Workbook wb, CreationHelper helper, JSONObject column, JSONObject colStyle,
			String defaultFormat, JSONObject settings, Integer value, JSONObject rowObject,
			HashMap<String, String> mapColumns, HashMap<String, String> mapColumnsTypes,
			HashMap<String, Object> variablesMap, HashMap<String, Object> parametersMap) {
		String colName = null;
		XSSFCellStyle toReturn = getCellStyleByFormat(wb, helper, defaultFormat, Optional.empty(), Optional.empty());
		try {
			colName = column.getString("name");
			boolean isAvoidSeparator = isAvoidSeparator(colStyle);
			String format = null;
			if (isAvoidSeparator) {
				format = "0";
			} else {
				format = "#,##0";
			}

			// precision (i.e. number of digits to right of the decimal point) that is specified on dashboard design wins
			if ((colStyle != null && colStyle.has("precision")) || isAvoidSeparator) {
				int precision = (colStyle != null && colStyle.has("precision")) ? colStyle.getInt("precision") : 2;
				format = getNumberFormatByPrecision(precision, format);
				return getCellStyleByFormat(wb, helper, format, Optional.empty(), Optional.empty());
			}

			if (settings != null)
				toReturn = getRowStyle(wb, helper, settings, rowObject, mapColumns, defaultFormat, mapColumnsTypes,
						variablesMap, parametersMap);

			if (column.has("ranges")) {
				JSONArray ranges = column.getJSONArray("ranges");
				String formatStr = "#,##0.00";
				FillPatternType fillPatternType = null;
				Color color = null;
				for (int jj = 0; jj < ranges.length(); jj++) {

					JSONObject threshold = ranges.getJSONObject(jj);

					if (threshold.has("compareValueType")) {
						Color userColor = parseColor(threshold.getString("background-color"), Color.white);
						if (threshold.has("value")) {
							Integer valueToPut = null;
							if (threshold.getString("compareValueType").equals("static"))
								valueToPut = threshold.getInt("value");
							else if (threshold.getString("compareValueType").equals("variable")) {
								valueToPut = Integer
										.parseInt(variablesMap.get(threshold.getString("value")).toString());
							} else if (threshold.getString("compareValueType").equals("parameter")
									&& parametersMap.get(threshold.getString("value")) != null) {
								valueToPut = Integer
										.parseInt(parametersMap.get(threshold.getString("value")).toString());
							} else {
								break;
							}
							if (threshold.getString("operator").equals(">")) {

								if (value.intValue() > valueToPut.intValue()) {

									fillPatternType = FillPatternType.SOLID_FOREGROUND;
									color = userColor;

								}

							} else if (threshold.getString("operator").equals("<")) {

								if (value.intValue() < valueToPut.intValue()) {
									fillPatternType = FillPatternType.SOLID_FOREGROUND;
									color = userColor;
								}

							} else if (threshold.getString("operator").equals("==")) {

								if (value.intValue() == valueToPut.intValue()) {
									fillPatternType = FillPatternType.SOLID_FOREGROUND;
									color = userColor;
								}

							} else if (threshold.getString("operator").equals("<=")) {

								if (value.intValue() <= valueToPut.intValue()) {
									fillPatternType = FillPatternType.SOLID_FOREGROUND;
									color = userColor;
								}

							} else if (threshold.getString("operator").equals(">=")) {

								if (value.intValue() >= valueToPut.intValue()) {
									fillPatternType = FillPatternType.SOLID_FOREGROUND;
									color = userColor;
								}

							} else if (threshold.getString("operator").equals("!=")) {

								if (value.intValue() != valueToPut.intValue()) {
									fillPatternType = FillPatternType.SOLID_FOREGROUND;
									color = userColor;
								}

							}
						} else {
							if (threshold.getString("operator").equals("IN")) {

								String[] valueArray = threshold.getString("valueArray").replace("[", "")
										.replace("]", "").replace("\"", "").split(",");
								int[] numbers = new int[valueArray.length];
								for (int i = 0; i < valueArray.length; i++) {
									numbers[i] = Integer.parseInt(valueArray[i]);
								}
								if (containsInt(numbers, value)) {
									fillPatternType = FillPatternType.SOLID_FOREGROUND;
									color = userColor;
								}
							}
						}
					}

				}

				toReturn = getCellStyleByFormat(wb, helper, formatStr, Optional.ofNullable(fillPatternType),
						Optional.ofNullable(color));

			}

			return toReturn;
		} catch (Exception e) {
			LOGGER.error("Error while building column {} CellStyle. Default style will be used.", colName, e);
			return toReturn;
		}
	}

	protected CellStyle getDoubleCellStyle(Workbook wb, CreationHelper helper, JSONObject column, JSONObject colStyle,
			String defaultFormat, JSONObject settings, Double value, JSONObject rowObject,
			HashMap<String, String> mapColumns, HashMap<String, String> mapColumnsTypes,
			HashMap<String, Object> variablesMap, HashMap<String, Object> parametersMap) {
		String colName = null;
		CreationHelper createHelper = wb.getCreationHelper();
		XSSFCellStyle toReturn = getCellStyleByFormat(wb, helper, defaultFormat, Optional.empty(), Optional.empty());
		toReturn.setDataFormat(createHelper.createDataFormat().getFormat("#,##0.00"));
		try {
			colName = column.getString("name");
			boolean isAvoidSeparator = isAvoidSeparator(colStyle);
			String format = null;
			if (isAvoidSeparator) {
				format = "0";
			} else {
				format = "#,##0";
			}

			// precision (i.e. number of digits to right of the decimal point) that is specified on dashboard design wins
			if ((colStyle != null && colStyle.has("precision")) || isAvoidSeparator) {
				int precision = (colStyle != null && colStyle.has("precision")) ? colStyle.getInt("precision") : 2;
				format = getNumberFormatByPrecision(precision, format);
				return getCellStyleByFormat(wb, helper, format, Optional.empty(), Optional.empty());
			}

			if (settings != null)
				toReturn = getRowStyle(wb, helper, settings, rowObject, mapColumns, defaultFormat, mapColumnsTypes,
						variablesMap, parametersMap);

			if (column.has("ranges")) {
				JSONArray ranges = column.getJSONArray("ranges");
				String formatStr = "#,##0.00";
				FillPatternType fillPatternType = null;
				Color color = null;
				for (int jj = 0; jj < ranges.length(); jj++) {

					JSONObject threshold = ranges.getJSONObject(jj);

					if (threshold.has("compareValueType") && threshold.has("background-color")) {
						Color userColor = parseColor(threshold.getString("background-color"), Color.white);
						if (threshold.has("value")) {
							Double valueToPut = null;
							if (threshold.getString("compareValueType").equals("static"))
								valueToPut = threshold.getDouble("value");
							else if (threshold.getString("compareValueType").equals("variable")) {
								valueToPut = Double
										.parseDouble(variablesMap.get(threshold.getString("value")).toString());
							} else if (threshold.getString("compareValueType").equals("parameter")
									&& parametersMap.get(threshold.getString("value")) != null) {
								valueToPut = Double
										.parseDouble(parametersMap.get(threshold.getString("value")).toString());
							} else {
								break;
							}
							if (threshold.getString("operator").equals(">")) {

								if (value.floatValue() > valueToPut.floatValue()) {
									fillPatternType = FillPatternType.SOLID_FOREGROUND;
									color = userColor;
								}

							} else if (threshold.getString("operator").equals("<")) {

								if (value.floatValue() < valueToPut.floatValue()) {
									fillPatternType = FillPatternType.SOLID_FOREGROUND;
									color = userColor;
								}

							} else if (threshold.getString("operator").equals("==")) {

								if (value.floatValue() == valueToPut.floatValue()) {
									fillPatternType = FillPatternType.SOLID_FOREGROUND;
									color = userColor;
								}

							} else if (threshold.getString("operator").equals("<=")) {

								if (value.floatValue() <= valueToPut.floatValue()) {
									fillPatternType = FillPatternType.SOLID_FOREGROUND;
									color = userColor;
								}

							} else if (threshold.getString("operator").equals(">=")) {

								if (value.floatValue() >= valueToPut.floatValue()) {
									fillPatternType = FillPatternType.SOLID_FOREGROUND;
									color = userColor;
								}

							} else if (threshold.getString("operator").equals("!=")) {

								if (value.floatValue() != valueToPut.floatValue()) {
									fillPatternType = FillPatternType.SOLID_FOREGROUND;
									color = userColor;
								}

							}
						} else {
							if (threshold.getString("operator").equals("IN")) {

								String[] valueArray = threshold.getString("valueArray").replace("[", "")
										.replace("]", "").replace("\"", "").split(",");
								double[] numbers = new double[valueArray.length];
								for (int i = 0; i < valueArray.length; i++) {
									numbers[i] = Double.parseDouble(valueArray[i]);
								}
								if (containsDouble(numbers, value)) {
									fillPatternType = FillPatternType.SOLID_FOREGROUND;
									color = userColor;
								}
							}
						}
					}

				}

				toReturn = getCellStyleByFormat(wb, helper, formatStr, Optional.ofNullable(fillPatternType),
						Optional.ofNullable(color));

			}

			return toReturn;
		} catch (Exception e) {
			LOGGER.error("Error while building column {} CellStyle. Default style will be used.", colName, e);
			return toReturn;
		}
	}

	private XSSFCellStyle getRowStyle(Workbook wb, CreationHelper helper, JSONObject settings, JSONObject rowObject,
			HashMap<String, String> mapColumns, String defaultFormat, HashMap<String, String> mapColumnsTypes,
			HashMap<String, Object> variablesMap, HashMap<String, Object> parametersMap) throws JSONException {

		FillPatternType fillPatternType = null;
		Color color = null;

		if (settings.has("rowThresholds")
				&& settings.getJSONObject("rowThresholds").getString("enabled").equals("true")) {

			JSONArray listOfThresholds = settings.getJSONObject("rowThresholds").getJSONArray("list");

			for (int i = 0; i < listOfThresholds.length(); i++) {

				JSONObject entry = listOfThresholds.getJSONObject(i);

				Color userColor = parseColor(entry.getJSONObject("style").getString("background-color"), Color.white);
				Object rowValueOBJ = rowObject.get(mapColumns.get(entry.getString("column")));

				String type = mapColumnsTypes.get(mapColumns.get(entry.getString("column")));

				if (type.equals("float")) {
					Double rowValue = null;

					Double valueToPut = null;

					if (entry.getString("compareValueType").equals("static")) {
						valueToPut = entry.getDouble("compareValue");
					} else if (entry.getString("compareValueType").equals("variable")) {
						valueToPut = Double.parseDouble(variablesMap.get(entry.getString("compareValue")).toString());
					} else if (entry.getString("compareValueType").equals("parameter")
							&& parametersMap.get(entry.getString("compareValue")) != null) {
						valueToPut = Double.parseDouble(parametersMap.get(entry.getString("compareValue")).toString());
					} else {
						break;
					}
					if (rowValueOBJ instanceof Integer) {
						rowValue = ((Integer) rowValueOBJ).doubleValue();
					} else
						rowValue = (Double) rowValueOBJ;

					if (entry.getString("condition").equals(">")) {

						if (rowValue.floatValue() > valueToPut.floatValue()) {

							fillPatternType = FillPatternType.SOLID_FOREGROUND;
							color = userColor;
						}

					} else if (entry.getString("condition").equals("<")) {

						if (rowValue.floatValue() < valueToPut.floatValue()) {
							fillPatternType = FillPatternType.SOLID_FOREGROUND;
							color = userColor;
						}

					} else if (entry.getString("condition").equals("==")) {

						if (rowValue.floatValue() == valueToPut.floatValue()) {
							fillPatternType = FillPatternType.SOLID_FOREGROUND;
							color = userColor;
						}

					} else if (entry.getString("condition").equals("<=")) {

						if (rowValue.floatValue() <= valueToPut.floatValue()) {
							fillPatternType = FillPatternType.SOLID_FOREGROUND;
							color = userColor;
						}

					} else if (entry.getString("condition").equals(">=")) {

						if (rowValue.floatValue() >= valueToPut.floatValue()) {
							fillPatternType = FillPatternType.SOLID_FOREGROUND;
							color = userColor;
						}

					} else if (entry.getString("condition").equals("!=")) {

						if (rowValue.floatValue() != valueToPut.floatValue()) {
							fillPatternType = FillPatternType.SOLID_FOREGROUND;
							color = userColor;
						}
					} else if (entry.getString("condition").equals("IN")) {

						String[] valueArray = entry.getString("valueArray").replace("[", "").replace("]", "")
								.replace("\"", "").split(",");
						double[] numbers = new double[valueArray.length];
						for (int ii = 0; ii < valueArray.length; ii++) {
							numbers[ii] = Double.parseDouble(valueArray[ii]);
						}
						if (containsDouble(numbers, rowValue)) {
							fillPatternType = FillPatternType.SOLID_FOREGROUND;
							color = userColor;
						}
					}
				}

				if (type.equals("integer")) {

					Integer valueToPut = null;

					if (entry.getString("compareValueType").equals("static")) {
						valueToPut = entry.getInt("compareValue");
					} else if (entry.getString("compareValueType").equals("variable")) {
						valueToPut = Integer.parseInt(variablesMap.get(entry.getString("compareValue")).toString());
					} else if (entry.getString("compareValueType").equals("parameter")
							&& parametersMap.get(entry.getString("compareValue")) != null) {
						valueToPut = Integer.parseInt(parametersMap.get(entry.getString("compareValue")).toString());
					} else {
						break;
					}
					Integer rowValue = (Integer) rowValueOBJ;

					if (entry.getString("condition").equals(">")) {

						if (rowValue.intValue() > valueToPut.intValue()) {

							fillPatternType = FillPatternType.SOLID_FOREGROUND;
							color = userColor;
						}

					} else if (entry.getString("condition").equals("<")) {

						if (rowValue.intValue() < valueToPut.intValue()) {
							fillPatternType = FillPatternType.SOLID_FOREGROUND;
							color = userColor;
						}

					} else if (entry.getString("condition").equals("==")) {

						if (rowValue.intValue() == valueToPut.intValue()) {
							fillPatternType = FillPatternType.SOLID_FOREGROUND;
							color = userColor;
						}

					} else if (entry.getString("condition").equals("<=")) {

						if (rowValue.intValue() <= valueToPut.intValue()) {
							fillPatternType = FillPatternType.SOLID_FOREGROUND;
							color = userColor;
						}

					} else if (entry.getString("condition").equals(">=")) {

						if (rowValue.intValue() >= valueToPut.intValue()) {
							fillPatternType = FillPatternType.SOLID_FOREGROUND;
							color = userColor;
						}

					} else if (entry.getString("condition").equals("!=")) {

						if (rowValue.intValue() != valueToPut.intValue()) {
							fillPatternType = FillPatternType.SOLID_FOREGROUND;
							color = userColor;
						}
					} else if (entry.getString("condition").equals("IN")) {

						String[] valueArray = entry.getString("valueArray").replace("[", "").replace("]", "")
								.replace("\"", "").split(",");
						int[] numbers = new int[valueArray.length];
						for (int ii = 0; ii < valueArray.length; ii++) {
							numbers[ii] = Integer.parseInt(valueArray[ii]);
						}
						if (containsInt(numbers, rowValue)) {
							fillPatternType = FillPatternType.SOLID_FOREGROUND;
							color = userColor;
						}
					}
				}

				if (type.equals("string")) {

					String valueToPut = null;

					if (entry.getString("compareValueType").equals("static")) {
						valueToPut = entry.getString("compareValue");
					} else if (entry.getString("compareValueType").equals("variable")) {
						valueToPut = variablesMap.get(entry.getString("compareValue")).toString();
					} else if (entry.getString("compareValueType").equals("parameter")
							&& parametersMap.get(entry.getString("compareValue")) != null) {
						valueToPut = parametersMap.get(entry.getString("compareValue")).toString();
					} else {
						break;
					}
					String rowValue = rowValueOBJ.toString();

					if (entry.getString("condition").equals(">")) {

						if (rowValue.compareTo(valueToPut) > 0) {

							fillPatternType = FillPatternType.SOLID_FOREGROUND;
							color = userColor;
						}

					} else if (entry.getString("condition").equals("<")) {

						if (rowValue.compareTo(valueToPut) < 0) {
							fillPatternType = FillPatternType.SOLID_FOREGROUND;
							color = userColor;
						}

					} else if (entry.getString("condition").equals("==")) {

						if (rowValue.equals(valueToPut)) {
							fillPatternType = FillPatternType.SOLID_FOREGROUND;
							color = userColor;
						}

					} else if (entry.getString("condition").equals("<=")) {

						if (rowValue.compareTo(valueToPut) <= 0) {
							fillPatternType = FillPatternType.SOLID_FOREGROUND;
							color = userColor;
						}

					} else if (entry.getString("condition").equals(">=")) {

						if (rowValue.compareTo(valueToPut) >= 0) {
							fillPatternType = FillPatternType.SOLID_FOREGROUND;
							color = userColor;
						}

					} else if (entry.getString("condition").equals("!=")) {

						if (!rowValue.equals(valueToPut)) {
							fillPatternType = FillPatternType.SOLID_FOREGROUND;
							color = userColor;
						}
					} else if (entry.getString("condition").equals("IN")) {

						String[] valueArray = entry.getString("valueArray").replace("[", "").replace("]", "")
								.replace("\"", "").split(",");
						if (Arrays.stream(valueArray).anyMatch(rowValue::equals)) {
							fillPatternType = FillPatternType.SOLID_FOREGROUND;
							color = userColor;
						}
					}
				}

			}
		}
		return getCellStyleByFormat(wb, helper, defaultFormat, Optional.ofNullable(fillPatternType),
				Optional.ofNullable(color));

	}

	public boolean containsInt(final int[] array, final int key) {
		return ArrayUtils.contains(array, key);
	}

	public boolean containsDouble(final double[] array, final double key) {
		return ArrayUtils.contains(array, key);
	}

	protected CellStyle getStringCellStyle(Workbook wb, CreationHelper helper, JSONObject column, JSONObject colStyle,
			String defaultFormat, JSONObject settings, String value, JSONObject rowObject,
			HashMap<String, String> mapColumns, HashMap<String, String> mapColumnsTypes,
			HashMap<String, Object> variablesMap, HashMap<String, Object> parametersMap) {
		String colName = null;
		XSSFCellStyle toReturn = getCellStyleByFormat(wb, helper, defaultFormat, Optional.empty(), Optional.empty());
		try {
			colName = column.getString("name");
			boolean isAvoidSeparator = isAvoidSeparator(colStyle);
			String format = null;
			if (isAvoidSeparator) {
				format = "0";
			} else {
				format = "#,##0";
			}

			// precision (i.e. number of digits to right of the decimal point) that is specified on dashboard design wins
			if ((colStyle != null && colStyle.has("precision")) || isAvoidSeparator) {
				int precision = (colStyle != null && colStyle.has("precision")) ? colStyle.getInt("precision") : 2;
				format = getNumberFormatByPrecision(precision, format);
				return getCellStyleByFormat(wb, helper, format, Optional.empty(), Optional.empty());
			}

			if (settings != null)
				toReturn = getRowStyle(wb, helper, settings, rowObject, mapColumns, defaultFormat, mapColumnsTypes,
						variablesMap, parametersMap);

			if (column.has("ranges")) {
				JSONArray ranges = column.getJSONArray("ranges");
				String formatStr = "#,##0.00";
				FillPatternType fillPatternType = null;
				Color color = null;

				for (int jj = 0; jj < ranges.length(); jj++) {

					JSONObject threshold = ranges.getJSONObject(jj);

					if (threshold.has("compareValueType") && threshold.has("background-color")) {
						Color userColor = parseColor(threshold.getString("background-color"), Color.white);
						if (threshold.has("value")) {
							String valueToPut = null;
							if (threshold.getString("compareValueType").equals("static"))
								valueToPut = threshold.getString("value");
							else if (threshold.getString("compareValueType").equals("variable")) {
								valueToPut = variablesMap.get(threshold.getString("value")).toString();
							} else if (threshold.getString("compareValueType").equals("parameter")
									&& parametersMap.get(threshold.getString("value")) != null) {
								valueToPut = parametersMap.get(threshold.getString("value")).toString();
							} else {
								break;
							}

							if (threshold.getString("operator").equals(">")) {

								if (value.compareTo(valueToPut) > 0) {
									fillPatternType = FillPatternType.SOLID_FOREGROUND;
									color = userColor;
								}

							} else if (threshold.getString("operator").equals("<")) {

								if (value.compareTo(valueToPut) < 0) {
									fillPatternType = FillPatternType.SOLID_FOREGROUND;
									color = userColor;
								}

							} else if (threshold.getString("operator").equals("==")) {

								if (value.equals(valueToPut)) {
									fillPatternType = FillPatternType.SOLID_FOREGROUND;
									color = userColor;
								}

							} else if (threshold.getString("operator").equals("<=")) {

								if (value.compareTo(valueToPut) <= 0) {
									fillPatternType = FillPatternType.SOLID_FOREGROUND;
									color = userColor;
								}

							} else if (threshold.getString("operator").equals(">=")) {

								if (value.compareTo(valueToPut) >= 0) {
									fillPatternType = FillPatternType.SOLID_FOREGROUND;
									color = userColor;
								}

							} else if (threshold.getString("operator").equals("!=")) {

								if (!value.equals(valueToPut)) {
									fillPatternType = FillPatternType.SOLID_FOREGROUND;
									color = userColor;
								}

							}
						} else {
							if (threshold.getString("operator").equals("IN")) {
								String[] valueArray = threshold.getString("valueArray").replace("[", "")
										.replace("]", "").replace("\"", "").split(",");
								if (Arrays.stream(valueArray).anyMatch(value::equals)) {
									fillPatternType = FillPatternType.SOLID_FOREGROUND;
									color = userColor;
								}

							}
						}
					}

				}

				toReturn = getCellStyleByFormat(wb, helper, formatStr, Optional.ofNullable(fillPatternType),
						Optional.ofNullable(color));

			}

			return toReturn;
		} catch (Exception e) {
			LOGGER.error("Error while building column {} CellStyle. Default style will be used.", colName, e);
			return toReturn;
		}
	}

	protected CellStyle getDateCellStyle(Workbook wb, CreationHelper helper, JSONObject column, JSONObject colStyle,
			String defaultDateFormat, JSONObject settings, JSONObject rowObject, HashMap<String, String> mapColumns,
			HashMap<String, String> mapColumnsTypes, HashMap<String, Object> variablesMap,
			HashMap<String, Object> parametersMap) {
		String colName = null;
		XSSFCellStyle toReturn = getCellStyleByFormat(wb, helper, defaultDateFormat, Optional.empty(),
				Optional.empty());
		try {
			if (settings != null)
				toReturn = getRowStyle(wb, helper, settings, rowObject, mapColumns, defaultDateFormat, mapColumnsTypes,
						variablesMap, parametersMap);
			return toReturn;
		} catch (Exception e) {
			LOGGER.error("Error while building column {} CellStyle. Default style will be used.", colName, e);
			return toReturn;
		}
	}

	protected CellStyle getGenericCellStyle(Workbook wb, CreationHelper helper, JSONObject column, JSONObject colStyle,
			String defaultFormat, JSONObject settings, JSONObject rowObject, HashMap<String, String> mapColumns,
			HashMap<String, String> mapColumnsTypes, HashMap<String, Object> variablesMap,
			HashMap<String, Object> parametersMap) {
		String colName = null;
		XSSFCellStyle toReturn = getCellStyleByFormat(wb, helper, defaultFormat, Optional.empty(), Optional.empty());
		try {
			if (settings != null)
				toReturn = getRowStyle(wb, helper, settings, rowObject, mapColumns, defaultFormat, mapColumnsTypes,
						variablesMap, parametersMap);

			return toReturn;
		} catch (Exception e) {
			LOGGER.error("Error while building column {} CellStyle. Default style will be used.", colName, e);
			return toReturn;
		}
	}

	public static Color parseColor(String input, Color defaultColor) {
		return CSS_COLOR_PARSER.parse(input, defaultColor);
	}

	private boolean isAvoidSeparator(JSONObject colStyle) throws JSONException {
		if (colStyle != null && colStyle.has("asString")) {
			if (colStyle.getBoolean("asString")) {
				return true;
			}
		}
		return false;
	}

	/*
	 * This method avoids cell style objects number to increase by rows number (see https://production.eng.it/jira/browse/KNOWAGE-6692 and
	 * https://production.eng.it/jira/browse/KNOWAGE-6693)
	 */
	protected final XSSFCellStyle getCellStyleByFormat(Workbook wb, CreationHelper helper, String format,
			Optional<FillPatternType> fillPatternTypeOpt, Optional<Color> colorOpt) {
		Integer styleKey = getStyleKey(format, fillPatternTypeOpt, colorOpt);
		formatHash2CellStyle.computeIfAbsent(styleKey,
				key -> doCreateCellStyle(wb, helper, format, fillPatternTypeOpt, colorOpt));
		return formatHash2CellStyle.get(styleKey);
	}

	private final XSSFCellStyle doCreateCellStyle(Workbook wb, CreationHelper helper, String format,
			Optional<FillPatternType> fillPatternTypeOpt, Optional<Color> colorOpt) {

		LOGGER.debug("New style created for format {}, fill pattern {} and color {}", format, fillPatternTypeOpt,
				colorOpt);

		XSSFCellStyle cellStyle = (XSSFCellStyle) wb.createCellStyle();
		cellStyle.setDataFormat(helper.createDataFormat().getFormat(format));
		fillPatternTypeOpt.ifPresent(cellStyle::setFillPattern);
		colorOpt.ifPresent(
				color -> cellStyle.setFillForegroundColor(new XSSFColor(color, new DefaultIndexedColorMap())));

		LOGGER.debug("New style is {}", cellStyle);

		return cellStyle;
	}

	private final Integer getStyleKey(String format, Optional<FillPatternType> fillPatternTypeOpt,
			Optional<Color> colorOpt) {
		Integer hashcode = format.hashCode();
		if (fillPatternTypeOpt.isPresent()) {
			FillPatternType fillPatternType = fillPatternTypeOpt.get();
			hashcode += fillPatternType.hashCode();
		}
		if (colorOpt.isPresent()) {
			Color color = colorOpt.get();
			hashcode += color.hashCode();
		}

		LOGGER.debug("Getting style for {}, {} and {} return {}", format, fillPatternTypeOpt, colorOpt, hashcode);

		return hashcode;
	}

	protected String getNumberFormatByPrecision(int precision, String initialFormat) {
		StringBuilder format = new StringBuilder(initialFormat);
		if (precision > 0) {
			format.append(".");
			for (int j = 0; j < precision; j++) {
				format.append("0");
			}
		}
		return format.toString();
	}
	
	protected final JSONObject getWidgetContentFromBody(JSONObject widget) {
		JSONObject curWidget = new JSONObject();
		
		if (body == null || body.length() == 0)
			return curWidget;
		
		try {
			JSONArray allWidgets = body.getJSONArray("widget");
			int i;
			for (i = 0; i < allWidgets.length(); i++) {
				curWidget = allWidgets.getJSONObject(i);
				if (curWidget.getLong("id") == widget.getLong("id")) {
					return curWidget.optJSONObject("content");
				}
			}
		} catch (Exception e) {
		LOGGER.error("Cannot get widget content field", e);
			return new JSONObject();
		}
		return curWidget;
	}

	protected final Map<String, String> getGroupAndColumnsMap(JSONObject widgetContent, JSONArray groupsArray) {
		Map<String, String> mapGroupsAndColumns = new HashMap<>();
		try {
			if (widgetContent.get("columnSelectedOfDataset") instanceof JSONArray)
				mapGroupsAndColumns = getMapFromGroupsArray(groupsArray,
						widgetContent.getJSONArray("columnSelectedOfDataset"));
		} catch (JSONException e) {
			LOGGER.error("Couldn't retrieve groups", e);
		}
		return mapGroupsAndColumns;
	}

	protected final JSONArray getGroupsFromWidgetContent(JSONObject widgetData) throws JSONException {
		// column.header matches with name or alias
		// Fill Header
		JSONArray groupsArray = new JSONArray();
		if (widgetData.has("groups")) {
			groupsArray = widgetData.getJSONArray("groups");
		}
		return groupsArray;
	}
	
	public void adjustColumnWidth(Sheet sheet, String imageB64) {
		try {		    			
			((SXSSFSheet) sheet).trackAllColumnsForAutoSizing();
			Row row = sheet.getRow(sheet.getLastRowNum());
			if(row != null) {
				for (int i = 0; i < row.getLastCellNum(); i++) {
					sheet.autoSizeColumn(i);
					if(StringUtils.isNotEmpty(imageB64) && (i == 0 || i == 1)) {
						// first or second column
						int colWidth = 25;
						if (sheet.getColumnWidthInPixels(i) < (colWidth * 256))
							sheet.setColumnWidth(i, colWidth * 256);
					}
				}	
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Cannot write data to Excel file", e);
		}
	}
	
	public int createBrandedHeaderSheet(Sheet sheet, String imageB64, 
			int startRow, float rowHeight, int rowspan, int startCol, int colWidth, int colspan, int namespan, int dataspan, 
			String documentName, String widgetName) {				
		if (StringUtils.isNotEmpty(imageB64)) {			
			for (int r = startRow; r < startRow+rowspan; r++) {
				   sheet.createRow(r).setHeightInPoints(rowHeight);
				   for (int c = startCol; c < startCol+colspan; c++) {
					   sheet.getRow(r).createCell(c);
					   sheet.setColumnWidth(c, colWidth * 256);
				}
			}
			
			// set brandend header image
			sheet.addMergedRegion(new CellRangeAddress(startRow, startRow+rowspan-1, startCol, startCol+colspan-1));
			drawBrandendHeaderImage(sheet, imageB64, Workbook.PICTURE_TYPE_PNG, startCol, startRow, colspan, rowspan);				
			
			// set document name
			sheet.getRow(startRow).createCell(startCol+colspan).setCellValue(documentName);
			sheet.addMergedRegion(new CellRangeAddress(startRow, startRow, startCol+colspan, namespan));
			// set cell style
			CellStyle documentNameCellStyle = buildCellStyle(sheet, true, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, (short) 16);
			sheet.getRow(startRow).getCell(startCol+colspan).setCellStyle(documentNameCellStyle);
			
			// set date 
			DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			Date date = new Date();
			sheet.getRow(startRow+1).createCell(startCol+colspan).setCellValue("Data di generazione: " + dateFormat.format(date));
			sheet.addMergedRegion(new CellRangeAddress(startRow+1, startRow+1, startCol+colspan, dataspan));
			// set cell style
			CellStyle dateCellStyle = buildCellStyle(sheet, false, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, (short) 8);
			sheet.getRow(startRow+1).getCell(startCol+colspan).setCellStyle(dateCellStyle);
		}
		
		int headerIndex = (StringUtils.isNotEmpty(imageB64)) ? (startRow+rowspan) : 0;
		Row widgetNameRow = sheet.createRow((short) headerIndex);
		Cell widgetNameCell = widgetNameRow.createCell(0);
		widgetNameCell.setCellValue(widgetName);
		// set cell style
		CellStyle widgetNameStyle = buildCellStyle(sheet, true, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, (short) 14);
		widgetNameCell.setCellStyle(widgetNameStyle);
		
		return headerIndex;
	}
	
	public void drawBrandendHeaderImage(Sheet sheet, String imageB64, int pictureType, int startCol, int startRow,
			int colspan, int rowspan) {
		try {
			Workbook wb = sheet.getWorkbook();
			
			// load the picture
		    String encodingPrefix = "base64,";
		    int contentStartIndex = imageB64.indexOf(encodingPrefix) + encodingPrefix.length();
		    byte[] bytes = org.apache.commons.codec.binary.Base64.decodeBase64(imageB64.substring(contentStartIndex));			
			int pictureIdx = wb.addPicture(bytes, pictureType);

			// create an anchor with upper left cell startCol/startRow
			CreationHelper helper = wb.getCreationHelper();
			ClientAnchor anchor = helper.createClientAnchor();
			anchor.setCol1(startCol);
			anchor.setRow1(startRow);

			Drawing drawing = sheet.createDrawingPatriarch();
			Picture pict = drawing.createPicture(anchor, pictureIdx);

			int pictWidthPx = pict.getImageDimension().width;
			int pictHeightPx = pict.getImageDimension().height;
			
			// get the heights of all merged rows in px
			float[] rowHeightsPx = new float[startRow+rowspan];
			float rowsHeightPx = 0f;
			for (int r = startRow; r < startRow+rowspan; r++) {
				Row row = sheet.getRow(r);
				float rowHeightPt = row.getHeightInPoints();
				rowHeightsPx[r-startRow] = rowHeightPt * Units.PIXEL_DPI / Units.POINT_DPI;
				rowsHeightPx += rowHeightsPx[r-startRow];
			}

			// get the widths of all merged cols in px
			float[] colWidthsPx = new float[startCol + colspan];
			float colsWidthPx = 0f;
			for (int c = startCol; c < startCol + colspan; c++) {
				colWidthsPx[c - startCol] = sheet.getColumnWidthInPixels(c);
				colsWidthPx += colWidthsPx[c - startCol];
			}

			// calculate scale
			float scale = 1;
			if (pictHeightPx > rowsHeightPx) {
				float tmpscale = rowsHeightPx / pictHeightPx;
				if (tmpscale < scale)
					scale = tmpscale;
			}
			if (pictWidthPx > colsWidthPx) {
				float tmpscale = colsWidthPx / pictWidthPx;
				if (tmpscale < scale)
					scale = tmpscale;
			}

			// calculate the horizontal center position
			int horCenterPosPx = Math.round(colsWidthPx / 2f - pictWidthPx * scale / 2f);
			Integer col1 = null;
			colsWidthPx = 0f;
			for (int c = 0; c < colWidthsPx.length; c++) {
				float colWidthPx = colWidthsPx[c];
				if (colsWidthPx + colWidthPx > horCenterPosPx) {
					col1 = c + startCol;
					break;
				}
				colsWidthPx += colWidthPx;
			}
			
			// set the horizontal center position as Col1 plus Dx1 of anchor
			if (col1 != null) {
				anchor.setCol1(col1);
				anchor.setDx1(Math.round(horCenterPosPx - colsWidthPx) * Units.EMU_PER_PIXEL);
			}

			// calculate the vertical center position
			int vertCenterPosPx = Math.round(rowsHeightPx / 2f - pictHeightPx * scale / 2f);
			Integer row1 = null;
			rowsHeightPx = 0f;
			for (int r = 0; r < rowHeightsPx.length; r++) {
				float rowHeightPx = rowHeightsPx[r];
				if (rowsHeightPx + rowHeightPx > vertCenterPosPx) {
					row1 = r + startRow;
				    break;
				}
				rowsHeightPx += rowHeightPx;
			}
			  
			if (row1 != null) {
				anchor.setRow1(row1);
				anchor.setDy1(Math.round(vertCenterPosPx - rowsHeightPx) * Units.EMU_PER_PIXEL); //in unit EMU for XSSF
			}
			 
			anchor.setCol2(startCol+colspan);
			anchor.setDx2(Math.round(colsWidthPx - Math.round(horCenterPosPx - colsWidthPx)) * Units.EMU_PER_PIXEL);
			anchor.setRow2(startRow+rowspan);
			anchor.setDy2(Math.round(rowsHeightPx - Math.round(vertCenterPosPx - rowsHeightPx)) * Units.EMU_PER_PIXEL);
			
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Cannot write data to Excel file", e);
		}
	}
	
	public CellStyle buildCellStyle(Sheet sheet, boolean bold, HorizontalAlignment alignment, VerticalAlignment verticalAlignment, short headerFontSizeShort) {
		
		// CELL
		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		
		// alignment 
		cellStyle.setAlignment(alignment);
		cellStyle.setVerticalAlignment(verticalAlignment);

		// foreground color
//		String headerBGColor = (String) this.getProperty(PROPERTY_HEADER_BACKGROUND_COLOR);
//		short backgroundColorIndex = headerBGColor != null ? IndexedColors.valueOf(headerBGColor).getIndex()
//				: IndexedColors.valueOf(DEFAULT_HEADER_BACKGROUND_COLOR).getIndex();
//		cellStyle.setFillForegroundColor(backgroundColorIndex);

		// pattern
//		cellStyle.setFillPattern(fp);

		// borders
//		cellStyle.setBorderBottom(borderBottom);
//		cellStyle.setBorderLeft(borderLeft);
//		cellStyle.setBorderRight(borderRight);
//		cellStyle.setBorderTop(borderTop);

//		String bordeBorderColor = (String) this.getProperty(PROPERTY_HEADER_BORDER_COLOR);
//		short borderColorIndex = bordeBorderColor != null ? IndexedColors.valueOf(bordeBorderColor).getIndex()
//				: IndexedColors.valueOf(DEFAULT_HEADER_BORDER_COLOR).getIndex();
//		cellStyle.setLeftBorderColor(borderColorIndex);
//		cellStyle.setRightBorderColor(borderColorIndex);
//		cellStyle.setBottomBorderColor(borderColorIndex);
//		cellStyle.setTopBorderColor(borderColorIndex);

		// FONT
		Font font = sheet.getWorkbook().createFont();

		// size
//		Short headerFontSize = (Short) this.getProperty(PROPERTY_HEADER_FONT_SIZE);
//		short headerFontSizeShort = headerFontSize != null ? headerFontSize.shortValue() : DEFAULT_HEADER_FONT_SIZE;
		font.setFontHeightInPoints(headerFontSizeShort);

		// name
//		String fontName = (String) this.getProperty(PROPERTY_FONT_NAME);
//		fontName = fontName != null ? fontName : DEFAULT_FONT_NAME;
//		font.setFontName(fontName);

		// color
//		String headerColor = (String) this.getProperty(PROPERTY_HEADER_COLOR);
//		short headerColorIndex = headerColor != null ? IndexedColors.valueOf(headerColor).getIndex()
//				: IndexedColors.valueOf(DEFAULT_HEADER_COLOR).getIndex();
//		font.setColor(headerColorIndex);

		// bold		
		font.setBold(bold);
		
		cellStyle.setFont(font);
		return cellStyle;
	}
}
