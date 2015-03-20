/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.threshold.bo;

import java.awt.Color;
import java.io.Serializable;
import java.util.List;

public class ThresholdValue implements Serializable {

	Integer id = null;
	Integer thresholdId = null;
	Integer severityId = null;
	String severityCd = null;
	Integer position = null;
	Double minValue = null;// null if type = max
	Double maxValue = null;// null if type = min
	String label = null;
	Color color = null;
	String colourString = null;
	List alarms = null;
	String thresholdType = null;
	String thresholdCode = null;

	private Boolean minClosed = null;
	private Boolean maxClosed = null;
	private Double value = null;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getThresholdId() {
		return thresholdId;
	}

	public void setThresholdId(Integer thresholdId) {
		this.thresholdId = thresholdId;
	}

	public Integer getSeverityId() {
		return severityId;
	}

	public void setSeverityId(Integer severityId) {
		this.severityId = severityId;
	}

	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}

	public Double getMinValue() {
		return minValue;
	}

	public void setMinValue(Double minValue) {
		this.minValue = minValue;
	}

	public Double getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(Double maxValue) {
		this.maxValue = maxValue;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color _color) {
		this.color = _color;
	}

	public List getAlarms() {
		return alarms;
	}

	public void setAlarms(List alarms) {
		this.alarms = alarms;
	}

	public String getColourString() {
		return colourString;
	}

	public void setColourString(String colourString) {
		this.colourString = colourString;
	}

	public String getThresholdType() {
		return thresholdType;
	}

	public void setThresholdType(String thresholdType) {
		this.thresholdType = thresholdType;
	}

	public String getThresholdCode() {
		return thresholdCode;
	}

	public void setThresholdCode(String thresholdCode) {
		this.thresholdCode = thresholdCode;
	}

	public Boolean getMinClosed() {
		return minClosed;
	}

	public void setMinClosed(Boolean minClosed) {
		this.minClosed = minClosed;
	}

	public Boolean getMaxClosed() {
		return maxClosed;
	}

	public void setMaxClosed(Boolean maxClosed) {
		this.maxClosed = maxClosed;
	}

	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}
	
	public String getSeverityCd() {
		return severityCd;
	}

	public void setSeverityCd(String severityCd) {
		this.severityCd = severityCd;
	}

}
