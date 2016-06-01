package it.eng.knowage.engines.svgviewer.map.renderer.configurator;

import it.eng.knowage.engines.svgviewer.SvgViewerEngineException;
import it.eng.knowage.engines.svgviewer.map.renderer.AbstractMapRenderer;
import it.eng.knowage.engines.svgviewer.map.renderer.GuiSettings;
import it.eng.knowage.engines.svgviewer.map.renderer.ILabelProducer;
import it.eng.knowage.engines.svgviewer.map.renderer.Layer;
import it.eng.knowage.engines.svgviewer.map.renderer.Measure;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spagobi.commons.utilities.StringUtilities;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

// TODO: Auto-generated Javadoc
/**
 * The Class AbstractMapRendererConfigurator.
 *
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class AbstractMapRendererConfigurator {

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(AbstractMapRendererConfigurator.class);

	/**
	 * Configure.
	 *
	 * @param abstractMapRenderer
	 *            the abstract map renderer
	 * @param conf
	 *            the conf
	 *
	 * @throws SvgViewerEngineException
	 *             the geo engine exception
	 */
	public static void configure(AbstractMapRenderer abstractMapRenderer, Object conf) throws SvgViewerEngineException {
		SourceBean confSB = null;

		if (conf instanceof String) {
			try {
				confSB = SourceBean.fromXMLString((String) conf);
			} catch (SourceBeanException e) {
				logger.error("Impossible to parse configuration block for MapRenderer", e);
				throw new SvgViewerEngineException("Impossible to parse configuration block for MapRenderer", e);
			}
		} else {
			confSB = (SourceBean) conf;
		}

		if (confSB != null) {
			SourceBean measuresConfigurationSB = (SourceBean) confSB.getAttribute("MEASURES");
			String defaultMeasureName = (String) measuresConfigurationSB.getAttribute("default_kpi");
			if (!StringUtilities.isEmpty(defaultMeasureName)) {
				abstractMapRenderer.setSelectedMeasureName(defaultMeasureName);
			}
			Map measures = getMeasures(measuresConfigurationSB);

			SourceBean layersConfigurationSB = (SourceBean) confSB.getAttribute("LAYERS");
			Map layers = getLayers(layersConfigurationSB);

			SourceBean guiSettingsConfigurationSB = (SourceBean) confSB.getAttribute("GUI_SETTINGS");
			GuiSettings guiSettings = getGuiSettings(guiSettingsConfigurationSB);

			abstractMapRenderer.setMeasures(measures);
			abstractMapRenderer.setLayers(layers);
			abstractMapRenderer.setGuiSettings(guiSettings);
		}
	}

	/**
	 * Gets the measures.
	 *
	 * @param measuresConfigurationSB
	 *            the measures configuration sb
	 *
	 * @return the measures
	 */
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

		measureList = measuresConfigurationSB.getAttributeAsList("KPI");
		for (int i = 0; i < measureList.size(); i++) {

			measureSB = (SourceBean) measureList.get(i);
			measure = new Measure();

			attributeValue = (String) measureSB.getAttribute("column_id");
			measure.setColumnId(attributeValue);
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

	/**
	 * Gets the layers.
	 *
	 * @param layersConfigurationSB
	 *            the layers configuration sb
	 *
	 * @return the layers
	 */
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

	private static GuiSettings getGuiSettings(SourceBean guiSettingsConfigurationSB) {
		GuiSettings guiSettings = null;
		SourceBean windowSettingsSB;
		SourceBean labelSettingsSB;

		SourceBean settingsSB;
		List params;

		guiSettings = new GuiSettings();

		if (guiSettingsConfigurationSB == null)
			return guiSettings;

		params = guiSettingsConfigurationSB.getAttributeAsList("PARAM");
		if (params != null) {
			addSettings(guiSettings.getGeneralSettings(), params);
		}

		windowSettingsSB = (SourceBean) guiSettingsConfigurationSB.getAttribute("WINDOWS");

		if (windowSettingsSB != null) {
			settingsSB = (SourceBean) windowSettingsSB.getAttribute("DEFAULTS");
			if (settingsSB != null) {
				params = settingsSB.getAttributeAsList("PARAM");
				if (params != null) {
					addSettings(guiSettings.getWindowDefaultSettings(), params);
				}
			}

			settingsSB = (SourceBean) windowSettingsSB.getFilteredSourceBeanAttribute("WINDOW", "name", "navigation");
			if (settingsSB != null) {
				guiSettings.getNavigationWindowSettings().put("name", "navigation");
				params = settingsSB.getAttributeAsList("PARAM");
				if (params != null) {
					addSettings(guiSettings.getNavigationWindowSettings(), params);
				}
			}

			settingsSB = (SourceBean) windowSettingsSB.getFilteredSourceBeanAttribute("WINDOW", "name", "measures");
			if (settingsSB != null) {
				guiSettings.getMeasureWindowSettings().put("name", "measures");
				params = settingsSB.getAttributeAsList("PARAM");
				if (params != null) {
					addSettings(guiSettings.getMeasureWindowSettings(), params);
				}
			}

			settingsSB = (SourceBean) windowSettingsSB.getFilteredSourceBeanAttribute("WINDOW", "name", "layers");
			if (settingsSB != null) {
				guiSettings.getLayersWindowSettings().put("name", "layers");
				params = settingsSB.getAttributeAsList("PARAM");
				if (params != null) {
					addSettings(guiSettings.getLayersWindowSettings(), params);
				}
			}

			settingsSB = (SourceBean) windowSettingsSB.getFilteredSourceBeanAttribute("WINDOW", "name", "detail");
			if (settingsSB != null) {
				guiSettings.getDetailWindowSettings().put("name", "detail");
				params = settingsSB.getAttributeAsList("PARAM");
				if (params != null) {
					addSettings(guiSettings.getDetailWindowSettings(), params);
				}
			}

			settingsSB = (SourceBean) windowSettingsSB.getFilteredSourceBeanAttribute("WINDOW", "name", "legend");
			if (settingsSB != null) {
				guiSettings.getLegendWindowSettings().put("name", "legend");
				params = settingsSB.getAttributeAsList("PARAM");
				if (params != null) {
					addSettings(guiSettings.getLegendWindowSettings(), params);
				}
			}

			settingsSB = (SourceBean) windowSettingsSB.getFilteredSourceBeanAttribute("WINDOW", "name", "colourpicker");
			if (settingsSB != null) {
				params = settingsSB.getAttributeAsList("PARAM");
				guiSettings.getColourpickerWindowSettings().put("name", "colourpicker");
				if (params != null) {
					addSettings(guiSettings.getColourpickerWindowSettings(), params);
				}
			}
		}

		labelSettingsSB = (SourceBean) guiSettingsConfigurationSB.getAttribute("LABELS");
		if (labelSettingsSB != null) {
			Map labelProducers = getLabelProducers(labelSettingsSB);
			guiSettings.setLabelProducers(labelProducers);
		}

		return guiSettings;
	}

	/**
	 * Gets the label producers.
	 *
	 * @param labelsConfigurationSB
	 *            the labels configuration sb
	 *
	 * @return the label producers
	 */
	public static Map getLabelProducers(SourceBean labelsConfigurationSB) {
		Map labelProducers = new HashMap();
		List labelList = labelsConfigurationSB.getAttributeAsList("LABEL");
		Iterator labelIterator = labelList.iterator();
		while (labelIterator.hasNext()) {
			SourceBean label = (SourceBean) labelIterator.next();
			String position = (String) label.getAttribute("position");
			String clazz = (String) label.getAttribute("class_name");

			ILabelProducer labelProducer = null;
			try {
				labelProducer = (ILabelProducer) Class.forName(clazz).newInstance();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (labelProducer != null) {
				labelProducer.init(label);
				labelProducers.put(position, labelProducer);
			}
		}

		return labelProducers;
	}

	public static void addSettings(Map settingsMap, List params) {

		Iterator it = params.iterator();
		while (it.hasNext()) {
			SourceBean param = (SourceBean) it.next();
			String paramName = (String) param.getAttribute("name");
			String paramValue = param.getCharacters().trim();
			if (!StringUtilities.isEmpty(paramName) && !StringUtilities.isEmpty(paramValue)) {
				settingsMap.put(paramName, downCastValue(paramValue));
			}
		}

	}

	private static Object downCastValue(String paramValue) {
		Object value = null;

		if (paramValue.equalsIgnoreCase("true") || paramValue.equalsIgnoreCase("false")) {
			value = new Boolean(paramValue);
		} else {
			try {
				value = new Double(paramValue);
			} catch (Exception e) {
				value = paramValue;
			}
		}

		/*
		 * if(paramValue.startsWith("'") && paramValue.endsWith("'")) { value = paramValue; } else if(paramValue.startsWith("\"") && paramValue.endsWith("\""))
		 * { value = paramValue; } else if(paramValue.startsWith("{") && paramValue.endsWith("}")) { value = paramValue; } else if(paramValue.startsWith("[") &&
		 * paramValue.endsWith("]")) { value = paramValue; } else if(paramValue.startsWith("function") && paramValue.endsWith("}")) { value = paramValue; } else
		 * {
		 * 
		 * }
		 */

		return value;
	}

}
