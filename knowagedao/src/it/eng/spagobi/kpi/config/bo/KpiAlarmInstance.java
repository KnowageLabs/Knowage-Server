/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.config.bo;

public class KpiAlarmInstance {
	
	Integer kpiInstanceId = null;
	String kpiName = null;
	String kpiModelName = null;
	
	public Integer getKpiInstanceId() {
		return kpiInstanceId;
	}
	public void setKpiInstanceId(Integer kpiInstanceId) {
		this.kpiInstanceId = kpiInstanceId;
	}
	public String getKpiName() {
		return kpiName;
	}
	public void setKpiName(String kpiName) {
		this.kpiName = kpiName;
	}
	public String getKpiModelName() {
		return kpiModelName;
	}
	public void setKpiModelName(String kpiModelName) {
		this.kpiModelName = kpiModelName;
	}


}
