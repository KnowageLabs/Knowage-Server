/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.kpi.bo;

import it.eng.spagobi.kpi.config.bo.Kpi;
import it.eng.spagobi.kpi.config.bo.KpiValue;
import it.eng.spagobi.kpi.threshold.bo.ThresholdValue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class KpiLine implements Serializable{
	
	Integer modelInstanceNodeId = null;
	String modelNodeName = null;
	String modelInstanceCode = null;
	Boolean alarm = false ; //if the kpi is under alarm control
	KpiValue value = null;
	ThresholdValue thresholdOfValue = null;
	List children = null;//List ok kpiLineChildren
	List documents = null;//List of documents related to the Kpi
	
	//added 22/06/2012
	Integer trend = null;
	Kpi kpi = null;
	Integer kpiInstId = null;
	boolean visible = true;
	

	public KpiLine() {
		super();
		this.children = new ArrayList();
		this.documents = new ArrayList();
	}
	public KpiLine(String modelNodeName, Boolean alarm, KpiValue value, List children,
			List documents, Integer modelInstanceNodeId, String modelInstanceCode,ThresholdValue thresholdOfValue,
			Integer trend,
			Kpi kpi,
			Integer kpiInstId,
			boolean visible) {
		super();
		this.modelNodeName = modelNodeName;
		this.alarm = alarm;
		this.value = value;
		this.children = children;
		this.documents = documents;
		this.modelInstanceNodeId = modelInstanceNodeId;
		this.modelInstanceCode = modelInstanceCode;
		this.thresholdOfValue = thresholdOfValue;
		this.trend = trend;
		this.kpi = kpi;
		this.kpiInstId = kpiInstId;
		this.visible = visible;
	}
	
	public boolean isVisible() {
		return visible;
	}
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	public Integer getKpiInstId() {
		return kpiInstId;
	}
	public void setKpiInstId(Integer kpiInstId) {
		this.kpiInstId = kpiInstId;
	}
	public Kpi getKpi() {
		return kpi;
	}
	public void setKpi(Kpi kpi) {
		this.kpi = kpi;
	}
	public Integer getTrend() {
		return trend;
	}
	public void setTrend(Integer trend) {
		this.trend = trend;
	}
	public String getModelNodeName() {
		return modelNodeName;
	}
	public void setModelNodeName(String modelNodeName) {
		this.modelNodeName = modelNodeName;
	}
	public Boolean getAlarm() {
		return alarm;
	}
	public void setAlarm(Boolean alarm) {
		this.alarm = alarm;
	}
	public KpiValue getValue() {
		return value;
	}
	public void setValue(KpiValue value) {
		this.value = value;
	}
	public List getChildren() {
		return children;
	}
	public void setChildren(List children) {
		this.children = children;
	}
	public List getDocuments() {
		return documents;
	}
	public void setDocuments(List documents) {
		this.documents = documents;
	}
	public Integer getModelInstanceNodeId() {
		return modelInstanceNodeId;
	}
	public void setModelInstanceNodeId(Integer modelInstanceNodeId) {
		this.modelInstanceNodeId = modelInstanceNodeId;
	}
	public String getModelInstanceCode() {
		return modelInstanceCode;
	}
	public void setModelInstanceCode(String modelInstanceCode) {
		this.modelInstanceCode = modelInstanceCode;
	}
	public ThresholdValue getThresholdOfValue() {
		return thresholdOfValue;
	}
	public void setThresholdOfValue(ThresholdValue thresholdOfValue) {
		this.thresholdOfValue = thresholdOfValue;
	}
	
	public int compareTo(KpiLine l)    {
		return this.modelInstanceCode.compareTo(l.getModelInstanceCode());
	 }
}
