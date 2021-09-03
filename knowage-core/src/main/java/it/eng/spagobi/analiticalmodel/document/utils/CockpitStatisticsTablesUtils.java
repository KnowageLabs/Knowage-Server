/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2020 Engineering Ingegneria Informatica S.p.A.
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

/**
 *
 */
package it.eng.spagobi.analiticalmodel.document.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjTemplates;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.SpagoBIDAOException;
import it.eng.spagobi.commons.metadata.SbiBinContents;
import it.eng.spagobi.commons.metadata.SbiCockpitAssociation;
import it.eng.spagobi.commons.metadata.SbiCockpitWidget;
import it.eng.spagobi.commons.metadata.SbiCommonInfo;
import it.eng.spagobi.commons.metadata.SbiHibernateModel;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;

/**
 * @author albnale
 * @since 2021/02/15
 *
 *        This class is used to manage cockpit widgets and associations information
 *
 */
public class CockpitStatisticsTablesUtils {

	static private Logger logger = Logger.getLogger(CockpitStatisticsTablesUtils.class);

	public static void deleteCockpitWidgetsTable(BIObject biObject, Session session) {
		deleteCockpitWidgetsTable(biObject, session, false);
	}

	public static void deleteCockpitWidgetsTable(BIObject biObject, Session session, boolean isImport) {
		logger.debug("IN");
		Transaction tx = null;
		if (!isImport) {
			tx = session.beginTransaction();
		}

		Integer biobjId = biObject.getId();

		logger.debug("Cleaning SbiCockpitWidget table - START");
		List<SbiCockpitWidget> sbiCockpitWidgetList = session.createCriteria(SbiCockpitWidget.class).add(Restrictions.eq("biobjId", biobjId)).list();
		for (SbiCockpitWidget sbiCockpitWidget : sbiCockpitWidgetList) {

			logger.debug(String.format("Removing SbiCockpitWidget with id [%s]", sbiCockpitWidget.getSbiCockpitWidgetId()));
			session.evict(sbiCockpitWidget);
			session.delete(sbiCockpitWidget);
			session.flush();

		}

		logger.debug("Cleaning SbiCockpitWidget table - STOP");

		List<SbiCockpitAssociation> sbiCockpitAssociationList = session.createCriteria(SbiCockpitAssociation.class).add(Restrictions.eq("biobjId", biobjId))
				.list();

		logger.debug("Cleaning SbiCockpitAssociation table - START");
		for (SbiCockpitAssociation sbiCockpitAssociation : sbiCockpitAssociationList) {
			logger.debug(String.format("Removing SbiCockpitAssociation with id [%s]", sbiCockpitAssociation.getSbiCockpitAssociationId()));
			session.evict(sbiCockpitAssociation);
			session.delete(sbiCockpitAssociation);
			session.flush();
		}
		logger.debug("Cleaning SbiCockpitAssociation table - STOP");

		if (tx != null && !tx.wasCommitted())
			tx.commit();

		logger.debug("OUT");
	}

	public static void updateCockpitWidgetsTable(BIObject biObject, Session session) {
		updateCockpitWidgetsTable(biObject, session, false);
	}

	public static void updateCockpitWidgetsTable(BIObject biObject, Session session, boolean isImport) {
		logger.debug("IN");
		deleteCockpitWidgetsTable(biObject, session, isImport);

		SbiObjects sbiObjects = (SbiObjects) session.createCriteria(SbiObjects.class).add(Restrictions.eq("biobjId", biObject.getId())).uniqueResult();
		Transaction tx = null;
		if (!isImport) {
			tx = session.beginTransaction();
		}

		try {
			parseTemplate(sbiObjects, session, false);
		} catch (Exception e) {
			logger.error("Error during parsing template widget", e);
			if (tx != null)
				tx.rollback();
		} finally {
			if (tx != null && !tx.wasCommitted())
				tx.commit();
		}
		logger.debug("OUT");
	}

