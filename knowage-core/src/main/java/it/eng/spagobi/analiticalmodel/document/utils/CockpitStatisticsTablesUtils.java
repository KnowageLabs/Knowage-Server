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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
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
		Transaction tx = session.beginTransaction();
		try {
			Integer biobjId = biObject.getId();

			List<SbiCockpitWidget> sbiCockpitWidgetsList = session.createCriteria(SbiCockpitWidget.class).add(Restrictions.eq("biobjId", biobjId)).list();
			for (SbiCockpitWidget sbiCockpitWidgets : sbiCockpitWidgetsList) {
				session.evict(sbiCockpitWidgets);
				session.delete(sbiCockpitWidgets);
				session.flush();
			}

			List<SbiCockpitAssociation> sbiCockpitAssociationsList = session.createCriteria(SbiCockpitAssociation.class)
					.add(Restrictions.eq("biobjId", biobjId)).list();
			for (SbiCockpitAssociation sbiCockpitAssociation : sbiCockpitAssociationsList) {
				session.evict(sbiCockpitAssociation);
				session.delete(sbiCockpitAssociation);
				session.flush();
			}

		} finally {
			if (!tx.wasCommitted())
				tx.commit();

		}

	}

	public static void updateCockpitWidgetsTable(BIObject biObject, Session session) {
		deleteCockpitWidgetsTable(biObject, session);

		SbiObjects sbiObjects = (SbiObjects) session.createCriteria(SbiObjects.class).add(Restrictions.eq("biobjId", biObject.getId())).uniqueResult();

		parseTemplate(sbiObjects, session, false);
	}

	public static void parseTemplate(SbiObjects sbiObjects, Session session, boolean initializer) {

		Criteria c = session.createCriteria(SbiObjTemplates.class, "sot");
		c.add(Restrictions.eq("sot.sbiObject.biobjId", sbiObjects.getBiobjId()));
		c.add(Restrictions.eq("sot.active", true));
		SbiObjTemplates sbiObjTemplates = (SbiObjTemplates) c.uniqueResult();

		if (sbiObjTemplates == null) {

		} else {

			try {
				SbiBinContents binContent = sbiObjTemplates.getSbiBinContents();

				byte[] contentByte = binContent.getContent();
				String contentString = new String(contentByte);
				JSONObject template = new JSONObject(contentString);

				Transaction tx = session.beginTransaction();
				Set<String> associationsMap = handleDatasetAssociations(sbiObjects, template, session, initializer);
				Map<Integer, JSONObject> dataSetMap = getDatasetMap(sbiObjects, template, session);

				JSONArray sheets = template.optJSONArray("sheets");
				if (sheets != null) {

					for (int i = 0; i < sheets.length(); i++) {
						JSONObject sheet;

						sheet = sheets.getJSONObject(i);

						JSONArray widgets = sheet.getJSONArray("widgets");
						for (int j = 0; j < widgets.length(); j++) {
							String datasetLabel = null;
							JSONObject widget = widgets.getJSONObject(j);

							String widgetType = widget.getString("type");
							if (widgetType.equals("map")) {
								Set<Integer> dsIds = parseContentForMapWidget(datasetLabel, widget);
								for (Integer dsId : dsIds) {
									SbiCockpitWidget sbiCockpitWidgets = new SbiCockpitWidget();

									sbiCockpitWidgets.setTab(sheet.getString("label"));
									sbiCockpitWidgets.setBiobjId(sbiObjects.getBiobjId());
									sbiCockpitWidgets.setWidgetType(widgetType);

									sbiCockpitWidgets.setDsId(dsId);

									boolean useCache = dataSetMap.get(dsId).optBoolean("useCache");
									sbiCockpitWidgets.setCache(useCache);

									updateSbiCommonInfo4Insert(sbiObjects, sbiCockpitWidgets, initializer);

									session.save(sbiCockpitWidgets);
									session.flush();

									if (!tx.wasCommitted())
										tx.commit();

								}
							} else {
								SbiCockpitWidget sbiCockpitWidgets = new SbiCockpitWidget();

								sbiCockpitWidgets.setTab(sheet.getString("label"));
								sbiCockpitWidgets.setBiobjId(sbiObjects.getBiobjId());
								sbiCockpitWidgets.setWidgetType(widgetType);

//								JSONObject cross = widget.optJSONObject("cross");
//								if (cross != null) {
//									JSONObject preview = cross.optJSONObject("preview");
//									if (preview != null) {
//										int oldId = preview.optInt("dataset");
//										Integer newId = dsIds.get(oldId);
//										preview.put("dataset", newId);
//
//									}
//								}

								datasetLabel = parseDatasetObject(sbiCockpitWidgets, datasetLabel, widget);

								datasetLabel = parseContentObject(sbiCockpitWidgets, datasetLabel, widget);

								datasetLabel = parseFiltersObject(sbiCockpitWidgets, datasetLabel, widget);

								sbiCockpitWidgets.setAssociative(associationsMap.contains(datasetLabel));

								if (sbiCockpitWidgets.getDsId() != null && sbiCockpitWidgets.getDsId() > 0) {
									boolean useCache = dataSetMap.get(sbiCockpitWidgets.getDsId()).optBoolean("useCache");
									sbiCockpitWidgets.setCache(useCache);
								}

								updateSbiCommonInfo4Insert(sbiObjects, sbiCockpitWidgets, initializer);
								sbiCockpitWidgets.getCommonInfo().setOrganization(sbiObjects.getCommonInfo().getOrganization());
								session.save(sbiCockpitWidgets);
								session.flush();

								if (!tx.wasCommitted())
									tx.commit();

							}

						} // end single widget
					} // end single sheet
				}

			} catch (JSONException e) {
				logger.error("Error while reading template information");
			} catch (Exception e) {
				logger.error("Error during parsing template widget", e);
			}
		}
		logger.debug("OUT");
	}

	private static Map<Integer, JSONObject> getDatasetMap(SbiObjects sbiObjects, JSONObject template, Session session) throws JSONException {
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
					}
				}
			}
		}
		return datasetMap;
	}

	private static String parseFiltersObject(SbiCockpitWidget sbiCockpitWidgets, String datasetLabel, JSONObject widget) throws JSONException {
		// FILTERS
		JSONArray filters = widget.optJSONArray("filters");
		boolean hasFilters = filters != null && !filters.toString().equals("{}");
		sbiCockpitWidgets.setFilters(hasFilters);
		if (filters != null) {

			for (int filterIdx = 0; filterIdx < filters.length(); filterIdx++) {
				JSONObject filter = (JSONObject) filters.get(filterIdx);
				if (filter != null && filter.has("dataset")) {
					JSONObject contDataset = filter.optJSONObject("dataset");
					if (contDataset != null && contDataset.has("dsId")) {
						Integer dsId = contDataset.optInt("dsId");
						sbiCockpitWidgets.setDsId(dsId);
						datasetLabel = contDataset.optString("label");
					}
				}
			}
		}
		return datasetLabel;
	}

	private static Set<Integer> parseContentForMapWidget(String datasetLabel, JSONObject widget) throws JSONException {
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
						dsIds.add(dsId);
						datasetLabel = layer.optString("label");
					}
				}
			}
		}

		return dsIds;
	}

	private static String parseContentObject(SbiCockpitWidget sbiCockpitWidgets, String datasetLabel, JSONObject widget) throws JSONException {
		/* DEVO CREARE UNA RIGA PER OGNUNO */
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
//						sbiCockpitWidgets.setDsId(oldId);
//						datasetLabel = layer.optString("label");
//					}
//				}
//			}

			JSONObject columns = content.optJSONObject("columnSelectedOfDataset");
			if (columns != null) {
				List<Integer> idToClean = new ArrayList();
				Iterator<?> keys = columns.keys();

				while (keys.hasNext()) {
					String key = (String) keys.next();
					if (columns.get(key) instanceof JSONArray) {
						Integer oldId = Integer.valueOf(key);
						if (oldId != null) {
							idToClean.add(oldId);
						}
					}
				}
				for (int d = 0; d < idToClean.size(); d++) { // update objects
					Integer oldId = idToClean.get(d);
					sbiCockpitWidgets.setDsId(oldId);
				}

			}

			if (content.has("dataset")) {
				JSONObject contDataset = content.optJSONObject("dataset");
				if (contDataset != null) {
					JSONObject id = contDataset.optJSONObject("id");
					if (id.has("dsId")) {
						Integer oldId = id.optInt("dsId");
						if (oldId != null) {
							sbiCockpitWidgets.setDsId(oldId);
							datasetLabel = contDataset.optString("label");
						}

					}

					if (contDataset.has("dsId")) {
						Integer oldId = contDataset.optInt("dsId");
						if (oldId != null && oldId != 0) {
							sbiCockpitWidgets.setDsId(oldId);
							datasetLabel = contDataset.optString("label");
						}
					}
				}
			}
		}
		return datasetLabel;
	}

	private static String parseDatasetObject(SbiCockpitWidget sbiCockpitWidgets, String datasetLabel, JSONObject widget) {
		JSONObject dataset = widget.optJSONObject("dataset");
		if (dataset != null) {
			if (dataset.has("dsId")) {
				Integer oldId = dataset.optInt("dsId");
				sbiCockpitWidgets.setDsId(oldId);
				datasetLabel = dataset.optString("label");

				/* CREARE UNA RIGA PER DATASET??? */
				try {
					JSONArray oldIdArray = dataset.getJSONArray("dsId");
					JSONArray tmp = new JSONArray();
					for (int k = 0; k < oldIdArray.length(); k++) {
						Integer jsonOldId = oldIdArray.getInt(k);
						if (jsonOldId != null) {
							tmp.put(jsonOldId);
							sbiCockpitWidgets.setDsId(jsonOldId);
							datasetLabel = dataset.optString("label");
						}
					}

					if (tmp.length() > 0) {
						// dataset.remove("dsId");
						// dataset.put("dsId", tmp);
						// sbiCockpitWidgets.setDsId(tmp);
					}
				} catch (JSONException ex) {
					logger.debug("Field dsId is not an array");
				}
			}

		} else {
			// html widget case: the datasetId is a simple property of widget obj
			Integer oldId = widget.optInt("datasetId");
			if (oldId != null) {
				sbiCockpitWidgets.setDsId(oldId);
				datasetLabel = widget.optString("label");
			}
		}
		return datasetLabel;
	}

	private static Set<String> handleDatasetAssociations(SbiObjects sbiObjects, JSONObject template, Session session, boolean initializer)
			throws JSONException {
		Set<String> associationsMap = new HashSet<String>();
		Transaction tx = session.beginTransaction();
		try {
			JSONObject configuration = template.optJSONObject("configuration");
			if (configuration != null) {
				JSONArray associations = configuration.optJSONArray("associations");
				boolean isAssociative = associations != null && !associations.toString().equals("{}");
				if (isAssociative) {
					for (int k = 0; k < associations.length(); k++) {
						JSONObject association = (JSONObject) associations.get(k);

						JSONArray fields = association.optJSONArray("fields");
						if (fields != null) {

							for (int i = 0; i < fields.length() - 1; i++) {

								IDataSetDAO dataSetDAO = DAOFactory.getDataSetDAO();
								dataSetDAO.setTenant(sbiObjects.getCommonInfo().getOrganization());

								JSONObject field1 = fields.optJSONObject(i);
								JSONObject field2 = fields.optJSONObject(i + 1);

								SbiCockpitAssociation sbiCockpitAssociations = new SbiCockpitAssociation();
								sbiCockpitAssociations.setBiobjId(sbiObjects.getBiobjId());

								if (field1.getString("store") != null) {
									Integer fromDatasetId = dataSetDAO.loadDataSetByLabel(field1.getString("store")).getId();
									sbiCockpitAssociations.setDsIdFrom(fromDatasetId);
									sbiCockpitAssociations.setColumnNameFrom(field1.getString("column"));
								}

								if (field2.getString("store") != null) {
									Integer toDatasetId = dataSetDAO.loadDataSetByLabel(field2.getString("store")).getId();
									sbiCockpitAssociations.setDsIdTo(toDatasetId);
									sbiCockpitAssociations.setColumnNameTo(field2.getString("column"));
								}

								updateSbiCommonInfo4Insert(sbiObjects, sbiCockpitAssociations, initializer);
								sbiCockpitAssociations.getCommonInfo().setOrganization(sbiObjects.getCommonInfo().getOrganization());
								session.save(sbiCockpitAssociations);
								session.flush();

							}
							associationsMap.add(association.getString("description"));
						}
					}
				}
			}
		} finally {
			if (!tx.wasCommitted())
				tx.commit();

		}

		return associationsMap;
	}

	/**
	 * @param sbiCockpitWidgets
	 * @param b
	 */
	private static void updateSbiCommonInfo4Insert(SbiObjects sbiObjects, SbiHibernateModel sbiCockpitWidgets, boolean initializer) {
		sbiCockpitWidgets.getCommonInfo().setTimeIn(new Date());
		sbiCockpitWidgets.getCommonInfo().setSbiVersionIn(SbiCommonInfo.getVersion());
		String userIn = initializer ? "server" : sbiObjects.getCommonInfo().getUserIn();
		sbiCockpitWidgets.getCommonInfo().setUserIn(userIn);
		sbiCockpitWidgets.getCommonInfo().setOrganization(sbiObjects.getCommonInfo().getOrganization());

	}
}
