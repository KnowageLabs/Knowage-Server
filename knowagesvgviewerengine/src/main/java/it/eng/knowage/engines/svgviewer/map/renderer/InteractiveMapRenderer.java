/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
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
package it.eng.knowage.engines.svgviewer.map.renderer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGElement;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import it.eng.knowage.engines.svgviewer.SvgViewerEngineConstants;
import it.eng.knowage.engines.svgviewer.SvgViewerEngineException;
import it.eng.knowage.engines.svgviewer.SvgViewerEngineRuntimeException;
import it.eng.knowage.engines.svgviewer.datamart.provider.IDataMartProvider;
import it.eng.knowage.engines.svgviewer.dataset.DataMart;
import it.eng.knowage.engines.svgviewer.dataset.HierarchyMember;
import it.eng.knowage.engines.svgviewer.map.provider.IMapProvider;
import it.eng.knowage.engines.svgviewer.map.renderer.configurator.InteractiveMapRendererConfigurator;
import it.eng.knowage.engines.svgviewer.map.utils.SVGMapLoader;
import it.eng.knowage.engines.svgviewer.map.utils.SVGMapMerger;
import it.eng.knowage.engines.svgviewer.map.utils.SVGMapSaver;
import it.eng.spago.base.SourceBean;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.json.JSONUtils;

public class InteractiveMapRenderer extends AbstractMapRenderer {

	private SVGMapLoader svgMapLoader;

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(InteractiveMapRenderer.class);

	public InteractiveMapRenderer() {
		super();
	}

	@Override
	public void init(Object conf) throws SvgViewerEngineException {
		super.init(conf);
		svgMapLoader = new SVGMapLoader();
		InteractiveMapRendererConfigurator.configure(this, getConf());
	}

	@Override
	public File renderMap(IMapProvider mapProvider, IDataMartProvider datamartProvider) throws SvgViewerEngineException {
		return renderMap(mapProvider, datamartProvider, SvgViewerEngineConstants.DSVG);
	}

	@Override
	public File renderMap(IMapProvider mapProvider, IDataMartProvider datamartProvider, String outputFormat) throws SvgViewerEngineRuntimeException {

		Monitor totalTimeMonitor = null;
		Monitor totalTimePerFormatMonitor = null;

		try {

			totalTimeMonitor = MonitorFactory.start("GeoEngine.drawMapAction.renderMap.totalTime");
			totalTimePerFormatMonitor = MonitorFactory.start("GeoEngine.drawMapAction.renderMap." + outputFormat + ".totalTime");

			if (outputFormat.equalsIgnoreCase(SvgViewerEngineConstants.SVG)) {
				return renderSVGMap(mapProvider, datamartProvider);
			} else if (outputFormat.equalsIgnoreCase(SvgViewerEngineConstants.DSVG)) {
				return renderDSVGMap(mapProvider, datamartProvider, false);
			} else if (outputFormat.equalsIgnoreCase(SvgViewerEngineConstants.XDSVG)) {
				return renderDSVGMap(mapProvider, datamartProvider, true);
			} else if (outputFormat.equalsIgnoreCase(SvgViewerEngineConstants.JPEG)) {
				return renderSVGMap(mapProvider, datamartProvider);
			}
		} finally {
			if (totalTimePerFormatMonitor != null)
				totalTimePerFormatMonitor.stop();
			if (totalTimeMonitor != null)
				totalTimeMonitor.stop();
		}

		return renderDSVGMap(mapProvider, datamartProvider, true);
	}

	/**
	 * Render dsvg map.
	 *
	 * @param mapProvider      the map provider
	 * @param datamartProvider the datamart provider
	 * @param includeScript    the include script
	 *
	 * @return the file
	 *
	 * @throws SvgViewerEngineException the geo engine exception
	 */
	private File renderDSVGMap(IMapProvider mapProvider, IDataMartProvider datamartProvider, boolean includeScript) throws SvgViewerEngineRuntimeException {

		SVGDocument targetMap;
		SVGDocument masterMap = null;
		File tmpMap;
		DataMart dataMart;
		Monitor loadDataMartTotalTimeMonitor = null;
		Monitor loadMasterMapTotalTimeMonitor = null;
		Monitor loadTargetMapTotalTimeMonitor = null;
		Monitor mergeAndDecorateMapTotalTimeMonitor = null;

		// load datamart
		HierarchyMember activeMember = null;
		try {
			loadDataMartTotalTimeMonitor = MonitorFactory.start("GeoEngine.drawMapAction.renderMap.loadDatamart");
			dataMart = datamartProvider.getDataMart();
			// load active members' layers and measures
			activeMember = datamartProvider.getHierarchyMember(datamartProvider.getSelectedMemberName());
			setLayers(activeMember.getLayers());
			setMeasures(activeMember.getMeasures());
			setSelectedMeasureName(activeMember.getMeasures());
		} finally {
			if (loadDataMartTotalTimeMonitor != null)
				loadDataMartTotalTimeMonitor.stop();
		}

		// load master map
		try {
			loadMasterMapTotalTimeMonitor = MonitorFactory.start("GeoEngine.drawMapAction.renderMap.loadMasterMap");
			masterMap = svgMapLoader.loadMapAsDocument(getMasterMapFile(true));
		} catch (IOException e) {
			SvgViewerEngineRuntimeException svgException;
			logger.error("Impossible to load map from file: " + getMasterMapFile(true));
			String description = "Impossible to load map from file: " + getMasterMapFile(true);
			svgException = new SvgViewerEngineRuntimeException("Impossible to render map", e);
			svgException.setDescription(description);
			throw svgException;
		} finally {
			if (loadMasterMapTotalTimeMonitor != null)
				loadMasterMapTotalTimeMonitor.stop();
		}

		// load target map
		try {
			loadTargetMapTotalTimeMonitor = MonitorFactory.start("GeoEngine.drawMapAction.renderMap.loadTargetMap");
			targetMap = mapProvider.getSVGMapDOMDocument(mapProvider.getSelectedHierarchyMember());
		} finally {
			if (loadTargetMapTotalTimeMonitor != null)
				loadTargetMapTotalTimeMonitor.stop();
		}

		// merge and decorate map
		try {

			mergeAndDecorateMapTotalTimeMonitor = MonitorFactory.start("GeoEngine.drawMapAction.renderMap.mergeAndDecorateMap");

			decorateMap(targetMap, masterMap, datamartProvider, dataMart, mapProvider);

			if (includeScript) {
				includeScripts(masterMap);
			} else {
				importScripts(masterMap);
			}

			setMainMapDimension(masterMap, targetMap);

			Element scriptInit = masterMap.getElementById("init");
			Node scriptText = scriptInit.getFirstChild();

			JSONObject conf = new JSONObject();

			try {
				if (!activeMember.getIsCustomized()) {
					JSONArray measures;
					measures = getMeasuresConfigurationScript(dataMart);
					String selectedMeasureName = getSelectedMeasureName();
					logger.debug("Selected measure [" + selectedMeasureName + "]");
					Assert.assertTrue(selectedMeasureName != null, "default_kpi attribute cannot be null. Please add it to MEASURES tag in your template file");

					int selectedMeasureIndexIndex = -1;
					for (int i = 0; i < measures.length(); i++) {
						JSONObject measure = (JSONObject) measures.get(i);
						logger.debug("Comparing selected measure [" + selectedMeasureName + "] with measure [" + (String) measure.get("name") + "]");
						String nm = (String) measure.get("name");
						if (selectedMeasureName.equalsIgnoreCase(nm)) {
							logger.debug("Selected measure [" + selectedMeasureName + "] is equal to measure [" + (String) measure.get("name") + "]");
							selectedMeasureIndexIndex = i;
							break;
						}
					}
					logger.debug("Selected measure index [" + selectedMeasureIndexIndex + "]");
					conf.put("selected_measure_index", selectedMeasureIndexIndex);
					conf.put("measures", measures);

					String infoText = datamartProvider.getSelectedMemberInfo();
					if (measures.length() == 0) {
						conf.put("info_text", "No data found");
					} else {
						conf.put("info_text", infoText);
					}
				} else {
					// for customized svg manage only info section
					String infoText = datamartProvider.getSelectedMemberInfo();
					conf.put("info_text", infoText);
				}

				JSONArray layers = getLayersConfigurationScript(targetMap);
				// String targetLayer = datamartProvider.getSelectedLevel().getFeatureName();
				// String targetLayer = dataMart.getTargetFeatureName();
				String targetLayer = dataMart.getTargetFeatureName().get(0); // as default put the first selected layer
				int targetLayerIndex = -1;
				for (int i = 0; i < layers.length(); i++) {
					JSONObject layer = (JSONObject) layers.get(i);

					if (targetLayer.equals(layer.get("name"))) {
						targetLayerIndex = i;
						break;
					}
				}
				conf.put("target_layer_index", targetLayerIndex);
				conf.put("layers", layers);

				JSONObject guiSettings = getGUIConfigurationScript();
				guiSettings.put("includeChartLayer", getLayer("grafici") != null);
				guiSettings.put("includeValuesLayer", getLayer("valori") != null);
				conf.put("gui_settings", guiSettings);

				String execId = (String) this.getEnv().get("SBI_EXECUTION_ID");
				// conf.put("execId", execId);
				conf.put("SBI_EXECUTION_ID", execId);

				JSONObject localeJSON = new JSONObject();
				Locale locale = (Locale) this.getEnv().get(EngineConstants.ENV_LOCALE);
				logger.debug("Current environment locale is: " + locale);
				if (locale == null) {
					logger.debug("Using default english locale");
					locale = Locale.ENGLISH;
				}
				localeJSON.put("language", locale.getLanguage());
				localeJSON.put("country", locale.getCountry());
				localeJSON.put("script", locale.getScript());
				DecimalFormatSymbols dfs = new DecimalFormatSymbols(locale);
				localeJSON.put("decimalSeparator", new Character(dfs.getDecimalSeparator()).toString());
				localeJSON.put("groupingSeparator", new Character(dfs.getGroupingSeparator()).toString());
				conf.put("locale", localeJSON);
			} catch (SvgViewerEngineRuntimeException e1) {
				throw e1;
			} catch (Exception e2) {
				SvgViewerEngineRuntimeException svgException;
				logger.error("Impossible to create sbi.geo.conf", e2);
				String description = "Impossible to create sbi.geo.conf";
				svgException = new SvgViewerEngineRuntimeException("Impossible to create sbi.geo.conf", e2);
				svgException.setDescription(description);
				throw svgException;
			}

			scriptText.setNodeValue("sbi = {};\n sbi.geo = {};\n sbi.geo.conf = " + conf.toString());

			try {
				tmpMap = getTempFile();
			} catch (IOException e) {
				SvgViewerEngineRuntimeException svgException;
				logger.error("Impossible to create a temporary file", e);
				String description = "Impossible to create a temporary file";
				svgException = new SvgViewerEngineRuntimeException("Impossible to render map", e);
				svgException.setDescription(description);
				throw svgException;
			} catch (Throwable t) {
				SvgViewerEngineRuntimeException svgException;
				logger.error("Impossible to create a temporary file", t);
				String description = "Impossible to create a temporary file";
				svgException = new SvgViewerEngineRuntimeException("Impossible to render map", t);
				svgException.setDescription(description);
				throw svgException;
			}
			try {
				SVGMapSaver.saveMap(masterMap, tmpMap);
			} catch (FileNotFoundException e) {
				SvgViewerEngineRuntimeException svgException;
				logger.error("Impossible to save map on temporary file " + tmpMap, e);
				String str = e.getMessage() != null ? e.getMessage() : e.getClass().getName();
				String description = "Impossible to save map on temporary file " + tmpMap + ". Root cause: " + str;
				svgException = new SvgViewerEngineRuntimeException("Impossible to render map", e);
				svgException.setDescription(description);
				throw svgException;
			} catch (TransformerException e) {
				SvgViewerEngineRuntimeException svgException;
				logger.error("Impossible to save map on temporary file " + tmpMap, e);
				String str = e.getMessage() != null ? e.getMessage() : e.getClass().getName();
				String description = "Impossible to save map on temporary file " + tmpMap + ". Root cause: " + str;
				svgException = new SvgViewerEngineRuntimeException("Impossible to render map", e);
				svgException.setDescription(description);
				throw svgException;
			} catch (Throwable t) {
				SvgViewerEngineRuntimeException svgException;
				logger.error("Impossible to save map on temporary file " + tmpMap, t);
				String str = t.getMessage() != null ? t.getMessage() : t.getClass().getName();
				String description = "Impossible to save map on temporary file " + tmpMap + ". Root cause: " + str;
				svgException = new SvgViewerEngineRuntimeException("Impossible to render map", t);
				svgException.setDescription(description);
				throw svgException;
			}
		} finally {
			if (mergeAndDecorateMapTotalTimeMonitor != null)
				mergeAndDecorateMapTotalTimeMonitor.stop();
		}

		return tmpMap;
	}