	public static void parseTemplate(SbiObjects sbiObjects, Session session, boolean initializer) {
		logger.debug("IN");
		Criteria c = session.createCriteria(SbiObjTemplates.class, "sot");
		c.add(Restrictions.eq("sot.sbiObject.biobjId", sbiObjects.getBiobjId()));
		c.add(Restrictions.eq("sot.active", true));
		SbiObjTemplates sbiObjTemplates = (SbiObjTemplates) c.uniqueResult();

		if (sbiObjTemplates == null) {
			logger.error(String.format("No active template found for sbiObject with id [%s]", sbiObjects.getBiobjId()));
		} else {

			try {
				SbiBinContents binContent = sbiObjTemplates.getSbiBinContents();

				byte[] contentByte = binContent.getContent();
				String contentString = new String(contentByte);
				JSONObject template = new JSONObject(contentString);

				Map<Integer, JSONObject> dataSetMap = getDatasetMap(sbiObjects, template, session);
				Set<String> associationsMap = handleDatasetAssociations(sbiObjects, template, session, dataSetMap, initializer);

				JSONArray sheets = template.optJSONArray("sheets");
				if (sheets != null) {
					logger.debug(String.format("Document with label [%s] has [%s] sheets", sbiObjects.getLabel(), sheets.length()));

					for (int i = 0; i < sheets.length(); i++) {
						JSONObject sheet = sheets.getJSONObject(i);

						JSONArray widgets = sheet.getJSONArray("widgets");
						logger.debug(String.format("Document with label [%s] has [%s] widgets", sbiObjects.getLabel(), widgets.length()));

						for (int j = 0; j < widgets.length(); j++) {
							String datasetLabel = null;
							JSONObject widget = widgets.getJSONObject(j);

							String widgetType = widget.getString("type");
							logger.debug(String.format("Parsing widget with id [%s] of type [%s]", widget.get("id"), widgetType));

							if (widgetType.equals("map")) {

								Set<Integer> dsIds = parseContentForMapWidget(datasetLabel, widget);
								logger.debug(String.format("Found [%s] dataset ids in content JSON object", dsIds.size()));
								for (Integer dsId : dsIds) {
									SbiCockpitWidget sbiCockpitWidget = new SbiCockpitWidget();
									sbiCockpitWidget.setTab(sheet.getString("label"));
									sbiCockpitWidget.setBiobjId(sbiObjects.getBiobjId());
									sbiCockpitWidget.setWidgetType(widgetType);

									sbiCockpitWidget.setDsId(dsId);

									boolean useCache = dataSetMap.get(dsId).optBoolean("useCache");
									sbiCockpitWidget.setCache(useCache);

									setIsAssociative(associationsMap, datasetLabel, sbiCockpitWidget);

									setUseCache(dataSetMap, datasetLabel, sbiCockpitWidget);

									updateSbiCommonInfo4Insert(sbiObjects, sbiCockpitWidget, initializer);

									session.save(sbiCockpitWidget);
									session.flush();
									logger.debug(
											String.format("Persisted SbiCockpitWidget with id [%s] to database", sbiCockpitWidget.getSbiCockpitWidgetId()));
								}
							} else {
								SbiCockpitWidget sbiCockpitWidget = new SbiCockpitWidget();

								sbiCockpitWidget.setTab(sheet.getString("label"));
								sbiCockpitWidget.setBiobjId(sbiObjects.getBiobjId());
								sbiCockpitWidget.setWidgetType(widgetType);

								datasetLabel = parseDatasetObject(sbiCockpitWidget, datasetLabel, widget, dataSetMap);

								datasetLabel = parseContentObject(sbiCockpitWidget, datasetLabel, widget);

								datasetLabel = parseFiltersObject(sbiCockpitWidget, datasetLabel, widget);

								JSONObject content = widget.optJSONObject("content");
								if (content != null)
									datasetLabel = parseFiltersObject(sbiCockpitWidget, datasetLabel, content);

								setIsAssociative(associationsMap, datasetLabel, sbiCockpitWidget);
								setUseCache(dataSetMap, datasetLabel, sbiCockpitWidget);

								updateSbiCommonInfo4Insert(sbiObjects, sbiCockpitWidget, initializer);
								sbiCockpitWidget.getCommonInfo().setOrganization(sbiObjects.getCommonInfo().getOrganization());
								session.save(sbiCockpitWidget);
								session.flush();
								logger.debug(String.format("Persisted SbiCockpitWidget with id [%s] to database", sbiCockpitWidget.getSbiCockpitWidgetId()));
							}

						} // end single widget
					} // end single sheet
				}

			} catch (JSONException e) {
				logger.error("Error while reading template information");
			}

		}
		logger.debug("OUT");
	}

