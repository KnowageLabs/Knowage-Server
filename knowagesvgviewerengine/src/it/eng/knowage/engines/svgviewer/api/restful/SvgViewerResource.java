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

import it.eng.knowage.engines.svgviewer.SvgViewerEngineConstants;
import it.eng.knowage.engines.svgviewer.api.AbstractSvgViewerEngineResource;
import it.eng.knowage.engines.svgviewer.map.renderer.Layer;
import it.eng.knowage.engines.svgviewer.map.renderer.Measure;
import it.eng.spago.base.SourceBean;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */

@Path("1.0/svgviewer")
@ManageAuthorization
public class SvgViewerResource extends AbstractSvgViewerEngineResource {

	@Path("/drawMap")
	@GET
	@Produces(SvgViewerEngineConstants.SVG_MIME_TYPE + "; charset=UTF-8")
	// public Response drawMap(@Context HttpServletRequest req) {
	public Response drawMap(@QueryParam("level") String level) {
		logger.debug("IN");
		try {
			// TODO: let the output format to be configurable with a parameter
			// Integer actualLevel = getLevel(level);

			File maptmpfile = getEngineInstance().renderMap("dsvg", level);
			byte[] data = Files.readAllBytes(maptmpfile.toPath());

			ResponseBuilder response = Response.ok(data);
			response.header("Content-Disposition", "inline; filename=map.svg");
			return response.build();

		} catch (Exception e) {
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException("", getEngineInstance(), e);
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
	@GET
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	// public Response getMeasures(@Context HttpServletRequest req) {
	public Response getMeasures(@QueryParam("level") String level) {
		logger.debug("IN");
		try {
			// SourceBean memberSB = getActiveMemberSB(req);
			SourceBean memberSB = getActiveMemberSB(level);
			SourceBean measuresConfigurationSB = (SourceBean) memberSB.getAttribute("MEASURES");

			Map measures = getMeasures(measuresConfigurationSB);
			ResponseBuilder response = Response.ok(measures);

			return response.build();

		} catch (Exception e) {
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
	@GET
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	// public Response getLayers(@Context HttpServletRequest req) {
	public Response getLayers(@QueryParam("level") String level) {
		logger.debug("IN");
		try {
			// SourceBean memberSB = getActiveMemberSB(req);
			SourceBean memberSB = getActiveMemberSB(level);
			SourceBean measuresConfigurationSB = (SourceBean) memberSB.getAttribute("LAYERS");
			// SourceBean measuresConfigurationSB = (SourceBean) confSB.getAttribute("LAYERS");

			Map measures = getLayers(measuresConfigurationSB);
			ResponseBuilder response = Response.ok(measures);

			return response.build();

		} catch (Exception e) {
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException("", getEngineInstance(), e);
		} finally {
			logger.debug("OUT");
		}
	}

	/** Utility methods ******************************************************************************************************/
	private static Map getLayers(SourceBean layersConfigurationSB) {
		Map layers;
		List layerList;
		Layer layer;
		Properties attributes;
		String attributeValue;

		layers = new HashMap();

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

		measures = new HashMap();

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

		SourceBean templateSB = getEngineInstance().getTemplate();
		SourceBean confSB = (SourceBean) templateSB.getAttribute(SvgViewerEngineConstants.DATAMART_PROVIDER_TAG);
		SourceBean hierarchySB = (SourceBean) confSB.getAttribute("HIERARCHY");
		List members = hierarchySB.getAttributeAsList(SvgViewerEngineConstants.MEMBER_TAG);

		for (int i = 1; i <= members.size(); i++) {
			if (i == actualLevel + 1) {
				logger.debug("Parsing member  [" + i + "]");
				toReturn = (SourceBean) members.get(i - 1);
				break;
			}
		}
		// @TODO gestire caso in cui non esiste il membro con il livello cercato
		return toReturn;
	}

	private Integer getLevel(String actualLevelStr) {

		// String actualLevelStr = (String) req.getAttribute("level");
		Integer actualLevel = 0; // default is the first level
		if (actualLevelStr != null)
			actualLevel = Integer.valueOf(actualLevelStr);

		return actualLevel;
	}
}