	/**
	 * Render svg map.
	 *
	 * @param mapProvider      the map provider
	 * @param datamartProvider the datamart provider
	 *
	 * @return the file
	 *
	 * @throws SvgViewerEngineException the geo engine exception
	 */
	private File renderSVGMap(IMapProvider mapProvider, IDataMartProvider datamartProvider) throws SvgViewerEngineRuntimeException {
		SVGDocument targetMap;
		SVGDocument masterMap;

		DataMart datamart;

		datamart = datamartProvider.getDataMart();

		targetMap = mapProvider.getSVGMapDOMDocument();
		try {
			masterMap = svgMapLoader.loadMapAsDocument(getMasterMapFile(false));
		} catch (IOException e) {
			SvgViewerEngineRuntimeException svgException;
			logger.error("Impossible to load map from file: " + getMasterMapFile(true));
			String description = "Impossible to load map from file: " + getMasterMapFile(true);
			svgException = new SvgViewerEngineRuntimeException("Impossible to render map", e);
			svgException.setDescription(description);
			throw svgException;
		}

		decorateMap(masterMap, targetMap, datamart);

		SVGMapMerger.mergeMap(targetMap, masterMap, null, "targetMap");

		setMainMapDimension(masterMap, targetMap);
		// setMainMapBkgRectDimension(masterMap, targetMap);

		File tmpMap;
		try {
			tmpMap = getTempFile();
		} catch (IOException e) {
			SvgViewerEngineRuntimeException svgException;
			logger.error("Impossible to create a temporary file", e);
			String description = "Impossible to create a temporary file";
			svgException = new SvgViewerEngineRuntimeException("Impossible to render map", e);
			svgException.setDescription(description);
			throw svgException;
		}
		try {
			SVGMapSaver.saveMap(masterMap, tmpMap);
		} catch (FileNotFoundException e) {
			SvgViewerEngineRuntimeException svgException;
			logger.error("Impossible to save map on temporary file " + tmpMap, e);
			String str = e.getMessage() != null ? e.getMessage() : e.getClass().getName();
			String description = "Impossible to save map on temporary file " + tmpMap + ". Root cause: " + str;
			svgException = new SvgViewerEngineRuntimeException("Impossible to render map", e);
			svgException.setDescription(description);
			throw svgException;
		} catch (TransformerException e) {
			SvgViewerEngineRuntimeException svgException;
			logger.error("Impossible to save map on temporary file " + tmpMap, e);
			String str = e.getMessage() != null ? e.getMessage() : e.getClass().getName();
			String description = "Impossible to save map on temporary file " + tmpMap + ". Root cause: " + str;
			svgException = new SvgViewerEngineRuntimeException("Impossible to render map", e);
			svgException.setDescription(description);
			throw svgException;
		}

		return tmpMap;
	}