	private static void setIsAssociative(Set<String> associationsMap, String datasetLabel, SbiCockpitWidget sbiCockpitWidget) {
		/* Not all widgets have dataset associated */
		if (datasetLabel != null) {
			final String finalDatasetLabel = datasetLabel;
			boolean isAssociative = associationsMap.stream().anyMatch(x -> x.contains(finalDatasetLabel));
			sbiCockpitWidget.setAssociative(isAssociative);
			logger.debug(String.format("Dataset with label [%s] is %s in associative logic", datasetLabel, isAssociative ? "" : "NOT"));
		}
	}

	private static void setUseCache(Map<Integer, JSONObject> dataSetMap, String datasetLabel, SbiCockpitWidget sbiCockpitWidget) {
		if (sbiCockpitWidget.getDsId() != null && sbiCockpitWidget.getDsId() > 0) {
			boolean useCache = dataSetMap.get(sbiCockpitWidget.getDsId()).optBoolean("useCache");
			sbiCockpitWidget.setCache(useCache);
			logger.debug(String.format("Dataset with label [%s] is %s cached", datasetLabel, useCache ? "" : "NOT"));
		}
	}

	private static Map<Integer, JSONObject> getDatasetMap(SbiObjects sbiObjects, JSONObject template, Session session) throws JSONException {
		logger.debug("IN");
		Map<Integer, JSONObject> datasetMap = new HashMap<Integer, JSONObject>();
		JSONObject configuration = template.optJSONObject("configuration");
		if (configuration != null) {
			JSONArray datasets = configuration.optJSONArray("datasets");
			if (datasets != null) {
				for (int index = 0; index < datasets.length(); index++) {
					JSONObject datasetInConf = (JSONObject) datasets.get(index);

					if (datasetInConf != null && datasetInConf.has("dsId")) {
						Integer dsId = datasetInConf.optInt("dsId");
						datasetMap.put(dsId, datasetInConf);
						logger.debug(String.format("Added dataset with id [%s] and label [%s] to datasetMap", dsId, datasetInConf.get("dsLabel")));
					}
				}
			}
		}
		logger.debug("OUT");
		return datasetMap;
	}

	private static String parseFiltersObject(SbiCockpitWidget sbiCockpitWidget, String datasetLabel, JSONObject widgetOrContent) throws JSONException {
		logger.debug("IN");
		// FILTERS
		JSONArray filters = widgetOrContent.optJSONArray("filters");
		boolean hasFilters = filters != null && !filters.toString().equals("{}");
		sbiCockpitWidget.setFilters(hasFilters);
		if (filters != null) {
			logger.debug(String.format("Widget has [%s] filters", filters.length()));
			for (int filterIdx = 0; filterIdx < filters.length(); filterIdx++) {
				JSONObject filter = (JSONObject) filters.get(filterIdx);
				if (filter != null && filter.has("dataset")) {
					JSONObject contDataset = filter.optJSONObject("dataset");
					if (contDataset != null && contDataset.has("dsId")) {
						Integer dsId = contDataset.optInt("dsId");
						if (dsId != 0) {
							sbiCockpitWidget.setDsId(dsId);
							datasetLabel = contDataset.optString("label");
							logger.debug(String.format("Widget has dataset with id [%s] and label [%s] associated", dsId, datasetLabel));
						}
					}
				}
			}
		}
		logger.debug("OUT");
		return datasetLabel;
	}

