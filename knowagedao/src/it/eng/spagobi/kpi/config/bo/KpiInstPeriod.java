/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.config.bo;

import java.io.Serializable;

public class KpiInstPeriod implements Serializable{

	Integer id;
	Integer kpiInstId;
	Integer periodicityId;
	Boolean defaultValue;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getKpiInstId() {
		return kpiInstId;
	}
	public void setKpiInstId(Integer kpiInstId) {
		this.kpiInstId = kpiInstId;
	}
	public Integer getPeriodicityId() {
		return periodicityId;
	}
	public void setPeriodicityId(Integer periodicityId) {
		this.periodicityId = periodicityId;
	}
	public Boolean getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(Boolean defaultValue) {
		this.defaultValue = defaultValue;
	}
	
}