	/**
	 * Decorate map. Used ONLY with renderDSVGMap().
	 *
	 * @param targetMap the target map
	 * @param datamart  the datamart
	 */
	private void decorateMap(SVGDocument targetMap, SVGDocument masterMap, IDataMartProvider datamartProvider, DataMart dataMart, IMapProvider mapProvider) {

		IDataStore dataStore;
		IMetaData dataStoreMeta;
		List lstElements = new ArrayList();
		Map mapElements = null;

		dataStore = dataMart.getDataStore();
		Assert.assertNotNull(dataStore, "DataStore cannot be null");

		dataStoreMeta = dataStore.getMetaData();
		Assert.assertNotNull(dataStore, "DataStoreMeta cannot be null");

		try {
			// List elements (one for each specific columnId)
			// find the geoID field
			List listID = dataStoreMeta.findFieldMeta("ROLE", "GEOID");
			List listDrillNav = dataStoreMeta.findFieldMeta("ROLE", "DRILLID");
			List listTooltip = dataStoreMeta.findFieldMeta("ROLE", "TOOLTIP");
			List listCrossType = dataStoreMeta.findFieldMeta("ROLE", "CROSSTYPE");
			List listInfo = dataStoreMeta.findFieldMeta("ROLE", "INFO");

			for (int l = 0; l < dataMart.getTargetFeatureName().size(); l++) {
				// Element targetLayer = targetMap.getElementById(dataMart.getTargetFeatureName());
				Element targetLayer = targetMap.getElementById(dataMart.getTargetFeatureName().get(l));
				if (targetLayer == null) {
					logger.error("Layer [" + dataMart.getTargetFeatureName() + "] doesn't exist for Hierarchy [" + datamartProvider.getSelectedHierarchyName()
							+ "], MemberName [" + datamartProvider.getSelectedMemberName() + "] and Level [" + datamartProvider.getSelectedLevel()
							+ "]. Please, check the template.");
					String description = "Layer [" + dataMart.getTargetFeatureName() + "] doesn't exist for Hierarchy ["
							+ datamartProvider.getSelectedHierarchyName() + "], MemberName [" + datamartProvider.getSelectedMemberName() + "] and Level ["
							+ datamartProvider.getSelectedLevel() + "]. Please, check the template.";
					SvgViewerEngineRuntimeException svgException;
					svgException = new SvgViewerEngineRuntimeException("Layer [" + dataMart.getTargetFeatureName() + "] doesn't exist for Hierarchy ["
							+ datamartProvider.getSelectedHierarchyName() + "], MemberName [" + datamartProvider.getSelectedMemberName() + "] and Level ["
							+ datamartProvider.getSelectedLevel() + "]. Please, check the template.");
					svgException.setDescription(description);
					throw svgException;
				}

				NodeList nodeList = targetLayer.getChildNodes();
				for (int i = 0; i < nodeList.getLength(); i++) {
					SVGElement child = null;
					String column_id = null;
					Node childNode = nodeList.item(i);
					if (childNode instanceof Element) {
						child = (SVGElement) childNode;
						String childId = child.getId();
						column_id = childId;
						IRecord record = null;

						try {
							record = dataStore.getRecordByID(column_id);
						} catch (NullPointerException ne) {
							logger.error("Searcing a record with the svg key [" + column_id
									+ "], was found a field with [null] as value. Please, check join column value into the dataset for all records!");
							continue;
						}

						if (record == null) {
							logger.warn("No data available for feature [" + column_id + "]");
							continue;
						}

						// ONLY FOR DEBUG
						String objName = null;
						try {
							IField fieldObjName = record.getFieldAt(dataStoreMeta.getFieldIndex("OBJ_NAME"));
							objName = (String) fieldObjName.getValue();
						} catch (ArrayIndexOutOfBoundsException e) {
							// do nothing (simply the field doesn't exist into the dataset)
						}
						// END DEBUG

						// defines base list of element to decorate
						mapElements = new HashMap();
						mapElements.put("column_id", column_id);

						// 1. addData details as attributes
						addData(dataStore, child, record);
						mapElements.put("path", child);

						// 2. add CROSS link ONLY if it's required by the template (at the moment is mutual exclusive with the drill link)
						boolean useCrossNav = false; // default
						boolean disabledLink = false; // default (when the link is disabled from the dataset column with the null value)
						String defaultUrl = "javascript:void(0)";
						// check the dynamic cross type definition (throught the dataset)
						if (listCrossType.size() > 0) {
							IFieldMetaData fieldMetaCrossable = (IFieldMetaData) listCrossType.get(0);
							IField fieldCrossable = record.getFieldAt(dataStoreMeta.getFieldIndex(fieldMetaCrossable.getName()));
							String fieldCrossableValue = (String) fieldCrossable.getValue();
							if (fieldCrossableValue != null) {
								useCrossNav = (((String) fieldCrossable.getValue()).equalsIgnoreCase("cross")) ? true : false;
							} else {
								disabledLink = true;
								logger.debug("[crosstype] property for the element is null. The link will be disabled.");
							}
						}
						if (listCrossType.size() == 0 && datamartProvider.getHierarchyMember(datamartProvider.getSelectedMemberName()).getEnableCross()) {
							useCrossNav = true;
						}
						if (!disabledLink && useCrossNav) {
							logger.debug("Required cross navigation for member [" + datamartProvider.getSelectedHierarchyName() + "]. "
									+ " Checking presence of cross navigation definition...");
							boolean isCrossable = DAOFactory.getCrossNavigationDAO().documentIsCrossable((String) this.getEnv().get("DOCUMENT_LABEL"));
							if (isCrossable) {
								JSONArray crossData = addCrossData(listID, record, dataStoreMeta, datamartProvider, mapProvider);
								if (crossData != null)
									mapElements.put("crossData", crossData);
								mapElements.put("link_cross", defaultUrl);
							} else {
								logger.debug("... The cross navigation for the document isn't present." + " Please, check its definition through the GUI.");
							}
						}

						// 3. add DRILL links ONLY if it isn't the last level
						int intSelectedLevel = (datamartProvider.getSelectedLevel() == null) ? 1 : Integer.parseInt(datamartProvider.getSelectedLevel());
						int totalLevels = datamartProvider.getHierarchyMembersNames().size();
						if (!disabledLink && !useCrossNav && (intSelectedLevel < totalLevels)) {
							String drillIdValue = addLinkDrillId(listDrillNav, record, dataStoreMeta);
							mapElements.put("drill_id", drillIdValue);
							mapElements.put("link_drill", defaultUrl);
						}
						// 4. add Tooltip
						String tooltip = addTooltip(listTooltip, record, dataStoreMeta);
						mapElements.put("tooltip", tooltip);
						// add complete element to the final list
						lstElements.add(mapElements);
					}
				}

				// adds href links (element and event)
				for (int j = 0; j < lstElements.size(); j++) {
					Map tmpMap = (Map) lstElements.get(j);
					Element featureElement = (Element) tmpMap.get("path");
					String elementId = featureElement.getAttribute("id");

					String linkType = null;
					if (tmpMap.get("link_cross") != null) {
						linkType = "cross";
					} else if (tmpMap.get("link_drill") != null) {
						linkType = "drill";
					}

					String linkUrl = (linkType != null && linkType.equals("cross")) ? (String) tmpMap.get("link_cross") : (String) tmpMap.get("link_drill");
					if (linkUrl != null) {
						String drillId = null;
						if (tmpMap.get("drill_id") != null) {
							drillId = (String) tmpMap.get("drill_id");
						}

						if (linkType.equals("cross")) {
							JSONArray jsonValue = (JSONArray) tmpMap.get("crossData");
							JSONArray jsonCross = datamartProvider.getHierarchyMember(datamartProvider.getSelectedMemberName()).getLabelsCross();
							addHRefLinksCross(targetMap, featureElement, jsonValue, jsonCross, datamartProvider);
						} else {
							addHRefLinksDrill(targetMap, featureElement, elementId, drillId, linkUrl);
						}
					}
					// add tooltips events
					String tooltip = null;
					if (tmpMap.get("tooltip") != null) {
						tooltip = (String) tmpMap.get("tooltip");
					}
					addTooltipEvents(featureElement, tooltip, elementId);

					// append element to the targer svg
					targetLayer.appendChild(featureElement);
					Node lf = targetMap.createTextNode("\n");
					targetLayer.appendChild(lf);
				}

				SVGMapMerger.mergeMap(targetMap, masterMap, null, "targetMap");

				// decorate from datastore: labels and visibility
				List listLabel = dataStoreMeta.findFieldMeta("ROLE", "LABEL");
				// find field with visibility values
				List listVis = dataStoreMeta.findFieldMeta("ROLE", "VISIBILITY");

				if (listLabel.size() == 0 && listID.size() == 0 && listVis.size() == 0) {
					return;
				}
				IFieldMetaData labelsIdMetaData = (listLabel.size() > 0) ? (IFieldMetaData) listLabel.get(0) : null;
				IFieldMetaData geoIdMetaData = (listID.size() > 0) ? (IFieldMetaData) listID.get(0) : null;
				IFieldMetaData visibilityIdMetaData = (listVis.size() > 0) ? (IFieldMetaData) listVis.get(0) : null;

				for (int i = 0; i < dataStore.getRecordsCount(); i++) {
					IRecord aRecord = dataStore.getRecordAt(i);
					List<IField> fields = aRecord.getFields();

					IField field = aRecord.getFieldAt(dataStoreMeta.getIdFieldIndex());
					// String id = (String) field.getValue();
					String id = String.valueOf(field.getValue());

					// 5. add LABELS
					String centroideId = "centroidi_" + id;
					Element centroide = masterMap.getElementById(centroideId);
					if (centroide != null && labelsIdMetaData != null) {
						IField labelField = aRecord.getFieldAt(dataStoreMeta.getFieldIndex(labelsIdMetaData.getName()));
						Element labelGroup = null;
						if (fields.size() > 0) {
							labelGroup = masterMap.createElement("g");
							addLabels(masterMap, centroide, labelGroup, aRecord, labelField);
						}
					}
					// 6. manage visibility
					if (geoIdMetaData != null && visibilityIdMetaData != null) {
						IField geoIdField = aRecord.getFieldAt(dataStoreMeta.getFieldIndex(geoIdMetaData.getName()));
						IField visibilityIdField = aRecord.getFieldAt(dataStoreMeta.getFieldIndex(visibilityIdMetaData.getName()));

						if (geoIdField != null && visibilityIdField != null) {
							showElements(masterMap, geoIdField, visibilityIdField);
						}
					}

					// 7. add INFO content only one time (if it's configurated)
					String infoText = null;
					if (infoText == null) {
						infoText = addInfoText(listInfo, aRecord, dataStoreMeta);
						if (infoText != null) {
							logger.debug("infoText is [" + infoText + "]");
							datamartProvider.setSelectedMemberInfo(infoText);
						} else {
							logger.debug("Not Info text found");
							datamartProvider.setSelectedMemberInfo(null);
						}
					}
				}
			}
		} catch (Throwable t) {
			SvgViewerEngineRuntimeException svgException;
			logger.error("Impossible to decorate target SVG.", t);
			String description = "Impossible to decorate target SVG.";
			svgException = new SvgViewerEngineRuntimeException("Impossible to decorate target SVG.", t);
			svgException.setDescription(description);
			throw svgException;
		}
	}

