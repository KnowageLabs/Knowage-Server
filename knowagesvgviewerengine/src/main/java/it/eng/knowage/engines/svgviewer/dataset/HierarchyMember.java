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
package it.eng.knowage.engines.svgviewer.dataset;

import it.eng.knowage.engines.svgviewer.map.renderer.Layer;
import it.eng.knowage.engines.svgviewer.map.renderer.Measure;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * The Class HierarchyMember.
 *
 * @author Antonella Giachino (antonella.giachino@eng.it)
 */

public class HierarchyMember {

	private String hierarchy;
	private String name;
	private String dsMeasure;
	private String dsPlaceholder;
	private Integer level;
	private Boolean enableCross;
	private JSONArray labelsCross;
	private Boolean isCustomized;

	private DataSetMetaData dsMetaData;
	private Map<String, Layer> layers;
	private Map<String, Measure> measures;
	private JSONObject customizationSettings;

	private boolean isActive;

	/**
	 * @return the hierarchy
	 */
	public String getHierarchy() {
		return hierarchy;
	}

	/**
	 * @param hierarchy
	 *            the hierarchy to set
	 */
	public void setHierarchy(String hierarchy) {
		this.hierarchy = hierarchy;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the dsMeasure
	 */
	public String getDsMeasure() {
		return dsMeasure;
	}

	/**
	 * @param dsMeasure
	 *            the dsMeasure to set
	 */
	public void setDsMeasure(String dsMeasure) {
		this.dsMeasure = dsMeasure;
	}

	/**
	 * @return the dsPlaceholder
	 */
	public String getDsPlaceholder() {
		return dsPlaceholder;
	}

	/**
	 * @param dsPlaceholder
	 *            the dsPlaceholder to set
	 */
	public void setDsPlaceholder(String dsPlaceholder) {
		this.dsPlaceholder = dsPlaceholder;
	}

	/**
	 * @return the level
	 */
	public Integer getLevel() {
		return level;
	}

	/**
	 * @param level
	 *            the level to set
	 */
	public void setLevel(Integer level) {
		this.level = level;
	}

	/**
	 * @return the dsMetaData
	 */
	public DataSetMetaData getDsMetaData() {
		return dsMetaData;
	}

	/**
	 * @param dsMetaData
	 *            the dsMetaData to set
	 */
	public void setDsMetaData(DataSetMetaData dsMetaData) {
		this.dsMetaData = dsMetaData;
	}

	/**
	 * @return the layers
	 */
	public Map<String, Layer> getLayers() {
		return layers;
	}

	/**
	 * @param layers
	 *            the layers to set
	 */
	public void setLayers(Map<String, Layer> layers) {
		this.layers = layers;
	}

	/**
	 * @return the measures
	 */
	public Map<String, Measure> getMeasures() {
		return measures;
	}

	/**
	 * @param measures
	 *            the measures to set
	 */
	public void setMeasures(Map<String, Measure> measures) {
		this.measures = measures;
	}

	/**
	 * @return the isActive
	 */
	public boolean isActive() {
		return isActive;
	}

	/**
	 * @param isActive
	 *            the isActive to set
	 */
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	/**
	 * @return the enableCross
	 */
	public Boolean getEnableCross() {
		return enableCross;
	}

	/**
	 * @param enableCross
	 *            the enableCross to set
	 */
	public void setEnableCross(Boolean enableCross) {
		this.enableCross = enableCross;
	}

	/**
	 * @return the labelsCross
	 */
	public JSONArray getLabelsCross() {
		return labelsCross;
	}

	/**
	 * @param labelsCross
	 *            the labelsCross to set
	 */
	public void setLabelsCross(JSONArray labelsCross) {
		this.labelsCross = labelsCross;
	}

	/**
	 * @return the isCustomized
	 */
	public Boolean getIsCustomized() {
		return isCustomized;
	}

	/**
	 * @param isCustomized
	 *            the isCustomized to set
	 */
	public void setIsCustomized(Boolean isCustomized) {
		this.isCustomized = isCustomized;
	}

	/**
	 * @return the customizationSettings
	 */
	public JSONObject getCustomizationSettings() {
		return customizationSettings;
	}

	/**
	 * @param customizationSettings
	 *            the customizationSettings to set
	 */
	public void setCustomizationSettings(JSONObject customizationSettings) {
		this.customizationSettings = customizationSettings;
	}

}