	private static Set<Integer> parseContentForMapWidget(String datasetLabel, JSONObject widget) throws JSONException {
		logger.debug("IN");
		Set<Integer> dsIds = new HashSet<Integer>();
		JSONObject content = widget.optJSONObject("content");
		// check if there is datasetId array, if it is there is a text widget with user selected Id
//				JSONArray datasetIds = widget.optJSONArray("datasetId");
//				if (datasetIds != null) {
//					for (int k = 0; k < datasetIds.length(); k++) {
//						Integer oldId = datasetIds.optInt(k);
//						if (oldId != null && dsIds.get(oldId) != null) {
//							Integer newId = dsIds.get(oldId);
//							datasetIds.put(k, newId);
//						}
//					}
//				} else {
//					// check if there is datasetId single object, if it is there is a chart widget
//					if (content != null) {
//						Integer oldId = content.has("datasetId") ? content.optInt("datasetId") : null;
//						if (oldId != null) {
//							if (oldId != null && dsIds.get(oldId) != null) {
//								Integer newId = dsIds.get(oldId);
//								content.put("datasetId", newId);
//							}
//						}
//					}
//				}

		// MAP WIDGET specifics
		if (content != null) {
			JSONArray layers = content.optJSONArray("layers");
			if (layers != null) {
				for (int l = 0; l < layers.length(); l++) {
					JSONObject layer = layers.getJSONObject(l);
					if (layer != null && layer.has("dsId")) {
						Integer dsId = layer.optInt("dsId");
						if (dsId != 0) {
							dsIds.add(dsId);
							datasetLabel = layer.optString("label");
							logger.debug(String.format("Map widget has dataset with [%s] and label [%s] associated", dsId, datasetLabel));
						}
					}
				}
			}
		}
		logger.debug("OUT");
		return dsIds;
	}

	private static String parseContentObject(SbiCockpitWidget sbiCockpitWidget, String datasetLabel, JSONObject widget) throws JSONException {
		logger.debug("IN");
		// CONTENT
		JSONObject content = widget.optJSONObject("content");
		// check if there is datasetId array, if it is there is a text widget with user selected Id
//					JSONArray datasetIds = widget.optJSONArray("datasetId");
//					if (datasetIds != null) {
//						for (int k = 0; k < datasetIds.length(); k++) {
//							Integer oldId = datasetIds.optInt(k);
//							if (oldId != null && dsIds.get(oldId) != null) {
//								Integer newId = dsIds.get(oldId);
//								datasetIds.put(k, newId);
//							}
//						}
//					} else {
//						// check if there is datasetId single object, if it is there is a chart widget
//						if (content != null) {
//							Integer oldId = content.has("datasetId") ? content.optInt("datasetId") : null;
//							if (oldId != null) {
//								if (oldId != null && dsIds.get(oldId) != null) {
//									Integer newId = dsIds.get(oldId);
//									content.put("datasetId", newId);
//								}
//							}
//						}
//					}

		// MAP WIDGET specifics
		if (content != null) {
//			JSONArray layers = content.optJSONArray("layers");
//			if (layers != null) {
//				for (int l = 0; l < layers.length(); l++) {
//					JSONObject layer = layers.getJSONObject(l);
//					Integer oldId = layer.optInt("dsId");
//					if (oldId != null) {
//						sbiCockpitWidget.setDsId(oldId);
//						datasetLabel = layer.optString("label");
//					}
//				}
//			}

//			JSONObject columns = content.optJSONObject("columnSelectedOfDataset");
//			if (columns != null) {
//				List<Integer> idToClean = new ArrayList();
//				Iterator<?> keys = columns.keys();
//
//				while (keys.hasNext()) {
//					String key = (String) keys.next();
//					if (columns.get(key) instanceof JSONArray) {
//						Integer oldId = Integer.valueOf(key);
//						if (oldId != null) {
//							idToClean.add(oldId);
//						}
//					}
//				}
//				for (int d = 0; d < idToClean.size(); d++) { // update objects
//					Integer oldId = idToClean.get(d);
//					sbiCockpitWidget.setDsId(oldId);
//				}
//
//			}

			if (content.has("dataset")) {
				JSONObject contDataset = content.optJSONObject("dataset");
				if (contDataset != null) {
					JSONObject id = contDataset.optJSONObject("id");
					if (id != null && id.has("dsId")) {
						Integer oldId = id.optInt("dsId");
						if (oldId != null && oldId != 0) {
							logger.debug(String.format("Dataset id [%s] found in id.dsId property", oldId));
							sbiCockpitWidget.setDsId(oldId);
							datasetLabel = contDataset.optString("label");
							logger.debug(String.format("Widget has dataset with id [%s] and label [%s] associated", oldId, datasetLabel));
						}

					}

					if (contDataset.has("dsId")) {
						Integer oldId = contDataset.optInt("dsId");
						if (oldId != null && oldId != 0) {
							logger.debug(String.format("Dataset id [%s] found in dsId property", oldId));

							sbiCockpitWidget.setDsId(oldId);
							datasetLabel = contDataset.optString("label");
							logger.debug(String.format("Widget has dataset with id [%s] and label [%s] associated", oldId, datasetLabel));
						}
					}
				}
			}
		}
		logger.debug("OUT");
		return datasetLabel;
	}