	/**
	 * Decorate map. Used ONLY with renderSVGMap().
	 *
	 * @param masterMap the master map
	 * @param targetMap the target map
	 * @param datamart  the datamart
	 */
	private void decorateMap(SVGDocument masterMap, SVGDocument targetMap, DataMart datamart) {

		IDataStore dataStore = datamart.getDataStore();
		IMetaData dataStoreMeta = dataStore.getMetaData();
		List measureFieldsMeta = dataStoreMeta.findFieldMeta("ROLE", "MEASURE");
		String[] kpiNames = new String[measureFieldsMeta.size()];
		for (int i = 0; i < kpiNames.length; i++) {
			IFieldMetaData filedMeta = (IFieldMetaData) measureFieldsMeta.get(i);
			kpiNames[i] = filedMeta.getName();
		}

		// int selectedKpiIndex = dataStoreMeta.getFieldIndex( getSelectedMeasureName() );
		String selectedKpiName = getSelectedMeasureName(); // kpiNames[selectedKpiIndex];
		Assert.assertTrue(selectedKpiName != null, "default_kpi attribute cannot be null. Please add it to MEASURES tag in your template file");
		Measure measure = getMeasure(selectedKpiName);
		Number lb_value = null;
		Number ub_value = null;
		String lb_color = null;
		String ub_color = null;
		String null_values_color = null;
		String[] trasholdCalculationPercParams = null;
		Integer num_group = null;
		Integer trasholdCalculationUniformParams = null;
		String colorRangeCalculationGradParams = null;
		String[] col_kpi_array = null;
		Number[] trash_kpi_array = null;
		Number[] kpi_ordered_values = null;

		dataStore.sortRecords(dataStoreMeta.getFieldIndex(selectedKpiName));
		List orderedKpiValuesSet = dataStore.getFieldValues(dataStoreMeta.getFieldIndex(selectedKpiName));
		// Set orderedKpiValuesSet = datamart.getOrderedKpiValuesSet( selectedKpiName );
		kpi_ordered_values = (Number[]) orderedKpiValuesSet.toArray(new Number[0]);

		if (measure.getTresholdLb() == null || measure.getTresholdLb().trim().equalsIgnoreCase("") || measure.getTresholdLb().equalsIgnoreCase("none")) {
			lb_value = null;
		} else {
			lb_value = Double.parseDouble(measure.getTresholdLb());
		}

		if (measure.getTresholdUb() == null || measure.getTresholdUb().trim().equalsIgnoreCase("") || measure.getTresholdUb().equalsIgnoreCase("none")) {
			ub_value = null;
		} else {
			ub_value = Double.parseDouble(measure.getTresholdUb());
		}

		lb_color = measure.getColurOutboundCol();
		ub_color = measure.getColurOutboundCol();
		null_values_color = measure.getColurNullCol();

		String numGroupAttr = measure.getTresholdCalculatorParameters().getProperty("GROUPS_NUMBER");
		if (numGroupAttr != null) {
			num_group = Integer.parseInt(numGroupAttr);
			trasholdCalculationUniformParams = num_group;
		}

		colorRangeCalculationGradParams = measure.getColurCalculatorParameters().getProperty("BASE_COLOR");

		// ////////////////////////////////////////////////////////////////////////
		// SetTrashHolds
		// /////////////
		if (lb_value == null) {
			lb_value = kpi_ordered_values[0];
		}
		if (ub_value == null) {
			ub_value = kpi_ordered_values[kpi_ordered_values.length - 1];
		}

		if (lb_value.doubleValue() > ub_value.doubleValue()) {
			Number t = lb_value;
			ub_value = lb_value;
			lb_value = t;
		}

		if (ub_value.doubleValue() < kpi_ordered_values[0].doubleValue()
				|| lb_value.doubleValue() > kpi_ordered_values[kpi_ordered_values.length - 1].doubleValue()) {
			lb_value = kpi_ordered_values[0];
			ub_value = kpi_ordered_values[kpi_ordered_values.length - 1];
		}

		if (measure.getTresholdCalculatorType().equalsIgnoreCase("quantile")) {

			trash_kpi_array = new Number[num_group + 1];

			int diff_value_num = 0;
			int start_index = -1;
			if (kpi_ordered_values[0].doubleValue() >= lb_value.doubleValue()
					&& kpi_ordered_values[kpi_ordered_values.length - 1].doubleValue() <= ub_value.doubleValue()) {
				diff_value_num = kpi_ordered_values.length;
				start_index = 0;
			} else {
				for (int j = 0; j < kpi_ordered_values.length; j++) {
					if (kpi_ordered_values[j].doubleValue() >= lb_value.doubleValue() && kpi_ordered_values[j].doubleValue() <= ub_value.doubleValue()) {
						start_index = (start_index == -1 ? j : start_index);
						diff_value_num++;
					}
				}
			}

			if (diff_value_num < num_group)
				num_group = diff_value_num;
			int blockSize = (int) Math.floor(diff_value_num / num_group);

			trash_kpi_array[0] = lb_value;
			for (int j = 1; j < num_group; j++) {
				trash_kpi_array[j] = kpi_ordered_values[start_index + (j * blockSize)];
			}
			trash_kpi_array[num_group] = ub_value;

		} else if (measure.getTresholdCalculatorType().equalsIgnoreCase("perc")) {
			double range = ub_value.doubleValue() - lb_value.doubleValue();

			trasholdCalculationPercParams = getTresholdsArray(measure.getColumnId());
			trash_kpi_array = new Number[trasholdCalculationPercParams.length + 1];

			trash_kpi_array[0] = lb_value;
			for (int j = 0; j < trasholdCalculationPercParams.length; j++) {
				double groupSize = (range / 100.0) * Double.parseDouble(trasholdCalculationPercParams[j]);
				trash_kpi_array[j + 1] = trash_kpi_array[j].doubleValue() + groupSize;
			}
			trash_kpi_array[trash_kpi_array.length - 1] = ub_value;
			num_group = trash_kpi_array.length - 1;
		} else if (measure.getTresholdCalculatorType().equalsIgnoreCase("uniform")) {
			trash_kpi_array = new Number[trasholdCalculationUniformParams.intValue() + 1];
			double perc = 100 / (trasholdCalculationUniformParams.doubleValue());
			trasholdCalculationPercParams = new String[trasholdCalculationUniformParams.intValue() + 1];
			for (int j = 0; j < trasholdCalculationPercParams.length; j++) {
				trasholdCalculationPercParams[j] = "" + perc;
			}

			double range = ub_value.doubleValue() - lb_value.doubleValue();
			trash_kpi_array[0] = lb_value;

			for (int j = 0; j < trash_kpi_array.length - 2; j++) {
				double groupSize = (range / 100.0) * Double.parseDouble(trasholdCalculationPercParams[j]);
				trash_kpi_array[j + 1] = trash_kpi_array[j].doubleValue() + groupSize;
			}
			trash_kpi_array[trash_kpi_array.length - 1] = ub_value;
			num_group = trasholdCalculationPercParams.length - 1;
		} else if (measure.getTresholdCalculatorType().equalsIgnoreCase("static")) {
			String[] trasholdsArray = getTresholdsArray(selectedKpiName);
			trash_kpi_array = new Number[trasholdsArray.length];
			for (int j = 0; j < trasholdsArray.length; j++) {
				trash_kpi_array[j] = new Double(trasholdsArray[j]);
			}
		} else {
			// setQuantileTrasholds(kpi_names[i]);
		}

		if (num_group == null) { // static case, num_group is calculated from bounds
			num_group = new Integer(trash_kpi_array.length - 1);
		}

		if (measure.getColurCalculatorType().equalsIgnoreCase("static")) {
			col_kpi_array = getColoursArray(selectedKpiName);
		} else if (measure.getColurCalculatorType().equalsIgnoreCase("gradient") || measure.getColurCalculatorType().equalsIgnoreCase("grad")) {
			col_kpi_array = getGradientColourRange(colorRangeCalculationGradParams, num_group);
		} else {
			col_kpi_array = getGradientColourRange(colorRangeCalculationGradParams, num_group);
		}
		logger.debug(Arrays.toString(col_kpi_array));

		// Element targetLayer = targetMap.getElementById(datamart.getTargetFeatureName());
		Element targetLayer = targetMap.getElementById(datamart.getTargetFeatureName().get(0)); // for default uses the fist element (old version)

		NodeList nodeList = targetLayer.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node childNode = nodeList.item(i);
			if (childNode instanceof Element) {
				SVGElement child = (SVGElement) childNode;

				String childId = child.getId();
				String column_id = childId.replaceAll(datamart.getTargetFeatureName() + "_", "");

				IRecord record = dataStore.getRecordByID(column_id);

				// Map attributes = (Map)datamart.getAttributeseById(column_id);

				String targetColor = null;
				Number kpyValue = null;
				if (record != null) {
					IField field = record.getFieldAt(dataStoreMeta.getFieldIndex(selectedKpiName));
					String kpyValueAttr = "" + field.getValue();
					// String kpyValueAttr = (String)attributes.get( selectedKpiName );
					if (kpyValueAttr == null) {
						targetColor = null_values_color;
					} else {
						kpyValue = Double.parseDouble(kpyValueAttr);

						if (kpyValue.doubleValue() < lb_value.doubleValue()) {
							targetColor = lb_color;
						} else if (kpyValue.doubleValue() > ub_value.doubleValue()) {
							targetColor = ub_color;
						} else if (kpyValue.doubleValue() == ub_value.doubleValue()) {
							targetColor = col_kpi_array[trash_kpi_array.length - 2];
						} else {
							for (int j = 0; j < trash_kpi_array.length - 1; j++) {
								if (kpyValue.doubleValue() >= trash_kpi_array[j].doubleValue()
										&& kpyValue.doubleValue() < trash_kpi_array[j + 1].doubleValue()) {
									targetColor = col_kpi_array[j];
									break;
								}
							}
						}
					}
				}

				if (targetColor != null) {
					if (child.getNodeName().equals("path") || child.getNodeName().equals("polygon") || child.getNodeName().equals("ellipse")
							|| child.getNodeName().equals("circle") || child.getNodeName().equals("rect")) {

						child.setAttribute("fill", targetColor);
					} else if (child.getNodeName().equals("line") || child.getNodeName().equals("polyline")) {
						child.setAttribute("stroke", targetColor);
					}

					String opacity = measure.getColurCalculatorParameters().getProperty("opacity");
					if (opacity != null) {
						child.setAttribute("opacity", opacity);
					}

				}

			}
		}

		// add label
		// Map values = datamart.getValues();
		// Iterator it = values.keySet().iterator();
		Iterator it = dataStore.iterator();
		while (it.hasNext()) {
			IRecord record = (IRecord) it.next();
			IField field = null;

			field = record.getFieldAt(dataStoreMeta.getIdFieldIndex());
			String id = (String) field.getValue();
			// String id = (String)it.next();

			// Map kpiValueMap = (Map)values.get(id);

			String centroideId = "centroidi_" + datamart.getTargetFeatureName() + "_" + id;
			Element centroide = targetMap.getElementById(centroideId);
			if (centroide != null) {
				List fields = record.getFields();
				int line = 0;
				Element labelGroup = null;
				if (fields.size() > 0)
					labelGroup = masterMap.createElement("g");
				boolean isFirst = true;
				for (int i = 0; i < fields.size(); i++) {
					if (i == dataStoreMeta.getIdFieldIndex())
						continue;

					field = (IField) fields.get(i);
					String fieldName = dataStoreMeta.getFieldAlias(i);
					// String tmpKpiName = (String)kpiValueIterator.next();

					Measure kpi = getMeasure(fieldName);
					String kpiValue = "" + field.getValue();
					labelGroup.setAttribute("transform", "translate(" + centroide.getAttribute("cx") + "," + centroide.getAttribute("cy") + ") scale(40)");
					labelGroup.setAttribute("display", "inherit");
					// get correct anchor from the centroide throught property inkscape:label; for default uses 'middle'
					String anchor = "middle";
					if (centroide.getAttribute("inkscape:label").equals("start") || centroide.getAttribute("inkscape:label").equals("middle")
							|| centroide.getAttribute("inkscape:label").equals("end")) {
						anchor = centroide.getAttribute("inkscape:label");
					}
					Element label = masterMap.createElement("text");
					label.setAttribute("x", "0");
					label.setAttribute("y", "" + ((line++) * 16));
					label.setAttribute("text-anchor", anchor);
					label.setAttribute("font-family", "Arial,Helvetica");
					label.setAttribute("font-size", isFirst ? "16px" : "14px");
					label.setAttribute("font-style", isFirst ? "normal" : "italic");
					label.setAttribute("fill", "black");
					isFirst = false;

					String kpiValueString = null;
					if (kpi.getPattern() != null) {
						String pattern = kpi.getPattern();
						DecimalFormat df = new DecimalFormat(pattern);
						kpiValueString = kpiValue != null ? df.format(Double.parseDouble(kpiValue)) : "?";
					} else {
						kpiValueString = kpiValue != null ? kpiValue : "?";
					}

					if (!kpiValueString.equalsIgnoreCase("?") && kpi.getUnit() != null) {
						String unit = kpi.getUnit();
						kpiValueString = kpiValueString + unit;
					}

					Node labelText = masterMap.createTextNode(kpiValueString);
					label.appendChild(labelText);

					labelGroup.appendChild(label);
				}

				if (labelGroup != null) {
					Element valuesLayer = masterMap.getElementById("values");
					valuesLayer.appendChild(labelGroup);
				}
			}
		}

