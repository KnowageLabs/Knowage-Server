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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.i18n.dao.I18NMessagesDAO;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.SolrDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public abstract class AbstractFormatExporter {
	static private Logger logger = Logger.getLogger(AbstractFormatExporter.class);
	protected Locale locale;
	protected final String userUniqueIdentifier;
	protected final JSONObject body;
	public static final String UNIQUE_ALIAS_PLACEHOLDER = "_$_";
	protected static final String DATE_FORMAT = "dd/MM/yyyy";
	public static final String TIMESTAMP_FORMAT = "dd/MM/yyyy HH:mm:ss.SSS";
	protected List<Integer> hiddenColumns;
	protected Map<String, String> i18nMessages;
	protected Map<String, CellStyle> format2CellStyle = new HashMap<String, CellStyle>();

	public AbstractFormatExporter(String userUniqueIdentifier, JSONObject body) {
		this.userUniqueIdentifier = userUniqueIdentifier;
		this.body = body;
		locale = getLocaleFromBody(body);
	}

	private Locale getLocaleFromBody(JSONObject body) {
		try {
			String language = body.getString(SpagoBIConstants.SBI_LANGUAGE);
			String country = body.getString(SpagoBIConstants.SBI_COUNTRY);
			Locale toReturn = new Locale(language, country);
			return toReturn;
		} catch (Exception e) {
			logger.warn("Cannot get locale information from input parameters body", e);
			return Locale.ENGLISH;
		}

	}

	protected HashMap<String, String> getMapFromGroupsArray(JSONArray groupsArray, JSONArray aggr) {
		HashMap<String, String> returnMap = new HashMap<String, String>();
		try {
			if (aggr != null && groupsArray != null) {

				for (int i = 0; i < groupsArray.length(); i++) {

					String id = groupsArray.getJSONObject(i).getString("id");
					String groupName = groupsArray.getJSONObject(i).getString("name");

					for (int ii = 0; ii < aggr.length(); ii++) {
						JSONObject column = aggr.getJSONObject(ii);

						if (column.has("group") && column.getString("group").equals(id)) {
							String nameToInsert = getTableColumnHeaderValue(column);
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
			logger.error("Couldn't get widget " + widgetId + " type, it will be exported as a normal widget.");
		}
		logger.error("Couldn't get widget " + widgetId + " type, it will be exported as a normal widget.");
		return "";
	}

	protected List<Integer> getHiddenColumnsList(JSONArray columns) {
		List<Integer> hiddenColumns = new ArrayList<Integer>();
		try {
			for (int i = 0; i < columns.length(); i++) {
				JSONObject column = columns.getJSONObject(i);
				// check if column is hidden with flag "Hide column"
				if (column.has("style")) {
					JSONObject style = column.optJSONObject("style");
					if (style.has("hiddenColumn")) {
						if (style.getString("hiddenColumn").equals("true")) {
							hiddenColumns.add(i);
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
								hiddenColumns.add(i);
						}
					}
				}
			}
			return hiddenColumns;
		} catch (Exception e) {
			logger.error("Error while getting hidden columns list", e);
			return new ArrayList<Integer>();
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
			logger.error("Error while evaluating if column must be hidden according to variable.", e);
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

				String newHeader = getTableColumnHeaderValue(columnNew);

				for (int j = 0; j < columnsOld.length(); j++) {
					JSONObject columnOld = columnsOld.getJSONObject(j);
					if (columnOld.getString("header").equals(newHeader)) {

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
			logger.error("Error retrieving ordered columns");
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
			logger.error("Error retrieving table column header values.", e);
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
			logger.error("Cannot retrieve cockpit variables", e);
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
		Map<String, Object> map = new java.util.HashMap<String, Object>();
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
			throw new SpagoBIRuntimeException("Error getting datastore for widget [type=" + widget.optString("type") + "] [id=" + widget.optLong("id") + "]",
					e);
		}
		return datastore;
	}

	protected JSONObject getDatastore(String datasetLabel, Map<String, Object> map, String selections, int offset, int fetchSize) {
		ExporterClient client = new ExporterClient();
		try {
			JSONObject datastore = client.getDataStore(map, datasetLabel, userUniqueIdentifier, selections, offset, fetchSize);
			return datastore;
		} catch (Exception e) {
			String message = "Unable to get data";
			logger.error(message, e);
			throw new SpagoBIRuntimeException(message);
		}
	}

	protected String getInternationalizedHeader(String columnName) {
		if (i18nMessages == null) {
			I18NMessagesDAO messageDao = DAOFactory.getI18NMessageDAO();
			try {
				i18nMessages = messageDao.getAllI18NMessages(locale);
			} catch (Exception e) {
				logger.error("Error while getting i18n messages", e);
				i18nMessages = new HashMap<String, String>();
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
					if (orderedCol.getString("header").equals(getTableColumnHeaderValue(col))) {
						if (col.has("style")) {
							toReturn[i] = col.getJSONObject("style");
						}
						break;
					}
				}
			}
			return toReturn;
		} catch (Exception e) {
			logger.error("Error while retrieving table columns styles.", e);
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

															if (hideSummary != null && !hideSummary.isEmpty() && hideSummary.equalsIgnoreCase("true")) {
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
													String name = formula.isEmpty() ? column.optString("name") : formula;
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

															if (hideSummary != null && !hideSummary.isEmpty() && hideSummary.equalsIgnoreCase("true")) {
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

													if (hideSummary != null && !hideSummary.isEmpty() && hideSummary.equalsIgnoreCase("true")) {
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
		Set<String> dimensionsAliases = new HashSet<String>();
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
			logger.error("Can not filter Columns Array");
		}
		return columns;
	}

	public static String[] toStringArray(JSONArray array) {
		if (array == null)
			return null;

		String[] arr = new String[array.length()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = array.optString(i);
		}
		return arr;
	}

	protected CellStyle getIntCellStyle(Workbook wb, CreationHelper helper, JSONObject column, JSONObject colStyle, CellStyle defaultStyle, JSONObject settings,
			Integer value, JSONObject rowObject, HashMap<String, String> mapColumns, HashMap<String, String> mapColumnsTypes,
			HashMap<String, Object> variablesMap, HashMap<String, Object> parametersMap) {
		String colName = null;
		CreationHelper createHelper = wb.getCreationHelper();
		XSSFCellStyle toReturn = (XSSFCellStyle) wb.createCellStyle();
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
				CellStyle toReturnn = getCellStyleByFormat(wb, helper, format);
				return toReturnn;
			}

			setRowStyle(settings, rowObject, mapColumns, toReturn, mapColumnsTypes, variablesMap, parametersMap);

			if (column.has("ranges")) {
				JSONArray ranges = column.getJSONArray("ranges");

				for (int jj = 0; jj < ranges.length(); jj++) {

					JSONObject threshold = ranges.getJSONObject(jj);

					if (threshold.has("compareValueType")) {
						Color userColor = parseColor(threshold.getString("background-color"));
						if (threshold.has("value")) {
							Integer valueToPut = null;
							if (threshold.getString("compareValueType").equals("static"))
								valueToPut = threshold.getInt("value");
							else if (threshold.getString("compareValueType").equals("variable")) {
								valueToPut = Integer.parseInt(variablesMap.get(threshold.getString("value")).toString());
							} else if (threshold.getString("compareValueType").equals("parameter") && parametersMap.get(threshold.getString("value")) != null) {
								valueToPut = Integer.parseInt(parametersMap.get(threshold.getString("value")).toString());
							} else {
								break;
							}
							if (threshold.getString("operator").equals(">")) {

								if (value.intValue() > valueToPut.intValue()) {

									toReturn.setFillPattern(FillPatternType.SOLID_FOREGROUND);
									toReturn.setFillForegroundColor(new XSSFColor(userColor, new DefaultIndexedColorMap()));
								}

							} else if (threshold.getString("operator").equals("<")) {

								if (value.intValue() < valueToPut.intValue()) {
									toReturn.setFillPattern(FillPatternType.SOLID_FOREGROUND);
									toReturn.setFillForegroundColor(new XSSFColor(userColor, new DefaultIndexedColorMap()));
								}

							} else if (threshold.getString("operator").equals("==")) {

								if (value.intValue() == valueToPut.intValue()) {
									toReturn.setFillPattern(FillPatternType.SOLID_FOREGROUND);
									toReturn.setFillForegroundColor(new XSSFColor(userColor, new DefaultIndexedColorMap()));
								}

							} else if (threshold.getString("operator").equals("<=")) {

								if (value.intValue() <= valueToPut.intValue()) {
									toReturn.setFillPattern(FillPatternType.SOLID_FOREGROUND);
									toReturn.setFillForegroundColor(new XSSFColor(userColor, new DefaultIndexedColorMap()));
								}

							} else if (threshold.getString("operator").equals(">=")) {

								if (value.intValue() >= valueToPut.intValue()) {
									toReturn.setFillPattern(FillPatternType.SOLID_FOREGROUND);
									toReturn.setFillForegroundColor(new XSSFColor(userColor, new DefaultIndexedColorMap()));
								}

							} else if (threshold.getString("operator").equals("!=")) {

								if (value.intValue() != valueToPut.intValue()) {
									toReturn.setFillPattern(FillPatternType.SOLID_FOREGROUND);
									toReturn.setFillForegroundColor(new XSSFColor(userColor, new DefaultIndexedColorMap()));
								}

							}
						} else {
							if (threshold.getString("operator").equals("IN")) {

								String[] valueArray = threshold.getString("valueArray").replace("[", "").replace("]", "").replaceAll("\"", "").split(",");
								int[] numbers = new int[valueArray.length];
								for (int i = 0; i < valueArray.length; i++) {
									numbers[i] = Integer.parseInt(valueArray[i]);
								}
								if (containsInt(numbers, value)) {
									toReturn.setFillPattern(FillPatternType.SOLID_FOREGROUND);
									toReturn.setFillForegroundColor(new XSSFColor(userColor, new DefaultIndexedColorMap()));
								}
							}
						}
					}

				}

			}

			return toReturn;
		} catch (Exception e) {
			logger.error("Error while building column {" + colName + "} CellStyle. Default style will be used.", e);
			return toReturn;
		}
	}

	protected CellStyle getDoubleCellStyle(Workbook wb, CreationHelper helper, JSONObject column, JSONObject colStyle, XSSFCellStyle defaultStyle,
			JSONObject settings, Double value, JSONObject rowObject, HashMap<String, String> mapColumns, HashMap<String, String> mapColumnsTypes,
			HashMap<String, Object> variablesMap, HashMap<String, Object> parametersMap) {
		String colName = null;
		CreationHelper createHelper = wb.getCreationHelper();
		XSSFCellStyle toReturn = (XSSFCellStyle) wb.createCellStyle();
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
				CellStyle toReturnFormat = getCellStyleByFormat(wb, helper, format);
				return toReturnFormat;
			}

			setRowStyle(settings, rowObject, mapColumns, toReturn, mapColumnsTypes, variablesMap, parametersMap);

			if (column.has("ranges")) {
				JSONArray ranges = column.getJSONArray("ranges");

				for (int jj = 0; jj < ranges.length(); jj++) {

					JSONObject threshold = ranges.getJSONObject(jj);

					if (threshold.has("compareValueType") && threshold.has("background-color")) {
						Color userColor = parseColor(threshold.getString("background-color"));
						if (threshold.has("value")) {
							Double valueToPut = null;
							if (threshold.getString("compareValueType").equals("static"))
								valueToPut = threshold.getDouble("value");
							else if (threshold.getString("compareValueType").equals("variable")) {
								valueToPut = Double.parseDouble(variablesMap.get(threshold.getString("value")).toString());
							} else if (threshold.getString("compareValueType").equals("parameter") && parametersMap.get(threshold.getString("value")) != null) {
								valueToPut = Double.parseDouble(parametersMap.get(threshold.getString("value")).toString());
							} else {
								break;
							}
							if (threshold.getString("operator").equals(">")) {

								if (value.floatValue() > valueToPut.floatValue()) {
									toReturn.setFillPattern(FillPatternType.SOLID_FOREGROUND);
									toReturn.setFillForegroundColor(new XSSFColor(userColor, new DefaultIndexedColorMap()));
								}

							} else if (threshold.getString("operator").equals("<")) {

								if (value.floatValue() < valueToPut.floatValue()) {
									toReturn.setFillPattern(FillPatternType.SOLID_FOREGROUND);
									toReturn.setFillForegroundColor(new XSSFColor(userColor, new DefaultIndexedColorMap()));
								}

							} else if (threshold.getString("operator").equals("==")) {

								if (value.floatValue() == valueToPut.floatValue()) {
									toReturn.setFillPattern(FillPatternType.SOLID_FOREGROUND);
									toReturn.setFillForegroundColor(new XSSFColor(userColor, new DefaultIndexedColorMap()));
								}

							} else if (threshold.getString("operator").equals("<=")) {

								if (value.floatValue() <= valueToPut.floatValue()) {
									toReturn.setFillPattern(FillPatternType.SOLID_FOREGROUND);
									toReturn.setFillForegroundColor(new XSSFColor(userColor, new DefaultIndexedColorMap()));
								}

							} else if (threshold.getString("operator").equals(">=")) {

								if (value.floatValue() >= valueToPut.floatValue()) {
									toReturn.setFillPattern(FillPatternType.SOLID_FOREGROUND);
									toReturn.setFillForegroundColor(new XSSFColor(userColor, new DefaultIndexedColorMap()));
								}

							} else if (threshold.getString("operator").equals("!=")) {

								if (value.floatValue() != valueToPut.floatValue()) {
									toReturn.setFillPattern(FillPatternType.SOLID_FOREGROUND);
									toReturn.setFillForegroundColor(new XSSFColor(userColor, new DefaultIndexedColorMap()));
								}

							}
						} else {
							if (threshold.getString("operator").equals("IN")) {

								String[] valueArray = threshold.getString("valueArray").replace("[", "").replace("]", "").replaceAll("\"", "").split(",");
								double[] numbers = new double[valueArray.length];
								for (int i = 0; i < valueArray.length; i++) {
									numbers[i] = Double.parseDouble(valueArray[i]);
								}
								if (containsDouble(numbers, value)) {
									toReturn.setFillPattern(FillPatternType.SOLID_FOREGROUND);
									toReturn.setFillForegroundColor(new XSSFColor(userColor, new DefaultIndexedColorMap()));
								}
							}
						}
					}

				}

			}

			return toReturn;
		} catch (Exception e) {
			logger.error("Error while building column {" + colName + "} CellStyle. Default style will be used.", e);
			return toReturn;
		}
	}

	private void setRowStyle(JSONObject settings, JSONObject rowObject, HashMap<String, String> mapColumns, XSSFCellStyle toReturn,
			HashMap<String, String> mapColumnsTypes, HashMap<String, Object> variablesMap, HashMap<String, Object> parametersMap) throws JSONException {
		if (settings.has("rowThresholds") && settings.getJSONObject("rowThresholds").getString("enabled").equals("true")) {

			JSONArray listOfThresholds = settings.getJSONObject("rowThresholds").getJSONArray("list");

			for (int i = 0; i < listOfThresholds.length(); i++) {

				JSONObject entry = listOfThresholds.getJSONObject(i);

				Color userColor = parseColor(entry.getJSONObject("style").getString("background-color"));
				Object rowValueOBJ = rowObject.get(mapColumns.get(entry.getString("column")));

				String type = mapColumnsTypes.get(mapColumns.get(entry.getString("column")));

				if (type.equals("float")) {
					Double rowValue = null;

					Double valueToPut = null;

					if (entry.getString("compareValueType").equals("static")) {
						valueToPut = entry.getDouble("compareValue");
					} else if (entry.getString("compareValueType").equals("variable")) {
						valueToPut = Double.parseDouble(variablesMap.get(entry.getString("compareValue")).toString());
					} else if (entry.getString("compareValueType").equals("parameter") && parametersMap.get(entry.getString("compareValue")) != null) {
						valueToPut = Double.parseDouble(parametersMap.get(entry.getString("compareValue")).toString());
					} else {
						break;
					}
					if (rowValueOBJ instanceof Integer) {
						rowValue = new Double((Integer) rowValueOBJ);
					} else
						rowValue = (Double) rowValueOBJ;

					if (entry.getString("condition").equals(">")) {

						if (rowValue.floatValue() > valueToPut.floatValue()) {

							toReturn.setFillPattern(FillPatternType.SOLID_FOREGROUND);
							toReturn.setFillForegroundColor(new XSSFColor(userColor, new DefaultIndexedColorMap()));
						}

					} else if (entry.getString("condition").equals("<")) {

						if (rowValue.floatValue() < valueToPut.floatValue()) {
							toReturn.setFillPattern(FillPatternType.SOLID_FOREGROUND);
							toReturn.setFillForegroundColor(new XSSFColor(userColor, new DefaultIndexedColorMap()));
						}

					} else if (entry.getString("condition").equals("==")) {

						if (rowValue.floatValue() == valueToPut.floatValue()) {
							toReturn.setFillPattern(FillPatternType.SOLID_FOREGROUND);
							toReturn.setFillForegroundColor(new XSSFColor(userColor, new DefaultIndexedColorMap()));
						}

					} else if (entry.getString("condition").equals("<=")) {

						if (rowValue.floatValue() <= valueToPut.floatValue()) {
							toReturn.setFillPattern(FillPatternType.SOLID_FOREGROUND);
							toReturn.setFillForegroundColor(new XSSFColor(userColor, new DefaultIndexedColorMap()));
						}

					} else if (entry.getString("condition").equals(">=")) {

						if (rowValue.floatValue() >= valueToPut.floatValue()) {
							toReturn.setFillPattern(FillPatternType.SOLID_FOREGROUND);
							toReturn.setFillForegroundColor(new XSSFColor(userColor, new DefaultIndexedColorMap()));
						}

					} else if (entry.getString("condition").equals("!=")) {

						if (rowValue.floatValue() != valueToPut.floatValue()) {
							toReturn.setFillPattern(FillPatternType.SOLID_FOREGROUND);
							toReturn.setFillForegroundColor(new XSSFColor(userColor, new DefaultIndexedColorMap()));
						}
					} else if (entry.getString("condition").equals("IN")) {

						String[] valueArray = entry.getString("valueArray").replace("[", "").replace("]", "").replaceAll("\"", "").split(",");
						double[] numbers = new double[valueArray.length];
						for (int ii = 0; ii < valueArray.length; ii++) {
							numbers[ii] = Double.parseDouble(valueArray[ii]);
						}
						if (containsDouble(numbers, rowValue)) {
							toReturn.setFillPattern(FillPatternType.SOLID_FOREGROUND);
							toReturn.setFillForegroundColor(new XSSFColor(userColor, new DefaultIndexedColorMap()));
						}
					}
				}

				if (type.equals("integer")) {

					Integer valueToPut = null;

					if (entry.getString("compareValueType").equals("static")) {
						valueToPut = entry.getInt("compareValue");
					} else if (entry.getString("compareValueType").equals("variable")) {
						valueToPut = Integer.parseInt(variablesMap.get(entry.getString("compareValue")).toString());
					} else if (entry.getString("compareValueType").equals("parameter") && parametersMap.get(entry.getString("compareValue")) != null) {
						valueToPut = Integer.parseInt(parametersMap.get(entry.getString("compareValue")).toString());
					} else {
						break;
					}
					Integer rowValue = (Integer) rowValueOBJ;

					if (entry.getString("condition").equals(">")) {

						if (rowValue.intValue() > valueToPut.intValue()) {

							toReturn.setFillPattern(FillPatternType.SOLID_FOREGROUND);
							toReturn.setFillForegroundColor(new XSSFColor(userColor, new DefaultIndexedColorMap()));
						}

					} else if (entry.getString("condition").equals("<")) {

						if (rowValue.intValue() < valueToPut.intValue()) {
							toReturn.setFillPattern(FillPatternType.SOLID_FOREGROUND);
							toReturn.setFillForegroundColor(new XSSFColor(userColor, new DefaultIndexedColorMap()));
						}

					} else if (entry.getString("condition").equals("==")) {

						if (rowValue.intValue() == valueToPut.intValue()) {
							toReturn.setFillPattern(FillPatternType.SOLID_FOREGROUND);
							toReturn.setFillForegroundColor(new XSSFColor(userColor, new DefaultIndexedColorMap()));
						}

					} else if (entry.getString("condition").equals("<=")) {

						if (rowValue.intValue() <= valueToPut.intValue()) {
							toReturn.setFillPattern(FillPatternType.SOLID_FOREGROUND);
							toReturn.setFillForegroundColor(new XSSFColor(userColor, new DefaultIndexedColorMap()));
						}

					} else if (entry.getString("condition").equals(">=")) {

						if (rowValue.intValue() >= valueToPut.intValue()) {
							toReturn.setFillPattern(FillPatternType.SOLID_FOREGROUND);
							toReturn.setFillForegroundColor(new XSSFColor(userColor, new DefaultIndexedColorMap()));
						}

					} else if (entry.getString("condition").equals("!=")) {

						if (rowValue.intValue() != valueToPut.intValue()) {
							toReturn.setFillPattern(FillPatternType.SOLID_FOREGROUND);
							toReturn.setFillForegroundColor(new XSSFColor(userColor, new DefaultIndexedColorMap()));
						}
					} else if (entry.getString("condition").equals("IN")) {

						String[] valueArray = entry.getString("valueArray").replace("[", "").replace("]", "").replaceAll("\"", "").split(",");
						int[] numbers = new int[valueArray.length];
						for (int ii = 0; ii < valueArray.length; ii++) {
							numbers[ii] = Integer.parseInt(valueArray[ii]);
						}
						if (containsInt(numbers, rowValue)) {
							toReturn.setFillPattern(FillPatternType.SOLID_FOREGROUND);
							toReturn.setFillForegroundColor(new XSSFColor(userColor, new DefaultIndexedColorMap()));
						}
					}
				}

				if (type.equals("string")) {

					String valueToPut = null;

					if (entry.getString("compareValueType").equals("static")) {
						valueToPut = entry.getString("compareValue");
					} else if (entry.getString("compareValueType").equals("variable")) {
						valueToPut = variablesMap.get(entry.getString("compareValue")).toString();
					} else if (entry.getString("compareValueType").equals("parameter") && parametersMap.get(entry.getString("compareValue")) != null) {
						valueToPut = parametersMap.get(entry.getString("compareValue")).toString();
					} else {
						break;
					}
					String rowValue = rowValueOBJ.toString();

					if (entry.getString("condition").equals(">")) {

						if (rowValue.compareTo(valueToPut) > 0) {

							toReturn.setFillPattern(FillPatternType.SOLID_FOREGROUND);
							toReturn.setFillForegroundColor(new XSSFColor(userColor, new DefaultIndexedColorMap()));
						}

					} else if (entry.getString("condition").equals("<")) {

						if (rowValue.compareTo(valueToPut) < 0) {
							toReturn.setFillPattern(FillPatternType.SOLID_FOREGROUND);
							toReturn.setFillForegroundColor(new XSSFColor(userColor, new DefaultIndexedColorMap()));
						}

					} else if (entry.getString("condition").equals("==")) {

						if (rowValue.equals(valueToPut)) {
							toReturn.setFillPattern(FillPatternType.SOLID_FOREGROUND);
							toReturn.setFillForegroundColor(new XSSFColor(userColor, new DefaultIndexedColorMap()));
						}

					} else if (entry.getString("condition").equals("<=")) {

						if (rowValue.compareTo(valueToPut) <= 0) {
							toReturn.setFillPattern(FillPatternType.SOLID_FOREGROUND);
							toReturn.setFillForegroundColor(new XSSFColor(userColor, new DefaultIndexedColorMap()));
						}

					} else if (entry.getString("condition").equals(">=")) {

						if (rowValue.compareTo(valueToPut) >= 0) {
							toReturn.setFillPattern(FillPatternType.SOLID_FOREGROUND);
							toReturn.setFillForegroundColor(new XSSFColor(userColor, new DefaultIndexedColorMap()));
						}

					} else if (entry.getString("condition").equals("!=")) {

						if (!rowValue.equals(valueToPut)) {
							toReturn.setFillPattern(FillPatternType.SOLID_FOREGROUND);
							toReturn.setFillForegroundColor(new XSSFColor(userColor, new DefaultIndexedColorMap()));
						}
					} else if (entry.getString("condition").equals("IN")) {

						String[] valueArray = entry.getString("valueArray").replace("[", "").replace("]", "").replaceAll("\"", "").split(",");
						if (Arrays.stream(valueArray).anyMatch(rowValue::equals)) {
							toReturn.setFillPattern(FillPatternType.SOLID_FOREGROUND);
							toReturn.setFillForegroundColor(new XSSFColor(userColor, new DefaultIndexedColorMap()));
						}
					}
				}

			}
		}
	}

	public boolean containsInt(final int[] array, final int key) {
		return ArrayUtils.contains(array, key);
	}

	public boolean containsDouble(final double[] array, final double key) {
		return ArrayUtils.contains(array, key);
	}

	protected CellStyle getStringCellStyle(Workbook wb, CreationHelper helper, JSONObject column, JSONObject colStyle, XSSFCellStyle defaultStyle,
			JSONObject settings, String value, JSONObject rowObject, HashMap<String, String> mapColumns, HashMap<String, String> mapColumnsTypes,
			HashMap<String, Object> variablesMap, HashMap<String, Object> parametersMap) {
		String colName = null;
		CreationHelper createHelper = wb.getCreationHelper();
		XSSFCellStyle toReturn = (XSSFCellStyle) wb.createCellStyle();
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
				CellStyle toReturnFormat = getCellStyleByFormat(wb, helper, format);
				return toReturnFormat;
			}

			setRowStyle(settings, rowObject, mapColumns, toReturn, mapColumnsTypes, variablesMap, parametersMap);

			if (column.has("ranges")) {
				JSONArray ranges = column.getJSONArray("ranges");

				for (int jj = 0; jj < ranges.length(); jj++) {

					JSONObject threshold = ranges.getJSONObject(jj);

					if (threshold.has("compareValueType") && threshold.has("background-color")) {
						Color userColor = parseColor(threshold.getString("background-color"));
						if (threshold.has("value")) {
							String valueToPut = null;
							if (threshold.getString("compareValueType").equals("static"))
								valueToPut = threshold.getString("value");
							else if (threshold.getString("compareValueType").equals("variable")) {
								valueToPut = variablesMap.get(threshold.getString("value")).toString();
							} else if (threshold.getString("compareValueType").equals("parameter") && parametersMap.get(threshold.getString("value")) != null) {
								valueToPut = parametersMap.get(threshold.getString("value")).toString();
							} else {
								break;
							}

							if (threshold.getString("operator").equals(">")) {

								if (value.compareTo(valueToPut) > 0) {
									toReturn.setFillPattern(FillPatternType.SOLID_FOREGROUND);
									toReturn.setFillForegroundColor(new XSSFColor(userColor, new DefaultIndexedColorMap()));
								}

							} else if (threshold.getString("operator").equals("<")) {

								if (value.compareTo(valueToPut) < 0) {
									toReturn.setFillPattern(FillPatternType.SOLID_FOREGROUND);
									toReturn.setFillForegroundColor(new XSSFColor(userColor, new DefaultIndexedColorMap()));
								}

							} else if (threshold.getString("operator").equals("==")) {

								if (value.equals(valueToPut)) {
									toReturn.setFillPattern(FillPatternType.SOLID_FOREGROUND);
									toReturn.setFillForegroundColor(new XSSFColor(userColor, new DefaultIndexedColorMap()));
								}

							} else if (threshold.getString("operator").equals("<=")) {

								if (value.compareTo(valueToPut) <= 0) {
									toReturn.setFillPattern(FillPatternType.SOLID_FOREGROUND);
									toReturn.setFillForegroundColor(new XSSFColor(userColor, new DefaultIndexedColorMap()));
								}

							} else if (threshold.getString("operator").equals(">=")) {

								if (value.compareTo(valueToPut) >= 0) {
									toReturn.setFillPattern(FillPatternType.SOLID_FOREGROUND);
									toReturn.setFillForegroundColor(new XSSFColor(userColor, new DefaultIndexedColorMap()));
								}

							} else if (threshold.getString("operator").equals("!=")) {

								if (!value.equals(valueToPut)) {
									toReturn.setFillPattern(FillPatternType.SOLID_FOREGROUND);
									toReturn.setFillForegroundColor(new XSSFColor(userColor, new DefaultIndexedColorMap()));
								}

							}
						} else {
							if (threshold.getString("operator").equals("IN")) {
								String[] valueArray = threshold.getString("valueArray").replace("[", "").replace("]", "").replaceAll("\"", "").split(",");
								if (Arrays.stream(valueArray).anyMatch(value::equals)) {
									toReturn.setFillPattern(FillPatternType.SOLID_FOREGROUND);
									toReturn.setFillForegroundColor(new XSSFColor(userColor, new DefaultIndexedColorMap()));
								}

							}
						}
					}

				}

			}

			return toReturn;
		} catch (Exception e) {
			logger.error("Error while building column {" + colName + "} CellStyle. Default style will be used.", e);
			return toReturn;
		}
	}

	protected CellStyle getGenericCellStyle(Workbook wb, CreationHelper helper, JSONObject column, JSONObject colStyle, XSSFCellStyle defaultStyle,
			JSONObject settings, JSONObject rowObject, HashMap<String, String> mapColumns, HashMap<String, String> mapColumnsTypes,
			HashMap<String, Object> variablesMap, HashMap<String, Object> parametersMap) {
		String colName = null;
		CreationHelper createHelper = wb.getCreationHelper();
		XSSFCellStyle toReturn = (XSSFCellStyle) wb.createCellStyle();
		toReturn.setDataFormat(createHelper.createDataFormat().getFormat(TIMESTAMP_FORMAT));
		try {

			setRowStyle(settings, rowObject, mapColumns, toReturn, mapColumnsTypes, variablesMap, parametersMap);

			return toReturn;
		} catch (Exception e) {
			logger.error("Error while building column {" + colName + "} CellStyle. Default style will be used.", e);
			return toReturn;
		}
	}

	public static Color parseColor(String input) {
		Pattern c = Pattern.compile("rgb *\\( *([0-9]+), *([0-9]+), *([0-9]+) *\\)");
		Matcher m = c.matcher(input);
		Color color = null;
		if (m.matches()) {
			color = new Color(Integer.valueOf(m.group(1)), // r
					Integer.valueOf(m.group(2)), // g
					Integer.valueOf(m.group(3))); // b
		} else {
			color = Color.decode(input);
		}
		return color;
	}

	private String getCellType(JSONObject column, String colName, JSONObject colStyle) {
		try {
			return column.getString("type");
		} catch (Exception e) {
			logger.error("Error while retrieving column {" + colName + "} type. It will be treated as string.", e);
			return "string";
		}
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
	protected CellStyle getCellStyleByFormat(Workbook wb, CreationHelper helper, String format) {
		if (!format2CellStyle.containsKey(format)) {
			// if cell style does not exist
			CellStyle cellStyle = wb.createCellStyle();
			cellStyle.setDataFormat(helper.createDataFormat().getFormat(format));
			format2CellStyle.put(format, cellStyle);
		}
		return format2CellStyle.get(format);
	}

	protected String getNumberFormatByPrecision(int precision, String initialFormat) {
		String format = initialFormat;
		if (precision > 0) {
			format += ".";
			for (int j = 0; j < precision; j++) {
				format += "0";
			}
		}
		return format;
	}
}