	private static String parseDatasetObject(SbiCockpitWidget sbiCockpitWidget, String datasetLabel, JSONObject widget, Map<Integer, JSONObject> dataSetMap)
			throws JSONException {
		logger.debug("IN");
		JSONObject dataset = widget.optJSONObject("dataset");
		if (dataset != null) {
			if (dataset.has("dsId")) {
				Integer oldId = dataset.optInt("dsId");
				logger.debug(String.format("Found dsId [%s] in datasets JSON object", oldId));
				sbiCockpitWidget.setDsId(oldId);

				JSONObject datasetJSONObject = dataSetMap.get(oldId);
				if (datasetJSONObject == null) {
					logger.debug(String.format("Dataset with id [%s] NOT found in dataset JSON object", oldId));
				} else {
					datasetLabel = datasetJSONObject.getString("dsLabel");
					logger.debug(String.format("Dataset label set to [%s]", datasetLabel));
				}

				try {
					JSONArray oldIdArray = dataset.getJSONArray("dsId");
					JSONArray tmp = new JSONArray();
					for (int k = 0; k < oldIdArray.length(); k++) {
						logger.debug(String.format("dsId is an array of length [%s]", oldIdArray.length()));

						Integer jsonOldId = oldIdArray.getInt(k);
						if (jsonOldId != null) {
							tmp.put(jsonOldId);
							sbiCockpitWidget.setDsId(jsonOldId);
							datasetLabel = dataset.optString("label");
							logger.debug(String.format("Widget has dataset with id [%s] and label [%s] associated", jsonOldId, datasetLabel));
						}
					}

					if (tmp.length() > 0) {
						// dataset.remove("dsId");
						// dataset.put("dsId", tmp);
						// sbiCockpitWidget.setDsId(tmp);
					}
				} catch (JSONException ex) {
					logger.debug("Field dsId is not an array");
				}
			}

		} else {
			// html widget case: the datasetId is a simple property of widget obj
			Integer oldId = widget.optInt("datasetId");
			if (oldId != null && oldId != 0) {
				sbiCockpitWidget.setDsId(oldId);
				datasetLabel = widget.optString("label");
				logger.debug(String.format("Widget has dataset with id [%s] and label [%s] associated", oldId, datasetLabel));
			}
		}
		logger.debug("OUT");
		return datasetLabel;
	}