		// add legend
		Element windowBackground = masterMap.createElement("rect");
		Element windowTitleBar = masterMap.createElement("rect");
		Element windowTitle = masterMap.createElement("text");
		Element windowBody = masterMap.createElement("g");
		for (int i = 0; i < col_kpi_array.length; i++) {
			Double lb = trash_kpi_array[i].doubleValue();
			Double ub = trash_kpi_array[i + 1] != null ? trash_kpi_array[i + 1].doubleValue() : null;
			String color = col_kpi_array[i];

			String lbValueString = null;
			String ubValueString = null;
			if (measure.getPattern() != null) {
				String pattern = measure.getPattern();
				DecimalFormat df = new DecimalFormat(pattern);
				lbValueString = lb != null ? df.format(lb.doubleValue()) : "?";
				ubValueString = ub != null ? df.format(ub.doubleValue()) : "?";
			} else {
				lbValueString = lb != null ? lb.toString() : "?";
				ubValueString = ub != null ? ub.toString() : "?";
			}

			if (!lb.toString().equalsIgnoreCase("?") && measure.getUnit() != null) {
				String unit = measure.getUnit();
				lbValueString = lbValueString + unit;
			}

			if (ub != null && !ub.toString().equalsIgnoreCase("?") && measure.getUnit() != null) {
				String unit = measure.getUnit();
				ubValueString = ubValueString + unit;
			}

			Element colorBox = masterMap.createElement("rect");
			int offset = 35 + (25 * i);
			colorBox.setAttribute("x", "30");
			colorBox.setAttribute("y", "" + offset);
			colorBox.setAttribute("width", "30");
			colorBox.setAttribute("height", "20");
			colorBox.setAttribute("fill", color);
			colorBox.setAttribute("stroke", "dimgray");

			offset = 50 + (25 * i);
			Element labelBox = masterMap.createElement("text");
			labelBox.setAttribute("x", "70");
			labelBox.setAttribute("y", "" + offset);
			labelBox.setAttribute("font-family", "Arial,Helvetica");
			labelBox.setAttribute("font-size", "14px");
			labelBox.setAttribute("fill", "dimgray");
			labelBox.setAttribute("startOffset", "0");
			Node labelBoxText = masterMap.createTextNode(lbValueString + " - " + ubValueString);
			labelBox.appendChild(labelBoxText);

			windowBody.appendChild(colorBox);
			windowBody.appendChild(labelBox);
		}

		// add labels
		Node labelText;
		Element label;

		ILabelProducer labelProducer;

		labelProducer = (ILabelProducer) getGuiSettings().getLabelProducers().get("header-left");
		if (labelProducer != null) {
			label = masterMap.getElementById("header-left");
			labelText = masterMap.createTextNode(labelProducer.getLabel());
			label.appendChild(labelText);
		}

		labelProducer = (ILabelProducer) getGuiSettings().getLabelProducers().get("header-left");
		if (labelProducer != null) {
			label = masterMap.getElementById("header-center");
			labelText = masterMap.createTextNode(labelProducer.getLabel());
			label.appendChild(labelText);
		}

		labelProducer = (ILabelProducer) getGuiSettings().getLabelProducers().get("header-left");
		if (labelProducer != null) {
			label = masterMap.getElementById("header-right");
			labelText = masterMap.createTextNode(labelProducer.getLabel());
			label.appendChild(labelText);
		}

		labelProducer = (ILabelProducer) getGuiSettings().getLabelProducers().get("header-left");
		if (labelProducer != null) {
			label = masterMap.getElementById("footer-left");
			labelText = masterMap.createTextNode(labelProducer.getLabel());
			label.appendChild(labelText);
		}

		labelProducer = (ILabelProducer) getGuiSettings().getLabelProducers().get("header-left");
		if (labelProducer != null) {
			label = masterMap.getElementById("footer-center");
			labelText = masterMap.createTextNode(labelProducer.getLabel());
			label.appendChild(labelText);
		}

