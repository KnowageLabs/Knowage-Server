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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
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

}
