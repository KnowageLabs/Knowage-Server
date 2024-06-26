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
package it.eng.knowage.engines.svgviewer.api.restful;

import it.eng.knowage.engines.svgviewer.SvgViewerEngine;
import it.eng.knowage.engines.svgviewer.SvgViewerEngineConstants;
import it.eng.knowage.engines.svgviewer.SvgViewerEngineInstance;
import it.eng.knowage.engines.svgviewer.api.AbstractSvgViewerEngineResource;
import it.eng.knowage.engines.svgviewer.datamart.provider.DataMartProvider;
import it.eng.knowage.engines.svgviewer.datamart.provider.configurator.DataMartProviderConfigurator;
import it.eng.knowage.engines.svgviewer.dataset.DataSetMetaData;
import it.eng.knowage.engines.svgviewer.dataset.HierarchyMember;
import it.eng.knowage.engines.svgviewer.interceptor.RestExceptionMapper;
import it.eng.knowage.engines.svgviewer.map.provider.SOMapProvider;
import it.eng.knowage.engines.svgviewer.map.provider.configurator.SOMapProviderConfigurator;
import it.eng.knowage.engines.svgviewer.map.renderer.InteractiveMapRenderer;
import it.eng.knowage.engines.svgviewer.map.renderer.Layer;
import it.eng.knowage.engines.svgviewer.map.renderer.Measure;
import it.eng.knowage.engines.svgviewer.map.renderer.configurator.InteractiveMapRendererConfigurator;
import it.eng.spago.base.SourceBean;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datawriter.JSONDataWriter;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

import java.io.File;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */

@Path("1.0/svgviewer")
@ManageAuthorization
public class SvgViewerResource extends AbstractSvgViewerEngineResource {

	@Path("/drawMap")
	@POST
	@Produces({ MediaType.APPLICATION_SVG_XML, MediaType.APPLICATION_JSON })
	public Response drawMap(@FormParam("level") String level) {
		logger.debug("IN");
		try {
			// SourceBean savedTemplate = getTemplateAsSourceBean();
			SourceBean savedTemplate = getTemplate();
			Map env = getEngineEnv();
			SvgViewerEngineInstance engineInstance = SvgViewerEngine.createInstance(savedTemplate, env);
			DataMartProvider dataMartProvider = (DataMartProvider) engineInstance.getDataMartProvider();
			dataMartProvider.setSelectedLevel(level);
			File maptmpfile = engineInstance.renderMap("dsvg");
			byte[] data = Files.readAllBytes(maptmpfile.toPath());

			ResponseBuilder response = Response.ok(data);
			response.header("Content-Type", SvgViewerEngineConstants.SVG_MIME_TYPE + "; charset=UTF-8");
			response.header("Content-Disposition", "inline; filename=map.svg");
			return response.build();

		} catch (Exception e) {
			logger.error("Error while draw svg", e);
			// return a json with the exception
			RestExceptionMapper re = new RestExceptionMapper();
			Response response = re.toResponse((RuntimeException) e);
			return response;
		} finally {
			logger.debug("OUT");
		}

	}