		labelProducer = (ILabelProducer) getGuiSettings().getLabelProducers().get("header-left");
		if (labelProducer != null) {
			label = masterMap.getElementById("footer-right");
			labelText = masterMap.createTextNode(labelProducer.getLabel());
			label.appendChild(labelText);
		}
	}

	/** The Constant R. */
	private static final int R = 0;

	/** The Constant G. */
	private static final int G = 1;

	/** The Constant B. */
	private static final int B = 2;

	/** The Constant BASE_COLOR. */
	private static final String BASE_COLOR = "#";

	/**
	 * Gets the gradient colour range.
	 *
	 * @param base_color the base_color
	 * @param num_group  the num_group
	 *
	 * @return the gradient colour range
	 */
	public String[] getGradientColourRange(String base_color, int num_group) {
		int[] A = new int[3];
		int[] RGB = new int[3];
		int[] Grad = new int[3];
		int new_rA;
		int new_gA;
		int new_bA;
		String shade;

		// if(!colurCalculatorType.equalsIgnoreCase("gradient")) return new String[]{"#FF0000","#00FF00","#FF00FF","#0000FF","#F0F0F0"};

		A[R] = Integer.parseInt(base_color.substring(1, 3), 16);
		A[G] = Integer.parseInt(base_color.substring(3, 5), 16);
		A[B] = Integer.parseInt(base_color.substring(5), 16);

		System.arraycopy(A, 0, RGB, 0, 3);
		Arrays.sort(RGB);
		for (int i = 0; i < A.length; i++) {
			if (A[i] == RGB[2]) {
				Grad[i] = (240 - A[i]) / (num_group - 1);
			} else if (A[i] == RGB[1]) {
				Grad[i] = (230 - A[i]) / (num_group - 1);
			} else {
				Grad[i] = (220 - A[i]) / (num_group - 1);
			}
		}

		String[] colorRangeArray = new String[num_group];
		for (int i = 0; i < num_group; i++) {
			new_rA = A[R] + Grad[R] * i;
			new_gA = A[G] + Grad[G] * i;
			new_bA = A[B] + Grad[B] * i;
			String rA = Integer.toHexString(new_rA);
			String gA = Integer.toHexString(new_gA);
			String bA = Integer.toHexString(new_bA);
			shade = "#" + (rA.length() == 1 ? "0" : "") + rA + (gA.length() == 1 ? "0" : "") + gA + (bA.length() == 1 ? "0" : "") + bA;
			colorRangeArray[i] = shade;
		}
		List colorRangeList = Arrays.asList(colorRangeArray);
		Collections.reverse(colorRangeList);
		colorRangeArray = (String[]) colorRangeList.toArray(new String[0]);

		return colorRangeArray;
	}

	/**
	 * Add label to the centroide
	 *
	 * @param masterMap  the svg content
	 * @param centroide  the centroide svg element
	 * @param labelGroup the svg label group element
	 * @param aRecord    the record with values
	 * @param labelField the label field
	 */
	private void addLabels(SVGDocument masterMap, Element centroide, Element labelGroup, IRecord aRecord, IField labelField) {
		logger.debug("IN");
		labelGroup.setAttribute("transform", "translate(" + centroide.getAttribute("cx") + "," + centroide.getAttribute("cy") + ") scale(1)");
		labelGroup.setAttribute("display", "inherit");

		Element label = masterMap.createElement("text");
		label.setAttribute("x", "0");
		label.setAttribute("y", "0");

		label.setAttribute("font-family", "Arial,Helvetica");
		label.setAttribute("font-size", "12px");
		label.setAttribute("font-style", "normal");
		label.setAttribute("fill", "black");
		// label.setAttribute("text-anchor", "middle");
		// get text-anchor property:
		// 1. throught text-anchor property
		// 2. throught style property
		// 3. if it isn't found force 'middle' as default
		String anchor = "middle";
		String anchorProperty = centroide.getAttribute("text-anchor");
		if (anchorProperty != null && anchorProperty.equals("start") || anchorProperty.equals("middle") || anchorProperty.equals("end")) {
			anchor = anchorProperty;
		} else {
			String styleProperty = centroide.getAttribute("style");
			int anchorPropertyPosStart = styleProperty.indexOf("text-anchor:");
			int anchorPropertyPosEnd = styleProperty.indexOf(";", anchorPropertyPosStart);
			if (null != styleProperty && anchorPropertyPosStart >= 0) {
				anchorProperty = styleProperty.substring(anchorPropertyPosStart + 12, anchorPropertyPosEnd);
				anchor = anchorProperty;
				// clean the style from the anchor information
				styleProperty = styleProperty.replace(styleProperty.substring(anchorPropertyPosStart, anchorPropertyPosEnd + 1), "");
				centroide.setAttribute("style", styleProperty);
			}
		}
		label.setAttribute("text-anchor", anchor);
		Node labelText = masterMap.createTextNode((String) labelField.getValue());
		label.appendChild(labelText);
		labelGroup.appendChild(label);
		if (labelGroup != null) {
			// append labels to default layer "valori"
			Element valuesLayer = masterMap.getElementById("_labels_layer");
			valuesLayer.appendChild(labelGroup);
		}

	}

	/**
	 * Show or hide elements through the specific column value
	 *
	 * @param masterMap         the svg content
	 *
	 * @param geoIdField        the element id field
	 * @param visibilityIdField the visibility field
	 */
	private void showElements(SVGDocument masterMap, IField geoIdField, IField visibilityIdField) {
		logger.debug("IN");

		String id_element = (String) geoIdField.getValue();
		String elementVisibility = (String) visibilityIdField.getValue();
		try {
			Element element = masterMap.getElementById(id_element);
			if (element != null) {
				String displayStyle = "";
				String elementStyle = element.getAttribute("style");
				// clean style from ;;
				if (elementStyle.indexOf(";;") >= 0) {
					elementStyle = elementStyle.replaceAll(";;", ";");
				}
				// get original display option if present
				int displayStyleStart = elementStyle.indexOf("display:");
				String displayStyleValue = "";

				// if (displayStyleStart >= 0) {
				// int displayStyleEnd = -1;
				// try {
				// displayStyleEnd = elementStyle.indexOf(";", displayStyleStart);
				// displayStyleValue = elementStyle.substring(displayStyleStart, displayStyleEnd + 1);
				// } catch (StringIndexOutOfBoundsException se) {
				// logger.error("An error occured while getting style content of element with id [" + id_element
				// + "]. Please, check that ALL the style elements into the SVG have the final [;] char. Ex: [display:none;]");
				// throw se;
				// }
				// elementStyle = elementStyle.replace(displayStyleValue, ""); // clean old style
				// }

				// Manage 'display:none' or 'display:none;' properties
				if (displayStyleStart >= 0) {
					int displayStyleEnd = -1;
					String displayContent = "";
					try {
						// case with ; or other properties
						if (elementStyle.length() >= displayStyleStart + 13) {
							displayContent = elementStyle.substring(displayStyleStart, displayStyleStart + 13);
						} else {
							// case without ';'. Style value is : style="display:none"
							displayContent = elementStyle.substring(displayStyleStart, displayStyleStart + 12);
						}
						displayContent = displayContent.trim();
						if (displayContent.indexOf("none") >= 0) {
							displayStyleEnd = displayStyleStart + 12;
							if (displayContent.indexOf(";") > 0)
								displayStyleEnd = displayStyleEnd + 1;
						}
						displayStyleValue = elementStyle.substring(displayStyleStart, displayStyleEnd);
					} catch (StringIndexOutOfBoundsException se) {
						logger.error("An error occured while getting style content of element with id [" + id_element
								+ "]. Please, check that ALL the style elements into the SVG have the final [;] char. Ex: [display:none;]");
						throw se;
					}
					elementStyle = elementStyle.replace(displayStyleValue, ""); // clean old style
				}

				if (elementVisibility.equalsIgnoreCase("false")) {
					displayStyle = elementStyle + ";display:none;";
				} else {
					displayStyle = elementStyle;
				}
				// sets new visibility style for the element
				element.setAttribute("style", displayStyle);
			}
		} catch (Exception e) {
			logger.error("An error occured while managing show property for the element [" + id_element + "]");
			throw e;
		}
		logger.debug("OUT");
	}

	/**
	 * Adds the data.
	 *
	 * @param datastore the data store
	 *
	 * @param child     the child element
	 * @param record    the record with data
	 */
	private void addData(IDataStore dataStore, Element child, IRecord record) {
		logger.debug("IN");
		List fields = record.getFields();
		for (int j = 0; j < fields.size(); j++) {
			if (j == dataStore.getMetaData().getIdFieldIndex()) {
				continue;
			}
			IField field = (IField) fields.get(j);
			child.setAttribute("attrib:" + dataStore.getMetaData().getFieldAlias(j), "" + field.getValue());
		}
		child.setAttribute("attrib:nome", child.getAttribute("id"));
		logger.debug("OUT");
	}

	private String addLinkDrillId(List listDrillNav, IRecord record, IMetaData dataStoreMeta) {
		logger.debug("IN");
		String toReturn = null;
		// search if there is a drill id specified in the dataset
		IFieldMetaData fieldDrillIdMeta = null;
		if (listDrillNav.size() > 0) {
			fieldDrillIdMeta = (IFieldMetaData) listDrillNav.get(0);
		}
		if (fieldDrillIdMeta != null) {
			IField drillField = record.getFieldAt(dataStoreMeta.getFieldIndex(fieldDrillIdMeta.getName()));
			String drillIdValue = "" + drillField.getValue();
			toReturn = drillIdValue;
		}
		logger.debug("OUT");
		return toReturn;
	}

	/**
	 * Adds the cross data link.
	 *
	 * @param datastore the data store
	 *
	 * @param child     the child element
	 * @param record    the record with data
	 */
	private JSONArray addCrossData(List listCrossNav, IRecord record, IMetaData dataStoreMeta, IDataMartProvider datamartProvider, IMapProvider mapProvider)
			throws Exception {
		logger.debug("IN");
		JSONArray toReturn = new JSONArray();
		logger.debug("... The cross navigation is founded. Define the link url...");
		logger.debug("Number of links per feature is equals to [" + listCrossNav.size() + "]");
		if (listCrossNav.size() == 0) {
			return null;
		}

		try {
			// add hierarchy key information
			HierarchyMember hierMember = mapProvider.getSelectedHierarchyMember();
			JSONObject jsonData = new JSONObject();
			// jsonData.put("HIERARCHY", datamartProvider.getSelectedHierarchyName());
			jsonData.put("HIERARCHY", hierMember.getHierarchy());
			toReturn.put(jsonData);

			jsonData = new JSONObject();
			jsonData.put("LEVEL", hierMember.getLevel());
			toReturn.put(jsonData);

			jsonData = new JSONObject();
			jsonData.put("MEMBER", hierMember.getName());
			toReturn.put(jsonData);

			jsonData = new JSONObject();
			IFieldMetaData fieldMeta = (IFieldMetaData) listCrossNav.get(0);
			IField field = record.getFieldAt(dataStoreMeta.getFieldIndex(fieldMeta.getName()));
			jsonData.put("ELEMENT_ID", "" + field.getValue());
			toReturn.put(jsonData);

			// add record dataset information
			// @TODO: aggiungere solo i field che sono veramente usati nella cross definition
			IMetaData fieldsMeta = record.getDataStore().getMetaData();
			for (int f = 0; f < record.getFields().size(); f++) {
				IField recField = record.getFields().get(f);
				String recFieldName = fieldsMeta.getFieldMeta(f).getName();
				Object recFieldValue = recField.getValue();
				jsonData = new JSONObject();
				jsonData.put(recFieldName, "" + recFieldValue);
				toReturn.put(jsonData);
			}

		} catch (Exception e) {
			logger.error("An error occured while defining object with element values for external cross navigation. ");
			throw e;
		}
		logger.debug("OUT");
		return toReturn;
	}

	/**
	 * Adds the tooltip.
	 *
	 * @param listTooltip   the tooltip object
	 * @param record        the record with data
	 * @param dataStoreMeta the data store
	 */
	private String addTooltip(List listTooltip, IRecord record, IMetaData dataStoreMeta) {
		logger.debug("IN");
		String toReturn = null;
		IFieldMetaData fieldTooltipIdMeta = null;
		// List listTooltip = dataStoreMeta.findFieldMeta("ROLE", "TOOLTIP");
		if (listTooltip.size() > 0) {
			fieldTooltipIdMeta = (IFieldMetaData) listTooltip.get(0);
		}
		if (fieldTooltipIdMeta != null) {
			IField field = record.getFieldAt(dataStoreMeta.getFieldIndex(fieldTooltipIdMeta.getName()));
			toReturn = "" + field.getValue();
		}
		logger.debug("OUT");
		return toReturn;
	}

	/**
	 * Adds the info content.
	 *
	 * @param listInfo      the info object
	 * @param record        the record with data
	 * @param dataStoreMeta the data store
	 */
	private String addInfoText(List listInfo, IRecord record, IMetaData dataStoreMeta) {
		logger.debug("IN");
		String toReturn = null;
		IFieldMetaData fieldInfoIdMeta = null;
		if (listInfo.size() > 0) {
			fieldInfoIdMeta = (IFieldMetaData) listInfo.get(0);
		}
		if (fieldInfoIdMeta != null) {
			IField field = record.getFieldAt(dataStoreMeta.getFieldIndex(fieldInfoIdMeta.getName()));
			toReturn = "" + field.getValue();
		}
		logger.debug("OUT");
		return toReturn;

	}

	/**
	 * Adds the href elements and click events
	 *
	 * @param targetMap      the target svg
	 * @param featureElement the svg element to manage
	 * @param elementId      the svg element id
	 * @param drillId        the drill id
	 * @param linkUrl        the link url
	 * @param linkType       the link type ('cross' or 'drill')
	 *
	 */
	private void addHRefLinksCross(SVGDocument targetMap, Element featureElement, JSONArray JSONValues, JSONArray JSONCross,
			IDataMartProvider datamatProvider) {
		logger.debug("IN");
		Element linkCrossElement = targetMap.createElement("a");
		linkCrossElement.setAttribute("xlink:href", "javascript:void(0)");

		if (featureElement.hasAttribute("style")) {
			String elementStyle = featureElement.getAttribute("style");
			elementStyle = elementStyle + ";cursor:pointer";
			featureElement.setAttribute("style", elementStyle);
		} else {
			featureElement.setAttribute("style", "cursor:pointer");
		}

		featureElement.setAttribute("onclick", "javascript:clickedElementCrossNavigation('" + JSONValues.toString() + "', '" + JSONCross.toString() + "')");

		logger.debug("OUT");
	}

	private void addHRefLinksDrill(SVGDocument targetMap, Element featureElement, String elementId, String drillId, String linkUrl) {
		logger.debug("IN");
		Element linkCrossElement = targetMap.createElement("a");
		linkCrossElement.setAttribute("xlink:href", linkUrl);

		if (featureElement.hasAttribute("style")) {
			String elementStyle = featureElement.getAttribute("style");
			elementStyle = elementStyle + ";cursor:pointer";
			featureElement.setAttribute("style", elementStyle);
		} else {
			featureElement.setAttribute("style", "cursor:pointer");
		}

		// get document_id
		String documentId = (String) this.getEnv().get("DOCUMENT_ID");

		JSONObject jsonOEnv = new JSONObject();
		String strEnv = "";
		try {
			jsonOEnv = JSONUtils.getJsonFromMap(this.getEnv());
			jsonOEnv.remove("ENV_USER_PROFILE"); // clean profile info for correct url
			jsonOEnv.remove("level");
			jsonOEnv.remove("DOCUMENT_OUTPUT_PARAMETERS");
			strEnv = "&" + JSONUtils.getQueryString(jsonOEnv);
			// strEnv = SpagoBIUtilities.encode(strEnv.substring(0, strEnv.length() - 1)); //2017-29-30 commented because changes the parameter values
		} catch (JSONException je) {
			logger.error("An error occured while convert map [env] to json object: " + je);
		}
		// String strEnv = StringUtils.mapToString(this.getEnv());

		if (drillId == null) {
			// featureElement.setAttribute("onclick", "javascript:clickedElement('" + documentId + "','" + elementId + "')");
			featureElement.setAttribute("onclick", "javascript:clickedElement('" + strEnv + "','" + documentId + "','" + elementId + "')");
		} else {
			// featureElement.setAttribute("onclick", "javascript:clickedElement('" + documentId + "','" + elementId + "','" + drillId + "')");
			featureElement.setAttribute("onclick", "javascript:clickedElement('" + strEnv + "','" + documentId + "','" + elementId + "','" + drillId + "')");
		}

		logger.debug("OUT");
	}

	/**
	 * Adds the tooltip events
	 *
	 * @param featureElement the svg element to manage
	 * @param elementId      the svg element id
	 * @param tooltip        the tooltip value
	 *
	 */
	private void addTooltipEvents(Element featureElement, String tooltip, String elementId) {
		logger.debug("IN");

		if (tooltip == null) {
			featureElement.setAttribute("onmouseover", "javascript:showTooltipElement('" + elementId + "')");
		} else {
			featureElement.setAttribute("onmouseover", "javascript:showTooltipElement('" + elementId + "','" + tooltip + "')");
		}
		featureElement.setAttribute("onmousemove", "javascript:getMousePosition()");
		featureElement.setAttribute("onmouseout", "javascript:hideTooltipElement()");
		logger.debug("OUT");
	}

	/**
	 * Include scripts.
	 *
	 * @param doc the doc
	 */
	private void includeScripts(SVGDocument doc) {
		Element scriptInit = doc.getElementById("included_scripts");
		Node scriptText = scriptInit.getFirstChild();
		StringBuffer buffer = new StringBuffer();
		includeScript(buffer, "helper_functions.js");
		includeScript(buffer, "timer.js");
		includeScript(buffer, "mapApp.js");
		includeScript(buffer, "timer.js");
		includeScript(buffer, "slider.js");
		includeScript(buffer, "button.js");
		includeScript(buffer, "Window.js");
		includeScript(buffer, "checkbox_and_radiobutton.js");
		includeScript(buffer, "navigation.js");
		includeScript(buffer, "tabgroup.js");
		includeScript(buffer, "colourPicker.js");

		includeScript(buffer, "custom/Utils.js");
		includeScript(buffer, "custom/BarChart.js");
		includeScript(buffer, "custom/NavigationWindow.js");
		includeScript(buffer, "custom/LayersWindow.js");
		includeScript(buffer, "custom/ThematicWindow.js");
		includeScript(buffer, "custom/DetailsWindow.js");
		includeScript(buffer, "custom/LegendWindow.js");
		includeScript(buffer, "custom/ColourPickerWindow.js");
		includeScript(buffer, "custom/ThresholdsFactory.js");
		includeScript(buffer, "custom/ColourRangesFactory.js");

		scriptText.setNodeValue(buffer.toString());
	}

	/**
	 * Import scripts.
	 *
	 * @param doc the doc
	 */
	private void importScripts(SVGDocument doc) {
		importScipt(doc, "helper_functions.js");
		importScipt(doc, "timer.js");
		importScipt(doc, "mapApp.js");
		importScipt(doc, "timer.js");
		importScipt(doc, "slider.js");
		importScipt(doc, "button.js");
		importScipt(doc, "Window.js");
		importScipt(doc, "checkbox_and_radiobutton.js");
		importScipt(doc, "navigation.js");
		importScipt(doc, "tabgroup.js");
		importScipt(doc, "colourPicker.js");

		importScipt(doc, "custom/Utils.js");
		importScipt(doc, "custom/BarChart.js");
		importScipt(doc, "custom/NavigationWindow.js");
		importScipt(doc, "custom/LayersWindow.js");
		importScipt(doc, "custom/ThematicWindow.js");
		importScipt(doc, "custom/DetailsWindow.js");
		importScipt(doc, "custom/LegendWindow.js");
		importScipt(doc, "custom/ColourPickerWindow.js");
		importScipt(doc, "custom/ThresholdsFactory.js");
		importScipt(doc, "custom/ColourRangesFactory.js");

	}

	/**
	 * Include script.
	 *
	 * @param buffer     the buffer
	 * @param scriptName the script name
	 */
	private void includeScript(StringBuffer buffer, String scriptName) {
		try {
			URL scriptUrl = new URL((String) getEnv().get(SvgViewerEngineConstants.ENV_ABSOLUTE_CONTEXT_URL) + "/js/lib/svg-widgets/" + scriptName);
			// URL scriptUrl = new URL("http://localhost:8080/SpagoBIGeoEngine" + "/js/lib/svg-widgets/" + scriptName);

			BufferedReader reader = new BufferedReader(new InputStreamReader(scriptUrl.openStream()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				buffer.append(line + "\n");
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void importScipt(SVGDocument map, String scriptName) {
		Element script = map.createElement("script");
		script.setAttribute("type", "text/ecmascript");
		script.setAttribute("xlink:href", (String) getEnv().get(SvgViewerEngineConstants.ENV_CONTEXT_URL) + "/js/lib/svg-widgets/" + scriptName);
		Element importsBlock = map.getElementById("imported_scripts");
		importsBlock.appendChild(script);
		Node lf = map.createTextNode("\n");
		importsBlock.appendChild(lf);
	}

	public void setMainMapDimension(SVGDocument masterMap, SVGDocument targetMap) {
		String viewBox;
		String[] chunks;
		double width;
		double heigth;
		double mainMapHeight;
		Element mainMapBlock;

		logger.debug("IN");

		try {
			Assert.assertNotNull(masterMap, "Input parameter [masterMap] cannot be null");
			Assert.assertNotNull(targetMap, "Input parameter [targetMap] cannot be null");

			viewBox = null;
			try {
				viewBox = targetMap.getRootElement().getAttribute("viewBox");
			} catch (Throwable t) {
				MapRenderingException e = new MapRenderingException("Impossible to read attribute [viewBox] from target map's root node");
				e.addHint("add to the svg map root tag the attribute [viewbox] with the following value: ]"
						+ "0 0 W D] (where W and H are respectively your map width and height)");
				throw e;
			}
			if (StringUtilities.isEmpty(viewBox)) {
				MapRenderingException e = new MapRenderingException("Impossible to read attribute [viewBox] from target map's root node");
				e.addHint("add to the svg map root tag the attribute [viewbox] with the following value: ["
						+ "0 0 W D] (where W and H are respectively your map width and height)");
				throw e;
			}
			logger.debug("Target map vieBox is equal to [" + viewBox + "]");
			chunks = viewBox.trim().split(" ");
			Assert.assertTrue(chunks.length == 4, "Attribute [viewBox] of  target ma is malformed: expected format is [x y width height]");

			width = Double.parseDouble(chunks[2]);
			heigth = Double.parseDouble(chunks[3]);
			mainMapHeight = 1100 * (heigth / width);

			mainMapBlock = masterMap.getElementById("mainMap");
			mainMapBlock.setAttribute("viewBox", viewBox);
			masterMap.getRootElement().setAttribute("viewBox", 0 + " " + 0 + " 1100 " + mainMapHeight);
		} catch (Throwable t) {
			if (t instanceof SvgViewerEngineException)
				throw (SvgViewerEngineRuntimeException) t;
			throw new SvgViewerEngineRuntimeException("An unpredicted error occurred while setting up main map viewbox attribute");
		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * Sets the main map bkg rect dimension.
	 *
	 * @param masterMap the master map
	 * @param targetMap the target map
	 */
	public void setMainMapBkgRectDimension(SVGDocument masterMap, SVGDocument targetMap) {
		String viewBox = targetMap.getRootElement().getAttribute("viewBox");
		String[] chunks = viewBox.split(" ");
		String x = chunks[0];
		String y = chunks[1];
		String width = chunks[2];
		String height = chunks[3];
		Element mapBackgroundRect = masterMap.getElementById("mapBackgroundRect");
		mapBackgroundRect.setAttribute("x", x);
		mapBackgroundRect.setAttribute("y", y);
		mapBackgroundRect.setAttribute("width", width);
		mapBackgroundRect.setAttribute("height", height);
	}

	/**
	 * Gets the measures configuration script.
	 *
	 * @param datamart the datamart
	 *
	 * @return the measures configuration script
	 * @throws JSONException
	 */
	public JSONArray getMeasuresConfigurationScript(DataMart datamart) throws Exception {

		JSONArray measures;

		IDataStore dataStore;
		IMetaData dataStoreMeta;
		List measureFieldsMeta;
		String[] measureNames;

		measures = new JSONArray();

		dataStore = datamart.getDataStore();
		dataStoreMeta = dataStore.getMetaData();

		measureFieldsMeta = dataStoreMeta.findFieldMeta("ROLE", "MEASURE");
		measureNames = new String[measureFieldsMeta.size()];
		for (int i = 0; i < measureNames.length; i++) {
			IFieldMetaData filedMeta = (IFieldMetaData) measureFieldsMeta.get(i);
			measureNames[i] = filedMeta.getName();
		}

		for (int i = 0; i < measureNames.length; i++) {

			JSONObject measure = new JSONObject();
			Measure localMeasure = getMeasure(measureNames[i]);
			if (localMeasure == null) {
				logger.error("Configuration for kpi [" + measureNames[i] + "] doesn't found. Please, check the template!");
				throw new SvgViewerEngineRuntimeException("Configuration for kpi [" + measureNames[i] + "] doesn't found. Please, check the template!");
			}
			measure.put("name", measureNames[i]);
			measure.put("description", localMeasure.getDescription());
			measure.put("colour", localMeasure.getColour());

			JSONArray orderedValues = new JSONArray();
			dataStore.sortRecords(dataStoreMeta.getFieldIndex(measureNames[i]));
			List orderedKpiValuesSet = dataStore.getFieldValues(dataStoreMeta.getFieldIndex(measureNames[i]));
			Iterator it = orderedKpiValuesSet.iterator();
			while (it.hasNext()) {
				orderedValues.put(it.next());
			}
			measure.put("ordered_values", orderedValues);

			if (getMeasure(measureNames[i]).getTresholdLb() == null || getMeasure(measureNames[i]).getTresholdLb().trim().equalsIgnoreCase("")
					|| getMeasure(measureNames[i]).getTresholdLb().equalsIgnoreCase("none")) {
				measure.put("lower_bound", "none");
			} else {
				measure.put("lower_bound", getMeasure(measureNames[i]).getTresholdLb());
			}

			if (getMeasure(measureNames[i]).getTresholdUb() == null || getMeasure(measureNames[i]).getTresholdUb().trim().equalsIgnoreCase("")
					|| getMeasure(measureNames[i]).getTresholdUb().equalsIgnoreCase("none")) {
				measure.put("upper_bound", "none");
			} else {
				measure.put("upper_bound", getMeasure(measureNames[i]).getTresholdUb());
			}

			measure.put("lower_bound_colour", getMeasure(measureNames[i]).getColurOutboundCol());
			measure.put("upper_bound_colour", getMeasure(measureNames[i]).getColurOutboundCol());
			measure.put("no_value_color", getMeasure(measureNames[i]).getColurNullCol());

			JSONObject thresholdCalculatorConf = new JSONObject();
			thresholdCalculatorConf.put("type", getMeasure(measureNames[i]).getTresholdCalculatorType());
			JSONObject thresholdCalculatorParams = new JSONObject();
			if (getMeasure(measureNames[i]).getTresholdCalculatorType().equalsIgnoreCase("static")
					|| getMeasure(measureNames[i]).getTresholdCalculatorType().equalsIgnoreCase("perc")) {

				String[] values = getTresholdsArray(getMeasure(measureNames[i]).getColumnId());
				JSONArray ranges = new JSONArray();

				for (int j = 0; j < values.length; j++) {
					ranges.put(values[j]);
				}
				thresholdCalculatorParams.put("ranges", ranges);

				String[] labels = getTresholdsDescriptionArray(getMeasure(measureNames[i]).getColumnId());
				JSONArray descriptions = new JSONArray();

				if (labels != null) {
					for (int j = 0; j < labels.length; j++) {
						descriptions.put(labels[j]);
					}
					thresholdCalculatorParams.put("descriptions", descriptions);
				}
			} else {
				String value = getMeasure(measureNames[i]).getTresholdCalculatorParameters().getProperty("GROUPS_NUMBER");
				thresholdCalculatorParams.put("num_group", Integer.parseInt(value));
			}

			thresholdCalculatorConf.put("params", thresholdCalculatorParams);
			measure.put("threshold_calculator_conf", thresholdCalculatorConf);

			JSONObject colourCalculatorConf = new JSONObject();
			colourCalculatorConf.put("type", getMeasure(measureNames[i]).getColurCalculatorType());
			JSONObject colourCalculatorParams = new JSONObject();
			if (getMeasure(measureNames[i]).getColurCalculatorType().equalsIgnoreCase("gradient")
					|| getMeasure(measureNames[i]).getColurCalculatorType().equalsIgnoreCase("grad")) {

				String colour = getMeasure(measureNames[i]).getColurCalculatorParameters().getProperty("BASE_COLOR");
				colourCalculatorParams.put("colour", colour);
			} else {
				String[] values = getColoursArray(getMeasure(measureNames[i]).getColumnId());
				JSONArray ranges = new JSONArray();

				for (int j = 0; j < values.length; j++) {
					ranges.put(values[j]);
				}
				colourCalculatorParams.put("ranges", ranges);
			}

			String opacity = getMeasure(measureNames[i]).getColurCalculatorParameters().getProperty("opacity");
			if (opacity != null) {
				colourCalculatorParams.put("opacity", opacity);
			}

			colourCalculatorConf.put("params", colourCalculatorParams);
			measure.put("colourrange_calculator_conf", colourCalculatorConf);

			measures.put(measure);
		}

		return measures;
	}

	/**
	 * Gets the layers configuration script.
	 *
	 * @param doc         the doc
	 * @param targetLayer the target layer
	 *
	 * @return the layers configuration script
	 * @throws JSONException
	 */
	public JSONArray getLayersConfigurationScript(SVGDocument doc) throws JSONException {
		JSONArray layers;

		String[] layerNames;
		int targetLayerIndex = 0;
		boolean includeChartLayer = false;
		boolean includeValuesLayer = false;

		layers = new JSONArray();

		layerNames = getLayerNames();
		for (int i = 0; i < layerNames.length; i++) {
			if (doc.getElementById(layerNames[i]) != null || layerNames[i].equalsIgnoreCase("grafici") || layerNames[i].equalsIgnoreCase("valori")) {
				JSONObject layer = new JSONObject();
				layer.put("name", layerNames[i]);
				if (layerNames[i].equalsIgnoreCase("grafici")) {
					layer.put("description", "Grafici");
				} else if (layerNames[i].equalsIgnoreCase("valori")) {
					layer.put("description", "Valori");
				} else {
					layer.put("description", getLayer(layerNames[i]).getDescription());
				}

				layers.put(layer);
			}
		}

		return layers;
	}

	/**
	 * Gets the master map file.
	 *
	 * @param interactiveMasterMap the interactive master map
	 *
	 * @return the master map file
	 */
	private File getMasterMapFile(boolean interactiveMasterMap) {
		File file = null;
		if (interactiveMasterMap) {
			file = new File(ConfigSingleton.getRootPath() + "/maps/knowage_svgmaster.svg");
		} else {
			file = new File(ConfigSingleton.getRootPath() + "/maps/export_spagobigeo.svg");
		}
		return file;
	}

	/**
	 * Gets the temporary file.
	 *
	 * @return the temporary file
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public File getTempFile() throws IOException {
		String tempDirName = null;
		File tempDir = null;
		File result = null;
		SourceBean geoEngineConf = (SourceBean) ConfigSingleton.getInstance().getAttribute("GEO-ENGINE");
		if (geoEngineConf != null) {
			tempDirName = (String) geoEngineConf.getAttribute("tempDir");
		}

		if (tempDirName != null) {
			logger.debug("temp directory path configured: " + tempDirName);
			if (tempDirName.startsWith("./")) {
				logger.debug("temp directory path is relative to working directory: " + System.getProperty("user.dir"));
				tempDir = new File(System.getProperty("user.dir") + "/" + tempDirName);
				logger.debug("temp directory absolute path: " + tempDir);
			} else {
				tempDir = new File(tempDirName);
			}
			result = new File(tempDir, "KnowageSVGViewerEngine_" + System.currentTimeMillis() + "_tmpMap.svg");
			boolean isFileCreated = result.createNewFile();
			if (isFileCreated) {
				logger.debug("temp file successfully created: " + result);
			} else {
				logger.error("impossible to create a new temp file: " + result);
			}
		} else {
			logger.debug("temp directory path not configured");
			tempDirName = System.getProperty("java.io.tmpdir");
			logger.debug("System temp directory will be used: " + tempDirName);
			tempDir = new File(tempDirName);
			result = File.createTempFile("KnowageSVGViewerEngine_", "_tmpMap.svg", tempDir);
			logger.debug("temp file successfully created: " + result);
		}

		return result;
	}

	/**
	 * Gets the gUI configuration script.
	 *
	 * @return the gUI configuration script
	 * @throws JSONException
	 */
	public JSONObject getGUIConfigurationScript() throws JSONException {
		JSONObject guiSettings = new JSONObject();

		guiSettings = getGuiSettings().toJSON();
		guiSettings.put("defaultDrillNav", true);

		return guiSettings;
	}

}