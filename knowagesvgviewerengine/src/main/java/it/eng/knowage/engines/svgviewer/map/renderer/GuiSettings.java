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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GuiSettings {
	Map generalSettings;
	Map windowDefaultSettings;
	Map navigationWindowSettings;
	Map measureWindowSettings;
	Map layersWindowSettings;
	Map detailWindowSettings;
	Map legendWindowSettings;
	Map colourpickerWindowSettings;
	Map labelProducers;

	public GuiSettings() {
		super();
		generalSettings = new HashMap();
		windowDefaultSettings = new HashMap();
		navigationWindowSettings = new HashMap();
		measureWindowSettings = new HashMap();
		layersWindowSettings = new HashMap();
		detailWindowSettings = new HashMap();
		legendWindowSettings = new HashMap();
		colourpickerWindowSettings = new HashMap();
		labelProducers = new HashMap();
	}

	public Map getWindowDefaultSettings() {
		return windowDefaultSettings;
	}

	public void setWindowDefaultSettings(Map windowDefaultSettings) {
		this.windowDefaultSettings = windowDefaultSettings;
	}

	public Map getNavigationWindowSettings() {
		return navigationWindowSettings;
	}

	public void setNavigationWindowSettings(Map navigationWindowSettings) {
		this.navigationWindowSettings = navigationWindowSettings;
	}

	public Map getMeasureWindowSettings() {
		return measureWindowSettings;
	}

	public void setMeasureWindowSettings(Map measureWindowSettings) {
		this.measureWindowSettings = measureWindowSettings;
	}

	public Map getLayersWindowSettings() {
		return layersWindowSettings;
	}

	public void setLayersWindowSettings(Map layersWindowSettings) {
		this.layersWindowSettings = layersWindowSettings;
	}

	public Map getDetailWindowSettings() {
		return detailWindowSettings;
	}

	public void setDetailWindowSettings(Map detailWindowSettings) {
		this.detailWindowSettings = detailWindowSettings;
	}

	public Map getLegendWindowSettings() {
		return legendWindowSettings;
	}

	public void setLegendWindowSettings(Map legendWindowSettings) {
		this.legendWindowSettings = legendWindowSettings;
	}

	public Map getColourpickerWindowSettings() {
		return colourpickerWindowSettings;
	}

	public void setColourpickerWindowSettings(Map colourpickerWindowSettings) {
		this.colourpickerWindowSettings = colourpickerWindowSettings;
	}

	public JSONObject toJSON() throws JSONException {
		JSONObject guiSettings;
		JSONArray windowsSettings;
		JSONArray labelsSettings;
		JSONObject settings;

		guiSettings = toJSON(getGeneralSettings());

		settings = toJSON(getWindowDefaultSettings());
		guiSettings.put("windowDefaults", settings);

		windowsSettings = new JSONArray();

		settings = toJSON(getNavigationWindowSettings());
		windowsSettings.put(settings);

		settings = toJSON(getMeasureWindowSettings());
		windowsSettings.put(settings);

		settings = toJSON(getLayersWindowSettings());
		windowsSettings.put(settings);

		settings = toJSON(getDetailWindowSettings());
		windowsSettings.put(settings);

		settings = toJSON(getLegendWindowSettings());
		windowsSettings.put(settings);

		settings = toJSON(getColourpickerWindowSettings());
		windowsSettings.put(settings);

		guiSettings.put("windows", windowsSettings);

		labelsSettings = new JSONArray();
		ILabelProducer labelProducer;

		labelProducer = (ILabelProducer) getLabelProducers().get("header-left");
		if (labelProducer != null) {
			settings = toJSON(labelProducer.getSettings());
			settings.put("position", "header-left");
			settings.put("text", labelProducer.getLabel());
			labelsSettings.put(settings);
		}

		labelProducer = (ILabelProducer) getLabelProducers().get("header-center");
		if (labelProducer != null) {
			settings = toJSON(labelProducer.getSettings());
			settings.put("position", "header-center");
			settings.put("text", labelProducer.getLabel());
			labelsSettings.put(settings);
		}

		labelProducer = (ILabelProducer) getLabelProducers().get("header-right");
		if (labelProducer != null) {
			settings = toJSON(labelProducer.getSettings());
			settings.put("position", "header-right");
			settings.put("text", labelProducer.getLabel());
			labelsSettings.put(settings);
		}

		labelProducer = (ILabelProducer) getLabelProducers().get("footer-left");
		if (labelProducer != null) {
			settings = toJSON(labelProducer.getSettings());
			settings.put("position", "footer-left");
			settings.put("text", labelProducer.getLabel());
			labelsSettings.put(settings);
		}

		labelProducer = (ILabelProducer) getLabelProducers().get("footer-center");
		if (labelProducer != null) {
			settings = toJSON(labelProducer.getSettings());
			settings.put("position", "footer-center");
			settings.put("text", labelProducer.getLabel());
			labelsSettings.put(settings);
		}

		labelProducer = (ILabelProducer) getLabelProducers().get("footer-right");
		if (labelProducer != null) {
			settings = toJSON(labelProducer.getSettings());
			settings.put("position", "footer-right");
			settings.put("text", labelProducer.getLabel());
			labelsSettings.put(settings);
		}

		guiSettings.put("labels", labelsSettings);

		return guiSettings;
	}

	private JSONObject toJSON(Map settings) throws JSONException {
		JSONObject result;

		result = new JSONObject();

		Iterator it = settings.keySet().iterator();
		while (it.hasNext()) {
			String settingName = (String) it.next();
			Object settingValue = settings.get(settingName);
			if (settingValue instanceof String) {
				String str = (String) settingValue;
				if (str.startsWith("{") && str.endsWith("}")) {
					settingValue = new JSONObject(str);
				} else if (str.startsWith("[") && str.endsWith("]")) {
					settingValue = new JSONArray(str);
					;
				}
			}

			result.put(settingName, settingValue);
		}

		return result;
	}

	public Map getGeneralSettings() {
		return generalSettings;
	}

	public void setGeneralSettings(Map generalSettings) {
		this.generalSettings = generalSettings;
	}

	public Map getLabelProducers() {
		return labelProducers;
	}

	public void setLabelProducers(Map labelProducers) {
		this.labelProducers = labelProducers;
	}
}
