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
package it.eng.spagobi.kpi.config.bo;

import it.eng.spagobi.kpi.threshold.bo.Threshold;
import it.eng.spagobi.tools.udp.bo.UdpValue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Kpi implements Serializable{
	
	Integer kpiId = null;
	Integer kpiDsId =null;

	String dsLabel = null;
	String kpiName = null; 
	Boolean isRoot = null;
	Double standardWeight = null;
	String metric = null;
	String description = null;
	String scaleCode = null;
	String scaleName = null;
	String code = null;
	Threshold threshold = null;
	Boolean isAdditive = null;

	String interpretation = null;
	String inputAttribute = null;
	String modelReference = null;
	String targetAudience = null;

	Integer kpiTypeId = null;
	Integer metricScaleId = null;
	Integer measureTypeId = null;
	String kpiTypeCd = null;
	String metricScaleCd = null;
	String measureTypeCd = null;
	
	List sbiKpiDocuments = new ArrayList(); // documents related to this KPI

	List udpValues = new ArrayList<UdpValue>();

	public List getSbiKpiDocuments() {
		return sbiKpiDocuments;
	}

	public void setSbiKpiDocuments(List sbiKpiDocuments) {
		this.sbiKpiDocuments = sbiKpiDocuments;
	}

	public String getDsLabel() {
		return dsLabel;
	}

	public void setDsLabel(String dsLabel) {
		this.dsLabel = dsLabel;
	}

	public String getKpiTypeCd() {
		return kpiTypeCd;
	}

	public void setKpiTypeCd(String kpiTypeCd) {
		this.kpiTypeCd = kpiTypeCd;
	}

	public String getMetricScaleCd() {
		return metricScaleCd;
	}

	public void setMetricScaleCd(String metricScaleCd) {
		this.metricScaleCd = metricScaleCd;
	}

	public String getMeasureTypeCd() {
		return measureTypeCd;
	}

	public void setMeasureTypeCd(String measureTypeCd) {
		this.measureTypeCd = measureTypeCd;
	}

	public Kpi() {
		super();
		this.isRoot = false;
	}

	public Integer getKpiId() {
		return kpiId;
	}

	public void setKpiId(Integer kpiId) {
		this.kpiId = kpiId;
	}

	public String getKpiName() {
		return kpiName;
	}

	public void setKpiName(String kpiName) {
		this.kpiName = kpiName;
	}

	public Boolean getIsParent() {
		return isRoot;
	}

	public void setIsParent(Boolean isParent) {
		this.isRoot = isParent;
	}

	public Double getStandardWeight() {
		return standardWeight;
	}

	public void setStandardWeight(Double standardWeight) {
		this.standardWeight = standardWeight;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMetric() {
		return metric;
	}

	public void setMetric(String metric) {
		this.metric = metric;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getScaleCode() {
		return scaleCode;
	}

	public void setScaleCode(String scaleCode) {
		this.scaleCode = scaleCode;
	}

	public String getScaleName() {
		return scaleName;
	}

	public void setScaleName(String scaleName) {
		this.scaleName = scaleName;
	}

	public Boolean getIsRoot() {
		return isRoot;
	}

	public void setIsRoot(Boolean isRoot) {
		this.isRoot = isRoot;
	}

	public Threshold getThreshold() {
		return threshold;
	}

	public void setThreshold(Threshold threshold) {
		this.threshold = threshold;
	}

	public String getInterpretation() {
		return interpretation;
	}

	public void setInterpretation(String interpretation) {
		this.interpretation = interpretation;
	}

	public String getInputAttribute() {
		return inputAttribute;
	}

	public void setInputAttribute(String inputAttribute) {
		this.inputAttribute = inputAttribute;
	}

	public String getModelReference() {
		return modelReference;
	}

	public void setModelReference(String modelReference) {
		this.modelReference = modelReference;
	}

	public String getTargetAudience() {
		return targetAudience;
	}

	public void setTargetAudience(String targetAudience) {
		this.targetAudience = targetAudience;
	}

	public Integer getKpiTypeId() {
		return kpiTypeId;
	}

	public void setKpiTypeId(Integer kpiTypeId) {
		this.kpiTypeId = kpiTypeId;
	}

	public Integer getMetricScaleId() {
		return metricScaleId;
	}

	public void setMetricScaleId(Integer metricScaleId) {
		this.metricScaleId = metricScaleId;
	}

	public Integer getMeasureTypeId() {
		return measureTypeId;
	}

	public void setMeasureTypeId(Integer measureTypeId) {
		this.measureTypeId = measureTypeId;
	}

	public String getComboBoxLabel(boolean trim) {
		int htmlLabelLenght = 60;
		String toReturn = "";
		toReturn += "[" + this.getCode() + "] ";
		toReturn += this.getKpiName();
		if (toReturn.length() >= htmlLabelLenght && trim) {
			toReturn = toReturn.substring(0, 57) + "...";
		}
		return toReturn;
	}

	public Integer getKpiDsId() {
		return kpiDsId;
	}

	public void setKpiDsId(Integer kpiDsId) {
		this.kpiDsId = kpiDsId;
	}

	public List getUdpValues() {
		return udpValues;
	}

	public void setUdpValues(List udpValues) {
		this.udpValues = udpValues;
	}


	public Boolean getIsAdditive() {
		return isAdditive;
	}

	public void setIsAdditive(Boolean isAdditive) {
		this.isAdditive = isAdditive;
	}
	
	
}