	private static Set<String> handleDatasetAssociations(SbiObjects sbiObjects, JSONObject template, Session session, Map<Integer, JSONObject> dataSetMap,
			boolean initializer) throws JSONException {
		logger.debug("IN");
		Set<String> associationsMap = new HashSet<String>();
		try {
			JSONObject configuration = template.optJSONObject("configuration");
			if (configuration != null) {
				JSONArray associations = configuration.optJSONArray("associations");
				boolean isAssociative = associations != null && !associations.toString().equals("{}");
				if (isAssociative) {
					for (int k = 0; k < associations.length(); k++) {
						logger.debug("Found " + associations.length() + " dataset associations in document with label " + sbiObjects.getLabel());

						JSONObject association = (JSONObject) associations.get(k);
						JSONArray fields = association.optJSONArray("fields");
						if (fields != null) {

							for (int i = 0; i < fields.length() - 1; i++) {

								IDataSetDAO dataSetDAO = DAOFactory.getDataSetDAO();
								dataSetDAO.setTenant(sbiObjects.getCommonInfo().getOrganization());

								JSONObject field1 = fields.optJSONObject(i);
								JSONObject field2 = fields.optJSONObject(i + 1);

								SbiCockpitAssociation sbiCockpitAssociation = new SbiCockpitAssociation();
								sbiCockpitAssociation.setBiobjId(sbiObjects.getBiobjId());

								String fromDataset = field1.getString("store");
								String fromColumn = field1.getString("column");
								if (fromDataset != null) {
									try {

										Integer fromDatasetId = dataSetMap.entrySet().stream().filter(e -> {
											try {
												return e.getValue().getString("dsLabel").equals(fromDataset);
											} catch (JSONException e1) {
												logger.error(e1.getMessage(), e1);
												return false;
											}
										}).map(Map.Entry::getKey).findFirst().orElse(null);

										sbiCockpitAssociation.setDsIdFrom(fromDatasetId);

									} catch (SpagoBIDAOException e) {
										logger.warn(e.getMessage(), e);
									}
									sbiCockpitAssociation.setColumnNameFrom(fromColumn);
								}

								String toDataset = field2.getString("store");
								String toColumn = field2.getString("column");
								if (toDataset != null) {
									try {

										Integer toDatasetId = dataSetMap.entrySet().stream().filter(e -> {
											try {
												return e.getValue().getString("dsLabel").equals(toDataset);
											} catch (JSONException e1) {
												logger.error(e1.getMessage(), e1);
												return false;
											}
										}).map(Map.Entry::getKey).findFirst().orElse(null);
										sbiCockpitAssociation.setDsIdTo(toDatasetId);
									} catch (SpagoBIDAOException e) {
										logger.warn(e.getMessage(), e);
									}
									sbiCockpitAssociation.setColumnNameTo(toColumn);
								}

								updateSbiCommonInfo4Insert(sbiObjects, sbiCockpitAssociation, initializer);
								sbiCockpitAssociation.getCommonInfo().setOrganization(sbiObjects.getCommonInfo().getOrganization());
								session.save(sbiCockpitAssociation);
								session.flush();

								logger.debug(String.format("Field [%s-%s] associated with field [%s-%s] in association", fromDataset, fromColumn, toDataset,
										toColumn));

							}
							String associationDescription = association.getString("description");
							associationsMap.add(associationDescription);
							logger.debug(String.format("Added association with description [%s] to associationMap", associationDescription));

						}
					}
				}
			}
		} catch (HibernateException e) {
			logger.error(e.getMessage(), e);
		} finally {

		}
		logger.debug("OUT");
		return associationsMap;
	}

	/**
	 * @param sbiCockpitWidget
	 * @param b
	 */
	private static void updateSbiCommonInfo4Insert(SbiObjects sbiObjects, SbiHibernateModel sbiCockpitWidget, boolean initializer) {
		sbiCockpitWidget.getCommonInfo().setTimeIn(new Date());
		sbiCockpitWidget.getCommonInfo().setSbiVersionIn(SbiCommonInfo.getVersion());
		String userIn = initializer ? "server" : sbiObjects.getCommonInfo().getUserIn();
		sbiCockpitWidget.getCommonInfo().setUserIn(userIn);
		sbiCockpitWidget.getCommonInfo().setOrganization(sbiObjects.getCommonInfo().getOrganization());

	}
}