	/**
	 * Retrieve the measures from the document template
	 *
	 * @param req
	 * @return
	 */
	@Path("/getMeasures")
	@POST
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getMeasures(@FormParam("level") String level) {
		logger.debug("IN");
		try {
			SourceBean memberSB = getActiveMemberSB(level);
			SourceBean measuresConfigurationSB = (SourceBean) memberSB.getAttribute("MEASURES");

			Map measures = getMeasures(measuresConfigurationSB);
			ResponseBuilder response = Response.ok(measures);

			return response.build();

		} catch (Exception e) {
			logger.error("Error while getting measures", e);
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException("", getEngineInstance(), e);
		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * Retrieve the layers from the document template
	 *
	 * @param req
	 * @return
	 */
	@Path("/getLayers")
	@POST
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getLayers(@FormParam("level") String level) {
		logger.debug("IN");
		try {
			SourceBean memberSB = getActiveMemberSB(level);
			SourceBean measuresConfigurationSB = (SourceBean) memberSB.getAttribute("LAYERS");

			Map measures = getLayers(measuresConfigurationSB);
			ResponseBuilder response = Response.ok(measures);

			return response.build();

		} catch (Exception e) {
			logger.error("Error while getting layers", e);
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException("", getEngineInstance(), e);
		} finally {
			logger.debug("OUT");
		}
	}

	@Path("/drillMap")
	@POST
	@Produces(SvgViewerEngineConstants.SVG_MIME_TYPE + "; charset=UTF-8")
	public Response drillMap(@FormParam("document") String documentId, @FormParam("level") String level, @FormParam("member") String member,
			@FormParam("parent") String parent) {
		logger.debug("IN");
		try {
			// 0. Define internal objects
			// SourceBean savedTemplate = getTemplateAsSourceBean();
			SourceBean savedTemplate = getTemplate();
			Map env = getEngineEnv();
			SvgViewerEngineInstance engineInstance = SvgViewerEngine.createInstance(savedTemplate, env);

			DataMartProvider dataMartProvider = (DataMartProvider) engineInstance.getDataMartProvider();
			SOMapProvider mapProvider = (SOMapProvider) engineInstance.getMapProvider();
			InteractiveMapRenderer mapRenderer = (InteractiveMapRenderer) engineInstance.getMapRenderer();

			// 1. load the new configuration by the template
			SourceBean memberSB = getActiveMemberSB(level);
			if (memberSB == null) {
				logger.error("Template dosen't contains configuration about level [" + level + "].");
				throw new SpagoBIServiceException("DrillMap", "Template dosen't contains configuration about level [" + level + "].");
			}

			// 2. updated key informations
			if (level.equals("1") && member == null) {
				// name = mapProvider.getDefaultMapName();
				member = getProperty("name", memberSB, this.getEnv());
			} else if (member == null) {
				logger.error("Name map of level [" + level + "] not found in request.");
				throw new SpagoBIServiceException("DrillMap", "Name map of level [" + level + "] not found in request.");
			}
			String hierarchy = dataMartProvider.getSelectedHierarchyName();

			// 3. update internal objects (datamartProvider and mapProvider)
			dataMartProvider.setSelectedMemberInfo(null);
			dataMartProvider.setSelectedParentName(parent);
			dataMartProvider.setSelectedMemberName(getProperty("name", memberSB, this.getEnv()));
			dataMartProvider.setSelectedLevel(level);

			DataMartProviderConfigurator.configure(dataMartProvider, memberSB.toString());

			HierarchyMember hierMember = dataMartProvider.getHierarchyMember(dataMartProvider.getSelectedMemberName());
			hierMember.setHierarchy(hierarchy);
			hierMember.setName(member);
			hierMember.setLevel(Integer.valueOf(level));
			SOMapProviderConfigurator.configure(mapProvider, hierMember);
			mapProvider.setSelectedMapName(mapProvider.getSelectedMapName());

			InteractiveMapRendererConfigurator.configure(mapRenderer, memberSB.toString());

			// 4. return the new SVG
			File maptmpfile = engineInstance.renderMap("dsvg");
			byte[] data = Files.readAllBytes(maptmpfile.toPath());

			ResponseBuilder response = Response.ok(data);
			// response.header("Content-Type", SvgViewerEngineConstants.SVG_MIME_TYPE + "; charset=UTF-8");
			response.header("Content-Disposition", "inline; filename=map.svg");
			return response.build();

		} catch (Exception e) {
			// throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException("", getEngineInstance(), e);
			// return a json with the exception
			logger.error("Error while drilling svg ", e);
			RestExceptionMapper re = new RestExceptionMapper();
			Response response = re.toResponse((RuntimeException) e);
			return response;
		} finally {
			logger.debug("OUT");
		}

	}

	/**
	 * Retrieve the customized configuration from the document template
	 *
	 * @param req
	 * @return
	 */
	@Path("/getCustomizedConfiguration")
	@POST
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getCustomizedConfiguration(@FormParam("level") String level) {
		logger.debug("IN");
		try {
			// SourceBean savedTemplate = getTemplateAsSourceBean();
			SourceBean savedTemplate = getTemplate();
			Map env = getEngineEnv();
			SvgViewerEngineInstance engineInstance = SvgViewerEngine.createInstance(savedTemplate, env);

			DataMartProvider dataMartProvider = (DataMartProvider) engineInstance.getDataMartProvider();

			// force level 1 if the parameter isn't valorized
			if (level == null)
				level = "1";

			HierarchyMember hierMember = dataMartProvider.getHierarchyMember(dataMartProvider.getSelectedMemberName());
			if (!hierMember.getIsCustomized()) {
				logger.debug("The member is configurated to use customizedSVG but confogiration tag [CUSTOMIZE_SETTINGS] isn't found into the template, please check it!");
				return null;
			}
			JSONObject customizedConfigurationJSON = hierMember.getCustomizationSettings();

			SourceBean memberSB = getActiveMemberSB(level);
			if (memberSB == null) {
				logger.error("Template dosen't contains configuration about level [" + level + "].");
				throw new SpagoBIServiceException("getCustomizedConfiguration", "Template dosen't contains configuration about level [" + level + "].");
			}

			IDataSet dataset = dataMartProvider.getDs();
			if (dataset != null) {
				JSONDataWriter writer = new JSONDataWriter();
				IDataStore dataStore = dataMartProvider.getDataMart().getDataStore();
				if (dataStore == null) {
					logger.error("DataStore getted from dataset is null!");
					throw new SpagoBIServiceException("getCustomizedConfiguration", "DataStore getted from dataset is null.");
				}
				JSONObject datasetJSON = (JSONObject) writer.write(dataStore);
				// add logicType to the fields

				JSONObject metaDataJSON = (JSONObject) datasetJSON.get("metaData");
				JSONArray fieldsJSON = metaDataJSON.getJSONArray("fields");
				for (int i = 0; i < fieldsJSON.length(); i++) {
					Object field = fieldsJSON.get(i);
					if (field instanceof JSONObject) {
						JSONObject fieldJSON = (JSONObject) fieldsJSON.get(i);
						String header = fieldJSON.getString("header");
						DataSetMetaData meauresMap = hierMember.getDsMetaData();
						String logicalType = getLogicalType(meauresMap, header);
						fieldJSON.put("logicalType", logicalType);
					} else
						continue;
				}

				customizedConfigurationJSON.put("data", datasetJSON);

				// // add measures metadata informations
				// DataSetMetaData meauresMap = hierMember.getDsMetaData();
				// Map columns = meauresMap.getColumns();
				// JSONArray meauresJSON = new JSONArray();
				// Iterator iter = columns.keySet().iterator();
				// while (iter.hasNext()) {
				// String key = (String) iter.next();
				// Properties values = (Properties) columns.get(key);
				// // get elementId reference
				// String type = values.getProperty("type");
				// if (type.equalsIgnoreCase("geoid")) {
				// elementID = key;
				// }
				// // if (key != null && values != null && !meauresJSON.has(key))
				// meauresJSON.put(values);
				// }
				// customizedConfigurationJSON.put("COLUMNS", meauresJSON);

				// // add cross url definition for elementID
				// JSONArray crossUrlJSON = new JSONArray();
				// JSONArray rows = (JSONArray) datasetJSON.get("rows");
				// for (int i = 0; i < rows.length(); i++) {
				// JSONObject crossJSON = new JSONObject();
				// JSONObject row = rows.getJSONObject(i);
				// String url = "javascript:window.parent.clickedElementCrossNavigation('";
				// JSONArray urlValues = new JSONArray();
				// JSONArray rowNames = row.names();
				// for (int r = 0; r < rowNames.length(); r++) {
				// String name = (String) rowNames.get(r);
				// // get real field name
				// JSONArray fields = (JSONArray) ((JSONObject) datasetJSON.get("metaData")).get("fields");
				// String fieldName = getFieldName(fields, name);
				// String fieldValue = String.valueOf(row.get(name));
				//
				// if (fieldName.equals(""))
				// continue;
				// // add the ELEMENT_ID as specific information
				// if (fieldName.equalsIgnoreCase(elementID)) {
				// crossJSON.put("elementId", fieldValue);
				// JSONObject newValue = new JSONObject();
				// newValue.put("ELEMENT_ID", fieldValue);
				// urlValues.put(newValue);
				// }
				// JSONObject newValue = new JSONObject();
				// newValue.put(fieldName, fieldValue);
				// urlValues.put(newValue);
				// }
				// url += urlValues.toString() + "') ";
				// crossJSON.put("url", url);
				// crossUrlJSON.put(crossJSON);
				// }
				// customizedConfigurationJSON.put("crossUrl", crossUrlJSON);
			} else {
				logger.debug("Dataset is null, no data values returned.");
			}

			ResponseBuilder response = Response.ok(customizedConfigurationJSON.toString());
			return response.build();

		} catch (Exception e) {
			logger.error("Error while getting customization response", e);
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException("", getEngineInstance(), e);
		} finally {
			logger.debug("OUT");
		}
	}

	/** Utility methods ******************************************************************************************************/

	private String getLogicalType(DataSetMetaData measuresMap, String header) {
		String toReturn = "";
		try {
			Map columns = measuresMap.getColumns();
			JSONArray meauresJSON = new JSONArray();
			Iterator iter = columns.keySet().iterator();
			while (iter.hasNext()) {
				String key = (String) iter.next();
				Properties values = (Properties) columns.get(key);
				String columnId = values.getProperty("column_id");
				if (columnId.equalsIgnoreCase(header)) {
					toReturn = values.getProperty("type");
					break;
				}
			}
		} catch (Exception ex) {
			logger.error("An error occured while loading field name ");
		}
		return toReturn;
	}

	private String getFieldName(JSONArray fields, String metaName) {
		String toReturn = "";
		try {
			for (int f = 0; f < fields.length(); f++) {
				Object objField = fields.get(f);
				if (objField instanceof JSONObject) {
					JSONObject field = (JSONObject) fields.get(f);
					if (((String) field.get("name")).equalsIgnoreCase(metaName)) {
						toReturn = field.getString("header");
						break;
					}
				}
			}
		} catch (Exception ex) {
			logger.error("An error occured while loading field name ");
		}
		return toReturn;
	}

	private static Map getLayers(SourceBean layersConfigurationSB) {
		Map layers;
		List layerList;
		Layer layer;
		String attributeValue;

		layers = new LinkedHashMap();

		layerList = layersConfigurationSB.getAttributeAsList("LAYER");

		for (int i = 0; i < layerList.size(); i++) {
			SourceBean layerSB = (SourceBean) layerList.get(i);

			layer = new Layer();

			attributeValue = (String) layerSB.getAttribute("name");
			layer.setName(attributeValue);
			attributeValue = (String) layerSB.getAttribute("description");
			layer.setDescription(attributeValue);
			attributeValue = (String) layerSB.getAttribute("selected");
			if (attributeValue != null) {
				layer.setSelected(attributeValue.equalsIgnoreCase("true"));
			} else {
				layer.setSelected(false);
			}

			attributeValue = (String) layerSB.getAttribute("default_fill_color");
			layer.setDefaultFillColor(attributeValue);

			layers.put(layer.getName(), layer);
		}

		return layers;
	}

	private static Map getMeasures(SourceBean measuresConfigurationSB) {
		Map measures;
		List measureList;
		SourceBean measureSB;
		SourceBean tresholdsSB;
		SourceBean coloursSB;
		List paramList;
		SourceBean paramSB;
		Measure measure;
		String attributeValue;

		measures = new LinkedHashMap();

		String defaultMeasure = (String) measuresConfigurationSB.getAttribute("default_kpi");

		measureList = measuresConfigurationSB.getAttributeAsList("KPI");
		for (int i = 0; i < measureList.size(); i++) {

			measureSB = (SourceBean) measureList.get(i);
			measure = new Measure();

			attributeValue = (String) measureSB.getAttribute("column_id");
			measure.setColumnId(attributeValue);
			if (defaultMeasure.equalsIgnoreCase(attributeValue)) {
				measure.setSelected(true);
			}

			attributeValue = (String) measureSB.getAttribute("description");
			measure.setDescription(attributeValue);
			attributeValue = (String) measureSB.getAttribute("agg_func");
			if (attributeValue == null)
				attributeValue = "sum";
			measure.setAggFunc(attributeValue);
			attributeValue = (String) measureSB.getAttribute("colour");
			measure.setColour(attributeValue);
			attributeValue = (String) measureSB.getAttribute("pattern");
			measure.setPattern(attributeValue);
			attributeValue = (String) measureSB.getAttribute("unit");
			measure.setUnit(attributeValue);

			tresholdsSB = (SourceBean) measureSB.getAttribute("TRESHOLDS");
			attributeValue = (String) tresholdsSB.getAttribute("lb_value");
			measure.setTresholdLb(attributeValue);
			attributeValue = (String) tresholdsSB.getAttribute("ub_value");
			measure.setTresholdUb(attributeValue);
			attributeValue = (String) tresholdsSB.getAttribute("type");
			measure.setTresholdCalculatorType(attributeValue);

			paramList = tresholdsSB.getAttributeAsList("PARAM");
			Properties tresholdCalculatorParameters = new Properties();
			for (int j = 0; j < paramList.size(); j++) {
				paramSB = (SourceBean) paramList.get(j);
				String pName = (String) paramSB.getAttribute("name");
				String pValue = (String) paramSB.getAttribute("value");
				tresholdCalculatorParameters.setProperty(pName, pValue);
			}
			measure.setTresholdCalculatorParameters(tresholdCalculatorParameters);

			coloursSB = (SourceBean) measureSB.getAttribute("COLOURS");
			attributeValue = (String) coloursSB.getAttribute("null_values_color");
			measure.setColurNullCol(attributeValue);
			attributeValue = (String) coloursSB.getAttribute("outbound_colour");
			measure.setColurOutboundCol(attributeValue);
			attributeValue = (String) coloursSB.getAttribute("type");
			measure.setColurCalculatorType(attributeValue);

			paramList = coloursSB.getAttributeAsList("PARAM");
			Properties colurCalculatorParameters = new Properties();
			for (int j = 0; j < paramList.size(); j++) {
				paramSB = (SourceBean) paramList.get(j);
				String pName = (String) paramSB.getAttribute("name");
				String pValue = (String) paramSB.getAttribute("value");
				colurCalculatorParameters.setProperty(pName, pValue);
			}
			measure.setColurCalculatorParameters(colurCalculatorParameters);

			measures.put(measure.getColumnId().toUpperCase(), measure);
		}

		return measures;
	}

	private SourceBean getActiveMemberSB(String level) {
		SourceBean toReturn = null;

		Integer actualLevel = getLevel(level);

		// SourceBean templateSB = getTemplateAsSourceBean();
		SourceBean templateSB = getTemplate();
		SourceBean confSB = (SourceBean) templateSB.getAttribute(SvgViewerEngineConstants.DATAMART_PROVIDER_TAG);
		SourceBean hierarchySB = (SourceBean) confSB.getAttribute("HIERARCHY");
		List members = hierarchySB.getAttributeAsList(SvgViewerEngineConstants.MEMBER_TAG);

		for (int i = 1; i <= members.size(); i++) {
			if (i == actualLevel) {
				logger.debug("Parsing member  [" + i + "]");
				toReturn = (SourceBean) members.get(i - 1);
				break;
			}
		}
		return toReturn;
	}

	private Integer getLevel(String actualLevelStr) {

		Integer actualLevel = 1; // default is the first level
		if (actualLevelStr != null)
			actualLevel = Integer.valueOf(actualLevelStr);

		return actualLevel;
	}

	private static String getProperty(String key, SourceBean memberSB, Map env) {
		String toReturn = null;
		toReturn = (String) memberSB.getAttribute(key);
		// replace the member name with analytical driver value if it's required
		if (toReturn != null && toReturn.indexOf("$P{") >= 0) {
			int startPos = toReturn.indexOf("$P{") + 3;
			int endPos = toReturn.indexOf("}", startPos);
			String placeholder = toReturn.substring(startPos, endPos);
			toReturn = (String) env.get(placeholder);
			logger.debug("Member name value getted from analytical driver [" + placeholder + "] is [" + toReturn + "]");
		}
		return toReturn;
	}

}
